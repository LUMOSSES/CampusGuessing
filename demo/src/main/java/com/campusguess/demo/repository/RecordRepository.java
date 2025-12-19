package com.campusguess.demo.repository;

import com.campusguess.demo.model.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {
    java.util.List<Record> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}