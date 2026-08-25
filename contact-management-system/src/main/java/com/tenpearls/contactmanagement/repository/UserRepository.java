package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}