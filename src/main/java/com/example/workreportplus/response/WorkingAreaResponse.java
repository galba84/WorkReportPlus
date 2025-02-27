package com.example.workreportplus.response;

import com.example.workreportplus.ENUM.AreaType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WorkingAreaResponse {
    String name;
    AreaType areaType;
    String county;
    String district;
    String region;
}
