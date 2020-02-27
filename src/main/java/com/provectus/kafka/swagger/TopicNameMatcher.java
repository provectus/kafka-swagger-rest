package com.provectus.kafka.swagger;

import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
@Setter
public class TopicNameMatcher {

    private Set<String> ignoreTopics = new HashSet<>();
    private Set<String> ignoreTopicsRegexp = new HashSet<>();

    private Set<Pattern> ignoreTopicsRegexpPatterns = new HashSet<>();

    public TopicNameMatcher(Set<String> ignoreTopics, Set<String> ignoreTopicsRegexp) {
        this.ignoreTopics = ignoreTopics;
        this.ignoreTopicsRegexp = ignoreTopicsRegexp;

        for (String ignoreTopicRegexp : ignoreTopicsRegexp) {
            ignoreTopicsRegexpPatterns.add(Pattern.compile(ignoreTopicRegexp));
        }
    }

    public TopicNameMatcher() {
    }

    public boolean matches(String name) {
        if (ignoreTopics.contains(name)) return true;

        for (Pattern pattern : ignoreTopicsRegexpPatterns) {
            if (pattern.matcher(name).matches()) return true;
        }

        return false;
    }
}
