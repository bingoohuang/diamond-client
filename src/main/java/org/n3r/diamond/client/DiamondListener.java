package org.n3r.diamond.client;

import java.util.concurrent.ExecutorService;

public interface DiamondListener {
    default ExecutorService getExecutor() {
        return null;
    }

    void accept(DiamondStone diamondStone);
}
