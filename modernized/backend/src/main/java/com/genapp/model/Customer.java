package com.genapp.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Db2 {@code customer} table. Number allocated by service (IDENTITY START WITH 1000001). */
@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Customer {

    @Id
    @Column(name = "customer_number", nullable = false)
    private Long customerNumber;

    @Column(name = "first_name", length = 10)
    private String firstName;

    @Column(name = "last_name", length = 20)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "house_name", length = 20)
    private String houseName;

    @Column(name = "house_number", length = 4)
    private String houseNumber;

    @Column(name = "postcode", length = 8)
    private String postcode;

    @Column(name = "phone_home", length = 20)
    private String phoneHome;

    @Column(name = "phone_mobile", length = 20)
    private String phoneMobile;

    @Column(name = "email_address", length = 100)
    private String emailAddress;

    public Customer(Long customerNumber) {
        this.customerNumber = customerNumber;
    }
}
