package com.sowmya.api.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import com.sowmya.api.utils.TestDataManager;

import io.restassured.response.Response;

public class TestDeleteUser {

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
    public void testDeleteExistingUser() {
        // Create a user to delete
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);

        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String userId = createdUser.getId();

        // Delete the created user
        Response deleteResponse = userServiceHelper.deleteUser(userId);

        Assert.assertEquals(deleteResponse.getStatusCode(), 200, "Expected status code 200 for user deletion");
        Assert.assertTrue(deleteResponse.getBody().asString().contains("User deleted successfully"), 
                          "Expected success message for user deletion");    
        Assert.assertTrue(deleteResponse.getBody().asString().contains(userId), 
                          "Response should contain the deleted user information");

        // Verify user is deleted
        Response getResponse = userServiceHelper.getUserById(userId);
        Assert.assertEquals(getResponse.getStatusCode(), 404, "Expected status code 404 for deleted user");
        Assert.assertTrue(getResponse.getBody().asString().contains("User not found"), 
                          "Expected 'User not found' message for deleted user");
    }

    @Test(priority = 2)
    public void testDeleteNonExistentUser() {
        // Attempt to delete a user that does not exist
        String nonExistingUserId = "99999999999999999";

        Response deleteResponse = userServiceHelper.deleteUser(nonExistingUserId);

        Assert.assertEquals(deleteResponse.getStatusCode(), 404, "Expected status code 404 for non-existing user deletion");
        Assert.assertTrue(deleteResponse.getBody().asString().contains("User not found"), 
                          "Response should contain 'User not found' message");
    }

    @Test(priority = 3)
    public void testDeleteUserWithInvalidId() {
        String invalidId = "invalid-user=id-123";

        Response response = userServiceHelper.deleteUser(invalidId);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for invalid user ID deletion");
        Assert.assertTrue(response.getBody().asString().contains("User not found"),
                            "Response should contain 'User not found' message");

    }


    @Test(priority = 4)
    public void testDeleteUserWithoutAuthentication() {
        // Create new user 
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(1);

        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String userId = createdUser.getId();

        // Remove auth token temporarily
        userServiceHelper.setAuthToken(null);

        Response deleteResponse = userServiceHelper.deleteUser(userId);

        Assert.assertEquals(deleteResponse.getStatusCode(), 403, "Expected status code 403 for unauthorized deletion");
        Assert.assertTrue(deleteResponse.getBody().asString().contains("Invalid or expired token"),
                          "Expected 'Invalid or expired token' message for unauthorized deletion");

        // Restore auth token
        userServiceHelper.setAuthToken(authToken);
        // Clean up by deleting the user if it was created
        userServiceHelper.deleteUser(userId);
    }

    @Test(priority = 5)
    public void testDeleteMultipleUsers() {
        // Create multiple users to delete
        List<User> validUsers = testDataManager.getValidUsers();
        String userId1 , userId2 ;

        // Create first user
        Response createResponse1 = userServiceHelper.createUser(validUsers.get(0));
        Assert.assertEquals(createResponse1.getStatusCode(), 201, "Expected status code 201 for user creation");
        userId1 = createResponse1.as(User.class).getId();

        // Create second user
        Response createResponse2 = userServiceHelper.createUser(validUsers.get(1));
        Assert.assertEquals(createResponse2.getStatusCode(), 201, "Expected status code 201 for user creation");
        userId2 = createResponse2.as(User.class).getId();

        // Delete first user
        Response deleteResponse1 = userServiceHelper.deleteUser(userId1);
        Assert.assertEquals(deleteResponse1.getStatusCode(), 200, "Expected status code 200 for user deletion");
        
        // Delete second user
        Response deleteResponse2 = userServiceHelper.deleteUser(userId2);
        Assert.assertEquals(deleteResponse2.getStatusCode(), 200, "Expected status code 200 for user deletion");

        // Verify both users are deleted
        Response getResponse1 = userServiceHelper.getUserById(userId1);
        Assert.assertEquals(getResponse1.getStatusCode(), 404, "Expected status code 404 for deleted user");
        
        Response getResponse2 = userServiceHelper.getUserById(userId2);
        Assert.assertEquals(getResponse2.getStatusCode(), 404, "Expected status code 404 for deleted user");
    }

    @Test(priority = 6)
    public void testDeleteUserReturnedData(){
        // Create a user to delete
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(2);

        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String userId = createdUser.getId();

        // Delete the created user
        Response deleteResponse = userServiceHelper.deleteUser(userId);

        Assert.assertEquals(deleteResponse.getStatusCode(), 200, "Expected status code 200 for user deletion");

        // Verify the response contains deleted user information
        String responseBody = deleteResponse.getBody().asString();
        Assert.assertTrue(responseBody.contains("message"),"Response should contain a message field");
        Assert.assertTrue(responseBody.contains("User"), "Response should contain 'User' field");
        Assert.assertTrue(responseBody.contains(testUser.getName()), "Response should contain the deleted user's name");
        Assert.assertTrue(responseBody.contains(testUser.getEmail()), "Response should contain the deleted user's email");

    }

    @Test(priority = 7)
    public void testDeleteSameUserAgain() {
        // Create a user to delete
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(1);

        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String userId = createdUser.getId();

        // Delete the created user
        Response deleteResponse1 = userServiceHelper.deleteUser(userId);
        Assert.assertEquals(deleteResponse1.getStatusCode(), 200, "Expected status code 200 for first deletion");

        // Attempt to delete the same user again
        Response deleteResponse2 = userServiceHelper.deleteUser(userId);
        Assert.assertEquals(deleteResponse2.getStatusCode(), 404, "Expected status code 404 for second deletion");
        
        Assert.assertTrue(deleteResponse2.getBody().asString().contains("User not found"), 
                          "Expected 'User not found' message for second deletion");
    }

    @Test(priority = 9)
    public void testDeleteUserFromList() {
        // Create multiple users to delete
        List<User> validUsers = testDataManager.getValidUsers();
        
        // Create first user
        Response createResponse1 = userServiceHelper.createUser(validUsers.get(0));
        String userId1 = createResponse1.as(User.class).getId();

        // Create second user
        Response createResponse2 = userServiceHelper.createUser(validUsers.get(1));
        String userId2 = createResponse2.as(User.class).getId();

        //create third user
        Response createResponse3 = userServiceHelper.createUser(validUsers.get(2));
        String userId3 = createResponse3.as(User.class).getId();

        // Get all users and verify count
        List<User> allUsers1 = userServiceHelper.getAllUsers();
        int initialCount = allUsers1.size();

        // Delete middle user
        Response deleteResponse = userServiceHelper.deleteUser(userId2);
        Assert.assertEquals(deleteResponse.getStatusCode(), 200, "Expected status code 200 for first user deletion");

        // Get all users again and verify count
        List<User> allUsers2 = userServiceHelper.getAllUsers();
        Assert.assertEquals(allUsers2.size(), initialCount - 1, "User count should decrease by 1");

        // Verify the deleted user is not in the list
        final String deletedUserId = userId2;
        boolean deleteUserFound = allUsers2.stream().anyMatch(user -> user.getId().equals(deletedUserId));
        Assert.assertFalse(deleteUserFound, "Deleted user should not be present in the list");


        // Clean up by deleting remaining users
        userServiceHelper.deleteUser(userId1);
        userServiceHelper.deleteUser(userId3);
    }

}

