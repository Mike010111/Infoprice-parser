package by.infoprice.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

// Корневой объект
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {

    @JsonProperty("Table")
    private List<ResultTable> table;

    public List<ResultTable> getTable() { return table; }
    public void setTable(List<ResultTable> table) { this.table = table; }
}
