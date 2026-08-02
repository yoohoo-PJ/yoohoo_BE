package com.example.yoohoo_be.checklists.repository;

import com.example.yoohoo_be.checklists.domain.CheckItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckItemRepository extends JpaRepository<CheckItem, Long> {
}