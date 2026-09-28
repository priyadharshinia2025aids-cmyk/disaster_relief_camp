package com.reliefcamp.tracker.config;

import com.reliefcamp.tracker.model.*;
import com.reliefcamp.tracker.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adds a small demo dataset only when the database is empty.
 * This makes the frontend immediately show meaningful output on first run.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedData(CampRepository camps, SupplyRepository supplies,
                               FamilyRepository families, DistributionRepository distributions) {
        return args -> seed(camps, supplies, families, distributions);
    }

    @Transactional
    void seed(CampRepository camps, SupplyRepository supplies,
              FamilyRepository families, DistributionRepository distributions) {
        if (camps.count() > 0 || supplies.count() > 0 || families.count() > 0 || distributions.count() > 0) {
            return;
        }

        Camp pollachi = new Camp();
        pollachi.setName("Pollachi Relief Camp");
        pollachi.setLocation("Pollachi");
        pollachi.setCapacity(250);
        pollachi.setCurrentOccupancy(0);
        pollachi.setStatus("ACTIVE");
        camps.save(pollachi);

        Camp coimbatore = new Camp();
        coimbatore.setName("Coimbatore Community Camp");
        coimbatore.setLocation("Coimbatore");
        coimbatore.setCapacity(180);
        coimbatore.setCurrentOccupancy(0);
        coimbatore.setStatus("ACTIVE");
        camps.save(coimbatore);

        Camp valparai = new Camp();
        valparai.setName("Valparai Emergency Camp");
        valparai.setLocation("Valparai");
        valparai.setCapacity(120);
        valparai.setCurrentOccupancy(0);
        valparai.setStatus("ACTIVE");
        camps.save(valparai);

        addSupply(supplies, pollachi, SupplyType.WATER, 500, "litres");
        addSupply(supplies, pollachi, SupplyType.FOOD, 320, "packs");
        addSupply(supplies, pollachi, SupplyType.MEDICAL_KITS, 45, "kits");
        addSupply(supplies, coimbatore, SupplyType.BLANKETS, 150, "pieces");
        addSupply(supplies, valparai, SupplyType.HYGIENE_KITS, 80, "kits");

        Family family1 = new Family();
        family1.setHeadName("Arun Kumar");
        family1.setMemberCount(4);
        family1.setContactNumber("9876543210");
        family1.setStatus(FamilyStatus.CHECKED_IN);
        family1.setCamp(pollachi);
        families.save(family1);

        Family family2 = new Family();
        family2.setHeadName("Meena Devi");
        family2.setMemberCount(3);
        family2.setContactNumber("9876501234");
        family2.setStatus(FamilyStatus.CHECKED_IN);
        family2.setCamp(coimbatore);
        families.save(family2);

        Family family3 = new Family();
        family3.setHeadName("Ravi Shankar");
        family3.setMemberCount(5);
        family3.setContactNumber("9123456780");
        family3.setStatus(FamilyStatus.REGISTERED);
        families.save(family3);

        pollachi.setCurrentOccupancy(4);
        coimbatore.setCurrentOccupancy(3);
        camps.save(pollachi);
        camps.save(coimbatore);

        Supply water = supplies.findByCamp_IdAndType(pollachi.getId(), SupplyType.WATER).orElseThrow();
        Distribution distribution = new Distribution();
        distribution.setCamp(pollachi);
        distribution.setFamily(family1);
        distribution.setSupply(water);
        distribution.setQuantity(20);
        distribution.setDistributedAt(java.time.LocalDateTime.now().minusHours(1));
        water.setQuantity(water.getQuantity() - 20);
        supplies.save(water);
        distributions.save(distribution);
    }

    private void addSupply(SupplyRepository supplies, Camp camp, SupplyType type, int quantity, String unit) {
        Supply supply = new Supply();
        supply.setCamp(camp);
        supply.setType(type);
        supply.setQuantity(quantity);
        supply.setUnit(unit);
        supplies.save(supply);
    }
}
