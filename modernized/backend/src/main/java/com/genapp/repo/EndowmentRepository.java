package com.genapp.repo;

import com.genapp.model.EndowmentPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndowmentRepository extends JpaRepository<EndowmentPolicy, Long> {
}
