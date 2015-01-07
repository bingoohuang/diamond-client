package org.n3r.diamond.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SubstituteTest {
    @BeforeClass
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterClass
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void test0() {
        MockDiamondServer.setConfigInfo("g", "d1", "bingoo");
        MockDiamondServer.setConfigInfo("g", "d2", "${g^d1} huang");
        String string = new Miner().getStone("g", "d2");
        assertThat(string, is(equalTo("bingoo huang")));
    }

    @Test
    public void test1() {
        MockDiamondServer.setConfigInfo("g", "d", "name=bingoo\nfull=${this.name} huang");
        String string = new Miner().getMiner("g", "d").getString("full");
        assertThat(string, is(equalTo("bingoo huang")));
    }

    @Test
    public void test01() {
        MockDiamondServer.setConfigInfo("g", "d1", "bingoo");
        MockDiamondServer.setConfigInfo("g", "d", "name=${g^d1}\nfull=${this.name} huang\nlong=${g^d^full} buddha");

        String string = new Miner().getMiner("g", "d").getString("full");
        assertThat(string, is(equalTo("bingoo huang")));

        string = new Miner().getMiner("g", "d").getString("long");
        assertThat(string, is(equalTo("bingoo huang buddha")));
    }

    @Test
    public void test2() {
        MockDiamondServer.setConfigInfo("g", "d",
                "name=bingoo\nfull=${this.name} huang\nlong=${this.full} buddha");
        String string = new Miner().getMiner("g", "d").getString("long");
        assertThat(string, is(equalTo("bingoo huang buddha")));
    }

    @Test
    public void test3() {
        MockDiamondServer.setConfigInfo("g", "d",
                "name=bingoo\nfull=${g^d^name} huang\nlong=${g^d^full} buddha");
        String string = new Miner().getMiner("g", "d").getString("long");
        assertThat(string, is(equalTo("bingoo huang buddha")));
    }
}
