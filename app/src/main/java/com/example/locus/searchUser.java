package com.example.locus;

public class searchUser {

    public String name, userName, profileImage;

    public searchUser(String name, String userName, String profileImage) {
        this.name = name;
        this.userName = userName;
        this.profileImage = profileImage;
    }

    public searchUser() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
