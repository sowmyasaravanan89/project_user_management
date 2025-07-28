package com.sowmya.api.helpers;

import com.sowmya.api.constants.Endpoints;
import com.sowmya.api.utils.ConfigManager;

public class UserServiceHelper {

    // We need to read the config variables
    // Rest Assured about the URL, port
    // MAke a Get request on this url and send the data to TestGetUser
    
    private static final String BASE_URL =  ConfigManager.getInstance().geString(("base_url"));
    private static final String PORT = ConfigManager.getInstance().geString("port");

    public UserServiceHelper(){
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = Integer.parseInt(PORT);
        RestAssured.useRelaxedHTTPSValidation();
    }

    public List<User> getAllUser() {
        Response response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .get(Endpoints.GET_ALL_USER)
                .andReturn();

        Type type = new TypeReference<List<User>>(){}.getType();

        List<User> userlist = response.as(type);
        return userlist;
    }
    
}
