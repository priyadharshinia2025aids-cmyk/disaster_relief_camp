package com.reliefcamp.tracker.service;

import com.reliefcamp.tracker.model.*;
import com.reliefcamp.tracker.exception.*;
import com.reliefcamp.tracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
public class DistributionService {
    private final DistributionRepository repo; private final CampRepository camps; private final FamilyRepository families; private final SupplyRepository supplies; private final NotificationService notifications;
    public DistributionService(DistributionRepository repo, CampRepository camps, FamilyRepository families, SupplyRepository supplies, NotificationService notifications) { this.repo=repo; this.camps=camps; this.families=families; this.supplies=supplies; this.notifications=notifications; }
    private Camp camp(Long id) { return camps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Camp not found: "+id)); }
    private Family family(Long id) { return families.findById(id).orElseThrow(() -> new ResourceNotFoundException("Family not found: "+id)); }
    private Distribution get(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Distribution not found: "+id)); }
    public List<Distribution> list() { return repo.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"distributedAt")); }
    @Transactional
    public Distribution create(Distribution input) {
        Long campId=input.getCampId(), familyId=input.getFamilyId(), supplyId=input.getSupplyId();
        if(campId==null || familyId==null || supplyId==null) throw new BusinessRuleException("Camp ID, family ID and supply ID are required.");
        Camp camp=camp(campId); Family family=family(familyId); Supply supply=supplies.findByIdForUpdate(supplyId).orElseThrow(() -> new ResourceNotFoundException("Supply not found: "+supplyId));
        if(family.getStatus()!=FamilyStatus.CHECKED_IN || family.getCamp()==null || !family.getCamp().getId().equals(camp.getId())) throw new BusinessRuleException("Distribution rejected: family must be checked in at the selected camp.");
        if(!supply.getCamp().getId().equals(camp.getId())) throw new BusinessRuleException("Distribution rejected: supply belongs to a different camp.");
        if(input.getQuantity()>supply.getQuantity()) throw new BusinessRuleException("Distribution rejected: requested quantity exceeds current inventory ("+supply.getQuantity()+" "+supply.getUnit()+").");
        supply.setQuantity(supply.getQuantity()-input.getQuantity()); supplies.save(supply);
        input.setId(null); input.setCamp(camp); input.setFamily(family); input.setSupply(supply); input.setDistributedAt(LocalDateTime.now());
        notifications.statusChanged("Distributed "+input.getQuantity()+" "+supply.getUnit()+" of "+supply.getType()+" to family #"+family.getId()+" at "+camp.getName()+".");
        return repo.save(input);
    }
    @Transactional
    public void delete(Long id) { Distribution distribution=get(id); Supply supply=supplies.findByIdForUpdate(distribution.getSupply().getId()).orElseThrow(() -> new ResourceNotFoundException("Supply not found: "+distribution.getSupply().getId())); supply.setQuantity(supply.getQuantity()+distribution.getQuantity()); supplies.save(supply); repo.delete(distribution); }
}
