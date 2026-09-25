package com.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_otp")
public class EmailOtp {

    @Id
    private String email;

    private String otp;

    private LocalDateTime expiryTime;

	public EmailOtp() {
		super();
		// TODO Auto-generated constructor stub
	}

	public EmailOtp(String email, String otp, LocalDateTime expiryTime) {
		super();
		this.email = email;
		this.otp = otp;
		this.expiryTime = expiryTime;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public LocalDateTime getExpiryTime() {
		return expiryTime;
	}

	public void setExpiryTime(LocalDateTime expiryTime) {
		this.expiryTime = expiryTime;
	}

	@Override
	public String toString() {
		return "EmailOtp [email=" + email + ", otp=" + otp + ", expiryTime=" + expiryTime + "]";
	}
	
	
}

