package com.buschmais.test;

import java.io.File;
import java.io.PrintWriter;

import org.junit.jupiter.api.Test;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.log4j.Log4jLogProvider;

import static java.util.Collections.singletonList;
import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

class APOCEmbeddedTest {

    @Test
    void apoc() {
        File pluginDir = new File("target/neo4j/plugins");
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(new File("target/neo4j").toPath()).setConfig(
                        GraphDatabaseSettings.plugin_dir, pluginDir.getAbsoluteFile()
                                .toPath())
                .setConfig(GraphDatabaseSettings.procedure_unrestricted, singletonList("apoc.*"))
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
