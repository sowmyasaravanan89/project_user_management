package com.sowmya.api.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sowmya.api.constants.Endpoints;
import com.sowmya.api.model.User;
import com.sowmya.api.utils.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class UserServiceHelper {

    // We need to read the config variables
    // Rest Assured about the URL, port
    // MAke a Get request on this url and send the data to TestGetUser
    
     private static final String BASE_URL =  ConfigManager.getInstance().geString(("base_url"));
     private static final String PORT = ConfigManager.getInstance().geString("backend_port");
     private static String authToken = null;

    public UserServiceHelper(){
         RestAssured.baseURI = BASE_URL;
         RestAssured.port = Integer.parseInt(PORT);
         RestAssured.useRelaxedHTTPSValidation();
    }

    // Authentication methods
    public String autheticateUser(String username, String password){
        Map<String, String> credentials = Map.of(
            "username" , username,
            "password" , password
        );

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .post(Endpoints.LOGIN);
        if(response.getStatusCode() == 200){
            authToken = response.jsonPath().getString("token");
            return authToken;
        }  
        return null;
    }
    
    
    public Response loginUser(String username, String password){
        Map<String, String> credentials = Map.of(
            "username" , username,
            "password" , password
        );

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .post(Endpoints.LOGIN);
    }

    public Response logoutUser(){
        return RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .post(Endpoints.LOGOUT);
    }

    public Response verifyToken(){
        return RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .get(Endpoints.VERIFY_TOKEN);
    }

    // User CRUD operations
    public List<User> getAllUsers() {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Authorization","Bearer " + authToken)
                .get(Endpoints.GET_ALL_USER)
                .andReturn();
        response.getBody().prettyPrint();
        Type type = new TypeReference<List<User>>(){}.getType(); 
        return response.as(type);
    }

    public Response getAllUsersResponse() {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization","Bearer " + authToken)
                .get(Endpoints.GET_ALL_USER);
    }

    public Response getUserById(String userId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("id", userId)
                .get(Endpoints.GET_SINGLE_USER);
    }

     public Response createUser(User user) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(user)
                .post(Endpoints.CREATE_USER);
    }

    public Response createUser(Map<String,  Object> userData) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .body(userData)
                .post(Endpoints.CREATE_USER);
    }

    public Response updateUser(String userId, User user) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("id", userId)
                .body(user)
                .put(Endpoints.UPDATE_USER);
    }

    public Response updateUser(String userId, Map<String ,Object> userData) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("id", userId)
                .body(userData)
                .put(Endpoints.UPDATE_USER);
    }

    public Response patchUser(String userId, Map<String ,  Object > patchData) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("id", userId)
                .body(patchData)
                .put(Endpoints.UPDATE_USER);
    }

    public Response deleteUser(String userId) {
        return RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)
                .pathParam("id", userId)
                .delete(Endpoints.DELETE_USER);
    }


    // Utility methods
    public void setAuthToken(String token) {
        UserServiceHelper.authToken = token;
    }

    public String getAuthToken() {
        return UserServiceHelper.authToken;
    }

    public Response healthCheck() {
        return RestAssured.given()
                .get(Endpoints.HEALTH_CHECK);
    }
    
    
}
