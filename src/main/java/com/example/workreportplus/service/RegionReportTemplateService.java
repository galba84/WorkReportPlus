package com.example.workreportplus.service;

import com.example.jooq.tables.records.RegionreporttemplateRecord;
import com.example.workreportplus.Utils.SecurityUtil;
import com.example.workreportplus.dto.RegionReportTemplateDto;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.example.jooq.Tables.REGIONREPORTTEMPLATE;

/**
 * @author Alex Sereda
 * @date 10.06.2025 17:38
 */
@Service
@RequiredArgsConstructor
public class RegionReportTemplateService {

    private final DSLContext dsl;

    public RegionReportTemplateDto create(RegionReportTemplateDto dto) {
        RegionreporttemplateRecord record = dsl.newRecord(REGIONREPORTTEMPLATE);
        record.setRegionid(dto.getRegionId());
        record.setPreamble(dto.getPreamble());
        record.setSignature(dto.getSignature());
        record.setCreatedby(SecurityUtil.getCurrentUsername());
        record.setUpdatedby(SecurityUtil.getCurrentUsername());

        record.setFileFormat(dto.getFileFormat());
        record.setContent(dto.getContent());
        record.setVariables(dto.getVariables().toArray(new String[0]));

        record.store();
        return toDto(record);
    }


    public RegionReportTemplateDto findById(UUID id) {
        RegionreporttemplateRecord record = dsl.selectFrom(REGIONREPORTTEMPLATE)
                .where(REGIONREPORTTEMPLATE.ID.eq(id))
                .fetchOne();
        return record != null ? toDto(record) : null;
    }

    public List<RegionReportTemplateDto> findAll() {
        return dsl.selectFrom(REGIONREPORTTEMPLATE)
                .fetch()
                .map(this::toDto);
    }

    public RegionReportTemplateDto update(UUID id, RegionReportTemplateDto dto) {
        RegionreporttemplateRecord record = dsl.fetchOne(REGIONREPORTTEMPLATE, REGIONREPORTTEMPLATE.ID.eq(id));
        if (record == null) return null;

        record.setPreamble(dto.getPreamble());
        record.setSignature(dto.getSignature());
        record.setUpdatedby(SecurityUtil.getCurrentUsername());
        record.setUpdatedon(OffsetDateTime.now());

        record.setFileFormat(dto.getFileFormat());
        record.setContent(dto.getContent());
        record.setVariables(dto.getVariables().toArray(new String[0]));

        record.store();
        return toDto(record);
    }


    public boolean delete(UUID id) {
        return dsl.deleteFrom(REGIONREPORTTEMPLATE)
                .where(REGIONREPORTTEMPLATE.ID.eq(id))
                .execute() > 0;
    }

    private RegionReportTemplateDto toDto(RegionreporttemplateRecord record) {
        return new RegionReportTemplateDto(
                record.getId(),
                record.getRegionid(),
                record.getPreamble(),
                record.getSignature(),
                record.getCreatedby(),
                record.getUpdatedby(),
                record.getCreatedon(),
                record.getUpdatedon(),
                record.getFileFormat(),
                record.getContent(),
                Arrays.asList(record.getVariables())
        );
    }

    public RegionReportTemplateDto getRecentTemplateByRegionId(UUID regionId) {
        RegionreporttemplateRecord record = dsl
                .selectFrom(REGIONREPORTTEMPLATE)
                .where(REGIONREPORTTEMPLATE.REGIONID.eq(regionId))
                .orderBy(REGIONREPORTTEMPLATE.UPDATEDON.desc())
                .limit(1)
                .fetchOne();

        return record != null ? toDto(record) : null;
    }

}
