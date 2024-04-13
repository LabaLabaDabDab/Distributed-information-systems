package com.manager.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableMongoRepositories(basePackages = "crackHashDB")
public class MongoConfig extends AbstractMongoClientConfiguration {
    private static final String CONNECTION_STRING = "mongodb://mongodb1:27018,mongodb2:27019,mongodb3:27020/?replicaSet=rs0&readConcernLevel=majority&writeConcernLevel=majority";
    private static final String DATABASE_NAME = "crackHashDB";


    @Override
    @NotNull
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();

        return MongoClients.create(mongoClientSettings);
    }


    @Override
    @NotNull
    protected String getDatabaseName() {
        return DATABASE_NAME;
    }

    @Override
    @NotNull
    public Collection getMappingBasePackages() {
        return Collections.singleton("crackHashDB");
    }

}
