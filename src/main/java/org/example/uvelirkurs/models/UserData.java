package org.example.uvelirkurs.models;

public class UserData {
    private final int id;
    private final String username;
    private final String email;
    private final String fullname;
    private final String phone;
    private final String role;

    public UserData(int id, String username, String email, String fullname, String phone, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.role = role;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullname() { return fullname; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
}
