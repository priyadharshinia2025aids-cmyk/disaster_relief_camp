package com.reliefcamp.tracker.service;

import com.reliefcamp.tracker.model.Camp;
import com.reliefcamp.tracker.exception.*;
import com.reliefcamp.tracker.repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CampService {
    private final CampRepository repo;
    private final SupplyRepository supplies;
    private final FamilyRepository families;
    private final DistributionRepository distributions;

    public CampService(CampRepository repo, SupplyRepository supplies, FamilyRepository families, DistributionRepository distributions) {
        this.repo = repo; this.supplies = supplies; this.families = families; this.distributions = distributions;
    }
    private Camp get(Long id) { return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + id)); }
    public Page<Camp> list(int page, int size, String sort, String direction) {
        Sort s = Sort.by(Sort.Direction.fromString(direction), sort);
        return repo.findAll(PageRequest.of(page, size, s));
    }
    public Camp getOne(Long id) { return get(id); }
    public Camp create(Camp camp) {
        camp.setId(null); camp.setName(camp.getName().trim()); camp.setLocation(camp.getLocation().trim());
        camp.setCurrentOccupancy(0); camp.setStatus("ACTIVE"); return repo.save(camp);
    }
    public Camp update(Long id, Camp input) {
        Camp camp = get(id);
        if (input.getCapacity() < camp.getCurrentOccupancy()) throw new BusinessRuleException("Capacity cannot be lower than current occupancy.");
        camp.setName(input.getName().trim()); camp.setLocation(input.getLocation().trim()); camp.setCapacity(input.getCapacity());
        return repo.save(camp);
    }
    public void delete(Long id) {
        Camp camp = get(id);
        if (camp.getCurrentOccupancy() > 0) throw new BusinessRuleException("A camp with checked-in families cannot be deleted.");
        if (supplies.countByCamp_Id(id) > 0 || families.countByCamp_Id(id) > 0 || distributions.countByCamp_Id(id) > 0)
            throw new BusinessRuleException("This camp has linked inventory, family, or distribution records and cannot be deleted.");
        repo.delete(camp);
    }
}
