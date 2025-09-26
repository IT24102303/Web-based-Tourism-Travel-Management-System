package org.example.tourmanagement.support;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    List<Inquiry> findByEmailOrderByCreatedAtDesc(String email);
}


