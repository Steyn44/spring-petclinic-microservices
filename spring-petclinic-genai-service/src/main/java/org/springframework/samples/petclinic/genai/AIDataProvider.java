package org.springframework.samples.petclinic.genai;

import java.util.List;

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

public class AIDataProvider {

    private final WebClient webClient;
    private final VectorStore vectorStore;

    public AIDataProvider(WebClient.Builder webClientBuilder, VectorStore vectorStore) {
        this.webClient = webClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public OwnersResponse getAllOwners() {
        RequestHeadersUriSpec<?> request = webClient.get();
        ResponseSpec responseSpec = request
                .uri("http://customers-service/owners")
                .retrieve();

        List<OwnerDetails> owners = responseSpec.bodyToMono(
                new ParameterizedTypeReference<List<OwnerDetails>>() {}).block();

        return new OwnersResponse(owners);
    }

    public AddedPetResponse addPetToOwner(AddPetRequest requestDto) {
        PetDetails pet = requestDto.pet();
        ResponseSpec responseSpec = webClient
                .post()
                .uri("http://customers-service/owners/" + requestDto.ownerId() + "/pets")
                .bodyValue(pet)
                .retrieve();

        PetDetails result = responseSpec.bodyToMono(PetDetails.class).block();
        return new AddedPetResponse(result);
    }

    public OwnerResponse addOwnerToPetclinic(OwnerRequest ownerRequest) {
        ResponseSpec responseSpec = webClient
                .post()
                .uri("http://customers-service/owners")
                .bodyValue(ownerRequest)
                .retrieve();

        OwnerDetails ownerDetails = responseSpec.bodyToMono(OwnerDetails.class).block();
        return new OwnerResponse(ownerDetails);
    }

    public VetResponse getVets(VetRequest vetRequest) {
        int topK = vetRequest.vet() == null ? 50 : 20;
        String query = vetRequest.vet() != null ? vetRequest.vet().name() : "default";

        SearchRequest searchRequest = SearchRequest.builder()
                .withQuery(query)
                .withTopK(topK)
                .build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        List<String> vetResults = documents.stream()
                .map(Document::getContent)
                .toList();

        return new VetResponse(vetResults);
    }
}
