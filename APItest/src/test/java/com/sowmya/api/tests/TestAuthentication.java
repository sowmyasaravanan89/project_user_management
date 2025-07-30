package com.sowmya.api.tests;

import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.utils.TestDataManager;

import io.restassured.response.Response;

public class TestAuthentication {

    private UserServiceHelper userServiceHelper;
    private TestDataManager testDataManager;
    

    @BeforeClass
    public void setUp() {
        userServiceHelper = new UserServiceHelper();
        testDataManager = TestDataManager.getInstance();    
    }

    @Test(priority = 1)
    public void testValidLogin(){
        Map<String,  String> validCredentials = testDataManager.getValidCredentials();
        
        Response response = userServiceHelper.loginUser(
            validCredentials.get("username"),
            validCredentials.get("password")
        );

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for valid login");
       
        // Verify the response contails required fields
        Assert.assertNotNull(response.jsonPath().getString("token"), "Token should not be null");
        Assert.assertNotNull(response.jsonPath().getString("user.username"), "Username should not be null");
        Assert.assertEquals(response.jsonPath().getString("user.role"), "User role should not be null");
        Assert.assertEquals(response.jsonPath().getString("user.expiresAt"), "ExpiresAt should not be null");
        
        // Verify username matches
        Assert.assertEquals(response.jsonPath().getString("user.username"), 
                            validCredentials.get("username"), "Username in response should match ");
        }

     @Test(priority = 2, dataProvider = "invalidCredentials")

     public void testInvalidLogin(Map<String,  String> credentials){
        Response response = userServiceHelper.loginUser(
            credentials.get("username"),
            credentials.get("password")
        );

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for invalid login");
        Assert.assertTrue(response.getBody().asString().contains("Invalid username or password"), 
                          "Response should contain 'Invalid username or password' message");
        
     }

    @Test(priority = 3)
    public void testLoginWithMissingCredentials(){
        // Test with misssing username
        Response response1 = userServiceHelper.loginUser(" ", "password123");
        Assert.assertEquals(response1.getStatusCode(), 400, "Expected status code 400 for missing username");
        Assert.assertTrue(response1.getBody().asString().contains("Username and password are required"), 
                          "Response should contain 'Username and password are required' message");

        // Test with missing password
        Response response2 = userServiceHelper.loginUser("admin", " ");
        Assert.assertEquals(response2.getStatusCode(), 400, "Expected status code 400 for missing password");
        Assert.assertTrue(response2.getBody().asString().contains("Username and password are required"), 
                          "Response should contain 'Username and password are required' message");
    }

    @Test(priority = 4)
    public void testToeknVerification() {
        // First login to get a token
        Map<String, String> validCredentials = testDataManager.getValidCredentials();
        String token = userServiceHelper.autheticateUser(
            validCredentials.get("username"),
            validCredentials.get("password")
        );

        Assert.assertNotNull(token, "Authentication should succeed");

        //Verify the token
        Response response = userServiceHelper.verifyToken();

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for token verification");
        Assert.assertNotNull(response.jsonPath().getString("user.username"), "Username should not be null");
        Assert.assertEquals(response.jsonPath().getString("user.username"), 
                            validCredentials.get("username"), "Username in response should match");
    }

    @Test(priority = 5)
    public void testLogout() {
        // First login to get a token
        Map<String, String> validCredentials = testDataManager.getValidCredentials();
        String token = userServiceHelper.autheticateUser(
            validCredentials.get("username"),
            validCredentials.get("password")
        );  

        Assert.assertNotNull(token, "Authentication should succeed");   

        // Logout
        Response response = userServiceHelper.logoutUser();

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for logout");
        Assert.assertTrue(response.getBody().asString().contains("Successfully logged out"), 
                         "Response should contain 'Successfully logged out' message"); 
    }
    
    @Test(priority = 6)
    public void testLogoutWithoutToken() {
        // Try to logout without authentication`
        userServiceHelper.setAuthToken(null);

        Response response = userServiceHelper.logoutUser();

        //Logout should still work even without a token

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for logout without token");
        Assert.assertTrue(response.getBody().asString().contains("Successfully logged out"),
                         "Response should contain 'Successfully logged out' message");
    }

    @Test(priority = 7)
    public void testVerifyTokenWithoutAuthentication() {
        // Try to verify token without authentication
        userServiceHelper.setAuthToken(null);

        Response response = userServiceHelper.verifyToken();

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for token verification without authentication");
        Assert.assertTrue(response.getBody().asString().contains("Unauthorized"), 
                          "Response should contain authentication error message");
    }   

    @Test(priority = 8)
    public void testVerifyInvalidToken() {
        // Set an invalid token
        userServiceHelper.setAuthToken("invalid-token-123");

        Response response = userServiceHelper.verifyToken();

        Assert.assertEquals(response.getStatusCode(), 401, "Expected status code 401 for invalid token verification");
        Assert.assertTrue(response.getBody().asString().contains("Unauthorized"), 
                          "Response should contain invalid token error message");
    }

    @Test(priority = 9)
    public void testHealthCheck() {
        Response response = userServiceHelper.healthCheck();

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for health check");
        Assert.assertEquals(response.jsonPath().getString("status"), "OK", "Health status should be OK");
        Assert.assertNotNull(response.jsonPath().getString("timestamp"), "Timestamp should not be null");
    }

    @Test(priority = 10)
    public void testLoginResponseTime() {

        Map<String, String> validCredentials = testDataManager.getValidCredentials();

        long startTime = System.currentTimeMillis();
        Response response = userServiceHelper.loginUser(
            validCredentials.get("username"),
            validCredentials.get("password")
        );

        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code 200 for valid login");
        Assert.assertTrue(responseTime < 3000, "Response time should be less than 3 seconds , actual: " + responseTime + " ms");
    }

    @DataProvider(name = "invalidCredentials")
    public Object[][] getinvalidCredentials() {
        List<Map<String, String>> invalidcreds = testDataManager.getInvalidCredentials();
        Object[][] data = new Object[invalidcreds.size()][1];
        for (int i = 0; i < invalidcreds.size(); i++) {
            data[i][0] = invalidcreds.get(i);       
        }
        return data;
    }
    
}
