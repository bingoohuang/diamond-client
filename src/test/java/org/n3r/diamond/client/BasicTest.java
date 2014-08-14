package org.n3r.diamond.client;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BasicTest {
    @BeforeClass
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterClass
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void test() {
        MockDiamondServer.setConfigInfo("SOLR_URL", "abc");
        String solrUrl1 = DiamondMiner.getString("SOLR_URL");
        String solrUrl2 = DiamondMiner.getStone("DEFAULT_GROUP", "SOLR_URL");

        assertThat(solrUrl1, is("abc"));
        assertThat(solrUrl2, is("abc"));
    }
}
