package com.buschmais.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

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
    void apoc() throws IOException {
        Path neo4jDir = Paths.get("target/neo4j")
                .toAbsolutePath();
        Path pluginDir = neo4jDir.resolve("plugins");

        prepareClassloader(pluginDir, ClassLoader.getSystemClassLoader());

        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(neo4jDir).setConfig(GraphDatabaseSettings.plugin_dir, pluginDir)
                .setConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("*"))
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

    private static void prepareClassloader(Path pluginDir, ClassLoader classLoader) throws IOException {
        Consumer<Path> consumer = getClasspathAppender(classLoader);
        Files.find(pluginDir, 1, (p, a) -> p.getFileName()
                        .toString()
                        .endsWith(".jar"))
                .forEach(file -> consumer.accept(file));
    }

    private static Consumer<Path> getClasspathAppender(ClassLoader classLoader) {
        try {
            return getURLClasspathAppender(classLoader);
        } catch (NoSuchMethodException e) {
            return getInstrumentationClasspathAppender(classLoader);
        }
    }

    private static Consumer<Path> getURLClasspathAppender(ClassLoader classLoader) throws NoSuchMethodException {
        Method method = classLoader.getClass()
                .getDeclaredMethod("addURL", URL.class);
        method.setAccessible(true);
        return path -> {
            try {
                method.invoke(classLoader, path.toUri()
                        .toURL());
            } catch (ReflectiveOperationException | MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        };
    }

    private static Consumer<Path> getInstrumentationClasspathAppender(ClassLoader classLoader) {
        try {
            Method method = classLoader.getClass()
                    .getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            method.setAccessible(true);
            return path -> {
                try {
                    method.invoke(classLoader, path.toAbsolutePath()
                            .toString());
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            };
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
