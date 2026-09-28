package com.reliefcamp.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "camps")
public class Camp {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String location;

    @NotNull @Min(1)
    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentOccupancy = 0;

    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getCurrentOccupancy() { return currentOccupancy; }
    public void setCurrentOccupancy(Integer currentOccupancy) { this.currentOccupancy = currentOccupancy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @JsonProperty("availableCapacity")
    public Integer getAvailableCapacity() {
        return capacity == null ? null : capacity - (currentOccupancy == null ? 0 : currentOccupancy);
    }
}
