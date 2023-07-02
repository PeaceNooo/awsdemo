# awsdemo

This application is a Homework Management system built using Spring Boot. 
The application is backed by DynamoDB for data persistence and S3 for storing homework files.

## API Documentation

### `GET /homeworks/{trainerId}/{homeworkId}`

This endpoint retrieves a homework with the given trainerId and homeworkId. The underlying service retrieves the homework from the DynamoDB table.

### `GET /homeworks/{trainerId}`

This endpoint retrieves all homeworks associated with the given trainerId. This is achieved by querying the DynamoDB table for items with the specified partition key.

### `POST /homeworks/{trainerId}`

This endpoint creates a new homework for the given trainerId. The details of the homework should be provided in the request body. Once the request is received, the service will generate a unique identifier for the homework, set the creation date, and persist the homework to the DynamoDB table.

### `DELETE /homeworks/{trainerId}/{homeworkId}`

This endpoint deletes a homework with the given trainerId and homeworkId. In addition to removing the homework from the DynamoDB table, if there's an associated file with the homework, it is also deleted from the S3 bucket.

### `GET /homeworks/{trainerId}/{homeworkId}/download`

This endpoint retrieves the file associated with a given homework. It will return a 302 redirect to the location of the file in the S3 bucket. And the s3 service will return the pre-signed url for 5 minutes duration. 

### `POST /homeworks/{trainerId}/{homeworkId}/upload`

This endpoint uploads a file for a given homework. The file should be included as a form-data parameter named `file`. The file is stored in the S3 bucket and the link is updated in the corresponding homework item in the DynamoDB table.

### `DELETE /homeworks/{trainerId}/{homeworkId}/delete`

This endpoint deletes the file associated with a given homework. The file is removed from the S3 bucket and the file path in the corresponding homework item in the DynamoDB table is also updated.

## AWS Services

This application uses the following AWS services: DynamoDB, S3, and IAM. The DynamoDB table is used to store homework items. The S3 bucket is used to store homework files. 
We used AWS SDK for Java 2.x to interact with these services. Use `DynamoDbTable` and `DynamoDbEnhancedClient`([related doc](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/ddb-en-client-getting-started-dynamodbTable.html)) to interact with DynamoDB and `S3Template` to interact with S3.

## References

- [Spring Cloud AWS 3.0.0](https://spring.io/blog/2023/05/02/announcing-spring-cloud-aws-3-0-0)
- [AWS Spring](https://github.com/awspring/spring-cloud-aws)
- [AWS Spring Doc](https://docs.awspring.io/spring-cloud-aws/docs/3.0.1/reference/html/index.html)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/home.html)