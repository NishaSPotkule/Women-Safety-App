package com.myandroid.nariguard;

public class UserProfile {
    private String fullName;
    private String phone;
    private String email;

    public UserProfile() {
        // Empty constructor required for Firestore
    }

    public UserProfile(String fullName, String phone, String email) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
}

