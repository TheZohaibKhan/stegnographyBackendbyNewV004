package com.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.entity.EmailOtp;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, String> {

}
