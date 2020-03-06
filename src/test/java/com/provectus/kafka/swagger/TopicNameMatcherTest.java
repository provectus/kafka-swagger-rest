package com.provectus.kafka.swagger;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicNameMatcherTest {

    private static final String _SCHEMAS = "_schemas";

    private static final String TOPIC_NAME_REGEXP_1 = "_.+";
    private static final String TOPIC_NAME_REGEXP_2 = "use.+";

    @Test
    public void testIgnoreTopics() {
        Set<String> ignoreNames = Set.of(_SCHEMAS);
        TopicNameMatcher topicNameMatcher = new TopicNameMatcher(ignoreNames, new HashSet<>());

        assertThat(topicNameMatcher.matches(_SCHEMAS)).isTrue();
    }

    @Test
    public void testIgnoreTopicsRegexp() {
        Set<String> ignoreTopicsRegexp = Set.of(TOPIC_NAME_REGEXP_1, TOPIC_NAME_REGEXP_2);
        TopicNameMatcher topicNameMatcher = new TopicNameMatcher(new HashSet<>(), ignoreTopicsRegexp);

        assertThat(topicNameMatcher.matches(_SCHEMAS)).isTrue();
        assertThat(topicNameMatcher.matches("users")).isTrue();
        assertThat(topicNameMatcher.matches("test")).isFalse();
    }
}
