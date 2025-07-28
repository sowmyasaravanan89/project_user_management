package com.sowmya.api.tests;

import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.sowmya.api.helpers.UserServiceHelper;
import com.sowmya.api.model.User;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

public class TestGetUser {

    private UserServiceHelper userServiceHelper;

    @BeforeClass
    public void init() {

        userServiceHelper = new UserServiceHelper();

    }
    @Test
    public void testGetAllUser() {
        List<User> userList = userServiceHelper.getAllUser();
        assertNotNull(userList, "User list should not be null");
        assertFalse(userList.isEmpty(), "User list should not be empty");

    }
}
