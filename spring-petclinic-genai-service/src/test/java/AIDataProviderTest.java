package org.springframework.samples.petclinic.genai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.samples.petclinic.genai.dto.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AIDataProviderTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private VectorStore vectorStore;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private AIDataProvider aiDataProvider;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        aiDataProvider = new AIDataProvider(webClientBuilder, vectorStore);

        when(webClientBuilder.build()).thenReturn(webClient);
    }

    @Test
    public void testGetAllOwners() {
        List<OwnerDetails> ownerList = List.of(
                new OwnerDetails(
                        1,
                        "John",
                        "Doe",
                        "123 Main St",
                        "Springfield",
                        "1234567890",
                        List.of()
                )
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any())).thenReturn(Mono.just(ownerList));

        OwnersResponse response = aiDataProvider.getAllOwners();
        assertNotNull(response);
        assertEquals(1, response.owners().size());
    }

    @Test
    public void testAddPetToOwner() {
        PetDetails petDetails = new PetDetails(
                1,
                "Buddy",
                "2024-01-01",
                new PetType("dog"),
                List.of()
        );

        PetRequest petRequest = new PetRequest(
                1,
                new Date(),
                "Buddy",
                1
        );

        AddPetRequest addPetRequest = new AddPetRequest(petRequest, 1);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PetDetails.class)).thenReturn(Mono.just(petDetails));

        AddedPetResponse response = aiDataProvider.addPetToOwner(addPetRequest);
        assertNotNull(response);
        assertEquals("Buddy", response.pet().name());
    }

    @Test
    public void testAddOwnerToPetclinic() {
        OwnerDetails ownerDetails = new OwnerDetails(
                2,
                "Alice",
                "Smith",
                "456 Park Ave",
                "Metropolis",
                "9876543210",
                List.of()
        );

        OwnerRequest ownerRequest = new OwnerRequest(
                "Alice",
                "Smith",
                "456 Park Ave",
                "Metropolis",
                "9876543210"
        );

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(String.class))).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.bodyValue(any())).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OwnerDetails.class)).thenReturn(Mono.just(ownerDetails));

        OwnerResponse response = aiDataProvider.addOwnerToPetclinic(ownerRequest);
        assertNotNull(response);
        assertEquals("Alice", response.owner().firstName());
    }

    @Test
    public void testGetVets() {
        List<Document> documents = List.of(
                new Document("Vet #1 info"),
                new Document("Vet #2 info")
        );

        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(documents);

        Set<Specialty> specialties = Set.of(new Specialty("Dentistry"));

        Vet vet = new Vet(1, "John", "Doe", specialties);
        VetRequest vetRequest = new VetRequest(vet);

        VetResponse response = aiDataProvider.getVets(vetRequest);

        assertNotNull(response);
        assertEquals(2, response.vets().size());
        assertEquals("Vet #1 info", response.vets().get(0));
    }
}
