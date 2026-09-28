package com.reliefcamp.tracker.repository;
import com.reliefcamp.tracker.model.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface FamilyRepository extends JpaRepository<Family,Long> {
    List<Family> findByCamp_Id(Long campId);
    long countByCamp_Id(Long campId);
    long countByStatus(FamilyStatus status);
}
