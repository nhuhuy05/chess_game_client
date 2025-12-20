package com.chess_client.models;

/**
 * Model đại diện cho một bạn bè
 */
public class Friend {
    private int id;
    private String username;
    private String displayName;
    private String avatar;
    private String status; // "online" hoặc "offline"

    public Friend() {
    }

    public Friend(int id, String username, String displayName, String avatar, String status) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return "online".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Friend{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}








