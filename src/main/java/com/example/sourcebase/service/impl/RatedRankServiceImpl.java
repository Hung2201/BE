package com.example.sourcebase.service.impl;

import com.example.sourcebase.domain.Assess;
import com.example.sourcebase.domain.AssessDetail;
import com.example.sourcebase.domain.enumeration.ERole;
import com.example.sourcebase.exception.AppException;
import com.example.sourcebase.repository.IAssessDetailRepository;
import com.example.sourcebase.repository.IAssessRepository;
import com.example.sourcebase.repository.ICriteriaRepository;
import com.example.sourcebase.service.IRatedRankService;
import com.example.sourcebase.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatedRankServiceImpl implements IRatedRankService {
    private final IAssessRepository assessRepository;
    private final IAssessDetailRepository assessDetailRepository;
    private final ICriteriaRepository criteriaRepository;


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
                        Collectors.averagingDouble(ad -> ad.getValue())
                ));

        return averagePointPerCriteria;
    }
}
