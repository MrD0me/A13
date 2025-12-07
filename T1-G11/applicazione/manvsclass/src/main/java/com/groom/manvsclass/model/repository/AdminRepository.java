package com.groom.manvsclass.model.repository;

import com.groom.manvsclass.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByResetToken(String resetToken);

    Optional<Admin> findByInvitationToken(String invitationToken);

    long count();
}
