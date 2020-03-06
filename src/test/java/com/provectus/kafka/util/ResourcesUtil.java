package com.provectus.kafka.util;

import org.cactoos.io.ResourceOf;
import org.cactoos.text.TextOf;

public class ResourcesUtil {

    public static String readResource(String path) {
        return new TextOf(new ResourceOf(path)).toString();
    }
}
