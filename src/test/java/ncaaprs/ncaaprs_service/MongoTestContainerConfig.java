package ncaaprs.ncaaprs_service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public abstract class MongoTestContainerConfig {

    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @BeforeAll
    static void startContainer() {
        mongoDBContainer.start();
        System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
    }
}
