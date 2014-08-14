package org.n3r.diamond.client;

import java.util.concurrent.ExecutorService;

public abstract class DiamondListenerAdapter implements DiamondListener {
    @Override
    public ExecutorService getExecutor() {
        return null;
    }

}
