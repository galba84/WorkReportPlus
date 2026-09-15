package com.example.workreportplus.service;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoogleSheetsService {
    public static final String SHPS_TABLE_ID = "personnel-sheet-id";
    public static final String DOVIDNYK_TABLE_ID = "reference-sheet-id";
    public static final String OPERATIVE_BPLA_TABLE_ID = "operations-primary-sheet-id";
    public static final String OPERATIVE_REB_TABLE_ID = "operations-secondary-sheet-id";
    public static final Map<String,String> regionTo100_30_Map = new ConcurrentHashMap<>();
    private final Environment environment;
    private final Sheets client;
    public GoogleSheetsService(Environment environment) throws IOException, GeneralSecurityException {
        this.environment = environment;
        if (!environment.getProperty("app.google.enabled", Boolean.class, false)) {
            client = null;
            return;
        }
        String credentialsFile = environment.getRequiredProperty("app.google.credentials-file");
        if (credentialsFile.isBlank()) throw new IllegalArgumentException("GOOGLE_CREDENTIALS_FILE is required when Google is enabled");
        try (var stream = Files.newInputStream(Path.of(credentialsFile))) {
            var credentials = GoogleCredentials.fromStream(stream).createScoped(List.of(SheetsScopes.SPREADSHEETS_READONLY));
            client = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials)).setApplicationName("WorkReportPlus").build();
        }
    }
    public boolean isEnabled() { return client != null; }
    private String resolve(String alias) {
        String id = environment.getProperty("app.google." + alias, alias);
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Missing Google sheet configuration: " + alias);
        return id;
    }
    public List<List<Object>> readSheet(String id, String range) throws IOException {
        if (!isEnabled()) return List.of();
        var values = client.spreadsheets().values().get(resolve(id), range).execute().getValues();
        return values == null ? List.of() : values;
    }
    public Spreadsheet readSheetWithGrid(String id, String range) throws IOException {
        if (!isEnabled()) return new Spreadsheet().setSheets(List.of());
        return client.spreadsheets().get(resolve(id)).setRanges(List.of(range)).setIncludeGridData(true).execute();
    }
}
