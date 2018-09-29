package org.n3r.diamond.client;

import org.junit.Test;
import org.n3r.diamond.client.impl.PropertiesBasedMiner;

import java.util.concurrent.TimeUnit;

public class PropertiesBaseMinerTest {

    @Test
    public void test() {
        Miner miner = new Miner();
        PropertiesBasedMiner pbm = (PropertiesBasedMiner)miner.getMiner("fco", "default");
        miner.getDiamondManager().addDiamondListener(pbm);
        while (true) {
            try {
                String host = pbm.getString("host");
                System.out.println(host);
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
