package com.genapp.service;

import com.genapp.dto.CustomerRequest;
import com.genapp.dto.CustomerResponse;
import com.genapp.model.Customer;
import com.genapp.model.CustomerSecure;
import com.genapp.repo.CustomerRepository;
import com.genapp.repo.CustomerSecureRepository;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for the SSC1 customer menu (01ICUS / 01ACUS / 01UCUS). */
@Service
@Transactional(readOnly = true)
public class CustomerService {

    /** MD5 hash seeded by LGACDB01/LGACDB02 for every new customer_secure row. */
    static final String DEFAULT_CUSTOMER_PASS = "5732fec825535eeafb8fac50fee3a8aa";

    private final CustomerRepository customers;
    private final CustomerSecureRepository customerSecure;
    private final long firstIdentityValue;

    public CustomerService(CustomerRepository customers, CustomerSecureRepository customerSecure,
            @Value("${genapp.first-identity-value}") long firstIdentityValue) {
        this.customers = customers;
        this.customerSecure = customerSecure;
        this.firstIdentityValue = firstIdentityValue;
    }

    public CustomerResponse getCustomer(Long customerNumber) {
        Customer c = customers.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> GenAppException.notFound("Customer " + customerNumber));
        return toResponse(c);
    }

    @Transactional
    public CustomerResponse addCustomer(CustomerRequest req) {
        validate(req);
        long next = Math.max(firstIdentityValue, customers.maxCustomerNumber() + 1);
        Customer c = new Customer(next);
        apply(c, req);
        c.setPostcode(upper(c.getPostcode()));
        customers.save(c);
        // LGACDB01 -> LGACDB02: every new customer gets a customer_secure row.
        customerSecure.save(new CustomerSecure(next, DEFAULT_CUSTOMER_PASS, "N", 0));
        return toResponse(c);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long customerNumber, CustomerRequest req) {
        validate(req);
        Customer c = customers.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> GenAppException.notFound("Customer " + customerNumber));
        apply(c, req);
        c.setPostcode(upper(c.getPostcode()));
        customers.save(c);
        return toResponse(c);
    }

    private void apply(Customer c, CustomerRequest req) {
        c.setFirstName(trim(req.firstName()));
        c.setLastName(trim(req.lastName()));
        c.setDateOfBirth(req.dateOfBirth());
        c.setHouseName(trim(req.houseName()));
        c.setHouseNumber(trim(req.houseNumber()));
        c.setPostcode(trim(req.postcode()));
        c.setPhoneHome(trim(req.phoneHome()));
        c.setPhoneMobile(trim(req.phoneMobile()));
        c.setEmailAddress(trim(req.emailAddress()));
    }

    private void validate(CustomerRequest req) {
        check("firstName", req.firstName(), 10);
        check("lastName", req.lastName(), 20);
        check("houseName", req.houseName(), 20);
        check("houseNumber", req.houseNumber(), 4);
        check("postcode", req.postcode(), 8);
        check("phoneHome", req.phoneHome(), 20);
        check("phoneMobile", req.phoneMobile(), 20);
        check("emailAddress", req.emailAddress(), 100);
        if (req.dateOfBirth() != null && req.dateOfBirth().isAfter(LocalDate.now().plusYears(1))) {
            throw GenAppException.invalid("dateOfBirth is not a valid date");
        }
    }

    private static void check(String field, String value, int max) {
        if (value != null && value.trim().length() > max) {
            throw GenAppException.invalid(field + " exceeds maximum length " + max);
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(c.getCustomerNumber(), c.getFirstName(), c.getLastName(),
                c.getDateOfBirth(), c.getHouseName(), c.getHouseNumber(), c.getPostcode(),
                c.getPhoneHome(), c.getPhoneMobile(), c.getEmailAddress(), 0);
    }
}
