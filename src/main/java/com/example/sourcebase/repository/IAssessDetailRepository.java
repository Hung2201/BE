package com.example.sourcebase.repository;

import com.example.sourcebase.domain.AssessDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface IAssessDetailRepository extends JpaRepository<AssessDetail, Long> {

    @Query("SELECT ad FROM AssessDetail ad WHERE ad.assess.id = :assessId")
    List<AssessDetail> findByAssessId(Long assessId);

    List<AssessDetail> findByAssess_IdAndAssess_AssessmentDate(Long assessId, LocalDate assessmentDate);
}