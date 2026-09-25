package com.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class UploadHistory {

  
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String fileType;   // IMAGE / AUDIO / VIDEO
    private String actionType; // ENCODE / DECODE
    private String fileName;

    private LocalDateTime uploadTime;

    // getters and setters
    
    public UploadHistory() {
  		super();
  		// TODO Auto-generated constructor stub
  	}

	public UploadHistory(Long id, String username, String fileType, String actionType, String fileName,
			LocalDateTime uploadTime) {
		super();
		this.id = id;
		this.username = username;
		this.fileType = fileType;
		this.actionType = actionType;
		this.fileName = fileName;
		this.uploadTime = uploadTime;
	}

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

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getActionType() {
		return actionType;
	}

	public void setActionType(String actionType) {
		this.actionType = actionType;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public LocalDateTime getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(LocalDateTime uploadTime) {
		this.uploadTime = uploadTime;
	}

	@Override
	public String toString() {
		return "UploadHistory [id=" + id + ", username=" + username + ", fileType=" + fileType + ", actionType="
				+ actionType + ", fileName=" + fileName + ", uploadTime=" + uploadTime + "]";
	}

    
}


