package org.n3r.diamond.client;

import org.junit.Test;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LogLevelChangerTest {

    @Test
    public void test() throws InterruptedException {
        DiamondSubscriber.getInstance().start();
        new Miner().getString("some");
        Logger logger = LoggerFactory.getLogger(LogLevelChangerTest.class);

        /*while (true)*/ {
            logger.debug("this is debug information");
            logger.info("this is info information");
            logger.warn("this is warn information");
            logger.error("this is error information");

            TimeUnit.SECONDS.sleep(3);
        }
    }
}
