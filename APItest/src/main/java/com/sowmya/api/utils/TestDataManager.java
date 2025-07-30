package com.sowmya.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sowmya.api.model.User;

public class TestDataManager {

    private  static TestDataManager instance;
    private JsonNode testData;
    private ObjectMapper objectMapper;

    private TestDataManager() throws IOException {
        objectMapper = new ObjectMapper();
        InputStream inputStream = TestDataManager.class.getResourceAsStream("/Testdata.json");
        testData = objectMapper.readTree(inputStream);
    }
        
    public static TestDataManager getInstance(){
        if(instance == null){
            synchronized (TestDataManager.class) {
            try {
                instance = new TestDataManager();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        return instance;
    }

    public List<User>  getValidUsers(){
        List<User> users = new ArrayList<>();
        JsonNode validUsers = testData.get("validUsers");   

        for (JsonNode userNode : validUsers) {
            try{
                User user = objectMapper.treeToValue(userNode, User.class);
                users.add(user);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return users;
    }

    public List <Map<String,  Object>> getInvalidUsers(){
        List<Map<String,  Object>> users = new ArrayList<>();
        JsonNode invalidUsers = testData.get("invalidUsers");

        for (JsonNode userNode : invalidUsers) {
            try{
                @SuppressWarnings("unchecked")
                Map<String, Object>  user = objectMapper.convertValue(userNode,  Map.class);
                users.add(user);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return users;
    }

    public List <Map<String,  Object>> getUpdateUserData(){
        List<Map<String,  Object>> users = new ArrayList<>();
        JsonNode updateData = testData.get("updateUserData");

        for (JsonNode userNode : updateData) {
            try{
                @SuppressWarnings("unchecked")
                Map<String, Object>  user = objectMapper.convertValue(userNode,  Map.class);
                users.add(user);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return users;
    }

    public List <Map<String,  Object>> getPatchUserData(){
        List<Map<String,  Object>> users = new ArrayList<>();
        JsonNode patchData = testData.get("patchUserData");

        for (JsonNode userNode : patchData) {
            try{
                @SuppressWarnings("unchecked")
                Map<String, Object>  user = objectMapper.convertValue(userNode,  Map.class);
                users.add(user);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return users;
    }

    public Map<String,  String> getValidCredentials() {
        JsonNode authData = testData.get("authData").get("validCredentials");
        Map<String , String> credentials = new HashMap<>();
        credentials.put("username", authData.get("username").asText());
        credentials.put("password", authData.get("password").asText());
        return credentials;
    }

    public List<Map<String, String>> getInvalidCredentials() {
        List<Map<String, String>> credentials = new ArrayList<>();
        JsonNode invalidCreds = testData.get("authData").get("invalidCredentials");

        for (JsonNode credNode : invalidCreds) {
            Map<String, String> cred = new HashMap<>();
            cred.put("username", credNode.get("username").asText());
            cred.put("password", credNode.get("password").asText());
            credentials.add(cred);
        }
        return credentials;
    }
    
}
