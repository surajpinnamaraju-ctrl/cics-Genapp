package com.genapp.dto;

import java.time.LocalDate;

public record CustomerResponse(
        Long customerNumber,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String houseName,
        String houseNumber,
        String postcode,
        String phoneHome,
        String phoneMobile,
        String emailAddress,
        int numPolicies) {
}
