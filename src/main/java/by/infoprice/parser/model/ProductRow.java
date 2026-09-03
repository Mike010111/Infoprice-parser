package by.infoprice.parser.model;

import java.util.LinkedHashMap;
import java.util.Map;

// Одна строка будущей Excel-таблицы: товар + карта "магазин -> цена с признаком акции"
public class ProductRow {

    // Хранит и цену, и флаг "акционная цена или нет"
    public static class PriceInfo {
        private final String price;
        private final boolean promotional;

        public PriceInfo(String price, boolean promotional) {
            this.price = price;
            this.promotional = promotional;
        }

        public String getPrice() { return price; }
        public boolean isPromotional() { return promotional; }
    }

    private final String productName;
    private final Map<String, PriceInfo> pricesByStore = new LinkedHashMap<>();

    public ProductRow(String productName, Map<Long, String> storeNames) {
        this.productName = productName;
        for (String storeName : storeNames.values()) {
            pricesByStore.put(storeName, new PriceInfo("-", false));
        }
    }

    public void setPrice(String storeName, String price, boolean promotional) {
        pricesByStore.put(storeName, new PriceInfo(price, promotional));
    }

    public String getProductName() { return productName; }

    public Map<String, PriceInfo> getPricesByStore() { return pricesByStore; }
}
