package by.infoprice.parser.web;

import by.infoprice.parser.client.InfoPriceClient;
import by.infoprice.parser.export.ExcelExporter;
import by.infoprice.parser.model.ProductRow;
import by.infoprice.parser.service.ParsingService;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScanManager {

    public enum State { IDLE, RUNNING, SUCCESS, ERROR }

    private volatile State state = State.IDLE;
    private volatile int progress = 0;
    private volatile String message = "";
    private volatile String latestFileName;
    private volatile List<ProductRow> latestProducts = new ArrayList<>();
    private volatile byte[] latestFileBytes;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean startScan() {
        if (!running.compareAndSet(false, true)) {
            return false;
        }
        state = State.RUNNING;
        progress = 0;
        message = "Запуск сканирования...";

        Thread thread = new Thread(this::runScan, "scan-thread");
        thread.setDaemon(true);
        thread.start();
        return true;
    }

    private void runScan() {
        try {
            InfoPriceClient client = new InfoPriceClient();
            ParsingService service = new ParsingService(client);

            List<ProductRow> products = service.parseAllPromotionalGoods((page, totalPages) -> {
                progress = totalPages > 0 ? (page * 100 / totalPages) : 0;
                message = "Обработана страница " + page + " из " + totalPages;
            });

            String timestamp = java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            latestFileName = "infoprice_" + timestamp + ".xlsx";
            latestProducts = products;

            // Новое сообщение перед генерацией файла
            message = "Подготовка Excel-файла...";

            latestFileBytes = new ExcelExporter().exportToBytes(products);

            progress = 100;
            message = "Готово. Товаров собрано: " + products.size();
            state = State.SUCCESS;

        } catch (Exception e) {
            state = State.ERROR;
            message = "Ошибка: " + e.getMessage();
        } finally {
            running.set(false);
        }
    }


    public State getState() { return state; }
    public int getProgress() { return progress; }
    public String getMessage() { return message; }
    public String getLatestFileName() { return latestFileName; }
    public List<ProductRow> getLatestProducts() { return latestProducts; }
    public byte[] getLatestFileBytes() { return latestFileBytes; }
}
