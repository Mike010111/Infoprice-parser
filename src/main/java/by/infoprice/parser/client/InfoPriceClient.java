package by.infoprice.parser.client;

import by.infoprice.parser.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class InfoPriceClient {

    private static final String API_URL = "https://api.infoprice.by/InfoPrice.Goods?v=3";

    // ID компании-заказчика для сравнения
    private static final long COMPARE_CONTRACTOR_ID = 72631;

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    private static final String FROM_ID = getEnvOrThrow("FROM_ID");
    private static final String SERVER_KEY = getEnvOrThrow("SERVER_KEY");

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public InfoPriceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Запрашивает одну страницу акционных товаров.
     * @param page номер страницы (сервер нумерует с 1)
     */
    public ApiResponse fetchPage(int page) throws Exception {

        String requestBody = buildRequestBody(page);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "text/plain;charset=UTF-8")
                .header("Accept", "*/*")
                .header("Origin", "https://infoprice.by")
                .header("Referer", "https://infoprice.by/")
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Сервер вернул ошибку: HTTP " + response.statusCode());
        }

        return objectMapper.readValue(response.body(), ApiResponse.class);
    }

    private String buildRequestBody(int page) {
        return """
                {
                  "CRC": "",
                  "Packet": {
                    "FromId": "%s",
                    "ServerKey": "%s",
                    "Data": {
                      "ContractorId": "",
                      "GoodsGroupId": "",
                      "Page": "%d",
                      "Search": "",
                      "OrderBy": 0,
                      "OrderByContractor": 0,
                      "CompareСontractorId": %d,
                      "CatalogType": 1,
                      "IsAgeLimit": 1,
                      "IsPromotionalPrice": 1
                    }
                  }
                }
                """.formatted(FROM_ID, SERVER_KEY, page, COMPARE_CONTRACTOR_ID);
    }

    private static String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "Переменная окружения " + key + " не задана. " +
                            "Локально создайте файл .env в корне проекта, " +
                            "на Render — задайте её в разделе Environment Variables."
            );
        }
        return value;
    }
}
