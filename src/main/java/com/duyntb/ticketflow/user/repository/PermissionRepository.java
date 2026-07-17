package com.duyntb.ticketflow.user.repository;

import com.duyntb.ticketflow.user.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}
