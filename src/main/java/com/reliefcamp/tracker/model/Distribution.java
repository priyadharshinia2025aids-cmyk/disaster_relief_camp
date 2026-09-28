package com.reliefcamp.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "distributions")
public class Distribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "camp_id", nullable = false)
    @JsonIgnore
    private Camp camp;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "family_id", nullable = false)
    @JsonIgnore
    private Family family;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "supply_id", nullable = false)
    @JsonIgnore
    private Supply supply;

    @NotNull @Positive
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime distributedAt;

    @Transient private Long campIdInput;
    @Transient private Long familyIdInput;
    @Transient private Long supplyIdInput;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @JsonIgnore public Camp getCamp() { return camp; }
    public void setCamp(Camp camp) { this.camp = camp; }
    @JsonIgnore public Family getFamily() { return family; }
    public void setFamily(Family family) { this.family = family; }
    @JsonIgnore public Supply getSupply() { return supply; }
    public void setSupply(Supply supply) { this.supply = supply; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public LocalDateTime getDistributedAt() { return distributedAt; }
    public void setDistributedAt(LocalDateTime distributedAt) { this.distributedAt = distributedAt; }

    @JsonProperty("campId") public Long getCampId() { return camp != null ? camp.getId() : campIdInput; }
    public void setCampId(Long id) { campIdInput = id; }
    @JsonProperty("familyId") public Long getFamilyId() { return family != null ? family.getId() : familyIdInput; }
    public void setFamilyId(Long id) { familyIdInput = id; }
    @JsonProperty("supplyId") public Long getSupplyId() { return supply != null ? supply.getId() : supplyIdInput; }
    public void setSupplyId(Long id) { supplyIdInput = id; }
    @JsonProperty("campName") public String getCampName() { return camp == null ? null : camp.getName(); }
    @JsonProperty("familyHeadName") public String getFamilyHeadName() { return family == null ? null : family.getHeadName(); }
    @JsonProperty("supplyType") public String getSupplyType() { return supply == null || supply.getType() == null ? null : supply.getType().name(); }
    @JsonProperty("unit") public String getUnit() { return supply == null ? null : supply.getUnit(); }
}
