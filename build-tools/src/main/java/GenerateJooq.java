import org.flywaydb.core.Flyway;
import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.testcontainers.containers.PostgreSQLContainer;

/** Builds the generated Java model from exactly the migrations shipped with the application. */
public final class GenerateJooq {
    public static void main(String[] args) throws Exception {
        try (var postgres = new PostgreSQLContainer<>("postgres:16.6-alpine")) {
            postgres.start();
            Flyway.configure().dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                    .locations("filesystem:" + args[0]).load().migrate();
            GenerationTool.generate(new Configuration()
                    .withJdbc(new Jdbc().withDriver("org.postgresql.Driver").withUrl(postgres.getJdbcUrl())
                            .withUser(postgres.getUsername()).withPassword(postgres.getPassword()))
                    .withGenerator(new Generator()
                            .withDatabase(new Database().withName("org.jooq.meta.postgres.PostgresDatabase")
                                    .withInputSchema("public").withExcludes("flyway_schema_history"))
                            .withGenerate(new Generate().withPojos(true).withDaos(true)
                                    .withGeneratedAnnotation(false))
                            .withTarget(new Target().withPackageName("com.example.jooq").withDirectory(args[1]))));
        }
    }
}
