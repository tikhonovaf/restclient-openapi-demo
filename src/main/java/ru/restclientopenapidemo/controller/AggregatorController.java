package ru.restclientopenapidemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.restclientopenapidemo.api.AggregateApi;
import ru.restclientopenapidemo.model.AggregatedInfo;
import ru.restclientopenapidemo.service.AggregatorService;

/**
 * Контроллер для агрегации данных о животных и заказах.
 * Имплементирует интерфейс AggregateApi, сгенерированный плагином openapi-generator.
 */
@RestController
@Slf4j
public class AggregatorController implements AggregateApi {

    private final AggregatorService aggregatorService;

    // Явный конструктор вместо @RequiredArgsConstructor
    public AggregatorController(AggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    /**
     * GET /aggregate/{petId}/{storeId}
     * * @param petId Идентификатор питомца (из пути запроса)
     *
     * @param storeId Идентификатор заказа/магазина (из пути запроса)
     * @return Объект AggregatedInfo с объединенными данными
     */
    @Override
    public ResponseEntity<AggregatedInfo> getAggregatedInfo(Long petId, Long storeId) {
//        log.info("Received request to aggregate data for petId: {} and storeId: {}", petId, storeId);

        // Вызов бизнес-логики через сервис
        AggregatedInfo result = aggregatorService.getAggregatedData(petId, storeId);

        return ResponseEntity.ok(result);
    }
}