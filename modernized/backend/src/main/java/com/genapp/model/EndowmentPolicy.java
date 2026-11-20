package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code endowment} table. */
@Entity
@Table(name = "endowment")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class EndowmentPolicy {

    @Id
    @Column(name = "policy_number", nullable = false)
    private Long policyNumber;

    @Column(name = "equities", length = 1)
    private String equities;

    @Column(name = "with_profits", length = 1)
    private String withProfits;

    @Column(name = "managed_fund", length = 1)
    private String managedFund;

    @Column(name = "fund_name", length = 10)
    private String fundName;

    @Column(name = "term")
    private Short term;

    @Column(name = "sum_assured")
    private Integer sumAssured;

    @Column(name = "life_assured", length = 31)
    private String lifeAssured;

    @Column(name = "padding_data", length = 32606)
    private String paddingData;

    public EndowmentPolicy(Long policyNumber) {
        this.policyNumber = policyNumber;
    }
}
