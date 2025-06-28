package com.giang.dataFAQ.repository;

import com.giang.dataFAQ.model.FAQ;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Integer> {
    FAQ findByCode(String code);
}


