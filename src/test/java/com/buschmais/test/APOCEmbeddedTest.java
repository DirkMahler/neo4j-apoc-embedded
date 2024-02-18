package com.buschmais.test;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.log4j.Log4jLogProvider;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

class APOCEmbeddedTest {

    @Test
    void apoc() {
        Path neo4jDir = Paths.get("target/neo4j")
                .toAbsolutePath();
        Path pluginDir = neo4jDir.resolve("plugins");
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(neo4jDir).setConfig(GraphDatabaseSettings.plugin_dir, pluginDir)
                .setConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("apoc.*"))
                .setUserLogProvider(new Log4jLogProvider(System.out))
                .build();
        GraphDatabaseService graphDb = managementService.database(DEFAULT_DATABASE_NAME);
        try (Transaction transaction = graphDb.beginTx()) {
            try (Result result = transaction.execute("call apoc.help('apoc')")) {
                result.writeAsStringTo(new PrintWriter(System.out));
            }
        }
        managementService.shutdown();
    }

}
