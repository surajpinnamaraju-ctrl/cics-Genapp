package com.genapp.repo;

import com.genapp.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("select coalesce(max(c.customerNumber), 0) from Customer c")
    long maxCustomerNumber();

    Optional<Customer> findByCustomerNumber(Long customerNumber);
}
