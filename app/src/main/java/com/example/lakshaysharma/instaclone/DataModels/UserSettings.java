package com.example.lakshaysharma.instaclone.DataModels;

public class UserSettings {

    private User user;
    private UserAccountSettings accountSettings;

    public UserSettings() {

    }

    public UserSettings(User user, UserAccountSettings accountSettings) {
        this.user = user;
        this.accountSettings = accountSettings;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserAccountSettings getAccountSettings() {
        return accountSettings;
    }

    public void setAccountSettings(UserAccountSettings accountSettings) {
        this.accountSettings = accountSettings;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "user=" + user +
                ", accountSettings=" + accountSettings +
                '}';
    }
}
