package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code Commercial} table. */
@Entity
@Table(name = "commercial")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CommercialPolicy {

    @Id
    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "renewal_date")
    private LocalDate renewalDate;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "zipcode", length = 8)
    private String zipcode;

    @Column(name = "latitude_n", length = 11)
    private String latitudeN;

    @Column(name = "longitude_w", length = 11)
    private String longitudeW;

    @Column(name = "customer", length = 255)
    private String customer;

    @Column(name = "property_type", length = 255)
    private String propertyType;

    @Column(name = "fire_peril")
    private Short firePeril;

    @Column(name = "fire_premium")
    private Integer firePremium;

    @Column(name = "crime_peril")
    private Short crimePeril;

    @Column(name = "crime_premium")
    private Integer crimePremium;

    @Column(name = "flood_peril")
    private Short floodPeril;

    @Column(name = "flood_premium")
    private Integer floodPremium;

    @Column(name = "weather_peril")
    private Short weatherPeril;

    @Column(name = "weather_premium")
    private Integer weatherPremium;

    @Column(name = "status")
    private Short status;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    public CommercialPolicy(Long policyNumber) {
        this.policyNumber = policyNumber;
    }
}
