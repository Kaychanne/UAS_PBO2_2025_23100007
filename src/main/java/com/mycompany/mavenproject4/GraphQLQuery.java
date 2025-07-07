/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mavenproject4;

/**
 *
 * @author ASUS
 */
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLQuery {
    public static GraphQL init() throws IOException {
        InputStream schemaStream = GraphQLQuery.class.getClassLoader().getResourceAsStream("schema.graphqls");

        if (schemaStream == null) {
            throw new RuntimeException("schema.graphqls not found in classpath.");
        }

        String schema = new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);

        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schema);
        RuntimeWiring wiring = RuntimeWiring.newRuntimeWiring()
            .type("Query", builder -> builder
                .dataFetcher("allVisitLogs", env -> VisitLogMahasiswa.findAll())
                .dataFetcher("VisitLogById", env -> {
                    Long id = Long.parseLong(env.getArgument("id").toString());
                    return VisitLogMahasiswa.findById(id);
                })
            )
            .type("Mutation", builder -> builder
                .dataFetcher("addVisitLog", env -> VisitLogMahasiswa.add(
                    env.getArgument("studentId"),
                    env.getArgument("studentName"),
                    env.getArgument("studyProgram"),
                    env.getArgument("purpose"),
                    env.getArgument("visitTime")
                ))
                .dataFetcher("updateVisitLog", env -> {
                    Long id = Long.parseLong(env.getArgument("id").toString());
                    String studentId = env.getArgument("studentId");
                    String studentName = env.getArgument("studentName");
                    String studyProgram = env.getArgument("studyProgram");
                    String purpose = env.getArgument("purpose");
                    LocalDateTime visitTime = env.getArgument("visitTime");
                    return VisitLogMahasiswa.update( id,  studentId,  studentName,  studyProgram,  purpose,  visitTime);
                })
                .dataFetcher("deleteProduct", env -> {
                    Long id = Long.parseLong(env.getArgument("id").toString());
                    return VisitLogMahasiswa.delete(id);
                })
            )
            .build();

        GraphQLSchema schemaFinal = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring);
        return GraphQL.newGraphQL(schemaFinal).build();
    }
}
