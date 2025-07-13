package org.springframework.samples.petclinic.genai;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.AddPetRequest;
import org.springframework.samples.petclinic.genai.dto.AddedPetResponse;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.OwnerRequest;
import org.springframework.samples.petclinic.genai.dto.OwnerResponse;
import org.springframework.samples.petclinic.genai.dto.OwnersResponse;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;
import org.springframework.samples.petclinic.genai.dto.PetType;
import org.springframework.samples.petclinic.genai.dto.Specialty;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.samples.petclinic.genai.dto.VetRequest;
import org.springframework.samples.petclinic.genai.dto.VetResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import reactor.core.publisher.Mono;

public class AIDataProviderTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private VectorStore vectorStore;

    private AIDataProvider aiDataProvider;

    @Mock
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        aiDataProvider = new AIDataProvider(webClientBuilder, vectorStore);
    }

    @Test
    void testGetAllOwners() {
        WebClient.RequestHeadersUriSpec<?> requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        OwnerDetails owner = new OwnerDetails(
            1,
            "John",
            "Doe",
            "123 Street",
            "City",
            "1234567890",
            Collections.emptyList()
        );

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
            .thenReturn(Mono.just(List.of(owner)));

        OwnersResponse response = aiDataProvider.getAllOwners();

        assert response.owners().size() == 1;
        assert response.owners().get(0).firstName().equals("John");
    }

    @Test
    void testAddPetToOwner() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
        RequestHeadersSpec<?> requestHeadersSpec = mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        PetDetails petDetails = new PetDetails(
            1,
            "Buddy",
            "2022-01-01",
            new PetType("Dog"),
            Collections.emptyList()
        );

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PetDetails.class))
            .thenReturn(Mono.just(petDetails));

        PetRequest petRequest = new PetRequest(
            1,
            new Date(),
            "Buddy",
            2
        );
        AddPetRequest request = new AddPetRequest(petRequest, 1);

        AddedPetResponse response = aiDataProvider.addPetToOwner(request);

        assert response.pet().name().equals("Buddy");
    }

    @Test
    void testAddOwnerToPetclinic() {
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        RequestBodySpec requestBodySpec = mock(RequestBodySpec.class);
        RequestHeadersSpec<?> requestHeadersSpec = mock(RequestHeadersSpec.class);
        ResponseSpec responseSpec = mock(ResponseSpec.class);

        OwnerDetails ownerDetails = new OwnerDetails(
            1,
            "Jane",
            "Smith",
            "456 Road",
            "Town",
            "9876543210",
            Collections.emptyList()
        );

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OwnerDetails.class))
            .thenReturn(Mono.just(ownerDetails));

        OwnerRequest ownerRequest = new OwnerRequest(
            "Jane",
            "Smith",
            "456 Road",
            "Town",
            "9876543210"
        );

        OwnerResponse response = aiDataProvider.addOwnerToPetclinic(ownerRequest);

        assert response.owner().firstName().equals("Jane");
    }

    @Test
    void testGetVets() {
        Document document = new Document("Vet: Dr. Strange\nSpecialty: Surgery");

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
            .thenReturn(List.of(document));

        Vet vet = new Vet(
            1,
            "Stephen",
            "Strange",
            Set.of(new Specialty(1, "Surgery"))
        );

        VetRequest vetRequest = new VetRequest(vet);

        VetResponse response = aiDataProvider.getVets(vetRequest);

        assert response.vet().size() == 1;
        assert response.vet().get(0).contains("Dr. Strange");
    }
}
