package com.sowmya.api.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import com.sowmya.api.utils.TestDataManager;
import io.restassured.response.Response;


public class TestGetUser {

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

        // create a test user to use in tests
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
    public void testGetAllUsers() {
        Response response = userServiceHelper.getAllUsersResponse();
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for getting all users");
        
        // Verify response is an array
        Assert.assertTrue(response.getBody().asString().startsWith("["), "Response should be an array");

        //Verify using helper method
        List<User> users = userServiceHelper.getAllUsers();
        Assert.assertNotNull(users, "User list should not be null");
        Assert.assertTrue(users.size() >= 1 , "should have atleast one user");
        
    }

    @Test(priority = 2)
    public void testGetUserById() {
        Response response = userServiceHelper.getUserById(testUserId);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for getting user by ID");

        User retrievedUser = response.as(User.class);
        Assert.assertNotNull(retrievedUser, "Retrieved user should not be null");
        Assert.assertEquals(retrievedUser.getId(), testUserId, " User ID should match the test user ID");
        Assert.assertNotNull(retrievedUser.getName(), "User name should not be null");
        Assert.assertNotNull(retrievedUser.getEmail(), "User email should not be null");
        Assert.assertNotNull(retrievedUser.getCreatedAt(), "User creation date should not be null");
        Assert.assertNotNull(retrievedUser.getUpdatedAt(), "User update date should not be null");
    }   

    @Test(priority = 3)
    public void testGetUserByInvalidId() {
        String invalidId = "invalid-user-id-23";
        Response response = userServiceHelper.getUserById(invalidId);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for getting user by invalid ID");
        Assert.assertTrue(response.getBody().asString().contains("User not found"), 
                          "Response should contain 'User not found' message");
    }

    @Test(priority = 4)
    public void testGetUserByNonExistentId() {
        String nonExistentId = "999999999999999999"; // Assuming this ID does not exist
        Response response = userServiceHelper.getUserById(nonExistentId);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for getting user by non-existent ID");
        Assert.assertTrue(response.getBody().asString().contains("User not found"), 
                          "Response should contain 'User not found' message");
    }

    @Test(priority = 5)
    public void testAllUsersWithoutAuthentication() {
        // Remove auth token temporarily
        userServiceHelper.setAuthToken(null);

        Response response = userServiceHelper.getAllUsersResponse();

        Assert.assertEquals(response.getStatusCode(), 403, "Expected status code 403 for unauthorized access");
        Assert.assertTrue(response.getBody().asString().contains("Invalid or expired token"), 
                          "Response should contain 'Invalid or expired token' message");

        // Restore auth token
        userServiceHelper.setAuthToken(authToken);
    }

    @Test(priority = 6)
    public void testGetUserByIdWithoutAuthentication() {
        // Remove auth token temporarily
        userServiceHelper.setAuthToken(null);

        Response response = userServiceHelper.getUserById(testUserId);

        Assert.assertEquals(response.getStatusCode(), 403, "Expected status code 403 for unauthorized access");
        Assert.assertTrue(response.getBody().asString().contains("Invalid or expired token"), 
                          "Response should contain 'Invalid or expired token' message");
        // Restore auth token
        userServiceHelper.setAuthToken(authToken);
    }

    @Test(priority = 7)
    public void testGetUsersResponseTime() {
        long startTime = System.currentTimeMillis();
        Response response = userServiceHelper.getAllUsersResponse();
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for getting all users");
        Assert.assertTrue(responseTime < 5000, "Response time should be less than 5 seconds, actual: " + responseTime + " ms");

    }

    @Test(priority = 8)
    public void testGetUsersResponseHeaders() {
        Response response = userServiceHelper.getAllUsersResponse();

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for getting all users");
        Assert.assertNotNull(response.getHeader("Content-Type"), "Content-Type header should not be null");
        Assert.assertTrue(response.getHeader("Content-Type").contains("application/json"), 
                         "Content-Type should be application/json");

    }

    @Test(priority = 9)
    public void testVerifyUserDataStructure() {
        Response response = userServiceHelper.getUserById(testUserId);
        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for getting all users");

        User user = response.as(User.class);

    
        // Verify required fields ar pesent
        Assert.assertNotNull(user.getId(), "ID should be present");
        Assert.assertNotNull(user.getName(), "Name should be present");
        Assert.assertNotNull(user.getEmail(), "Email should be present");
        Assert.assertNotNull(user.getCreatedAt(), "Creation date should be present");
        Assert.assertNotNull(user.getUpdatedAt(), "Update date should be present");

        // Verify data types and formats    
        Assert.assertTrue(user.getId().length() > 0 , "ID should not be empty");
        Assert.assertTrue(user.getEmail().contains("@"), "Email should contain @ symbol");
    }
    
}
