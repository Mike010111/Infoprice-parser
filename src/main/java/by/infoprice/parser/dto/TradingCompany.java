package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// магазины
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradingCompany {

    @JsonProperty("ContractorId")
    private long contractorId;

    @JsonProperty("ContractorName")
    private String contractorName;

    public long getContractorId() { return contractorId; }
    public void setContractorId(long contractorId) { this.contractorId = contractorId; }

    public String getContractorName() { return contractorName; }
    public void setContractorName(String contractorName) { this.contractorName = contractorName; }
}
