package org.springframework.samples.petclinic.genai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.*;
import org.springframework.samples.petclinic.genai.openai.VectorStore;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AIDataProviderTest {

    private WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private WebClient.ResponseSpec responseSpec;
    private VectorStore vectorStore;

    @BeforeEach
    void setUp() {
        // Deep stubbing solves all generics issues
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        webClientBuilder = mock(WebClient.Builder.class);
        responseSpec = mock(WebClient.ResponseSpec.class);
        vectorStore = mock(VectorStore.class);

        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    void testGetAllOwners() {
        OwnerDetails owner = new OwnerDetails(
                1,
                "John",
                "Doe",
                "Street",
                "City",
                "1234567890",
                Collections.emptyList()
        );

        // Arrange the chain
        when(webClient.get()
                      .uri(anyString())
                      .retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of(owner)));

        AIDataProvider provider = new AIDataProvider(webClientBuilder, vectorStore);

        OwnersResponse response = provider.getAllOwners();

        assert response.owners().size() == 1;
    }

    @Test
    void testGetAllPets() {
        PetDetails pet = new PetDetails(
                1,
                "Buddy",
                "dog",
                "2021-01-01",
                "ownerName"
        );

        when(webClient.get()
                      .uri(anyString())
                      .retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of(pet)));

        AIDataProvider provider = new AIDataProvider(webClientBuilder, vectorStore);

        PetsResponse response = provider.getAllPets();

        assert response.pets().size() == 1;
    }

    @Test
    void testGetAllVets() {
        VetDetails vet = new VetDetails(
                1,
                "Dr",
                "Smith",
                List.of("surgery")
        );

        when(webClient.get()
                      .uri(anyString())
                      .retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(List.of(vet)));

        AIDataProvider provider = new AIDataProvider(webClientBuilder, vectorStore);

        VetsResponse response = provider.getAllVets();

        assert response.vets().size() == 1;
    }

    @Test
    void testAddPet() {
        PetRequest petRequest = new PetRequest("Buddy", "dog", "2022-05-01");

        // The second parameter in AddPetRequest is an int
        AddPetRequest addPetRequest = new AddPetRequest(petRequest, 2);

        when(webClient.post()
                      .uri(anyString())
                      .body(any(), eq(AddPetRequest.class))
                      .retrieve())
                .thenReturn(responseSpec);

        when(responseSpec.bodyToMono(Void.class))
                .thenReturn(Mono.empty());

        AIDataProvider provider = new AIDataProvider(webClientBuilder, vectorStore);

        provider.addPet(addPetRequest);

        verify(webClient.post(), times(1)).uri(anyString());
    }
}
