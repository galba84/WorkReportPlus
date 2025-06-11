package com.example.workreportplus.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GoogleSheetsService {
    public static final String SHPS_TABLE_ID = "1LkkLuk7y_fB8BTa-T4kYPYTgT-cVhIIgJTlZyo2b0UA";
    public static final String DOVIDNYK_TABLE_ID = "1z78PLdhrabCpJR1fQfCW28d9FOE8B8YHvgq-aStBkss";
    public static final String OPERATIVE_BPLA_TABLE_ID = "1YtvuAvmyZ5JZFG9uQTy3GF38oHJpQsHRAjAGOl7ioF8";
    public static final String OPERATIVE_REB_TABLE_ID = "1je5TCzCRfCY-1dcpFrqLk7eCVjiZABgZfzd2POMMCGA";
    public static final String ZAPORIZZIA_100_30_TABLE_ID = "1QzRXeAevTEor857Gp2jY5JlYUvUgcgjyDjxJPB6eHmU";

    public static final Map<String, String> regionTo100_30_Map = Map.of(
            "a0e76880-4ff6-4b0b-b6a8-2e4ca73b481e", ZAPORIZZIA_100_30_TABLE_ID
    );


    private static final String APPLICATION_NAME = "WorkReportPlus";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private Sheets sheetsService;

    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        // Load service account credentials from resources
        try (InputStream credentialsStream = getClass().getResourceAsStream("/credentials.json")) {
            if (credentialsStream == null) {
                throw new IOException("credentials.json not found in resources folder");
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));

            sheetsService = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JSON_FACTORY,
                    new HttpCredentialsAdapter(credentials)
            ).setApplicationName(APPLICATION_NAME).build();
        }
    }

    public List<List<Object>> readSheet(String spreadsheetId, String range) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        return response.getValues();
    }

    public Spreadsheet readSheetWithGrid(String spreadsheetId, String range) throws IOException {
        Spreadsheet result = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .setRanges(List.of(range))
                .setIncludeGridData(true)
                .execute();

        return result;
    }
}
