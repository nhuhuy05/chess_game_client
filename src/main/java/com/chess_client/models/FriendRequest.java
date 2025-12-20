package com.chess_client.models;

/**
 * Model đại diện cho lời mời kết bạn
 */
public class FriendRequest {
    private int userId;
    private String username;
    private String displayName;
    private String avatar;

    public FriendRequest() {
    }

    public FriendRequest(int userId, String username, String displayName, String avatar) {
        this.userId = userId;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}








