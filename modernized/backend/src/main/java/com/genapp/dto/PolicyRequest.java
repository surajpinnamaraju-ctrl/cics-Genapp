package com.genapp.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mirrors the LG* commarea union: common policy fields plus every
 * policy-type specific block. Only the fields for the path's policy
 * type are populated. Mutable so partial updates can omit fields (null).
 */
public class PolicyRequest {

    // common header
    public Long customerNumber;
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

    // house (value is shared with the motor block above)
    public String propertyType;
    public Short bedrooms;
    public String houseName;
    public String houseNumber;
    public String postcode;

    // commercial
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
