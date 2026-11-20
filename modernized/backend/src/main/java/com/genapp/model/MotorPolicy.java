package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code motor} table. */
@Entity
@Table(name = "motor")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class MotorPolicy {

    @Id
    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "make", length = 15)
    private String make;

    @Column(name = "model", length = 15)
    private String model;

    @Column(name = "value")
    private Integer value;

    @Column(name = "reg_number", length = 7)
    private String regNumber;

    @Column(name = "colour", length = 8)
    private String colour;

    @Column(name = "cc")
    private Short cc;

    @Column(name = "year_of_manufacture")
    private LocalDate yearOfManufacture;

    @Column(name = "premium")
    private Integer premium;

    @Column(name = "accidents")
    private Integer accidents;

    public MotorPolicy(Long policyNumber) {
        this.policyNumber = policyNumber;
    }
}
