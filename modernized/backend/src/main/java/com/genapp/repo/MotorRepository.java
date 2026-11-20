package com.genapp.repo;

import com.genapp.model.MotorPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotorRepository extends JpaRepository<MotorPolicy, Long> {
}
