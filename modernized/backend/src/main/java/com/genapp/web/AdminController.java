package com.genapp.web;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health probe and modernization setup endpoint.
 * Cloud Run Google edge intercepts /healthz, so the frontend serves /health;
 * this backend exposes /api/health.
 */
@RestController
@RequestMapping("/api")
public class AdminController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", "genapp-backend");
        return body;
    }

    /** Idempotent setup hook used by the CD pipeline after deploy. */
    @PostMapping("/admin/setup")
    public Map<String, Object> setup() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "OK");
        body.put("seeded", true);
        return body;
    }
}
