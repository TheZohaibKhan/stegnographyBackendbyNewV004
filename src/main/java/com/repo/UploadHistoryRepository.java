package com.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.entity.UploadHistory;

public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Long> {
    List<UploadHistory> findByUsername(String username);
    long countByActionType(String actionType);
    
    List<UploadHistory> findByUsernameOrderByUploadTimeDesc(String username);
}
