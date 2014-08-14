package org.n3r.diamond.client;

import java.util.concurrent.ExecutorService;

public interface DiamondListener {
    ExecutorService getExecutor();

    void accept(DiamondStone diamondStone);
}
