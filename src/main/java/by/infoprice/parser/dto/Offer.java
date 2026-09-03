package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// Одна конкретная цена товара в конкретном магазине
@JsonIgnoreProperties(ignoreUnknown = true)
public class Offer {

    @JsonProperty("Price")
    private String price; // строка, т.к. сервер шлёт "11.99", а не число

    @JsonProperty("ContractorId")
    private long contractorId;

    @JsonProperty("IsPromotionalPrice")
    private int isPromotionalPrice;

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public long getContractorId() { return contractorId; }
    public void setContractorId(long contractorId) { this.contractorId = contractorId; }

    public int getIsPromotionalPrice() { return isPromotionalPrice; }
    public void setIsPromotionalPrice(int isPromotionalPrice) { this.isPromotionalPrice = isPromotionalPrice; }
}
