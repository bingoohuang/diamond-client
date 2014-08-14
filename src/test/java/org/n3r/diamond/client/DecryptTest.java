package org.n3r.diamond.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.DiamondUtils;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DecryptTest {
    @BeforeClass
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterClass
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void test1() {
        String password = DiamondUtils.tryDecrypt("{PBE}mzb7VnJcM/c=", "mypass");
        assertThat(password, is("secret"));
    }

    @Test
    public void test2() {
        MockDiamondServer.setConfigInfo("EqlConfig", "DEFAULT", "mypass={PBE}mzb7VnJcM/c=");
        Properties properties = DiamondMiner.getProperties("EqlConfig", "DEFAULT");
        assertThat(properties.getProperty("mypass"), is("secret"));

        MockDiamondServer.setConfigInfo("EqlConfig", "mypass", "{PBE}mzb7VnJcM/c=");
        String stone = DiamondMiner.getStone("EqlConfig", "mypass");
        assertThat(stone, is("secret"));
    }
}
