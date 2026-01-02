package ru.restclientopenapidemo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.restclientopenapidemo.external.api.PetApi;
import ru.restclientopenapidemo.external.api.StoreApi;
import ru.restclientopenapidemo.external.handler.ApiClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public PetApi petApi(@Value("${services.pet.url}") String petUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(petUrl);
        return new PetApi(apiClient);
    }

    @Bean
    public StoreApi storeApi(@Value("${services.store.url}") String storeUrl) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(storeUrl);
        return new StoreApi(apiClient);
    }
}