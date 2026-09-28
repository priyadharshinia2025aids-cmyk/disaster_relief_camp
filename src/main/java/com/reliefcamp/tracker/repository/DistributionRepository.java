package com.reliefcamp.tracker.repository;
import com.reliefcamp.tracker.model.Distribution;
import org.springframework.data.jpa.repository.*;
import java.time.*;
public interface DistributionRepository extends JpaRepository<Distribution,Long> {
    long countByDistributedAtGreaterThanEqual(LocalDateTime time);
    long countByCamp_Id(Long campId);
    long countByFamily_Id(Long familyId);
    long countBySupply_Id(Long supplyId);
}
