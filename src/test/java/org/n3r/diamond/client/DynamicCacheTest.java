package org.n3r.diamond.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class DynamicCacheTest {
    @BeforeClass
    public static void beforeClass() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterClass
    public static void afterClass() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testNullJson() {
        String json = JSON.toJSONString(null, SerializerFeature.WriteClassName);
        assertThat(json, is("null"));
    }

    @Test
    public void test() throws ExecutionException, InterruptedException, IOException {
        String filePath = System.getProperty("user.home") + File.separator + ".diamond-client";
        File file = new File(filePath + "/snapshot/DEFAULT_GROUP/dynamicCache.cache.18446744071658618532");
        FileUtils.writeStringToFile(file, "\"13:12:33.435\"");

        MockDiamondServer.setConfigInfo("dynamicCache", "@org.n3r.diamond.client.DemoDynamicUpdater");
        String now = new Miner().getDynamicCache("dynamicCache", "HH:mm:ss.SSS");
        assertThat(now, is("13:12:33.435"));
        String now2 = new Miner().getDynamicCache("dynamicCache", "HH:mm:ss.SSS");
        assertThat(now2, is(not("13:12:33.435")));

        Future<Object> future = MockDiamondServer.updateDiamond("dynamicCache", "@org.n3r.diamond.client.DemoDynamicUpdater @XXX");
        future.get();
        Utils.sleepMillis(100);
        String now3 = new Miner().getDynamicCache("dynamicCache", "HH:mm:ss.SSS");
        assertThat(now3, is(not(now2)));

        MockDiamondServer.setConfigInfo("staticCache", "@org.n3r.diamond.client.DemoUpdater(xx,yy)");
        DemoCacheBean bean = new Miner().getCache("staticCache");
        System.out.println(bean);
    }
}
