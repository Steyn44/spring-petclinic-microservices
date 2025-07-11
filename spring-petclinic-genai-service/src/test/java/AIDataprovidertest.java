package org.springframework.samples.petclinic.genai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.samples.petclinic.genai.dto.AddPetRequest;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.OwnerRequest;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Mono;

public class AIDataProviderTest {

    private WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private VectorStore vectorStore;
    private AIDataProvider aiDataProvider;

    @BeforeEach
    void setUp() {
        webClientBuilder = mock(WebClient.Builder.class);
        webClient = mock(WebClient.class);
        vectorStore = mock(VectorStore.class);

        when(webClientBuilder.build()).thenReturn(webClient);

        aiDataProvider = new AIDataProvider(webClientBuilder, vectorStore);
    }

    @Test
    void getAllOwners_shouldReturnOwnersResponse() {
        List<OwnerDetails> owners = List.of(new OwnerDetails());

        var request = mock(RequestHeadersUriSpec.class);
        var responseSpec = mock(ResponseSpec.class);

        when(webClient.get()).thenReturn(request);
        when(request.uri("http://customers-service/owners")).thenReturn(request);
        when(request.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(owners));

        OwnersResponse result = aiDataProvider.getAllOwners();

        assertThat(result.owners()).hasSize(1);
    }

    @Test
    void addPetToOwner_shouldReturnAddedPetResponse() {
        PetDetails pet = new PetDetails();
        AddPetRequest requestDto = new AddPetRequest(1, pet);

        var request = mock(RequestBodyUriSpec.class);
        var bodySpec = mock(RequestBodySpec.class);
        var responseSpec = mock(ResponseSpec.class);

        when(webClient.post()).thenReturn(request);
        when(request.uri("http://customers-service/owners/1/pets")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(pet)).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(PetDetails.class)).thenReturn(Mono.just(pet));

        AddedPetResponse response = aiDataProvider.addPetToOwner(requestDto);

        assertThat(response.pet()).isSameAs(pet);
    }

    @Test
    void addOwnerToPetclinic_shouldReturnOwnerResponse() {
        OwnerDetails ownerDetails = new OwnerDetails();
        OwnerRequest ownerRequest = new OwnerRequest();

        var request = mock(RequestBodyUriSpec.class);
        var bodySpec = mock(RequestBodySpec.class);
        var responseSpec = mock(ResponseSpec.class);

        when(webClient.post()).thenReturn(request);
        when(request.uri("http://customers-service/owners")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(ownerRequest)).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(OwnerDetails.class)).thenReturn(Mono.just(ownerDetails));

        OwnerResponse response = aiDataProvider.addOwnerToPetclinic(ownerRequest);

        assertThat(response.owner()).isSameAs(ownerDetails);
    }

    @Test
    void getVets_shouldQueryVectorStore_withNonNullVet() throws Exception {
        Vet dummyVet = new Vet("Dr. House");
        VetRequest vetRequest = new VetRequest(dummyVet);

        List<Document> documents = List.of(new Document("Doc1 Content"));

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(documents);

        VetResponse response = aiDataProvider.getVets(vetRequest);

        assertThat(response.vets()).containsExactly("Doc1 Content");

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        assertThat(captor.getValue().getTopK()).isEqualTo(20);
        assertThat(captor.getValue().getQuery()).contains("Dr. House");
    }

    @Test
    void getVets_shouldQueryVectorStore_withNullVet() throws Exception {
        VetRequest vetRequest = new VetRequest(null);

        List<Document> documents = List.of(new Document("Fallback Doc"));

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(documents);

        VetResponse response = aiDataProvider.getVets(vetRequest);

        assertThat(response.vets()).containsExactly("Fallback Doc");

        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        assertThat(captor.getValue().getTopK()).isEqualTo(50);
    }
}
