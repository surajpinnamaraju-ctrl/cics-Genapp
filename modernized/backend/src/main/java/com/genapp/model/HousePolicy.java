package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code house} table. */
@Entity
@Table(name = "house")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class HousePolicy {

    @Id
    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "property_type", length = 15)
    private String propertyType;

    @Column(name = "bedrooms")
    private Short bedrooms;

    @Column(name = "value")
    private Integer value;

    @Column(name = "house_name", length = 20)
    private String houseName;

    @Column(name = "house_number", length = 4)
    private String houseNumber;

    @Column(name = "postcode", length = 8)
    private String postcode;

    public HousePolicy(Long policyNumber) {
        this.policyNumber = policyNumber;
    }
}
