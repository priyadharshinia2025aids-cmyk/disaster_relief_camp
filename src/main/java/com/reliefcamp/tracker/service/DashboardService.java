package com.reliefcamp.tracker.service;

import com.reliefcamp.tracker.model.FamilyStatus;
import com.reliefcamp.tracker.repository.*;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.*;

@Service
public class DashboardService {
    private final CampRepository camps; private final FamilyRepository families; private final SupplyRepository supplies; private final DistributionRepository distributions;
    public DashboardService(CampRepository camps, FamilyRepository families, SupplyRepository supplies, DistributionRepository distributions) {
        this.camps=camps; this.families=families; this.supplies=supplies; this.distributions=distributions;
    }
    public Map<String, Long> get() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        Map<String, Long> data = new LinkedHashMap<>();
        data.put("camps", camps.count()); data.put("activeCamps", camps.countByStatus("ACTIVE"));
        data.put("families", families.count()); data.put("checkedInFamilies", families.countByStatus(FamilyStatus.CHECKED_IN));
        data.put("totalSupplyUnits", supplies.sumQuantity()); data.put("distributionsToday", distributions.countByDistributedAtGreaterThanEqual(start));
        return data;
    }
}
