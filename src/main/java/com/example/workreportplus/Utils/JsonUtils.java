package com.example.workreportplus.Utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // ✅ Convert Java Object to JSONB (for PostgreSQL)
    public static JSONB toJsonB(Object data) {
        try {
            return data == null ? null : JSONB.valueOf(objectMapper.writeValueAsString(data));
        } catch (IOException e) {
            throw new RuntimeException("Error converting object to JSONB", e);
        }
    }

    // ✅ Convert JSONB to Java Object (Generic Method)
    public static <T> T fromJsonB(JSONB jsonb, Class<T> clazz) {
        try {
            return (jsonb == null || jsonb.data() == null) ? null : objectMapper.readValue(jsonb.data(), clazz);
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSONB to object", e);
        }
    }

    // ✅ Convert JSONB to List<String>
    public static List<String> jsonbToList(JSONB jsonb) {
        try {
            return (jsonb == null || jsonb.data() == null)
                    ? Collections.emptyList()
                    : objectMapper.readValue(jsonb.data(), new TypeReference<List<String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSONB to List<String>", e);
        }
    }

    // ✅ Convert JSONB to Map<String, String>
    public static Map<String, String> jsonbToMap(JSONB jsonb) {
        try {
            return (jsonb == null || jsonb.data() == null)
                    ? Collections.emptyMap()
                    : objectMapper.readValue(jsonb.data(), new TypeReference<Map<String, String>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error converting JSONB to Map<String, String>", e);
        }
    }
}
