package com.reliefcamp.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "supplies", uniqueConstraints = @UniqueConstraint(name = "uk_camp_supply_type", columnNames = {"camp_id", "type"}))
public class Supply {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "camp_id", nullable = false)
    @JsonIgnore
    private Camp camp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @NotNull
    private SupplyType type;

    @Column(nullable = false)
    @NotNull @PositiveOrZero
    private Integer quantity;

    @Column(nullable = false, length = 30)
    @NotBlank @Size(max = 30)
    private String unit;

    @Transient
    private Long campIdInput;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    @JsonIgnore public Camp getCamp() { return camp; }
    public void setCamp(Camp camp) { this.camp = camp; }
    public SupplyType getType() { return type; }
    public void setType(SupplyType type) { this.type = type; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @JsonProperty("campId")
    public Long getCampId() { return camp != null ? camp.getId() : campIdInput; }
    public void setCampId(Long campId) { this.campIdInput = campId; }

    @JsonProperty("campName")
    public String getCampName() { return camp == null ? null : camp.getName(); }
}
