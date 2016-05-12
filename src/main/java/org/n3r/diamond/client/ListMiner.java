package org.n3r.diamond.client;

import java.util.List;

import static org.n3r.diamond.client.impl.DiamondUtils.splitLinesWoComments;

public class ListMiner extends Miner {
    public ListMiner() {
        super();
    }

    public ListMiner(String defaultGroupName) {
        super(defaultGroupName);
    }

    public List<String> getStringList(String group, String dataId) {
        String stone = getStone(group, dataId);
        return splitLinesWoComments(stone, "#");
    }
}
