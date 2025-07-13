package org.springframework.samples.petclinic.genai.dto;

import java.util.List;

public record VetResponse(List<String> vets) {
    public List<String> vets() {
        return vets;
    }
}
