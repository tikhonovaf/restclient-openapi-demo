package ru.restclientopenapidemo;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0) // Запуск WireMock на случайном порту
public class AggregatorIntegrationTest {

/*
    Ключевые моменты теста:
    1.  @AutoConfigureWireMock(port = 0):
        Это самый надежный способ. Тесты никогда не упадут из-за того, что порт 8080 занят другим процессом.

    2.  @DynamicPropertySource: Позволяет "на лету" внедрить адрес работающего WireMock в бин ExternalServiceClient.

    3. formatted(): Используется для подстановки ID в JSON-тело ответа для наглядности.

    4. verify(...): Позволяет убедиться, что ваше приложение не просто вернуло данные из кэша, а реально обратилось к внешним API.
 */

    @Autowired
    private MockMvc mockMvc;

    /**
     * Динамически подменяем URL внешних сервисов в конфигурации Spring,
     * чтобы RestClient отправлял запросы на локальный адрес WireMock.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Используем встроенную переменную ${wiremock.server.port}
        registry.add("services.pet.url", () -> "http://localhost:${wiremock.server.port}");
        registry.add("services.store.url", () -> "http://localhost:${wiremock.server.port}");
    }
    @Test
    @DisplayName("Должен успешно агрегировать данные из PetService и StoreService")
    void shouldReturnAggregatedDataFromTwoServices() throws Exception {
        // Данные для теста
        Long petId = 777L;
        Long storeId = 888L;

        // 1. Настраиваем заглушку для Pet Service (первый URL)
        stubFor(WireMock.get(urlEqualTo("/pet/" + petId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "id": %d,
                                  "name": "Buddy",
                                  "status": "available"
                                }
                                """.formatted(petId))));

        // 2. Настраиваем заглушку для Store Service (второй URL)
        stubFor(WireMock.get(urlEqualTo("/store/" + storeId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                  "id": %d,
                                  "shipDate": "2026-01-02T15:00:00Z"
                                }
                                """.formatted(storeId))));

        // 3. Выполняем GET запрос к нашему агрегатору
        mockMvc.perform(get("/aggregate/{petId}/{storeId}", petId, storeId)
                        .contentType(MediaType.APPLICATION_JSON))
                // Проверяем статус ответа нашего сервиса
                .andExpect(status().isOk())
                // Проверяем содержимое JSON, которое сформировал AggregatorService
                .andExpect(jsonPath("$.petName").value("Buddy"))
                .andExpect(jsonPath("$.status").value("available"))
                // Используем Matchers.containsString()
                .andExpect(jsonPath("$.storeAddress", org.hamcrest.Matchers.containsString(storeId.toString())));

        // Верификация: проверяем, что RestClient действительно сделал вызовы
        verify(getRequestedFor(urlEqualTo("/pet/" + petId)));
        verify(getRequestedFor(urlEqualTo("/store/" + storeId)));
    }

    @Test
    @DisplayName("Должен возвращать ошибку, если один из внешних сервисов недоступен")
    void shouldReturnErrorWhenExternalServiceFails() throws Exception {
        // Имитируем ошибку 404 во внешнем сервисе
        stubFor(WireMock.get(urlPathMatching("/pet/.*"))
                .willReturn(aResponse().withStatus(404)));

        mockMvc.perform(get("/aggregate/999/111"))
                .andExpect(status().isInternalServerError());
        // По умолчанию RestClient бросает исключение, которое Spring превращает в 500,
        // если не настроен GlobalExceptionHandler.
    }
}