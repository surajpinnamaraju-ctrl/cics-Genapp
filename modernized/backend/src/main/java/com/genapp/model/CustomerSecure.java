package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Db2 {@code customer_secure} table. Seeded for schema parity; the base
 * GenApp application never reads it (security-extension scenarios only).
 */
@Entity
@Table(name = "customer_secure")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class CustomerSecure {

    @Id
    @Column(name = "customer_number", nullable = false)
    private Long customerNumber;

    @Column(name = "customer_pass", length = 32)
    private String customerPass;

    @Column(name = "state_indicator", length = 1)
    private String stateIndicator;

    @Column(name = "pass_changes")
    private Integer passChanges;

    public CustomerSecure(Long customerNumber, String customerPass, String stateIndicator, Integer passChanges) {
        this.customerNumber = customerNumber;
        this.customerPass = customerPass;
        this.stateIndicator = stateIndicator;
        this.passChanges = passChanges;
    }
}
