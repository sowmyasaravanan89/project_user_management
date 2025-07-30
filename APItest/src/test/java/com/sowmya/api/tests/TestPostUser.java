package com.sowmya.api.tests;

import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;


import io.restassured.path.json.JsonPath;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TestPostUser {

    private UserServiceHelper userServiceHelper;

    @BeforeClass
    public void init() {

        userServiceHelper = new UserServiceHelper();

    }


    @Test
    public void testPostUser() {
    
       List<User> userList = userServiceHelper.getAllUser();
        assertNotNull(userList, "User list should not be null");
        assertFalse(userList.isEmpty(), "User list should not be empty");
        for (User user : userList) {
            assertNotNull(user.getId(), "User ID should not be null");
            assertNotNull(user.getName(), "User name should not be null");
            assertNotNull(user.getEmail(), "User email should not be null");
            assertNotNull(user.getCreatedAt(), "User creation date should not be null");
            assertNotNull(user.getUpdatedAt(), "User update date should not be null");
        
        }

    }
    
}
