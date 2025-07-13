package org.springframework.samples.petclinic.genai.dto;

public record AddPetRequest(int ownerId, PetDetails pet) {
}
