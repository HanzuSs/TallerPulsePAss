package com.pulsepass.repository;

import com.pulsepass.domain.UserRole;
import com.pulsepass.domain.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUser_Id(Long userId);
}
