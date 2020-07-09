package org.n3r.diamond.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import static java.util.Objects.isNull;
import static org.slf4j.LoggerFactory.getLogger;

public class DiamondLogger {

    public static Logger log() {
        if (isNull(Instance.log)) {
            return getLogger("com.github.bingoohuang.diamondclient");
        }
        return Instance.log;
    }

    @Slf4j(topic = "com.github.bingoohuang.diamondclient")
    private static class Instance {}

    private DiamondLogger() {}
}
