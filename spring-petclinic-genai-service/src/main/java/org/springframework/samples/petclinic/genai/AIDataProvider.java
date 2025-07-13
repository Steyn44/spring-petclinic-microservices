package org.springframework.samples.petclinic.genai;

import java.util.List;
import java.util.stream.Collectors;

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
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.samples.petclinic.genai.dto.VetRequest;
import org.springframework.samples.petclinic.genai.dto.VetResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class AIDataProvider {

    private final WebClient.Builder webClientBuilder;
    private final VectorStore vectorStore;

    public AIDataProvider(WebClient.Builder webClientBuilder, VectorStore vectorStore) {
        this.webClientBuilder = webClientBuilder;
        this.vectorStore = vectorStore;
    }

    public OwnersResponse getAllOwners() {
        WebClient webClient = webClientBuilder.build();

        List<OwnerDetails> owners = webClient.get()
                .uri("http://customers-service/owners")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<OwnerDetails>>() {})
                .block();

        return new OwnersResponse(owners);
    }

    public AddedPetResponse addPetToOwner(AddPetRequest request) {
        WebClient webClient = webClientBuilder.build();

        PetDetails response = webClient.post()
                .uri("http://customers-service/owners/" + request.ownerId() + "/pets")
                .bodyValue(request.pet())
                .retrieve()
                .bodyToMono(PetDetails.class)
                .block();

        return new AddedPetResponse(response);
    }

    public OwnerResponse addOwnerToPetclinic(OwnerRequest ownerRequest) {
        WebClient webClient = webClientBuilder.build();

        OwnerDetails response = webClient.post()
                .uri("http://customers-service/owners")
                .bodyValue(ownerRequest)
                .retrieve()
                .bodyToMono(OwnerDetails.class)
                .block();

        return new OwnerResponse(response);
    }

    public VetResponse getVets(VetRequest vetRequest) {
        String vetName = null;
        if (vetRequest != null && vetRequest.vet() != null) {
            vetName = vetRequest.vet().name();
        }

        int topK = (vetName == null) ? 50 : 20;
        String query = (vetName == null) ? "fallback" : vetName;

        List<Document> documents = vectorStore.similaritySearch(
                new SearchRequest(query).withTopK(topK)
        );

        List<String> vetContents = documents.stream()
                .map(Document::getContent)
                .collect(Collectors.toList());

        return new VetResponse(vetContents);
    }
}
