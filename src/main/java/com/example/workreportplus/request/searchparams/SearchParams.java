package com.example.workreportplus.request.searchparams;

import lombok.Data;

import java.util.List;
@Data
public class SearchParams {
    private List<String> selectColumns;
    private String orderBy;
    private boolean ascending = true;
    private Integer limit;  // Number of records to fetch
    private Integer offset; // Starting position
}
