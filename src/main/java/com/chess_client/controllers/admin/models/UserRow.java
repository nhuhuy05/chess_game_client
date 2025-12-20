package com.chess_client.controllers.admin.models;

/**
 * Data class cho hàng trong bảng người dùng
 */
public class UserRow {
    private final Integer id;
    private final String username;
    private final String displayName;
    private final String email;
    private final String phone;
    private final String status;

    public UserRow(Integer id, String username, String displayName, String email,
            String phone, String status) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.phone = phone;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getStatus() {
        return status;
    }
}

