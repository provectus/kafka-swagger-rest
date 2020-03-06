package com.provectus.kafka.container;

import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.Network;

public class ZookeeperContainer extends FixedHostPortGenericContainer<ZookeeperContainer> {

    public ZookeeperContainer(String imageVersion) {
        super("confluentinc/cp-zookeeper:" + imageVersion);

        withNetwork(Network.SHARED);

        withExposedPorts(2181);
        withFixedExposedPort(2181, 2181);
        withEnv("ZOOKEEPER_CLIENT_PORT", "2181");
        withEnv("ZOOKEEPER_TICK_TIME", "2000");
    }
}