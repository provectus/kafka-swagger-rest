package com.provectus.kafka.model.config;

import java.util.HashMap;
import java.util.Map;

public class MapParameters {

    public static Map<String, Object> mergeMapParams(Map<String, Object> params, String prefix, String delimiter) {
        Map<String, Object> resultParams = new HashMap<>();

        for (String key: params.keySet()) {
            Object value = params.get(key);

            if (value instanceof HashMap) {
                Map<String, Object> mergedParams = mergeMapParams((Map<String, Object>) value, prefix + key + delimiter, delimiter);
                resultParams.putAll(mergedParams);
            } else {
                resultParams.putIfAbsent(prefix + key, value);
            }
        }

        return resultParams;
    }

    public static Map<String, Object> mergeMapParams(Map<String, Object> params, String delimiter) {
        return mergeMapParams(params, "", delimiter);
    }

    public static Map<String, Object> mergeMapParams(Map<String, Object> params) {
        return mergeMapParams(params, "", ".");
    }
}
