package com.takeone.auth.repository;

import com.takeone.auth.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ManagerActiveContextRepository extends JpaRepository<ManagerActiveContext, Long> {
    
    // TODO: Add custom query methods as needed
    // Examples:
    // Optional<User> findByFirebaseUid(String firebaseUid);
    // List<ProfileManager> findByManagerUserIdAndStatus(Long managerId, String status);
    // Optional<RefreshToken> findByTokenAndIsRevoked(String token, boolean isRevoked);
}
