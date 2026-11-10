package com.oms.core.repository;

import com.oms.core.entity.User;
import com.oms.core.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    
    // Custom derived method to isolate accounts assigned to a specific security role
    List<User> findByRolesContaining(Role role);
}