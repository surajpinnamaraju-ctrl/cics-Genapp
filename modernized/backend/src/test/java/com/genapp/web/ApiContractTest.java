package com.genapp.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiContractTest {

    @Autowired
    MockMvc mvc;

    @Test
    void healthEndpoint() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void missingCustomerMapsTo404WithRc01() throws Exception {
        mvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.returnCode").value("01"));
    }

    @Test
    void customerRoundTrip() throws Exception {
        String body = "{\"firstName\":\"Web\",\"lastName\":\"Test\",\"dateOfBirth\":\"1990-05-05\","
                + "\"houseName\":\"\",\"houseNumber\":\"2\",\"postcode\":\"xy12zw\","
                + "\"phoneHome\":\"\",\"phoneMobile\":\"\",\"emailAddress\":\"web@test.com\"}";
        String created = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/customers").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerNumber").value(1000001))
                .andExpect(jsonPath("$.postcode").value("XY12ZW"))
                .andReturn().getResponse().getContentAsString();
        mvc.perform(get("/api/customers/1000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Web"));
    }

    @Test
    void stalePolicyUpdateMapsTo409WithRc02() throws Exception {
        // fetch current policy to learn nothing; send clearly stale timestamp
        String stale = "{\"lastChanged\":\"2000-01-01T00:00:00\",\"make\":\"X\"}";
        mvc.perform(put("/api/policies/motor/2/1").contentType(MediaType.APPLICATION_JSON).content(stale))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.returnCode").value("02"));
    }

    @Test
    void unknownPolicyTypeMapsTo400WithRc99() throws Exception {
        mvc.perform(get("/api/policies/banana/2/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.returnCode").value("99"));
    }
}
