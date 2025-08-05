package com.sowmya.api.tests;

import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import com.sowmya.api.utils.TestDataManager;
import io.restassured.response.Response;



public  class TestPutUser {
    
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

    }

    @BeforeMethod
    public void setUpMethod(){
        userServiceHelper.setAuthToken(authToken);

        //Create a test user for each test method
        List<User> validUsers = testDataManager.getValidUsers();
        User testUser = validUsers.get(0);

        Response createResponse = userServiceHelper.createUser(testUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201,"Failed to create test user");

        User createdUser = createResponse.as(User.class);
        testUserId = createdUser.getId();
        Assert.assertNotNull(testUserId,"Test user ID should not be null");

    }

    @AfterMethod
    public void tearDown(){
        // clean up the test user after each test
        if (testUserId != null) {
            userServiceHelper.deleteUser(testUserId);
            testUserId = null;
        }
    }

    @Test(priority = 1)
    public void testUpdateUserWithValidData(){
        //Get updated user data
        List<User> validUsers  = testDataManager.getValidUsers();
        User updatedUser = validUsers.get(1);// Use different user data for update

        Response response = userServiceHelper.updateUser(testUserId, updatedUser);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code for successful user update");

        //Verify response body
        User responseUser = response.as(User.class);
        Assert.assertNotNull(responseUser, "Updated user should not be null");
        Assert.assertEquals(responseUser.getId(), testUserId, "User ID should remain the same");
        Assert.assertEquals(responseUser.getName(), updatedUser.getName(), "Name should be updated");
        Assert.assertEquals(responseUser.getEmail(), updatedUser.getEmail(), "Email should be updated");
        Assert.assertEquals(responseUser.getAge(), updatedUser.getAge(), "Age should be updated");
        Assert.assertNotNull(responseUser.getUpdatedAt(), "UpdatedAt should not be null");

        // Verify that the user was actually updated by fetching it 
        Response getResponse = userServiceHelper.getUserById(testUserId);
        Assert.assertEquals(getResponse.getStatusCode(), 200, "Should be able to fetch updated user");

        User fetchedUser = getResponse.as(User.class);
        Assert.assertEquals(fetchedUser.getName(), updatedUser.getName(), "Fetched user name should match updated name");
        Assert.assertEquals(fetchedUser.getEmail(), updatedUser.getEmail(), "Fetched user name should match updated email");

    }

    @Test(priority =2 , dataProvider =  "validUpdateData")
    public void testUpdateUserWithMultipleValidData(Map<String, Object> updateData){
        Response response = userServiceHelper.updateUser(testUserId, updateData);

        Assert.assertEquals(response.getStatusCode(), 200, "Expected status code for successful user update");
        
        User responseUser = response.as(User.class);
        Assert.assertNotNull(responseUser, "Updated user should not be null");
        Assert.assertEquals(responseUser.getId(), testUserId, "User ID should remain the same");
        Assert.assertEquals(responseUser.getName(), updateData.get("name"), "Name should be updated");
        Assert.assertEquals(responseUser.getEmail(), updateData.get("email"), "Email should be updated");

        if(updateData.containsKey("age") && updateData.get("age") != null){
            Assert.assertEquals(responseUser.getAge(), updateData.get("age"), "Age should be updated");
        }
    }

    @Test(priority = 3)
    public void testUpdateUSerWithNonExistentId(){
        String nonExistentId = "non-existent-id-12345";
        List<User> validUsers = testDataManager.getValidUsers();
        User updatedUser = validUsers.get(1);

        Response response = userServiceHelper.updateUser(nonExistentId, updatedUser);

        Assert.assertEquals(response.getStatusCode(), 404, "Expected status code 404 for non existent user");
        Assert.assertTrue(response.getBody().asString().contains("User not found"),"Response should contain user not found error message");

    }

    @Test(priority = 4, dataProvider  ="invalidUpdateData")
    public void testUpdateUserWithInvalidData(Map<String, Object> invalidData){

        Response response = userServiceHelper.updateUser(testUserId, invalidData);

        Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 for invalid user data");
        Assert.assertTrue(response.getBody().asString().contains("error"),"Response should contain error message");

    }

    @Test(priority =5)
    public void testUpdateUserWithDuplicateEmail(){
        // Create another user
        List<User> validUsers = testDataManager.getValidUsers();
        User anotherUser = validUsers.get(2);

        Response createResponse = userServiceHelper.createUser(anotherUser);
        Assert.assertEquals(createResponse.getStatusCode(), 201, "Expected status code 201 for user creation");

        User createdUser = createResponse.as(User.class);
        String anotherUserId = createdUser.getId();

        // Try to update the first user with the email of the second user
        try{
        Map<String, Object> updateData = Map.of(
            "name", "Updated Name",
            "email", anotherUser.getEmail(), // Use email of the second user
            "age", 30
        );

            Response response = userServiceHelper.updateUser(testUserId, updateData);

            Assert.assertEquals(response.getStatusCode(), 400, "Expected status code 400 for duplicate email");
            Assert.assertTrue(response.getBody().asString().contains("Email already exists"), 
                                "Expected error message for duplicate email");

        }finally{
             // Clean up the second user created
            userServiceHelper.deleteUser(anotherUserId);
        }
    }

    @Test(priority =6)
    public void testUpdateUserWithoutAuthentication(){
        // Remove auth token temporarily
        userServiceHelper.setAuthToken(null); 
    
        List<User> validUsers = testDataManager.getValidUsers();
        User updatedUser = validUsers.get(1);
        
        Response response = userServiceHelper.updateUser(testUserId, updatedUser);

        Assert.assertEquals(response.getStatusCode(), 403, "Expected status code 403 for unauthenticated request");
        Assert.assertTrue(response.getBody().asString().contains("Invalid or expired token"), 
                                "Expected error message for authentication required");
        
        // Restore auth token
        userServiceHelper.setAuthToken(authToken);
    }
    

    @Test(priority =7)
    public void testUpdateUserWithMissingRequiredFields(){
        // Test with missing name
        Map<String, Object> userWithoutName = Map.of("email","Updated@example.com","age",25);
        Response response1 = userServiceHelper.updateUser(testUserId, userWithoutName);
        Assert.assertEquals(response1.getStatusCode(),400,"Should fail when name is missing");

        //Test with missing email
        Map<String, Object> userWithoutEmail = Map.of("name","Updated User","age",25);
        Response response2 = userServiceHelper.updateUser(testUserId, userWithoutEmail);
        Assert.assertEquals(response2.getStatusCode(),400,"Should fail when email is missing");

    }

    @Test(priority =8)
    public void testUpdateUserOptionalAge(){
        //Test user update without age(optionl field)
        Map<String, Object> userWithoutAge= Map.of(
            "name", "Updated User No Age",
            "email", "updated.noage@example.com"
        );

        Response response = userServiceHelper.updateUser(testUserId,userWithoutAge);
        Assert.assertEquals(response.getStatusCode(),200,"User update should suceed without age");

        User updatedUser = response.as(User.class);
        Assert.assertEquals(updatedUser.getId(),testUserId,"User ID should remain same");
        Assert.assertEquals(updatedUser.getName(),"Updated User No Age","Name should be updated");
        Assert.assertEquals(updatedUser.getEmail(),"updated.noage@example.com","Email should be updated");

    }

    @Test(priority = 9)
    public void testUpdateUserPartialData(){
        //Get current user data first
        Response getCurrentResponse = userServiceHelper.getUserById(testUserId);
        getCurrentResponse.getBody().prettyPrint();
        User currentUser = getCurrentResponse.as(User.class);

        //update only the name
        Map<String,Object> partialUpdate = Map.of(
            "name","Partially Updated Name",
            "email", currentUser.getEmail(), // keep same email
            "age", currentUser.getAge() // Keep same age
        );

        Response response = userServiceHelper.updateUser(testUserId, partialUpdate);
        
        Assert.assertEquals(response.getStatusCode(),200,"Partial update should succeed");

        User updatedUser = response.as(User.class);
        Assert.assertEquals(updatedUser.getName(),"Partially Updated Name","Name should be updated");
        Assert.assertEquals(updatedUser.getEmail(),currentUser.getEmail(),"Email should remain same");
        Assert.assertEquals(updatedUser.getAge(),currentUser.getAge(),"Age should remain same");

    }

    @Test(priority =10)
    public void testUpdateUserWithSameData(){
        //Get current user data
        Response getCurrentResponse = userServiceHelper.getUserById(testUserId);
        User currentUser = getCurrentResponse.as(User.class);

        // Update with same data
        Map<String,Object> sameData = Map.of(
            "name",currentUser.getName(),
            "email", currentUser.getEmail(),
            "age", currentUser.getAge() != null ? currentUser.getAge() : 25
        );

        Response response = userServiceHelper.updateUser(testUserId, sameData);
        Assert.assertEquals(response.getStatusCode(),200,"Update should succeed");

        User updatedUser = response.as(User.class);
        Assert.assertEquals(updatedUser.getName(),currentUser.getName(),"Name should remain same");
        Assert.assertEquals(updatedUser.getEmail(),currentUser.getEmail(),"Email should remain same");
    }

    @Test(priority =11)
    public void testUpdateUserWithInvalidToken(){
        // Set Invalid auth token
        userServiceHelper.setAuthToken("invalid-token-1234");

        List<User> validUSers = testDataManager.getValidUsers();
        User updatedUser = validUSers.get(1);

        Response response = userServiceHelper.updateUser(testUserId, updatedUser);

        Assert.assertEquals(response.getStatusCode(),403,"Status code 403 for invalid token should be displayed");
        Assert.assertTrue(response.getBody().asString().contains("Invalid"),"Response should conrain invalid token error message");

        //Restore valid auth toekn
        userServiceHelper.setAuthToken(authToken);

    }

    @DataProvider(name="validUpdateData")
    public Object[][] getValidUpdateData(){
        return new Object[][]{
            {Map.of("name","Updated User 1","email","updated1@example.com","Age",30)},
            {Map.of("name","Updated User 2","email","updated2@example.com","Age",44)},
            {Map.of("name","Updated User 3","email","updated3@example.com")}, // Without Age
            {Map.of("name","Updated User 4","email","updated4@example.com","Age",20)},
            {Map.of("name","Updated User 5","email","updated5@example.com","Age",30)}
        };
    }

    @DataProvider(name = "invalidUpdateData")
    public Object[][] getInvalidUpdateData(){
        List<Map<String, Object>> invalidUsers = testDataManager.getInvalidUsers();
        Object[][] data = new Object[invalidUsers.size()][1];
        for(int i =0 ; i<invalidUsers.size(); i++){
            data[i][0] = invalidUsers.get(i);
        }
        return data;
    }
    
}
