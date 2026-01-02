package ru.restclientopenapidemo.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.restclientopenapidemo.external.model.Pet;
import ru.restclientopenapidemo.external.model.Store;

@Component
public class ExternalServiceClient {

    private final RestClient petRestClient;
    private final RestClient storeRestClient;

    // Spring Boot 3.4 автоматически внедряет RestClient.Builder
    public ExternalServiceClient(
            RestClient.Builder builder,
            @Value("${services.pet.url}") String petUrl,
            @Value("${services.store.url}") String storeUrl) {

        this.petRestClient = builder.baseUrl(petUrl).build();
        this.storeRestClient = builder.baseUrl(storeUrl).build();
    }

    public Pet getPetById(Long petId) {
        return petRestClient.get()
                .uri("/pet/{petId}", petId)
                .retrieve()
                .body(Pet.class); // Используем сгенерированную модель Pet
    }

    public Store getStoreById(Long orderId) {
        return storeRestClient.get()
                .uri("/store/{orderId}", orderId)
                .retrieve()
                .body(Store.class); // Используем сгенерированную модель Order
    }
}