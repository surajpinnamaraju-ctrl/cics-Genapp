package com.genapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genapp.dto.CustomerRequest;
import com.genapp.dto.CustomerResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    CustomerService customers;

    private CustomerRequest validRequest() {
        return new CustomerRequest("Test", "User", LocalDate.of(1990, 1, 1),
                "House", "1", "ab12cd", "123", "456", "test@example.com");
    }

    @Test
    void seedDataIsLoaded() {
        CustomerResponse c = customers.getCustomer(1L);
        assertEquals("Andrew", c.firstName());
        assertEquals("Pandy", c.lastName());
        assertEquals("PI101OO", c.postcode());
    }

    @Test
    void unknownCustomerReturnsRc01() {
        GenAppException ex = assertThrows(GenAppException.class, () -> customers.getCustomer(999L));
        assertEquals("01", ex.getReturnCode());
    }

    @Test
    void addCustomerAllocatesIdentityValueAndSecureRow() {
        CustomerResponse created = customers.addCustomer(validRequest());
        assertEquals(1000001L, created.customerNumber());
        assertEquals("AB12CD", created.postcode());
        // second add continues the identity sequence
        CustomerResponse second = customers.addCustomer(validRequest());
        assertEquals(1000002L, second.customerNumber());
    }

    @Test
    void addCustomerValidatesFieldLengths() {
        CustomerRequest bad = new CustomerRequest("A".repeat(11), "User", null,
                null, null, null, null, null, null);
        GenAppException ex = assertThrows(GenAppException.class, () -> customers.addCustomer(bad));
        assertEquals("98", ex.getReturnCode());
    }

    @Test
    void updateCustomerReplacesFieldsAndUppercasesPostcode() {
        CustomerResponse updated = customers.updateCustomer(1L,
                new CustomerRequest("Andy", "Pandy", LocalDate.of(1950, 7, 11),
                        " ", "34", "pi101oo", "01962 811234", "07799 123456", "a.pandy@beebhouse.com"));
        assertEquals("Andy", updated.firstName());
        assertEquals("PI101OO", updated.postcode());
        assertTrue(customers.getCustomer(1L).emailAddress().equals("a.pandy@beebhouse.com"));
    }
}
