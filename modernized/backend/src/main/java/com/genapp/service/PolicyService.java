package com.genapp.service;

import com.genapp.dto.PolicyRequest;
import com.genapp.dto.PolicyResponse;
import com.genapp.model.Claim;
import com.genapp.model.CommercialPolicy;
import com.genapp.model.Customer;
import com.genapp.model.EndowmentPolicy;
import com.genapp.model.HousePolicy;
import com.genapp.model.MotorPolicy;
import com.genapp.model.Policy;
import com.genapp.repo.ClaimRepository;
import com.genapp.repo.CommercialRepository;
import com.genapp.repo.CustomerRepository;
import com.genapp.repo.EndowmentRepository;
import com.genapp.repo.HouseRepository;
import com.genapp.repo.MotorRepository;
import com.genapp.repo.PolicyRepository;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business logic for the SSP1-4 policy menus (01I/AC/UC/DC{TYPE}POL). */
@Service
@Transactional(readOnly = true)
public class PolicyService {

    public enum PolicyType {
        MOTOR("motor", "M"), ENDOWMENT("endowment", "E"), HOUSE("house", "H"), COMMERCIAL("commercial", "C");

        final String path;
        final String code;

        PolicyType(String path, String code) {
            this.path = path;
            this.code = code;
        }

        public static PolicyType fromPath(String path) {
            for (PolicyType t : values()) {
                if (t.path.equalsIgnoreCase(path)) {
                    return t;
                }
            }
            throw GenAppException.unsupported("Unknown policy type " + path);
        }
    }

    private final PolicyRepository policies;
    private final CustomerRepository customers;
    private final MotorRepository motors;
    private final EndowmentRepository endowments;
    private final HouseRepository houses;
    private final CommercialRepository commercials;
    private final ClaimRepository claims;
    private final long firstIdentityValue;

    public PolicyService(PolicyRepository policies, CustomerRepository customers,
            MotorRepository motors, EndowmentRepository endowments, HouseRepository houses,
            CommercialRepository commercials, ClaimRepository claims,
            @Value("${genapp.first-identity-value}") long firstIdentityValue) {
        this.policies = policies;
        this.customers = customers;
        this.motors = motors;
        this.endowments = endowments;
        this.houses = houses;
        this.commercials = commercials;
        this.claims = claims;
        this.firstIdentityValue = firstIdentityValue;
    }

    public PolicyResponse getPolicy(PolicyType type, Long customerNumber, Long policyNumber) {
        Policy p = findPolicy(type, customerNumber, policyNumber);
        return toResponse(type, p);
    }

    /** Commercial search mirrors 02ICOM (policy number), 03ICOM (customer) and 05ICOM (postcode). */
    public List<PolicyResponse> searchCommercial(Long policyNumber, String customerName, String postcode) {
        List<CommercialPolicy> rows = new ArrayList<>();
        if (policyNumber != null) {
            commercials.findById(policyNumber).ifPresent(rows::add);
        }
        if (customerName != null && !customerName.isBlank()) {
            rows.addAll(commercials.findByCustomer(customerName.trim().toUpperCase(Locale.ROOT)));
        }
        if (postcode != null && !postcode.isBlank()) {
            rows.addAll(commercials.findByZipcode(postcode.trim().toUpperCase(Locale.ROOT)));
        }
        if (rows.isEmpty()) {
            throw GenAppException.notFound("No matching commercial policies");
        }
        List<PolicyResponse> out = new ArrayList<>();
        for (CommercialPolicy c : rows) {
            policies.findByPolicyNumber(c.getPolicyNumber()).stream().findFirst()
                    .map(p -> toCommercialResponse(p, c))
                    .ifPresent(out::add);
        }
        if (out.isEmpty()) {
            throw GenAppException.notFound("No matching commercial policies");
        }
        return out;
    }

    @Transactional
    public PolicyResponse addPolicy(PolicyType type, PolicyRequest req) {
        Long customerNumber = req.customerNumber;
        if (customerNumber == null) {
            throw GenAppException.invalid("customerNumber is required");
        }
        Customer customer = customers.findByCustomerNumber(customerNumber)
                .orElseThrow(GenAppException::customerMissing);
        long next = Math.max(firstIdentityValue, policies.maxPolicyNumber() + 1);
        LocalDateTime now = nowTruncated();
        Policy p = new Policy(next);
        p.setCustomerNumber(customer.getCustomerNumber());
        applyCommon(p, req, now);
        p.setPolicyType(type.code);
        policies.save(p);
        saveTypeRow(type, next, req, now);
        return getPolicy(type, customerNumber, next);
    }

    @Transactional
    public PolicyResponse updatePolicy(PolicyType type, Long customerNumber, Long policyNumber, PolicyRequest req) {
        if (type == PolicyType.COMMERCIAL) {
            throw GenAppException.unsupported("Update of commercial policies is not supported");
        }
        Policy p = findPolicy(type, customerNumber, policyNumber);
        if (req.lastChanged == null) {
            throw GenAppException.invalid("lastChanged is required for optimistic concurrency check");
        }
        if (!p.getLastChanged().equals(req.lastChanged)) {
            throw GenAppException.conflict("Policy has been modified by another user");
        }
        LocalDateTime now = nowTruncated();
        applyCommon(p, req, now);
        policies.save(p);
        updateTypeRow(type, policyNumber, req);
        return getPolicy(type, customerNumber, policyNumber);
    }

    @Transactional
    public void deletePolicy(PolicyType type, Long customerNumber, Long policyNumber) {
        Policy p = findPolicy(type, customerNumber, policyNumber);
        for (Claim c : claims.findByPolicyNumber(policyNumber)) {
            claims.delete(c);
        }
        deleteTypeRow(type, policyNumber);
        policies.delete(p);
    }

    // ---- internals ----

    private Policy findPolicy(PolicyType type, Long customerNumber, Long policyNumber) {
        Policy p = policies.findByPolicyNumberAndCustomerNumber(policyNumber, customerNumber)
                .orElseThrow(() -> GenAppException.notFound(
                        "Policy " + policyNumber + " for customer " + customerNumber));
        if (!type.code.equals(p.getPolicyType())) {
            throw GenAppException.notFound(
                    "Policy " + policyNumber + " is not a " + type.path + " policy");
        }
        return p;
    }

    private void applyCommon(Policy p, PolicyRequest req, LocalDateTime now) {
        if (req.issueDate != null) p.setIssueDate(req.issueDate);
        if (req.expiryDate != null) p.setExpiryDate(req.expiryDate);
        if (req.brokerId != null) p.setBrokerId(req.brokerId);
        if (req.brokersReference != null) p.setBrokersReference(trim(req.brokersReference));
        if (req.payment != null) p.setPayment(req.payment);
        p.setLastChanged(now);
    }

    private void saveTypeRow(PolicyType type, long policyNumber, PolicyRequest req, LocalDateTime now) {
        switch (type) {
            case MOTOR -> {
                MotorPolicy m = new MotorPolicy(policyNumber);
                m.setMake(trim(req.make)); m.setModel(trim(req.model));
                m.setValue(req.value); m.setRegNumber(upper(req.regNumber));
                m.setColour(trim(req.colour)); m.setCc(req.cc);
                m.setYearOfManufacture(req.yearOfManufacture); m.setPremium(req.premium);
                m.setAccidents(req.accidents);
                motors.save(m);
            }
            case ENDOWMENT -> {
                EndowmentPolicy e = new EndowmentPolicy(policyNumber);
                e.setWithProfits(req.withProfits); e.setEquities(req.equities);
                e.setManagedFund(req.managedFund); e.setFundName(trim(req.fundName));
                e.setTerm(req.term); e.setSumAssured(req.sumAssured);
                e.setLifeAssured(trim(req.lifeAssured)); e.setPaddingData(req.paddingData);
                endowments.save(e);
            }
            case HOUSE -> {
                HousePolicy h = new HousePolicy(policyNumber);
                h.setPropertyType(trim(req.propertyType)); h.setBedrooms(req.bedrooms);
                h.setValue(req.value); h.setHouseName(trim(req.houseName));
                h.setHouseNumber(trim(req.houseNumber)); h.setPostcode(upper(req.postcode));
                houses.save(h);
            }
            case COMMERCIAL -> {
                CommercialPolicy c = new CommercialPolicy(policyNumber);
                // LGAPDB01 mappings: RequestDate=lastChanged, StartDate=issueDate, RenewalDate=expiryDate
                c.setRequestDate(now);
                c.setStartDate(req.issueDate);
                c.setRenewalDate(req.expiryDate);
                c.setAddress(trim(req.address)); c.setZipcode(upper(req.zipcode));
                c.setLatitudeN(req.latitude); c.setLongitudeW(req.longitude);
                c.setCustomer(upper(req.customer)); c.setPropertyType(trim(req.propertyType));
                c.setFirePeril(req.firePeril); c.setFirePremium(req.firePremium);
                c.setCrimePeril(req.crimePeril); c.setCrimePremium(req.crimePremium);
                c.setFloodPeril(req.floodPeril); c.setFloodPremium(req.floodPremium);
                c.setWeatherPeril(req.weatherPeril); c.setWeatherPremium(req.weatherPremium);
                c.setStatus(req.status); c.setRejectionReason(trim(req.rejectionReason));
                commercials.save(c);
            }
        }
    }

    private void updateTypeRow(PolicyType type, long policyNumber, PolicyRequest req) {
        switch (type) {
            case MOTOR -> motors.findById(policyNumber).ifPresent(m -> {
                if (req.make != null) m.setMake(trim(req.make));
                if (req.model != null) m.setModel(trim(req.model));
                if (req.value != null) m.setValue(req.value);
                if (req.regNumber != null) m.setRegNumber(upper(req.regNumber));
                if (req.colour != null) m.setColour(trim(req.colour));
                if (req.cc != null) m.setCc(req.cc);
                if (req.yearOfManufacture != null) m.setYearOfManufacture(req.yearOfManufacture);
                if (req.premium != null) m.setPremium(req.premium);
                if (req.accidents != null) m.setAccidents(req.accidents);
                motors.save(m);
            });
            case ENDOWMENT -> endowments.findById(policyNumber).ifPresent(e -> {
                if (req.withProfits != null) e.setWithProfits(req.withProfits);
                if (req.equities != null) e.setEquities(req.equities);
                if (req.managedFund != null) e.setManagedFund(req.managedFund);
                if (req.fundName != null) e.setFundName(trim(req.fundName));
                if (req.term != null) e.setTerm(req.term);
                if (req.sumAssured != null) e.setSumAssured(req.sumAssured);
                if (req.lifeAssured != null) e.setLifeAssured(trim(req.lifeAssured));
                if (req.paddingData != null) e.setPaddingData(req.paddingData);
                endowments.save(e);
            });
            case HOUSE -> houses.findById(policyNumber).ifPresent(h -> {
                if (req.propertyType != null) h.setPropertyType(trim(req.propertyType));
                if (req.bedrooms != null) h.setBedrooms(req.bedrooms);
                if (req.value != null) h.setValue(req.value);
                if (req.houseName != null) h.setHouseName(trim(req.houseName));
                if (req.houseNumber != null) h.setHouseNumber(trim(req.houseNumber));
                if (req.postcode != null) h.setPostcode(upper(req.postcode));
                houses.save(h);
            });
            default -> { }
        }
    }

    private void deleteTypeRow(PolicyType type, long policyNumber) {
        switch (type) {
            case MOTOR -> motors.deleteById(policyNumber);
            case ENDOWMENT -> endowments.deleteById(policyNumber);
            case HOUSE -> houses.deleteById(policyNumber);
            case COMMERCIAL -> commercials.deleteById(policyNumber);
        }
    }

    private PolicyResponse toResponse(PolicyType type, Policy p) {
        PolicyResponse r = new PolicyResponse();
        r.policyNumber = p.getPolicyNumber();
        r.customerNumber = p.getCustomerNumber();
        r.policyType = p.getPolicyType();
        r.issueDate = p.getIssueDate();
        r.expiryDate = p.getExpiryDate();
        r.lastChanged = p.getLastChanged();
        r.brokerId = p.getBrokerId();
        r.brokersReference = p.getBrokersReference();
        r.payment = p.getPayment();
        switch (type) {
            case MOTOR -> motors.findById(p.getPolicyNumber()).ifPresent(m -> {
                r.make = m.getMake(); r.model = m.getModel(); r.value = m.getValue();
                r.regNumber = m.getRegNumber(); r.colour = m.getColour(); r.cc = m.getCc();
                r.yearOfManufacture = m.getYearOfManufacture(); r.premium = m.getPremium();
                r.accidents = m.getAccidents();
            });
            case ENDOWMENT -> endowments.findById(p.getPolicyNumber()).ifPresent(e -> {
                r.withProfits = e.getWithProfits(); r.equities = e.getEquities();
                r.managedFund = e.getManagedFund(); r.fundName = e.getFundName();
                r.term = e.getTerm(); r.sumAssured = e.getSumAssured();
                r.lifeAssured = e.getLifeAssured(); r.paddingData = e.getPaddingData();
            });
            case HOUSE -> houses.findById(p.getPolicyNumber()).ifPresent(h -> {
                r.propertyType = h.getPropertyType(); r.bedrooms = h.getBedrooms();
                r.value = h.getValue(); r.houseName = h.getHouseName();
                r.houseNumber = h.getHouseNumber(); r.housePostcode = h.getPostcode();
            });
            case COMMERCIAL -> commercials.findById(p.getPolicyNumber()).ifPresent(c -> {
                r.requestDate = c.getRequestDate(); r.startDate = c.getStartDate();
                r.renewalDate = c.getRenewalDate(); r.address = c.getAddress();
                r.zipcode = c.getZipcode(); r.latitude = c.getLatitudeN();
                r.longitude = c.getLongitudeW(); r.customer = c.getCustomer();
                r.propertyType = c.getPropertyType();
                r.firePeril = c.getFirePeril(); r.firePremium = c.getFirePremium();
                r.crimePeril = c.getCrimePeril(); r.crimePremium = c.getCrimePremium();
                r.floodPeril = c.getFloodPeril(); r.floodPremium = c.getFloodPremium();
                r.weatherPeril = c.getWeatherPeril(); r.weatherPremium = c.getWeatherPremium();
                r.status = c.getStatus(); r.rejectionReason = c.getRejectionReason();
            });
        }
        return r;
    }

    private PolicyResponse toCommercialResponse(Policy p, CommercialPolicy c) {
        PolicyResponse r = new PolicyResponse();
        r.policyNumber = p.getPolicyNumber();
        r.customerNumber = p.getCustomerNumber();
        r.policyType = p.getPolicyType();
        r.issueDate = p.getIssueDate();
        r.expiryDate = p.getExpiryDate();
        r.lastChanged = p.getLastChanged();
        r.brokerId = p.getBrokerId();
        r.brokersReference = p.getBrokersReference();
        r.payment = p.getPayment();
        r.requestDate = c.getRequestDate(); r.startDate = c.getStartDate();
        r.renewalDate = c.getRenewalDate(); r.address = c.getAddress();
        r.zipcode = c.getZipcode(); r.latitude = c.getLatitudeN();
        r.longitude = c.getLongitudeW(); r.customer = c.getCustomer();
        r.propertyType = c.getPropertyType();
        r.firePeril = c.getFirePeril(); r.firePremium = c.getFirePremium();
        r.crimePeril = c.getCrimePeril(); r.crimePremium = c.getCrimePremium();
        r.floodPeril = c.getFloodPeril(); r.floodPremium = c.getFloodPremium();
        r.weatherPeril = c.getWeatherPeril(); r.weatherPremium = c.getWeatherPremium();
        r.status = c.getStatus(); r.rejectionReason = c.getRejectionReason();
        return r;
    }

    private static LocalDateTime nowTruncated() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String upper(String s) {
        return s == null ? null : s.trim().toUpperCase(Locale.ROOT);
    }
}
