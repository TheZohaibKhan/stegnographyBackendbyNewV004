package com.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username; // email

    private String name;
    private String mobileNo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String role = "ROLE_USER";

    @Column(nullable = false)
    private Boolean verified = false;

    private String bio;
    private String profilePic;
    private LocalDateTime lastLogin;
    private Boolean active = true;

    // ── Constructors ──────────────────────────────────────────
    public User() {}

    // ── Getters / Setters ─────────────────────────────────────
    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public String getUsername()                { return username; }
    public void setUsername(String username)   { this.username = username; }

    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }

    public String getMobileNo()                { return mobileNo; }
    public void setMobileNo(String mobileNo)   { this.mobileNo = mobileNo; }

    public String getPassword()                { return password; }
    public void setPassword(String password)   { this.password = password; }

    public String getRole()                    { return role; }
    public void setRole(String role)           { this.role = role; }

    public Boolean getVerified()               { return verified; }
    public void setVerified(Boolean verified)  { this.verified = verified; }
    public boolean isVerified()                { return verified != null && verified; }

    public String getBio()                     { return bio; }
    public void setBio(String bio)             { this.bio = bio; }

    public String getProfilePic()              { return profilePic; }
    public void setProfilePic(String p)        { this.profilePic = p; }

    public LocalDateTime getLastLogin()                  { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin)    { this.lastLogin = lastLogin; }

    public Boolean getActive()                 { return active; }
    public void setActive(Boolean active)      { this.active = active; }
    public boolean isActive()                  { return active == null || active; }
}