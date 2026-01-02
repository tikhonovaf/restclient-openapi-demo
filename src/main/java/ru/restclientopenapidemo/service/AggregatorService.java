package ru.restclientopenapidemo.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.restclientopenapidemo.client.ExternalServiceClient;
import ru.restclientopenapidemo.external.model.Pet;
import ru.restclientopenapidemo.external.model.Store;
import ru.restclientopenapidemo.model.AggregatedInfo;

@Service
public class AggregatorService {

    private final ExternalServiceClient externalServiceClient;

    // Явный конструктор вместо @RequiredArgsConstructor
    public AggregatorService(ExternalServiceClient externalServiceClient) {
        this.externalServiceClient = externalServiceClient;
    }
    public AggregatedInfo getAggregatedData(Long petId, Long storeId) {
        // Получаем данные из разных сервисов
        Pet pet = externalServiceClient.getPetById(petId);
        Store store = externalServiceClient.getStoreById(storeId);

        // Собираем ответ
        AggregatedInfo info = new AggregatedInfo();
        info.setPetName(pet.getName());
        info.setStatus(pet.getStatus());
        info.setStoreAddress("Order #" + store.getId() + " (Date: " + store.getShipDate() + ")");

        return info;
    }
}