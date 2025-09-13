package com.course.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRankingDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String nationalId;
    private Double totalScore;
    private Double averageScore;
    private Integer rank;
    private Long courseLocationId;
    private String courseLocationName;
}
