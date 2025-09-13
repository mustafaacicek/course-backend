package com.course.app.service;

import com.course.app.dto.StudentRankingDTO;
import com.course.app.repository.StudentRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentRankingService {

    private final StudentRankingRepository studentRankingRepository;

    public List<StudentRankingDTO> getTopStudents(int limit) {
        List<Object[]> results = studentRankingRepository.findTopStudentsByScore(limit);
        List<StudentRankingDTO> rankings = new ArrayList<>();
        
        int rank = 1;
        for (Object[] result : results) {
            StudentRankingDTO dto = StudentRankingDTO.builder()
                    .id(((Number) result[0]).longValue())
                    .firstName((String) result[1])
                    .lastName((String) result[2])
                    .nationalId((String) result[3])
                    .totalScore(((Number) result[4]).doubleValue())
                    .averageScore(((Number) result[5]).doubleValue())
                    .rank(rank++)
                    .build();
            rankings.add(dto);
        }
        
        return rankings;
    }

    public List<StudentRankingDTO> getTopStudentsByLocation(Long locationId, int limit) {
        List<Object[]> results = studentRankingRepository.findTopStudentsByScoreAndLocation(locationId, limit);
        List<StudentRankingDTO> rankings = new ArrayList<>();
        
        int rank = 1;
        for (Object[] result : results) {
            StudentRankingDTO dto = StudentRankingDTO.builder()
                    .id(((Number) result[0]).longValue())
                    .firstName((String) result[1])
                    .lastName((String) result[2])
                    .nationalId((String) result[3])
                    .totalScore(((Number) result[4]).doubleValue())
                    .averageScore(((Number) result[5]).doubleValue())
                    .courseLocationId(result[6] != null ? ((Number) result[6]).longValue() : null)
                    .courseLocationName((String) result[7])
                    .rank(rank++)
                    .build();
            rankings.add(dto);
        }
        
        return rankings;
    }
}
