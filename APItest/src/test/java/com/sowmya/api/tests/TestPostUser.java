package com.sowmya.api.tests;

import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import com.sowmya.api.utils.TestDataManager;
import io.restassured.response.Response;


public class TestPostUser {

    private UserServiceHelper userServiceHelper;
    private TestDataManager testDataManager;
    private String authToken;


    @BeforeClass
    public void setUp() {
        userServiceHelper = new UserServiceHelper();
        testDataManager = TestDataManager.getInstance();
        
        // Authenticate before runiing tests
        String username = ConfigManager.getInstance().geString("auth_username");
        String password = ConfigManager.getInstance().geString("auth_password");
        authToken = userServiceHelper.autheticateUser(username, password);
        Assert.assertNotNull(authToken, "Authentication failed");

    }

    @BeforeMethod
    public void setUpMethod(){
        userServiceHelper.setAuthToken(authToken);
    }


    @Test(priority = 1)
    public void testCreateValidUser() {
        // Get first valid user from test data
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);  

        Response response = userServiceHelper.createUser(testUser);

        Assert.assertEquals(response.getStatusCode(), 201, "Expected status code 201 for user creation");

        // Verify response body
        User createdUser = response.as(User.class);
        Assert.assertNotNull(createdUser.getId(), "User ID should not be null");
        Assert.assertEquals(createdUser.getName(), testUser.getName(), "User name should match");
        Assert.assertEquals(createdUser.getEmail(), testUser.getEmail(), "User email should match");
        Assert.assertNotNull(createdUser.getCreatedAt(),"CreatedAt should not be null");
        Assert.assertNotNull(createdUser.getUpdatedAt(),"UpdatedAt should not be null");

        // Clean up - delete the created user

        userServiceHelper.deleteUser(createdUser.getId());
        
     } 

    @Test(priority = 2, dataProvider = "validUserData")
    public void testCreateMultipleValidUsers(User user) {
        Response response = userServiceHelper.createUser(user);

        Assert.assertEquals(response.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = response.as(User.class);
        Assert.assertNotNull(createdUser.getId(), "User ID should not be null");
        Assert.assertEquals(createdUser.getName(), user.getName(), " Name should match");
        Assert.assertEquals(createdUser.getEmail(), user.getEmail(), "User email should match");

        // Clean up

        userServiceHelper.deleteUser(createdUser.getId());
    }
    
    @Test(priority = 3, dataProvider = "invalidUserData")
    public void testCreateInvalidUser(Map<String, Object> userData) {
        Response response = userServiceHelper.createUser(userData);

        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 for invalid user creation");
        Assert.assertTrue(response.getBody().asString().contains("error"), "Response should contain error message for invalid data");
    }

    @Test(priority = 4)
    public void testUserWithDuplicateEmail() {
        // Create first user
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);

        Response firstResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(firstResponse.getStatusCode(), 201, "Expected status code 201 for first user creation");

        User createdUser = firstResponse.as(User.class);

        // Try to create second user with same email
        Response secondResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(secondResponse.getStatusCode(), 400, "Expected status code 400 for duplicate email");
        Assert.assertTrue(secondResponse.getBody().asString().contains("Email already exists"), 
                            "Response should contain error message for duplicate email");
        // Clean up
        userServiceHelper.deleteUser(createdUser.getId()); 
        
    }
    
    @Test(priority = 5)
    public void testCreateUserWithoutAuthentication() {
        // Temporarily remove auth token
        String originalAuthToken = userServiceHelper.getAuthToken();
        userServiceHelper.setAuthToken(null);

        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);

        Response response = userServiceHelper.createUser(testUser);
        
        Assert.assertEquals(response.getStatusCode(), 403, "Expected status code 403 for unauthenticated request");
        Assert.assertTrue(response.getBody().asString().contains("Invalid or expired token"), 
                            "Response should contain authentication error message");

        // Restore original auth token
        userServiceHelper.setAuthToken(originalAuthToken);
    }

    @Test(priority = 6)
    public void testCreateUserWithMissingRequiredFields() {
        // Test with missing name
        Map<String, Object> userWithoutName  = Map.of(
            "email","test@example.com",
            "age",25
        );
        Response response1 = userServiceHelper.createUser(userWithoutName);
        Assert.assertEquals(response1.getStatusCode(), 400, "Expected status code 400 for missing name");

        // Test with missing email
        Map<String, Object> userWithoutEmail = Map.of(
            "name", "Test User",
            "age", 25
        );
        Response response2 = userServiceHelper.createUser(userWithoutEmail);
        Assert.assertEquals(response2.getStatusCode(), 400, "Expected status code 400 for missing email");        
    }

    @Test(priority = 7)
    public void testCreateUserOptionalAge() {
        // Test user creation without age (optional field)
        Map<String, Object> userWithoutAge = Map.of(
            "name", "Test User No Age",
            "email", "noage@example.com"
        );

        Response response = userServiceHelper.createUser(userWithoutAge);
        Assert.assertEquals(response.getStatusCode(), 201, "Expected status code 201 for user creation without age");

        User createdUser = response.as(User.class);
        Assert.assertNotNull(createdUser.getId(), "User ID should not be null");

        // Clean up
        userServiceHelper.deleteUser(createdUser.getId());
    }

    @DataProvider(name = "validUserData")
    public Object[][] getValidUserData() {
        List<User> validUsers = testDataManager.getValidUsers();
        Object[][] data = new Object[validUsers.size()][1];
        for (int i = 0; i < validUsers.size(); i++) {
            data[i][0] = validUsers.get(i);
        }
        return data;
    }

    @DataProvider(name = "invalidUserData")
    public Object[][] getInvalidUserData() {
        List<Map<String, Object>> invalidUsers = testDataManager.getInvalidUsers();
        Object[][] data = new Object[invalidUsers.size()][1];
        for (int i = 0; i < invalidUsers.size(); i++) {
            data[i][0] = invalidUsers.get(i);
        }
        return data;

    }
}
