package org.springframework.samples.petclinic.genai.dto;

import java.util.List;

public record OwnersResponse(List<OwnerDetails> owners) {
}
