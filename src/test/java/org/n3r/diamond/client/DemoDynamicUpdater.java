package org.n3r.diamond.client;

import org.n3r.diamond.client.cache.DynamicsAppliable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;

public class DemoDynamicUpdater implements Callable<String>, DynamicsAppliable {
    private String format = "yyyy-MM-dd HH:mm:ss.SSS";

    @Override
    public String call() {
        Utils.sleepMillis(3500);
        return new SimpleDateFormat(format).format(new Date());
    }

    @Override
    public void setDynamics(Object... dynamics) {
        if (dynamics.length > 0 && dynamics[0] instanceof String)
            this.format = (String)dynamics[0];
    }
}
