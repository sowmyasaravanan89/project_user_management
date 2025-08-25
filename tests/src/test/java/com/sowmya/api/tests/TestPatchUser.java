package com.sowmya.api.tests;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import com.sowmya.api.utils.TestDataManager;
import io.restassured.response.Response;


public class TestPatchUser {
    private UserServiceHelper userServiceHelper;
    private TestDataManager testDataManager;
    private String authToken;
    private String testUserId;

    @BeforeClass
    public void setUp() {
        userServiceHelper = new UserServiceHelper();
        testDataManager = TestDataManager.getInstance();
        
        // Authenticate before runiing tests
        String username = ConfigManager.getInstance().geString("auth_username");
        String password = ConfigManager.getInstance().geString("auth_password");
        authToken = userServiceHelper.autheticateUser(username, password);
        Assert.assertNotNull(authToken, "Authentication failed");

        // create a test user tfor Patch operations
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);
        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");
        testUserId = createResponse.as(User.class).getId();

    }

    @BeforeMethod
    public void setUpMethod(){
        userServiceHelper.setAuthToken(authToken);
    }

    @AfterClass
    public void tearDown(){
        // clean up the test user created
        if (testUserId != null) {
            userServiceHelper.deleteUser(testUserId);
        }
    }

    @Test(priority = 1)
    public void testUpdateUserCompletely() {
        // Get update data
        List<Map<String, Object>> updateData = testDataManager.getUpdateUserData();
        Map<String, Object> updatedUserData = updateData.get(0);

        Response response = userServiceHelper.updateUser(testUserId, updatedUserData);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for user update");

        User updatedUser = response.as(User.class);
        Assert.assertNotNull(updatedUser, "Updated user should not be null");
        Assert.assertEquals(updatedUser.getId(), testUserId, "User ID should remain the same");
        Assert.assertEquals(updatedUser.getName(), updatedUserData.get("name"), "Name should be updated");
        Assert.assertEquals(updatedUser.getEmail(), updatedUserData.get("email"), "Email should be updated");
        Assert.assertNotNull(updatedUser.getUpdatedAt(), "UpdatedAt should not be null");
    }

    @Test(priority = 2, dataProvider = "patchUserData")

    public void testPatchUserPartially(Map<String, Object> patchData) {

        // Note : The backend only supports PUT, but we can test partial updates
        // by including existing data with patch data

        // First get current user data
        Response getUserResponse = userServiceHelper.getUserById(testUserId);
        User currentUser = getUserResponse.as(User.class);

        // Create complete update data by merging current user data with patch data
        Map<String, Object> completeUpdateData = Map.of(
            "name", patchData.containsKey("name") ? patchData.get("name") : currentUser.getName(),
            "email", patchData.containsKey("email") ? patchData.get("email") : currentUser.getEmail(),
            "age", patchData.containsKey("age") ? patchData.get("age") : currentUser.getAge()
        );

        Response response = userServiceHelper.updateUser(testUserId, completeUpdateData);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for user patch");

        User updatedUser = response.as(User.class);
        Assert.assertNotNull(updatedUser, "Patched user should not be null");
        Assert.assertEquals(updatedUser.getId(), testUserId, "User ID should remain the same");

        // Verify that the patched fields are updated
        if (patchData.containsKey("name")) {
            Assert.assertEquals(updatedUser.getName(), patchData.get("name"), "Name should be updated");
        }
        if (patchData.containsKey("email")) {
            Assert.assertEquals(updatedUser.getEmail(), patchData.get("email"), "Email should be updated");
        }
        if (patchData.containsKey("age")) {
            Assert.assertEquals(updatedUser.getAge(), patchData.get("age"), "Age should be updated");
        }
    
    }

    @Test(priority = 3)
    public void testPatchUserWithInvalidId() {
        String invalidId = "invalid-user-id-123";
        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", "updated@test.com",
            "age", 30
        );

        Response response = userServiceHelper.updateUser(invalidId, updateData);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for invalid user");
        Assert.assertTrue(response.getBody().asString().contains("User not found"), 
                                "Expected error message for user not found");   

    }

    @Test(priority = 4)
    public void testUpdatedUserWithMissingRequiredFields() {
        // Test with missing names
        Map<String, Object> dataWithoutName = Map.of(
            "email", "test@example.com",
            "age", 25
        );
        Response response1 = userServiceHelper.updateUser(testUserId, dataWithoutName);
        Assert.assertEquals(response1.getStatusCode(), 400, "Should fail when name is missing");

        // Test with missing email
        Map<String, Object> dataWithoutEmail = Map.of(
            "name", "Test User",
            "age", 25
        );
        Response response2 = userServiceHelper.updateUser(testUserId, dataWithoutEmail);
        Assert.assertEquals(response2.getStatusCode(), 400, "Should fail when email is missing"); 

    }

    @Test(priority = 5)
    public void testUpdateUSerWithDuplicateEmail() {
        // Create another user
        List<User> validUsers = testDataManager.getValidUsers();
        User anotherUser = validUsers.get(1);
        Response createResponse = userServiceHelper.createUser(anotherUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String secondUserId = createdUser.getId();

        // Try to update the first user with the email of the second user
        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", anotherUser.getEmail(), // Use email of the second user
            "age", 30
        );

        Response response = userServiceHelper.updateUser(testUserId, updateData);
        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 for duplicate email");
        Assert.assertTrue(response.getBody().asString().contains("Email already exists"), 
                                "Expected error message for duplicate email");

        // Clean up the second user created
        userServiceHelper.deleteUser(secondUserId);
    }

    @Test(priority = 6)
    public void testUpdateUSerWithoutAuthentication() {
        // Remove auth token temporarily
    
        userServiceHelper.setAuthToken(null); 

        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", "updated@test.com",
            "age", 30
        );
        
        Response response = userServiceHelper.updateUser(testUserId, updateData);
        Assert.assertEquals(response.getStatusCode(), 403, "Expected status code 403 for unauthenticated request");
        Assert.assertTrue(response.getBody().asString().contains("Invalid or expired token"), 
                                "Expected error message for authentication required");
        
        // Restore auth token
        userServiceHelper.setAuthToken(authToken);
    }

    @Test(priority = 7)
    public void testUpdateUserWithoutOptionalAge() {
        // Test update without age(should be null)
        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", "noupdate@test.com"
        );

        Response response = userServiceHelper.updateUser(testUserId, updateData);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for user update without age");

        User updatedUser = response.as(User.class);
        Assert.assertEquals(updatedUser.getName(), updateData.get("name"), "Name should be updated");
        Assert.assertEquals(updatedUser.getEmail(), updateData.get("email"), "Email should be updated");
        // Age should be null since it wasnt provided
    }

    @Test(priority = 8)
    public void testUpdateUserTimeStamp() {
        // Get current user
        Response getUserResponse = userServiceHelper.getUserById(testUserId);
        User currentUser = getUserResponse.as(User.class);
        String originalUpdatedAt = currentUser.getUpdatedAt();

        // Wait a moment to ensure timestamp difference
        try {
            Thread.sleep(1000); // Sleep for 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Update user
        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", "timestamp@test.com",
            "age", 30
        );

        Response response = userServiceHelper.updateUser(testUserId, updateData);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for user update");

        User updatedUser = response.as(User.class);
        Assert.assertNotEquals(updatedUser.getUpdatedAt(), originalUpdatedAt, 
            "UpdatedAt timestamp should change after update");
        
    }

    @DataProvider(name = "patchUserData")
    public Object[][] getPatchUserData() {
        List<Map<String, Object>> patchData = testDataManager.getPatchUserData();
        Object[][] data = new Object[patchData.size()][1];
        for (int i = 0; i < patchData.size(); i++) {
            data[i][0] = patchData.get(i);
        }
        return data;
    }
}

