package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    private String role;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Voter ID is required")
    private String voterId;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    // ================= VOTING HISTORY =================
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_voted_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "election_type")
    private Set<String> votedTypes = new HashSet<>();

    // ================= PROFILE =================
    private LocalDate dob;
    private int age;

    private int failedAttempts;
    private boolean accountLocked;

    @Column(nullable = false)
    private boolean verified = false;

    // ================= GETTERS & SETTERS =================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getVotedTypes() {
        return votedTypes;
    }

    public void setVotedTypes(Set<String> votedTypes) {
        this.votedTypes = votedTypes;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public void setAccountLocked(boolean accountLocked) {
        this.accountLocked = accountLocked;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}