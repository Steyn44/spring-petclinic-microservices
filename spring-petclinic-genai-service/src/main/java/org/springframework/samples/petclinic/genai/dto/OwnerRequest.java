package org.springframework.samples.petclinic.genai.dto;

public record OwnerRequest(
    String firstName,
    String lastName,
    String address,
    String city,
    String telephone
) { }

