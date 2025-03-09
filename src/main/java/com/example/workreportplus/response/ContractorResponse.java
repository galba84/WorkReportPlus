package com.example.workreportplus.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ContractorResponse {
    String firstName;
    String lastName;
    String rank;
    String position;
    String nickname;

}
