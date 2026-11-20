package com.genapp.repo;

import com.genapp.model.HousePolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HouseRepository extends JpaRepository<HousePolicy, Long> {
}
