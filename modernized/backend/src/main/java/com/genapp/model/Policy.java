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

/** Db2 {@code policy} table — common header joined to per-type tables on policyNumber. */
@Entity
@Table(name = "policy")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Policy {

    @Id
    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "customer_number", nullable = false)
    private Long customerNumber;

    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "policy_type", length = 1)
    private String policyType;

    @Column(name = "last_changed", nullable = false)
    private LocalDateTime lastChanged;

    @Column(name = "broker_id")
    private Long brokerId;

    @Column(name = "brokers_reference", length = 10)
    private String brokersReference;

    @Column(name = "payment")
    private Integer payment;

    @Column(name = "commission")
    private Short commission;

    public Policy(Long policyNumber) {
        this.policyNumber = policyNumber;
    }
}
