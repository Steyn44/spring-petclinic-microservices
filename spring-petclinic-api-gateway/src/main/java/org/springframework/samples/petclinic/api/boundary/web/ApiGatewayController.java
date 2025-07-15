package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiGatewayController {

    @GetMapping("/api/gateway/health")
    public String health() {
        return "API Gateway is up!";
    }
}
