package com.example.workreportplus.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class WordTemplateService {

    public byte[] generateWordFromRtfTemplate(String templateName, Map<String, String> variables) throws IOException {
        // Load the RTF template from the docTemplates folder
        ClassPathResource templateResource = new ClassPathResource("docTemplates/" + templateName + ".rtf");
        if (!templateResource.exists()) {
            throw new FileNotFoundException("Template file not found: " + templateName);
        }

        // Read RTF content into a string
        String rtfContent;
        try (InputStream inputStream = templateResource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder contentBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
            rtfContent = contentBuilder.toString();
        }

        // Replace placeholders
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            rtfContent = rtfContent.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        // Convert modified text back to proper RTF format using RTFEditorKit
        RTFEditorKit rtfEditorKit = new RTFEditorKit();
        StyledDocument document = new DefaultStyledDocument(); // Use StyledDocument to handle text

        try (ByteArrayInputStream bais = new ByteArrayInputStream(rtfContent.getBytes(StandardCharsets.UTF_8))) {
            rtfEditorKit.read(bais, document, 0);
        } catch (BadLocationException e) {
            throw new IOException("Error processing RTF document", e);
        }

// ✅ Fix: Ensure text is inserted if document is empty
        if (document.getLength() == 0) {
            try {
                document.insertString(0, rtfContent, null); // Manually insert the processed text
            } catch (BadLocationException e) {
                throw new IOException("Error inserting text into RTF document", e);
            }
        }

// Write the properly formatted RTF document
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            rtfEditorKit.write(outputStream, document, 0, document.getLength());
        } catch (BadLocationException e) {
            throw new IOException("Error writing RTF document", e);
        }

        return outputStream.toByteArray();

    }
}
