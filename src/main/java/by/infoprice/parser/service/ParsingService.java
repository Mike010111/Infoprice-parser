package by.infoprice.parser.service;

import by.infoprice.parser.client.InfoPriceClient;
import by.infoprice.parser.dto.*;
import by.infoprice.parser.model.ProductRow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ParsingService {

    private final InfoPriceClient client;

    public ParsingService(InfoPriceClient client) {
        this.client = client;
    }

    public List<ProductRow> parseAllPromotionalGoods() throws Exception {
        return parseAllPromotionalGoods((page, totalPages) -> { });
    }

    public List<ProductRow> parseAllPromotionalGoods(BiConsumer<Integer, Integer> onPageDone) throws Exception {

        ApiResponse firstPage = client.fetchPage(1);
        ResultTable firstTable = firstPage.getTable().get(0);
        int totalPages = firstTable.getGeneralData().get(0).getAmountPages();

        Map<Long, String> storeNames = new LinkedHashMap<>();
        for (TradingCompany company : firstTable.getTradingCompanies()) {
            storeNames.put(company.getContractorId(), company.getContractorName());
        }

        Map<String, ProductRow> productsByName = new LinkedHashMap<>();

        collectGoodsFromTable(firstTable, productsByName, storeNames);
        onPageDone.accept(1, totalPages);

        for (int page = 2; page <= totalPages; page++) {
            ApiResponse response = client.fetchPage(page);
            ResultTable table = response.getTable().get(0);
            collectGoodsFromTable(table, productsByName, storeNames);
            onPageDone.accept(page, totalPages);

            Thread.sleep(700);
        }

        return new ArrayList<>(productsByName.values());
    }

    private void collectGoodsFromTable(ResultTable table,
                                       Map<String, ProductRow> productsByName,
                                       Map<Long, String> storeNames) {

        for (GoodsOffer goodsOffer : table.getGoodsOffers()) {

            String productName = goodsOffer.getGoodsName();
            ProductRow row = productsByName.computeIfAbsent(productName, name -> new ProductRow(name, storeNames));

            for (Offer offer : goodsOffer.getOffers()) {
                String storeName = storeNames.get(offer.getContractorId());
                if (storeName != null) {
                    boolean isPromotional = offer.getIsPromotionalPrice() == 1;
                    row.setPrice(storeName, offer.getPrice(), isPromotional);
                }
            }
        }
    }
}
