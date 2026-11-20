package com.genapp.repo;

import com.genapp.model.Policy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PolicyRepository extends JpaRepository<Policy, Long> {

    Optional<Policy> findByPolicyNumberAndCustomerNumber(Long policyNumber, Long customerNumber);

    List<Policy> findByCustomerNumber(Long customerNumber);

    List<Policy> findByPolicyNumber(Long policyNumber);

    @Query("select coalesce(max(p.policyNumber), 0) from Policy p")
    long maxPolicyNumber();
}
