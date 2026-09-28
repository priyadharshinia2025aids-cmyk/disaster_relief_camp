package com.reliefcamp.tracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "families")
public class Family {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String headName;

    @NotNull @Min(1)
    @Column(nullable = false)
    private Integer memberCount;

    @NotBlank @Pattern(regexp = "[0-9+() -]{7,20}", message = "Enter a valid contact number")
    @Column(nullable = false, length = 20)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FamilyStatus status = FamilyStatus.REGISTERED;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "camp_id")
    @JsonIgnore
    private Camp camp;

    @Transient
    private Long campIdInput;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getHeadName() { return headName; }
    public void setHeadName(String headName) { this.headName = headName; }
    public Integer getMemberCount() { return memberCount; }
    public void setMemberCount(Integer memberCount) { this.memberCount = memberCount; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public FamilyStatus getStatus() { return status; }
    public void setStatus(FamilyStatus status) { this.status = status; }

    @JsonIgnore
    public Camp getCamp() { return camp; }
    public void setCamp(Camp camp) { this.camp = camp; }

    @JsonProperty("campId")
    public Long getCampId() { return camp != null ? camp.getId() : campIdInput; }
    public void setCampId(Long campId) { this.campIdInput = campId; }

    @JsonProperty("campName")
    public String getCampName() { return camp == null ? null : camp.getName(); }
}
