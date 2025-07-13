package org.springframework.samples.petclinic.genai;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.samples.petclinic.genai.dto.AddPetRequest;
import org.springframework.samples.petclinic.genai.dto.AddedPetResponse;
import org.springframework.samples.petclinic.genai.dto.OwnerDetails;
import org.springframework.samples.petclinic.genai.dto.OwnerRequest;
import org.springframework.samples.petclinic.genai.dto.OwnerResponse;
import org.springframework.samples.petclinic.genai.dto.OwnersResponse;
import org.springframework.samples.petclinic.genai.dto.PetDetails;
import org.springframework.samples.petclinic.genai.dto.PetRequest;
import org.springframework.samples.petclinic.genai.dto.Vet;
import org.springframework.samples.petclinic.genai.dto.VetRequest;
import org.springframework.samples.petclinic.genai.dto.VetResponse;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * This class defines the @Bean functions that the LLM provider will invoke when it
 * requires more Information on a given topic. The currently available functions enable
 * the LLM to get the list of owners and their pets, get information about the
 * veterinarians, and add a pet to an owner.
 *
 * @author Oded Shopen
 */
@Configuration
class AIFunctionConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(AIFunctionConfiguration.class);

	@Bean
	@Description("List the owners that the pet clinic has")
	public Function<OwnerRequest, OwnersResponse> listOwners(AIDataProvider petclinicAiProvider) {
		return request -> petclinicAiProvider.getAllOwners();
	}

	@Bean
	@Description("Add a new pet owner to the pet clinic. " +
			"The Owner must include a first name and a last name " +
			"as two separate words, plus an address and a 10-digit phone number")
	public Function<OwnerRequest, OwnerResponse> addOwnerToPetclinic(AIDataProvider petclinicAiDataProvider) {
		return petclinicAiDataProvider::addOwnerToPetclinic;
	}

	@Bean
	@Description("List the veterinarians that the pet clinic has")
	public Function<VetRequest, VetResponse> listVets(AIDataProvider petclinicAiProvider) {
		return request -> {
			try {
				return petclinicAiProvider.getVets(request);
			} catch (JsonProcessingException e) {
				LOG.error("Error processing JSON in the listVets function", e);
				return null;
			}
		};
	}

	@Bean
	@Description("Add a pet with the specified petTypeId, " +
			"to an owner identified by the ownerId. " +
			"The allowed Pet types IDs are only: " +
			"1 - cat 2 - dog 3 - lizard 4 - snake 5 - bird 6 - hamster")
	public Function<AddPetRequest, AddedPetResponse> addPetToOwner(AIDataProvider petclinicAiProvider) {
		return petclinicAiProvider::addPetToOwner;
	}
}
