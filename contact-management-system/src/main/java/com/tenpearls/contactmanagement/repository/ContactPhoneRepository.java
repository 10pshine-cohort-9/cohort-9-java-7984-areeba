package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.ContactPhone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactPhoneRepository extends JpaRepository<ContactPhone, Long> {

    List<ContactPhone> findByContactId(Long contactId);

    void deleteByContactId(Long contactId);
}
