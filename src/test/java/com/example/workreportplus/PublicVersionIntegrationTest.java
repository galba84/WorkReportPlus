package com.example.workreportplus;

import com.example.workreportplus.request.GroupReportRequest;
import com.example.workreportplus.request.RegionReportRequest;
import com.example.workreportplus.service.RegionReportService;
import com.example.workreportplus.service.GoogleSheetsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static com.example.jooq.tables.Users.USERS;
import static com.example.jooq.tables.Regionreport.REGIONREPORT;
import static com.example.jooq.tables.Groupreport.GROUPREPORT;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.jwt.secret=synthetic-test-signing-key-at-least-32-bytes", "app.google.enabled=false"})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
class PublicVersionIntegrationTest {
    @Container static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16.6-alpine");
    @DynamicPropertySource static void database(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username", POSTGRES::getUsername);
        properties.add("spring.datasource.password", POSTGRES::getPassword);
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired DSLContext dsl;
    @Autowired DataSource dataSource;
    @Autowired PasswordEncoder passwords;
    @Autowired RegionReportService reports;
    @Autowired GoogleSheetsService sheets;

    @BeforeEach void fixtures() {
        dsl.execute("TRUNCATE region, positions, unit, users CASCADE");
        new ResourceDatabasePopulator(new ClassPathResource("demo/data.sql")).execute(dataSource);
        addUser("admin@example.test", "ADMIN");
        addUser("reader@example.test", "USER");
        addUser("writer@example.test", "POWER_USER");
    }
    void addUser(String email, String role) {
        dsl.insertInto(USERS).set(USERS.EMAIL,email).set(USERS.NICKNAME,"Synthetic user")
                .set(USERS.ROLE,role).set(USERS.PASSWORD,passwords.encode("synthetic-password")) .execute();
    }
    String token(String email) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("email", email, "password", "synthetic-password"))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }
    @Test void publicRegistrationCannotEscalatePrivileges() throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType("application/json")
                .content(""" 
                    {"email":"guest@example.test","password":"synthetic-password","nickname":"Guest","role":"ADMIN"}
                    """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.user.role").value("GUEST"))
                .andReturn().getResponse().getContentAsString();
        String guestToken = json.readTree(response).get("token").asText();
        mvc.perform(post("/api/users").header("Authorization", "Bearer " + guestToken)
                .contentType("application/json").content("""
                    {"email":"guest@example.test","nickname":"Guest","role":"ADMIN"}
                    """)) .andExpect(status().isForbidden());
        mvc.perform(get("/api/regions").header("Authorization", "Bearer " + guestToken)).andExpect(status().isForbidden());
        assertThat(dsl.select(USERS.ROLE).from(USERS).where(USERS.EMAIL.eq("guest@example.test")).fetchOne(USERS.ROLE)).isEqualTo("GUEST");
    }
    @ParameterizedTest @ValueSource(strings={"/api/users", "/api/settings", "/api/audit-log"})
    void readerCannotAccessAdministration(String endpoint) throws Exception {
        mvc.perform(get(endpoint).header("Authorization", "Bearer " + token("reader@example.test")))
                .andExpect(status().isForbidden());
    }
    @Test void administratorCanManageUsersWithoutReturningPasswords() throws Exception {
        String auth = "Bearer " + token("admin@example.test");
        mvc.perform(post("/api/users").header("Authorization", auth).contentType("application/json")
                .content("""
                    {"email":"new@example.test","password":"synthetic-password","nickname":"New user","role":"USER"}
                    """)) .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(get("/api/users").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].password").doesNotExist());
        assertThat(passwords.matches("synthetic-password", dsl.select(USERS.PASSWORD).from(USERS)
                .where(USERS.EMAIL.eq("new@example.test")).fetchOne(USERS.PASSWORD))).isTrue();
        mvc.perform(post("/api/users").header("Authorization", auth).contentType("application/json")
                .content("""
                    {"email":"new@example.test","nickname":"Promoted user","role":"POWER_USER"}
                    """)) .andExpect(status().isOk());
        assertThat(dsl.select(USERS.ROLE).from(USERS).where(USERS.EMAIL.eq("new@example.test")).fetchOne(USERS.ROLE)).isEqualTo("POWER_USER");
        assertThat(token("new@example.test")).isNotBlank();
    }
    @Test void roleRevocationTakesEffectForExistingToken() throws Exception {
        String auth = "Bearer " + token("admin@example.test");
        dsl.update(USERS).set(USERS.ROLE,"GUEST").where(USERS.EMAIL.eq("admin@example.test")).execute();
        mvc.perform(get("/api/users").header("Authorization",auth)).andExpect(status().isForbidden());
    }
    @Test void invalidAndMissingTokensAreRejected() throws Exception {
        mvc.perform(get("/api/regions")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/regions").header("Authorization","Bearer invalid.token.value")).andExpect(status().isUnauthorized());
    }
    @Test void readerCannotSubmitButWriterCan() throws Exception {
        String request = json.writeValueAsString(report("Demo North", "Demo Alpha"));
        mvc.perform(post("/api/daily-work-report").contentType("application/json").content(request)
                .header("Authorization", "Bearer " + token("reader@example.test"))).andExpect(status().isForbidden());
        mvc.perform(post("/api/daily-work-report").contentType("application/json").content(request)
                .header("Authorization", "Bearer " + token("writer@example.test"))).andExpect(status().isOk());
    }
    @Test @WithMockUser(username="writer@example.test",roles="POWER_USER")
    void replacementPreservesFlagsAndDoesNotChangeOtherRegions() {
        UUID id = reports.saveReport(report("Demo North","Demo Alpha")).getId();
        var child = dsl.selectFrom(GROUPREPORT).where(GROUPREPORT.REGION_REPORT_ID.eq(id)).fetchSingle();
        assertThat(child.getWorked()).isTrue();
        assertThat(child.getAmmoVerified()).isTrue();
        assertThat(child.getStatus()).isTrue();
        assertThat(dsl.selectFrom(REGIONREPORT).where(REGIONREPORT.ID.eq(UUID.fromString("70000000-0000-0000-0000-000000000001"))).fetchSingle().getStatus()).isFalse();
        assertThat(dsl.selectFrom(GROUPREPORT).where(GROUPREPORT.ID.eq(UUID.fromString("80000000-0000-0000-0000-000000000001"))).fetchSingle().getStatus()).isFalse();
        assertThat(dsl.selectFrom(GROUPREPORT).where(GROUPREPORT.ID.eq(UUID.fromString("80000000-0000-0000-0000-000000000002"))).fetchSingle().getStatus()).isTrue();
        assertThat(dsl.selectFrom(REGIONREPORT).where(REGIONREPORT.ID.eq(UUID.fromString("70000000-0000-0000-0000-000000000002"))).fetchSingle().getStatus()).isTrue();
    }
    @Test @WithMockUser(username="writer@example.test",roles="POWER_USER")
    void lateChildFailureRollsBackParentChildrenAndPreviousVersionStatus() {
        RegionReportRequest request = report("Demo North", "Demo Alpha");
        GroupReportRequest wrongRegion = report("Demo South", "Demo Beta").getGroupReports().getFirst();
        request.setGroupReports(List.of(request.getGroupReports().getFirst(), wrongRegion));
        assertThatThrownBy(() -> reports.saveReport(request)).isInstanceOf(IllegalArgumentException.class);
        assertThat(dsl.fetchCount(REGIONREPORT)).isEqualTo(2);
        assertThat(dsl.fetchCount(GROUPREPORT)).isEqualTo(2);
        assertThat(dsl.fetchCount(REGIONREPORT, REGIONREPORT.STATUS.eq(true))).isEqualTo(2);
        assertThat(dsl.fetchCount(GROUPREPORT, GROUPREPORT.STATUS.eq(true))).isEqualTo(2);
    }
    @Test void concurrentSubmissionsLeaveOneActiveVersion() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> { start.await(); return reports.saveReport(report("Demo North","Demo Alpha")); });
            var second = executor.submit(() -> { start.await(); return reports.saveReport(report("Demo North","Demo Alpha")); });
            start.countDown();
            first.get(20, TimeUnit.SECONDS); second.get(20, TimeUnit.SECONDS);
        }
        assertThat(dsl.fetchCount(REGIONREPORT, REGIONREPORT.REGION_ID.eq(UUID.fromString("10000000-0000-0000-0000-000000000001")).and(REGIONREPORT.STATUS.eq(true)))).isEqualTo(1);
        assertThat(dsl.fetchCount(GROUPREPORT, GROUPREPORT.GROUP_ID.eq(UUID.fromString("20000000-0000-0000-0000-000000000001")).and(GROUPREPORT.STATUS.eq(true)))).isEqualTo(1);
    }
    @Test void googleDisabledRequiresNoCredentialsOrNetwork() throws Exception {
        assertThat(sheets.isEnabled()).isFalse();
        assertThat(sheets.readSheet(GoogleSheetsService.SHPS_TABLE_ID,"Example!A1:B2")).isEmpty();
        assertThat(sheets.readSheetWithGrid("unconfigured","Example!A1").getSheets()).isEmpty();
        mvc.perform(post("/api/admin/sync").header("Authorization","Bearer " + token("admin@example.test")))
                .andExpect(status().isServiceUnavailable());
    }
    @Test void demoReportCanBeReadAndExportedAsAnRtfDocument() throws Exception {
        String auth = "Bearer " + token("reader@example.test");
        mvc.perform(get("/api/daily-work-report/view/70000000-0000-0000-0000-000000000001")
                .header("Authorization",auth)).andExpect(status().isOk())
                .andExpect(jsonPath("$.groupReports[0].contractors.length()").value(2));
        byte[] bytes = mvc.perform(get("/api/daily-work-report/export/word/" + java.time.YearMonth.now())
                .param("templateName","Region Report")
                .param("regionId","10000000-0000-0000-0000-000000000001")
                .header("Authorization",auth)).andExpect(status().isOk())
                .andExpect(content().contentType("application/rtf"))
                .andReturn().getResponse().getContentAsByteArray();
        var kit = new javax.swing.text.rtf.RTFEditorKit();
        var document = kit.createDefaultDocument();
        kit.read(new java.io.ByteArrayInputStream(bytes), document, 0);
        assertThat(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)).startsWith("{\\rtf");
        assertThat(document.getText(0, document.getLength())).contains("Demo North", "Alex", "Example");
    }
    private RegionReportRequest report(String region, String group) {
        var child = new GroupReportRequest();
        child.setGroupName(group); child.setDescription("Synthetic integration report");
        child.setWorked(true); child.setAmmoVerified(true);
        var request = new RegionReportRequest();
        request.setRegionName(region); request.setReportDate(LocalDate.now());
        request.setRegionDescription("Synthetic regional report");
        request.setGroupReports(List.of(child));
        return request;
    }
}
