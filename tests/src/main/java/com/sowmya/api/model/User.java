package com.sowmya.api.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"id",
"name",
"email",
"age",
"createdAt",
"updatedAt"
})
public class User {

@JsonProperty("id")
private String id;
@JsonProperty("name")
private String name;
@JsonProperty("email")
private String email;
@JsonProperty("age")
private Object age;
@JsonProperty("createdAt")
private String createdAt;
@JsonProperty("updatedAt")
private String updatedAt;
@JsonIgnore
private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

@JsonProperty("id")
public String getId() {
return id;
}

@JsonProperty("id")
public void setId(String id) {
this.id = id;
}

@JsonProperty("name")
public String getName() {
return name;
}

@JsonProperty("name")
public void setName(String name) {
this.name = name;
}

@JsonProperty("email")
public String getEmail() {
return email;
}

@JsonProperty("email")
public void setEmail(String email) {
this.email = email;
}

@JsonProperty("age")
public Object getAge() {
return age;
}

@JsonProperty("age")
public void setAge(Object age) {
this.age = age;
}

@JsonProperty("createdAt")
public String getCreatedAt() {
return createdAt;
}

@JsonProperty("createdAt")
public void setCreatedAt(String createdAt) {
this.createdAt = createdAt;
}

@JsonProperty("updatedAt")
public String getUpdatedAt() {
return updatedAt;
}

@JsonProperty("updatedAt")
public void setUpdatedAt(String updatedAt) {
this.updatedAt = updatedAt;
}

@JsonAnyGetter
public Map<String, Object> getAdditionalProperties() {
return this.additionalProperties;
}

@JsonAnySetter
public void setAdditionalProperty(String name, Object value) {
this.additionalProperties.put(name, value);
}

}