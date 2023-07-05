package com.example.awsdemo.config;

import io.awspring.cloud.dynamodb.DynamoDbTableNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration
public class DynamoDbConfig {
    /*
    * This is a custom DynamoDbTableNameResolver that converts the class name to the table name
    * The default DynamoDbTableNameResolver offered from the AWS Spring will convert the class name lowercase. "Homework" -> "homework"
    * This custom DynamoDbTableNameResolver will convert the class name as the table name. "Homework" -> "Homework"
    * */
    @Bean
    DynamoDbTableNameResolver dynamoDbTableNameResolver() {
        return new DynamoDbTableNameResolver() {
            @Override
            public <T> String resolve(Class<T> clazz) {
                Assert.notNull(clazz, "clazz is required");
                return clazz.getSimpleName().replaceAll("(.)(\\p{Lu})", "$1_$2");
            }
        };
    }
}
