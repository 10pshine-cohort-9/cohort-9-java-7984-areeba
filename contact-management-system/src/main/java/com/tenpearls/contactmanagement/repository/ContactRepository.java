package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}