package com.genapp.web;

import com.genapp.dto.CustomerRequest;
import com.genapp.dto.CustomerResponse;
import com.genapp.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Customer API mirroring the SSC1 menu (inquire/add/update; delete is not in base GenApp). */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customers;

    public CustomerController(CustomerService customers) {
        this.customers = customers;
    }

    @GetMapping("/{customerNumber}")
    public CustomerResponse get(@PathVariable Long customerNumber) {
        return customers.getCustomer(customerNumber);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse add(@Valid @RequestBody CustomerRequest req) {
        return customers.addCustomer(req);
    }

    @PutMapping("/{customerNumber}")
    public CustomerResponse update(@PathVariable Long customerNumber, @Valid @RequestBody CustomerRequest req) {
        return customers.updateCustomer(customerNumber, req);
    }
}
