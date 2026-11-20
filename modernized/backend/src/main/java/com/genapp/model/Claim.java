package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code Claim} table. No base GenApp transaction reads or writes it. */
@Entity
@Table(name = "claim")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Claim {

    @Id
    @Column(name = "claim_number", nullable = false)
    private Long claimNumber;

    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "claim_date")
    private LocalDate claimDate;

    @Column(name = "paid")
    private Integer paid;

    @Column(name = "value")
    private Integer value;

    @Column(name = "cause", length = 255)
    private String cause;

    @Column(name = "observations", length = 255)
    private String observations;
}
