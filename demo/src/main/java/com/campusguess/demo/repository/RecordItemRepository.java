package com.campusguess.demo.repository;

import com.campusguess.demo.model.entity.RecordItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordItemRepository extends JpaRepository<RecordItem, Long> {
}
