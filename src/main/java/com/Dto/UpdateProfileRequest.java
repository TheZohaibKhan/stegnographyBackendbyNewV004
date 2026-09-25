package com.Dto;

public class UpdateProfileRequest {

    private String name;
    private String mobileNo;
    private String bio;
    private String currentPassword;
    private String newPassword;

    public String getName()            { return name; }
    public void setName(String n)      { this.name = n; }

    public String getMobileNo()              { return mobileNo; }
    public void setMobileNo(String m)        { this.mobileNo = m; }

    public String getBio()             { return bio; }
    public void setBio(String b)       { this.bio = b; }

    public String getCurrentPassword()                     { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword()                  { return newPassword; }
    public void setNewPassword(String newPassword)  { this.newPassword = newPassword; }
}