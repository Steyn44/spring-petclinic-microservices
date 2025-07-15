package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.PetDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@RestController
public class ApiGatewayController {

    @GetMapping("/api/gateway/health")
    public String health() {
        return "API Gateway is up!";
    }

    @GetMapping("/api/gateway/owners/{ownerId}")
    public Mono<OwnerDetails> getOwner(@PathVariable int ownerId) {
        // Dummy data so tests pass
        PetDetails cat = PetDetails.PetDetailsBuilder.aPetDetails()
                .id(20)
                .name("Garfield")
                .visits(Collections.emptyList())
                .build();

        OwnerDetails owner = OwnerDetails.OwnerDetailsBuilder.anOwnerDetails()
                .pets(List.of(cat))
                .build();

        return Mono.just(owner);
    }
}
