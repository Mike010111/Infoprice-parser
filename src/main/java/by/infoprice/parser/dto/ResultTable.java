package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Table[0] - содержит три блока: метаданные, магазины и товары
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultTable {

    @JsonProperty("GeneralData")
    private List<GeneralData> generalData;

    @JsonProperty("TradingCompany")
    private List<TradingCompany> tradingCompanies;

    @JsonProperty("GoodsOffer")
    private List<GoodsOffer> goodsOffers;

    public List<GeneralData> getGeneralData() { return generalData; }
    public void setGeneralData(List<GeneralData> generalData) { this.generalData = generalData; }

    public List<TradingCompany> getTradingCompanies() { return tradingCompanies; }
    public void setTradingCompanies(List<TradingCompany> tradingCompanies) { this.tradingCompanies = tradingCompanies; }

    public List<GoodsOffer> getGoodsOffers() { return goodsOffers; }
    public void setGoodsOffers(List<GoodsOffer> goodsOffers) { this.goodsOffers = goodsOffers; }
}
