package com.chess_client.models;

/**
 * Model đại diện cho kết quả tìm kiếm người dùng
 */
public class SearchUser {
    private int id;
    private String username;
    private String displayName;
    private String avatar;
    private String status; // "online" hoặc "offline"
    private String friendshipStatus; // "none", "pending_sent", "pending_received", "accepted"

    public SearchUser() {
    }

    public SearchUser(int id, String username, String displayName, String avatar, 
                      String status, String friendshipStatus) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
        this.status = status;
        this.friendshipStatus = friendshipStatus;
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

    public String getFriendshipStatus() {
        return friendshipStatus;
    }

    public void setFriendshipStatus(String friendshipStatus) {
        this.friendshipStatus = friendshipStatus;
    }

    public boolean isOnline() {
        return "online".equalsIgnoreCase(status);
    }

    public boolean isFriend() {
        return "accepted".equals(friendshipStatus);
    }

    public boolean isPendingSent() {
        return "pending_sent".equals(friendshipStatus);
    }

    public boolean isPendingReceived() {
        return "pending_received".equals(friendshipStatus);
    }

    @Override
    public String toString() {
        return "SearchUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", friendshipStatus='" + friendshipStatus + '\'' +
                '}';
    }
}








