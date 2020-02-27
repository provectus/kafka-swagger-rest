package com.provectus.kafka.controller;

import com.provectus.kafka.container.KafkaTestEnvironmentContainer;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class KafkaControllerTest {

    @Autowired
    KafkaTestEnvironmentContainer kafkaTestEnvironmentContainer;

    @Before
    public void before() {

    }
}
