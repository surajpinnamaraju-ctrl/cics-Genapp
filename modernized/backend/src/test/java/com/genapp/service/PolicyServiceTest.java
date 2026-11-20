package com.genapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.genapp.dto.PolicyRequest;
import com.genapp.dto.PolicyResponse;
import com.genapp.service.PolicyService.PolicyType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PolicyServiceTest {

    @Autowired
    PolicyService policies;

    private static PolicyRequest motorRequest(Long customerNumber, String make) {
        PolicyRequest r = new PolicyRequest();
        r.customerNumber = customerNumber;
        r.issueDate = LocalDate.of(2024, 1, 1);
        r.expiryDate = LocalDate.of(2025, 1, 1);
        r.make = make;
        r.model = "KA";
        r.value = 9000;
        r.regNumber = "AB12CDE";
        r.colour = "RED";
        r.cc = (short) 1600;
        r.yearOfManufacture = LocalDate.of(2019, 5, 1);
        r.premium = 500;
        r.accidents = 0;
        return r;
    }

    private static PolicyRequest updateRequest(PolicyResponse current, String make) {
        PolicyRequest r = new PolicyRequest();
        r.customerNumber = current.customerNumber;
        r.lastChanged = current.lastChanged;
        r.make = make;
        return r;
    }

    @Test
    void getSeededMotorPolicy() {
        PolicyResponse p = policies.getPolicy(PolicyType.MOTOR, 2L, 1L);
        assertEquals("M", p.policyType);
        assertEquals("FORD", p.make);
        assertEquals("KA", p.model);
        assertEquals("LL60LOO", p.regNumber);
        assertNotNull(p.lastChanged);
    }

    @Test
    void getPolicyWithWrongTypeReturnsRc01() {
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.getPolicy(PolicyType.ENDOWMENT, 2L, 1L));
        assertEquals("01", ex.getReturnCode());
    }

    @Test
    void getUnknownPolicyReturnsRc01() {
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.getPolicy(PolicyType.MOTOR, 2L, 999L));
        assertEquals("01", ex.getReturnCode());
    }

    @Test
    void addPolicyForMissingCustomerReturnsRc70() {
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.addPolicy(PolicyType.MOTOR, motorRequest(999L, "FORD")));
        assertEquals("70", ex.getReturnCode());
    }

    @Test
    void addMotorPolicyAllocatesIdentityValue() {
        PolicyResponse created = policies.addPolicy(PolicyType.MOTOR, motorRequest(2L, "FORD"));
        assertTrue(created.policyNumber >= 1000001L);
        assertEquals("AB12CDE", created.regNumber);
        assertEquals("RED", created.colour);
    }

    @Test
    void updateWithStaleLastChangedReturnsRc02ThenSucceedsWithCurrent() {
        PolicyResponse current = policies.getPolicy(PolicyType.MOTOR, 2L, 1L);

        PolicyRequest stale = updateRequest(current, "VOLVO");
        stale.lastChanged = LocalDateTime.of(2000, 1, 1, 0, 0);
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.updatePolicy(PolicyType.MOTOR, 2L, 1L, stale));
        assertEquals("02", ex.getReturnCode());

        PolicyResponse updated = policies.updatePolicy(PolicyType.MOTOR, 2L, 1L,
                updateRequest(current, "VOLVO"));
        assertEquals("VOLVO", updated.make);
        assertTrue(updated.lastChanged.isAfter(current.lastChanged));
    }

    @Test
    void updateWithoutLastChangedReturnsRc98() {
        PolicyResponse current = policies.getPolicy(PolicyType.MOTOR, 2L, 1L);
        PolicyRequest r = updateRequest(current, "VOLVO");
        r.lastChanged = null;
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.updatePolicy(PolicyType.MOTOR, 2L, 1L, r));
        assertEquals("98", ex.getReturnCode());
    }

    @Test
    void deletePolicyRemovesHeaderAndTypeRow() {
        policies.deletePolicy(PolicyType.HOUSE, 4L, 6L);
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.getPolicy(PolicyType.HOUSE, 4L, 6L));
        assertEquals("01", ex.getReturnCode());
    }

    @Test
    void commercialSearchByPostcode() {
        List<PolicyResponse> results = policies.searchCommercial(null, null, "BB104BX");
        assertEquals(1, results.size());
        assertEquals("Burnley Football Club", results.get(0).customer);
    }

    @Test
    void commercialUpdateIsUnsupported() {
        PolicyResponse c = policies.getPolicy(PolicyType.COMMERCIAL, 5L, 9L);
        PolicyRequest r = new PolicyRequest();
        r.lastChanged = c.lastChanged;
        GenAppException ex = assertThrows(GenAppException.class,
                () -> policies.updatePolicy(PolicyType.COMMERCIAL, 5L, 9L, r));
        assertEquals("99", ex.getReturnCode());
    }
}
