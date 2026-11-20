package com.genapp.web;

import com.genapp.dto.PolicyRequest;
import com.genapp.dto.PolicyResponse;
import com.genapp.service.PolicyService;
import com.genapp.service.PolicyService.PolicyType;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Policy API mirroring the SSP1-4 menus. */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyService policies;

    public PolicyController(PolicyService policies) {
        this.policies = policies;
    }

    @GetMapping("/{type}/{customerNumber}/{policyNumber}")
    public PolicyResponse get(@PathVariable String type, @PathVariable Long customerNumber,
            @PathVariable Long policyNumber) {
        return policies.getPolicy(PolicyType.fromPath(type), customerNumber, policyNumber);
    }

    @PostMapping("/{type}")
    @ResponseStatus(HttpStatus.CREATED)
    public PolicyResponse add(@PathVariable String type, @Valid @RequestBody PolicyRequest req) {
        return policies.addPolicy(PolicyType.fromPath(type), req);
    }

    @PutMapping("/{type}/{customerNumber}/{policyNumber}")
    public PolicyResponse update(@PathVariable String type, @PathVariable Long customerNumber,
            @PathVariable Long policyNumber, @Valid @RequestBody PolicyRequest req) {
        return policies.updatePolicy(PolicyType.fromPath(type), customerNumber, policyNumber, req);
    }

    @DeleteMapping("/{type}/{customerNumber}/{policyNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String type, @PathVariable Long customerNumber,
            @PathVariable Long policyNumber) {
        policies.deletePolicy(PolicyType.fromPath(type), customerNumber, policyNumber);
    }

    /** Commercial search (02ICOM/03ICOM/05ICOM equivalents). */
    @GetMapping("/commercial/search")
    public List<PolicyResponse> searchCommercial(
            @RequestParam(required = false) Long policyNumber,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String postcode) {
        return policies.searchCommercial(policyNumber, customer, postcode);
    }
}
