package com.sowmya.api.constants;

public class Endpoints {

    // Authentication endpoints
    public static final String LOGIN = "/api/auth/login";
    public static final String LOGOUT = "/api/auth/logout";
    public static final String VERIFY_TOKEN = "api/auth/verify";

    // User endpoints
    public static final String GET_ALL_USER =  "/api/users";
    public static final String GET_SINGLE_USER = "/api/users/{id}"; 
    public static final String CREATE_USER = "/api/users";
    public static final String UPDATE_USER = "/api/users/{id}";
    public static final String DELETE_USER = "/api/users/{id}";

    // Health endopint
    public static final String HEALTH_CHECK = "/api/health";


}
