package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Метаданные выборки
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneralData {

    @JsonProperty("TotalGoods")
    private int totalGoods;

    @JsonProperty("AmountGoods")
    private int amountGoods;

    @JsonProperty("AmountPages")
    private int amountPages;

    public int getTotalGoods() { return totalGoods; }
    public void setTotalGoods(int totalGoods) { this.totalGoods = totalGoods; }

    public int getAmountGoods() { return amountGoods; }
    public void setAmountGoods(int amountGoods) { this.amountGoods = amountGoods; }

    public int getAmountPages() { return amountPages; }
    public void setAmountPages(int amountPages) { this.amountPages = amountPages; }
}
