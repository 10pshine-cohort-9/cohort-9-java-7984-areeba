package com.tenpearls.contactmanagement.repository;

import com.tenpearls.contactmanagement.entity.ContactEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ContactEmailRepository extends JpaRepository<ContactEmail, Long> {

    List<ContactEmail> findByContactId(Long contactId);

    List<ContactEmail> findByContact_IdIn(Collection<Long> contactIds);

    void deleteByContactId(Long contactId);
}
