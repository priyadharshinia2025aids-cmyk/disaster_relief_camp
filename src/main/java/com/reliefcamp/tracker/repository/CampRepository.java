package com.reliefcamp.tracker.repository;
import com.reliefcamp.tracker.model.Camp;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CampRepository extends JpaRepository<Camp,Long> { long countByStatus(String status); }
