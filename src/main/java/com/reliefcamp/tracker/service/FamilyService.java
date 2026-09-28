package com.reliefcamp.tracker.service;

import com.reliefcamp.tracker.model.*;
import com.reliefcamp.tracker.exception.*;
import com.reliefcamp.tracker.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class FamilyService {
    private final FamilyRepository repo; private final CampRepository camps; private final DistributionRepository distributions; private final NotificationService notifications;
    public FamilyService(FamilyRepository repo, CampRepository camps, DistributionRepository distributions, NotificationService notifications) { this.repo=repo; this.camps=camps; this.distributions=distributions; this.notifications=notifications; }
    private Family get(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Family not found: " + id)); }
    private Camp camp(Long id) { return camps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + id)); }
    public List<Family> list(Long campId) { return campId == null ? repo.findAll() : repo.findByCamp_Id(campId); }
    public Family getOne(Long id) { return get(id); }
    public Family create(Family input) { input.setId(null); input.setHeadName(input.getHeadName().trim()); input.setContactNumber(input.getContactNumber().trim()); input.setStatus(FamilyStatus.REGISTERED); input.setCamp(null); return repo.save(input); }
    public Family update(Long id, Family input) {
        Family family=get(id);
        if(family.getStatus()==FamilyStatus.CHECKED_IN && !family.getMemberCount().equals(input.getMemberCount())) throw new BusinessRuleException("Member count cannot be changed while a family is checked in. Check out first.");
        family.setHeadName(input.getHeadName().trim()); family.setMemberCount(input.getMemberCount()); family.setContactNumber(input.getContactNumber().trim()); return repo.save(family);
    }
    @Transactional
    public Family checkIn(Long id, Family input) {
        Family family=get(id); if(family.getStatus()==FamilyStatus.CHECKED_IN) throw new BusinessRuleException("Family is already checked in.");
        Long campId=input.getCampId(); if(campId==null) throw new BusinessRuleException("Camp ID is required for check-in.");
        Camp camp=camp(campId); int newOccupancy=camp.getCurrentOccupancy()+family.getMemberCount();
        if(newOccupancy>camp.getCapacity()) throw new BusinessRuleException("Check-in rejected: camp has only "+(camp.getCapacity()-camp.getCurrentOccupancy())+" spaces remaining.");
        family.setCamp(camp); family.setStatus(FamilyStatus.CHECKED_IN); camp.setCurrentOccupancy(newOccupancy); camps.save(camp);
        notifications.statusChanged("Family #"+family.getId()+" checked into "+camp.getName()+"."); return repo.save(family);
    }
    @Transactional
    public Family checkOut(Long id) {
        Family family=get(id); if(family.getStatus()!=FamilyStatus.CHECKED_IN) throw new BusinessRuleException("Only a checked-in family can be checked out.");
        Camp camp=family.getCamp(); camp.setCurrentOccupancy(Math.max(0,camp.getCurrentOccupancy()-family.getMemberCount())); family.setStatus(FamilyStatus.CHECKED_OUT); family.setCamp(null); camps.save(camp);
        notifications.statusChanged("Family #"+family.getId()+" checked out."); return repo.save(family);
    }
    public void delete(Long id) { Family family=get(id); if(family.getStatus()==FamilyStatus.CHECKED_IN) throw new BusinessRuleException("Check out the family before deleting its registration."); if(distributions.countByFamily_Id(id)>0) throw new BusinessRuleException("This family has distribution history and cannot be deleted."); repo.delete(family); }
}
