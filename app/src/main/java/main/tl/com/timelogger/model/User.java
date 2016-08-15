package main.tl.com.timelogger.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by vipulmittal on 28/06/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String uid;
    private String email;
    private String name;
    private String imageURL;
    private boolean isManager;
    private boolean isAdmin;
    private static User currentUser;
    private String key;

    public boolean isManager() {
        return isManager;
    }

    public void setManager(boolean manager) {
        isManager = manager;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
