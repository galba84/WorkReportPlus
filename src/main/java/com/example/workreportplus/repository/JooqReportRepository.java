package com.example.workreportplus.repository;

import com.example.jooq.tables.records.ReportsRecord;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.example.jooq.tables.Reports.REPORTS;

@Repository
public class JooqReportRepository {

    private final DSLContext dsl;

    public JooqReportRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<ReportsRecord> findReportsByDateRange(String startDate, String endDate) {
        return dsl.selectFrom(REPORTS)
                .where(REPORTS.DATE.between(startDate, endDate))
                .fetchInto(ReportsRecord.class);
    }
}
