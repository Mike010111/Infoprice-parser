package by.infoprice.parser.web;

import by.infoprice.parser.export.ExcelExporter;
import by.infoprice.parser.model.ProductRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class WebServer {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int PORT = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080"));

    private final ScanManager scanManager = new ScanManager();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/scan/status", this::handleScanStatus);
        server.createContext("/api/scan", this::handleScan);
        server.createContext("/api/results/latest", this::handleResultsLatest);
        server.createContext("/api/results/file/", this::handleResultsFile);
        server.createContext("/", this::handleStatic);

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(4));
        server.start();

        System.out.println("Сервер запущен: http://localhost:" + PORT);
    }

    private void handleScan(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        boolean started = scanManager.startScan();
        sendJson(exchange, 200, Map.of("started", started));
    }

    private void handleScanStatus(HttpExchange exchange) throws IOException {
        withCors(exchange);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("state", scanManager.getState().name());
        response.put("progress", scanManager.getProgress());
        response.put("message", scanManager.getMessage());
        sendJson(exchange, 200, response);
    }

    private void handleResultsLatest(HttpExchange exchange) throws IOException {
        withCors(exchange);
        String fileName = scanManager.getLatestFileName();
        Map<String, Object> response = new LinkedHashMap<>();

        List<ProductRow> products = scanManager.getLatestProducts();

        if (fileName == null || products.isEmpty()) {
            response.put("fileName", null);
            response.put("downloadUrl", null);
            response.put("stores", List.of());
            response.put("items", List.of());
        } else {
            response.put("fileName", fileName);
            response.put("downloadUrl", "/api/results/file/" + fileName);
            response.put("stores", extractStoreNames(products));
            response.put("items", toPreviewItems(products));
        }
        sendJson(exchange, 200, response);
    }

    private void handleResultsFile(HttpExchange exchange) throws IOException {
        withCors(exchange);

        byte[] fileBytes = scanManager.getLatestFileBytes();
        String fileName = scanManager.getLatestFileName();

        if (fileBytes == null || fileName == null) {
            sendJson(exchange, 404, Map.of("error", "Нет данных для выгрузки"));
            return;
        }

        exchange.getResponseHeaders().add(
                "Content-Type",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        exchange.getResponseHeaders().add(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + java.net.URLEncoder.encode(fileName, StandardCharsets.UTF_8)
        );

        exchange.sendResponseHeaders(200, fileBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(fileBytes);
        }
    }


    private List<String> extractStoreNames(List<ProductRow> products) {
        LinkedHashSet<String> stores = new LinkedHashSet<>();
        for (ProductRow product : products) {
            stores.addAll(product.getPricesByStore().keySet());
        }
        return new ArrayList<>(stores);
    }

    private List<Map<String, Object>> toPreviewItems(List<ProductRow> products) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProductRow product : products) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", product.getProductName());

            Map<String, Object> priceCells = new LinkedHashMap<>();
            for (Map.Entry<String, ProductRow.PriceInfo> entry : product.getPricesByStore().entrySet()) {
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("value", entry.getValue().getPrice());
                cell.put("promo", entry.getValue().isPromotional());
                priceCells.put(entry.getKey(), cell);
            }

            item.put("prices", priceCells);
            items.add(item);
        }
        return items;
    }

    private void handleStatic(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) {
            path = "/index.html";
        }

        String resourcePath = "/static" + path;
        InputStream resource = getClass().getResourceAsStream(resourcePath);

        if (resource == null) {
            sendStaticNotFound(exchange, "Not found: " + resourcePath);
            return;
        }

        String contentType;
        if (path.endsWith(".html")) contentType = "text/html; charset=utf-8";
        else if (path.endsWith(".js")) contentType = "application/javascript; charset=utf-8";
        else if (path.endsWith(".css")) contentType = "text/css; charset=utf-8";
        else contentType = "application/octet-stream";

        byte[] bytes = resource.readAllBytes();
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendStaticNotFound(HttpExchange exchange, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(404, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void withCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
