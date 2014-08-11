package org.n3r.diamond.client;

import org.junit.Test;
import org.n3r.diamond.client.impl.DiamondUtils;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DecryptTest {
    @Test
    public void test1() {
        String password = DiamondUtils.tryDecrypt("{PBE}mzb7VnJcM/c=", "mypass");
        assertThat(password, is("secret"));
    }

    @Test
    public void test2() {
        Properties properties = DiamondMiner.getProperties("EqlConfig", "DEFAULT");
        //assertThat(properties.getProperty("password"), is("libai123"));

        String stone = DiamondMiner.getStone("EqlConfig", "password");
        //assertThat(stone, is("libai123"));
    }
}
