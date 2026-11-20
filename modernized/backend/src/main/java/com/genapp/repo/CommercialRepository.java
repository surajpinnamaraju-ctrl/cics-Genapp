package com.genapp.repo;

import com.genapp.model.CommercialPolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommercialRepository extends JpaRepository<CommercialPolicy, Long> {
    List<CommercialPolicy> findByCustomer(String customer);
    List<CommercialPolicy> findByZipcode(String zipcode);
}
