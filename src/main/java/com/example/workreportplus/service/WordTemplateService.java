package com.example.workreportplus.service;

import com.example.workreportplus.dto.RegionReportTemplateDto;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Service
public class WordTemplateService {

    public byte[] generateWordFromRtfTemplate(String templateName, Map<String, String> variables,
                                              RegionReportTemplateDto template) throws IOException {
        // 1️⃣ Sanitize and convert the template name
        templateName = convertTemplateName(templateName);
        if (templateName == null || templateName.isBlank()) {
            throw new IllegalArgumentException("Template name must not be null or blank");
        }

        // 2️⃣ Load the resource safely
        ClassPathResource templateResource = new ClassPathResource("docTemplates/" + templateName + ".rtf");
        if (!templateResource.exists() || !templateResource.isReadable()) {
            throw new FileNotFoundException("Template file not found or not readable: " + templateName);
        }

        // 3️⃣ Read the RTF as raw bytes (avoiding BufferedReader which can corrupt RTF)
        byte[] rtfBytes;
        try (InputStream inputStream = templateResource.getInputStream()) {
            rtfBytes = inputStream.readAllBytes();
        }

        String rtfContent = new String(rtfBytes, StandardCharsets.UTF_8);

        if (template != null) {
            rtfContent =  template.getContent();
        }
        // 4️⃣ Replace placeholders safely
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                String value = Optional.ofNullable(entry.getValue()).orElse("");

                // Special formatting for groupName: UPPERCASE
                if ("groupName".equals(entry.getKey())) {
                    value = value.toUpperCase();
                }

                rtfContent = rtfContent.replace(placeholder, value);
            }
        }


        // 5️⃣ Remove any remaining placeholders like {{...}}
        rtfContent = rtfContent.replaceAll("\\{\\{[^}]+}}", "");
        //leave only one empty line
        rtfContent = rtfContent.replaceAll("(?m)(\\R\\s*){2,}", System.lineSeparator() + System.lineSeparator());


        // 5️⃣ Validate that at least something was replaced
        if (rtfContent.contains("{{")) {
            System.err.println("⚠ Warning: Some placeholders were not replaced in template: " + templateName);
        }

        // 6️⃣ Validate content is not empty
        if (rtfContent.isBlank()) {
            throw new IOException("RTF content became empty after processing, aborting.");
        }

        // 7️⃣ Return as byte array
        return rtfContent.getBytes(StandardCharsets.UTF_8);
    }


    private String convertTemplateName(String templateName) {
        if ("Group Report".equals(templateName)) {
            return "groupReport";
        } else if ("Region Report".equals(templateName)) {
            return "regionReport";
        }
        return templateName;
    }
}
