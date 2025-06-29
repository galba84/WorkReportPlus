package com.example.workreportplus.service;

import com.example.workreportplus.dto.AppSettingDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.jooq.tables.AppSettings.APP_SETTINGS;
import static com.example.workreportplus.service.GoogleSheetsService.regionTo100_30_Map;

/**
 * @author Alex Sereda
 * @date 29.06.2025 11:43
 */


@Service
public class AppSettingsService {

    public static final String ATTENDANCE_MAP = "attendance_map";
    public static final String OPERATIVE_REPORT_BPLA = "operative.report.bpla";
    public static final String OPERATIVE_REPORT_REB = "operative.report.reb";
    private final DSLContext dsl;
    private final RegionService regionService;

    public AppSettingsService(DSLContext dsl, RegionService regionService) {
        this.dsl = dsl;
        this.regionService = regionService;
    }

    public List<AppSettingDto> findAll() {
        return dsl.select(APP_SETTINGS.fields())
                .from(APP_SETTINGS)
                .fetchInto(AppSettingDto.class);
    }

    public Optional<AppSettingDto> findByKey(String key) {
        return Optional.ofNullable(
                dsl.select(APP_SETTINGS.fields())
                        .from(APP_SETTINGS)
                        .where(APP_SETTINGS.SETTING_KEY.eq(key))
                        .fetchOneInto(AppSettingDto.class)
        );
    }

    public AppSettingDto create(AppSettingDto input) {
        AppSettingDto dto = dsl.insertInto(APP_SETTINGS)
                .set(APP_SETTINGS.SETTING_KEY, input.getSettingKey())
                .set(APP_SETTINGS.SETTING_VALUE, input.getSettingValue())
                .set(APP_SETTINGS.SETTING_DATA, JSONB.valueOf(input.getSettingData().toString()))
                .set(APP_SETTINGS.FORMAT, input.getFormat())
                .set(APP_SETTINGS.DESCRIPTION, input.getDescription())
                .returning(APP_SETTINGS.fields())
                .fetchOne()
                .into(AppSettingDto.class);

        refreshIfRequired(dto);
        return dto;
    }

    public AppSettingDto update(String key, AppSettingDto input) {
        AppSettingDto dto = dsl.update(APP_SETTINGS)
                .set(APP_SETTINGS.SETTING_VALUE, input.getSettingValue())
                .set(APP_SETTINGS.SETTING_DATA, JSONB.valueOf(input.getSettingData().toString()))
                .set(APP_SETTINGS.FORMAT, input.getFormat())
                .set(APP_SETTINGS.DESCRIPTION, input.getDescription())
                .set(APP_SETTINGS.UPDATED_ON, DSL.currentOffsetDateTime())
                .where(APP_SETTINGS.SETTING_KEY.eq(key))
                .returning(APP_SETTINGS.fields())
                .fetchOne()
                .into(AppSettingDto.class);

        refreshIfRequired(dto);
        return dto;
    }

    private void refreshIfRequired(AppSettingDto dto) {
        if (ATTENDANCE_MAP.equals(dto.getSettingKey())) {
            try {
                Map<String, String> stringMap = convertJsonToMap(dto.getSettingData());

                for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                    String regionName = entry.getKey();
                    String value = entry.getValue();

                    UUID regionId = regionService.getRegionIdByName(regionName);
                    if (regionId != null) {
                        // ✅ Add or update entry without removing old ones
                        regionTo100_30_Map.put(regionId.toString(), value);
                    } else {
                        System.err.println("⚠️ Region not found: " + regionName);
                    }
                }

            }catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> convertJsonToMap(JsonNode settingData) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(settingData, new TypeReference<>() {});
    }
}
