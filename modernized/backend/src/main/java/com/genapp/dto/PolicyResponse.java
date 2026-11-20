package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response mirroring the commarea policy union: common header fields plus
 * every policy-type specific block. Only the fields for the policy's type
 * are populated; the rest are null.
 */
public class PolicyResponse {

    // common header
    public Long policyNumber;
    public Long customerNumber;
    public String policyType;
    public LocalDate issueDate;
    public LocalDate expiryDate;
    public LocalDateTime lastChanged;
    public Long brokerId;
    public String brokersReference;
    public Integer payment;

    // motor
    public String make;
    public String model;
    public Integer value;
    public String regNumber;
    public String colour;
    public Short cc;
    public LocalDate yearOfManufacture;
    public Integer premium;
    public Integer accidents;

    // endowment
    public String withProfits;
    public String equities;
    public String managedFund;
    public String fundName;
    public Short term;
    public Integer sumAssured;
    public String lifeAssured;
    public String paddingData;

    // house
    public String propertyType;
    public Short bedrooms;
    public String houseName;
    public String houseNumber;
    public String housePostcode;

    // commercial
    public LocalDateTime requestDate;
    public LocalDate startDate;
    public LocalDate renewalDate;
    public String address;
    public String zipcode;
    public String latitude;
    public String longitude;
    public String customer;
    public Short firePeril;
    public Integer firePremium;
    public Short crimePeril;
    public Integer crimePremium;
    public Short floodPeril;
    public Integer floodPremium;
    public Short weatherPeril;
    public Integer weatherPremium;
    public Short status;
    public String rejectionReason;
}
