package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Один товар с массивом цен по магазинам
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsOffer {

    @JsonProperty("GoodsId")
    private long goodsId;

    @JsonProperty("GoodsName")
    private String goodsName;

    @JsonProperty("GoodsGroupName")
    private String goodsGroupName;

    @JsonProperty("Offers")
    private List<Offer> offers;

    public long getGoodsId() { return goodsId; }
    public void setGoodsId(long goodsId) { this.goodsId = goodsId; }

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public String getGoodsGroupName() { return goodsGroupName; }
    public void setGoodsGroupName(String goodsGroupName) { this.goodsGroupName = goodsGroupName; }

    public List<Offer> getOffers() { return offers; }
    public void setOffers(List<Offer> offers) { this.offers = offers; }
}
