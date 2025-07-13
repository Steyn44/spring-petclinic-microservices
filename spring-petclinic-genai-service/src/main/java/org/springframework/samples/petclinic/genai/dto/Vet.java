package org.springframework.samples.petclinic.genai.dto;

import java.util.Set;

/**
 * Simple Data Transfer Object representing a vet.
 */
public record Vet(
    Integer id,
    String firstName,
    String lastName,
    Set<Specialty> specialties) {

    public String name() {
        return firstName + " " + lastName;
    }
}
