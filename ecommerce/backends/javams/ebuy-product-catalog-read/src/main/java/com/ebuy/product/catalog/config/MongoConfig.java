package com.ebuy.product.catalog.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.lang.NonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return "ebuy_db";
    }

    @Override
    @NonNull
    public MongoClient mongoClient() {
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> {
                    builder.maxSize(50);
                    builder.minSize(5);
                })
                .applyToSocketSettings(builder -> {
                    builder.connectTimeout(3, TimeUnit.SECONDS);
                    builder.readTimeout(3, TimeUnit.SECONDS);
                })
                .build();
        return MongoClients.create(settings);
    }

    @Override
    @NonNull
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new BinaryToUUIDConverter(),
                new UUIToBinaryConverter()
        ));
    }

    // Convert MongoDB Binary (UUID) to Java UUID
    static class BinaryToUUIDConverter implements Converter<Binary, UUID> {
        @Override
        public UUID convert(@NonNull Binary source) {
            if (source == null) return null;

            byte[] data = source.getData();
            if (data.length == 16) {
                ByteBuffer bb = ByteBuffer.wrap(data);
                long mostSigBits = bb.getLong();
                long leastSigBits = bb.getLong();
                return new UUID(mostSigBits, leastSigBits);
            }

            throw new IllegalArgumentException("Binary data is not a valid UUID format");
        }
    }

    // Convert Java UUID to MongoDB Binary
    static class UUIToBinaryConverter implements Converter<UUID, Binary> {
        @Override
        public Binary convert(@NonNull UUID source) {
            if (source == null) return null;

            ByteBuffer bb = ByteBuffer.allocate(16);
            bb.putLong(source.getMostSignificantBits());
            bb.putLong(source.getLeastSignificantBits());
            return new Binary(bb.array());
        }
    }
}