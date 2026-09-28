package com.reliefcamp.tracker.service;

import com.reliefcamp.tracker.model.*;
import com.reliefcamp.tracker.exception.*;
import com.reliefcamp.tracker.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class SupplyService {
    private final SupplyRepository repo; private final CampRepository camps; private final DistributionRepository distributions;
    public SupplyService(SupplyRepository repo, CampRepository camps, DistributionRepository distributions) { this.repo=repo; this.camps=camps; this.distributions=distributions; }
    private Camp camp(Long id) { return camps.findById(id).orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + id)); }
    private Supply get(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Supply not found: " + id)); }
    public List<Supply> list(Long campId) { return (campId == null ? repo.findAll() : repo.findByCamp_Id(campId)); }
    public Supply getOne(Long id) { return get(id); }
    public Supply create(Supply input) {
        Long campId = input.getCampId();
        if (campId == null) throw new BusinessRuleException("Camp ID is required.");
        if (repo.existsByCamp_IdAndType(campId, input.getType())) throw new BusinessRuleException("This camp already has a " + input.getType() + " inventory record.");
        input.setId(null); input.setCamp(camp(campId)); input.setUnit(input.getUnit().trim()); return repo.save(input);
    }
    public Supply update(Long id, Supply input) {
        Supply supply = get(id); Long campId = input.getCampId();
        if (campId == null) throw new BusinessRuleException("Camp ID is required.");
        if (!supply.getCamp().getId().equals(campId) && repo.existsByCamp_IdAndType(campId, input.getType())) throw new BusinessRuleException("This camp already has a " + input.getType() + " inventory record.");
        supply.setCamp(camp(campId)); supply.setType(input.getType()); supply.setQuantity(input.getQuantity()); supply.setUnit(input.getUnit().trim()); return repo.save(supply);
    }
    public void delete(Long id) { Supply supply=get(id); if(distributions.countBySupply_Id(id)>0) throw new BusinessRuleException("This supply has distribution history and cannot be deleted."); repo.delete(supply); }
    public Supply locked(Long id) { return repo.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Supply not found: " + id)); }
}
