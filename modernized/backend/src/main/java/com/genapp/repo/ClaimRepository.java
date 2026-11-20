package com.genapp.repo;

import com.genapp.model.Claim;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findByPolicyNumber(Long policyNumber);

    void deleteByPolicyNumber(Long policyNumber);
}
