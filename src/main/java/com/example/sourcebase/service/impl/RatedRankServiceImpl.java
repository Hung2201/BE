package com.example.sourcebase.service.impl;

import com.example.sourcebase.domain.Assess;
import com.example.sourcebase.domain.AssessDetail;
import com.example.sourcebase.domain.dto.resdto.AssessDetailResDto;
import com.example.sourcebase.domain.dto.resdto.AssessResDTO;
import com.example.sourcebase.domain.enumeration.ERole;
import com.example.sourcebase.exception.AppException;
import com.example.sourcebase.mapper.AssessMapper;
import com.example.sourcebase.repository.IAssessDetailRepository;
import com.example.sourcebase.repository.IAssessRepository;
import com.example.sourcebase.repository.ICriteriaRepository;
import com.example.sourcebase.service.IRatedRankService;
import com.example.sourcebase.util.ErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RatedRankServiceImpl implements IRatedRankService {
    IAssessRepository assessRepository;
    AssessMapper assessMapper;
    IAssessDetailRepository assessDetailRepository;
    ICriteriaRepository criteriaRepository;

    @Override
    public Map<Long, Object> getAverageValueByTeam(Long userId) {
        // 1. Lấy danh sách đánh giá TEAM của userId
        List<AssessResDTO> assessesResDto = assessRepository.getListAssessTeamOfUserId(userId).stream()
                .map(assess -> {
                    AssessResDTO assessResDTO = assessMapper.toAssessResDto(assess);
                    assessResDTO.setAssessDetails(assessResDTO.getAssessDetails().stream()
                            .peek(assessDetail -> assessDetail.setAssessId(assessResDTO.getId()))
                            .collect(Collectors.toList()));
                    return assessResDTO;
                })
                .collect(Collectors.toList());

        // 2. Tính giá trị trung bình của các tiêu chí (criteriaID giống nhau)
        Map<Long, Object> result = new HashMap<>();
        assessesResDto.forEach(assessResDTO -> {
            Map<Long, List<AssessDetailResDto>> groupedByCriteria = assessResDTO.getAssessDetails().stream()
                    .collect(Collectors.groupingBy(detail -> detail.getCriteria().getId()));

            groupedByCriteria.forEach((criteriaId, assessDetails) -> {
                double averageValue = assessDetails.stream()
                        .mapToDouble(AssessDetailResDto::getValue)
                        .average()
                        .orElse(0.0);

                if (Math.round(averageValue) != 0) {
                    result.put(criteriaId, Math.round(averageValue));
                }
            });
        });

        // 3. Trả về kết quả
        return result;
    }

    public Map<Long, Object> getAverageValueBySelf(Long userId) {
        // 1. Lấy danh sách đánh giá SELF của userId
        Assess assess = assessRepository.getAssessBySelf(userId);
        AssessResDTO assessResDTO = assessMapper.toAssessResDto(assess);
        assessResDTO.setAssessDetails(assessResDTO.getAssessDetails().stream()
                .peek(assessDetail -> assessDetail.setAssessId(assessResDTO.getId()))
                .collect(Collectors.toList()));
        // 2. Tính giá trị trung bình của các tiêu chí (criteriaID giống nhau)
        Map<Long, Object> result = new HashMap<>();
        Map<Long, List<AssessDetailResDto>> groupedByCriteria = assessResDTO.getAssessDetails().stream()
                .collect(Collectors.groupingBy(detail -> detail.getCriteria().getId()));

        groupedByCriteria.forEach((criteriaId, assessDetails) -> {
            double averageValue = assessDetails.stream()
                    .mapToDouble(AssessDetailResDto::getValue)
                    .average()
                    .orElse(0.0);

            result.put(criteriaId, Math.round(averageValue));
        });
        // 3. Trả về kết quả
        return result;
    }


    @Override
    public Map<Long, Double> getMapManagerRatingPointToUser(Long userId) {
        // find all assess of user
        List<Assess> aList = assessRepository.findByToUser_Id(userId);
        if (aList.isEmpty()) {
            throw new AppException(ErrorCode.ASSESS_IS_NOT_EXIST);
        }

        // find all the assesses by manager
        List<Assess> managerAssessesList = new ArrayList<>();
        for (Assess a : aList) {
            boolean isManager = a.getUser().getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getId() == ERole.MANAGER.getValue());
            if (isManager) {
                managerAssessesList.add(a);
            }
        }
        if (managerAssessesList.isEmpty()) {
            throw new AppException(ErrorCode.MANAGER_ASSESS_IS_NOT_EXIST);
        }

        // get the newest assess
        Assess newestAssess = managerAssessesList.getLast();
        // find all the assess detail of the newest assess. now the adList have the same assessId and assessmentDate
        List<AssessDetail> adList = assessDetailRepository
                .findByAssess_IdAndAssess_AssessmentDate(
                        newestAssess.getId(),
                        newestAssess.getAssessmentDate()
                );

        //group by assessId and criteriaId and get average point on scale 5
        Map<Long, Double> averagePointPerCriteria = adList.stream()
                .collect(Collectors.groupingBy(
                        ad -> ad.getCriteria().getId(),
                        Collectors.averagingDouble(AssessDetail::getValue)
                ));

        return averagePointPerCriteria;
    }
}
