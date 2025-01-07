package com.example.sourcebase.domain.dto.resdto.custom;

import com.example.sourcebase.domain.model.AverageValueInCriteria;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OverallRatedResDto {
    List<AverageValueInCriteria> averageValueBySelf;
    List<AverageValueInCriteria> averageValueByTeam;
    List<AverageValueInCriteria> averageValueByManager;
    Double overallPoint;
    String rank;
    Integer levelUpRecommend;
}
