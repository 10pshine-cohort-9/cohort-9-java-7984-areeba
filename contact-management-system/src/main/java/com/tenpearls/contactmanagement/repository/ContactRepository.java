package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    Page<Contact> findByUserId(Long userId, Pageable pageable);

    Optional<Contact> findByIdAndUserId(Long id, Long userId);

    List<Contact> findAllByUserIdOrderByLastNameAscFirstNameAsc(Long userId);

    @Query("""
            SELECT c FROM Contact c
            WHERE c.user.id = :userId
            AND (:firstName IS NULL OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :firstName, '%')))
            AND (:lastName IS NULL OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))
            """)
    Page<Contact> searchByUserId(
            @Param("userId") Long userId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            Pageable pageable
    );
}
