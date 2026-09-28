package com.reliefcamp.tracker.repository;
import com.reliefcamp.tracker.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface SupplyRepository extends JpaRepository<Supply,Long> {
    List<Supply> findByCamp_Id(Long campId);
    long countByCamp_Id(Long campId);
    boolean existsByCamp_IdAndType(Long campId, SupplyType type);
    Optional<Supply> findByCamp_IdAndType(Long campId, SupplyType type);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Supply s where s.id=:id") Optional<Supply> findByIdForUpdate(@Param("id") Long id);
    @Query("select coalesce(sum(s.quantity),0) from Supply s") long sumQuantity();
}
