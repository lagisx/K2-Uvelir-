package org.example.uvelirkurs.models;

public class UserData {
    private final int id;
    private final String username;
    private final String email;
    private final String fullname;
    private final String phone;
    private final String role;
    private final boolean active;

    public UserData(int id, String username, String email, String fullname, String phone, String role) {
        this(id, username, email, fullname, phone, role, true);
    }

    public UserData(int id, String username, String email, String fullname, String phone, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullname() { return fullname; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    public String getStatus() { return active ? "Активен" : "Заблокирован"; }
}
