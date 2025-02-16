package com.example.workreportplus.repository;

import com.example.workreportplus.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE r.date BETWEEN :startDate AND :endDate")
    List<Report> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT r FROM Report r WHERE r.date BETWEEN :startDate AND :endDate AND r.region = :region")
    List<Report> findByDateRangeAndRegion(@Param("startDate") String startDate, @Param("endDate") String endDate, @Param("region") String region);
}
