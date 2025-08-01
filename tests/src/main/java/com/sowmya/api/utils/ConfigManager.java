package com.sowmya.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ConfigManager {

    private static ConfigManager manager;
    private static final Properties prop = new Properties();
    
    private ConfigManager () throws IOException{

        InputStream inputStream = ConfigManager.class.getResourceAsStream("/config.properties");
        System.out.println("Loading configuration from: " + inputStream);
        prop.load(inputStream);
    }

    public void loadProperties() throws IOException {

        InputStream inputStream = ConfigManager.class.getResourceAsStream("/config.properties");
        if (inputStream != null) {
            prop.load(inputStream);
            inputStream.close();
        } else {
            throw new IOException("Not able to find config.properties");
        }
    }
    
    public static ConfigManager getInstance(){

        if (manager == null){
            synchronized (ConfigManager.class){
                try{
                manager = new ConfigManager();
                }catch(IOException e){
                    e.printStackTrace();
                }
                }

            }
            return manager;
        }
    public String geString(String key) {
            return System.getProperty(key, prop.getProperty(key));
    }

    public String getProperty(String key, String defaultValue) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null) {
            return systemProperty;
        }
        return prop.getProperty(key, defaultValue);
    }
        
}

