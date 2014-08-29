package org.n3r.diamond.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondExtender;
import org.n3r.diamond.client.DiamondStone;

import java.util.List;

public class DiamondExtenderManager {
    public void loadDiamondExtenders() {
        String diamondExtendersConfig = ClientProperties.readDiamondExtenders();
        if (StringUtils.isEmpty(diamondExtendersConfig))
            diamondExtendersConfig = "@org.n3r.diamond.client.loglevel.LoggerLevelChangerExtender";

        List<DiamondExtender> diamondExtenders = DiamondUtils.parseObjects(diamondExtendersConfig, DiamondExtender.class);
        DiamondSubscriber diamondSubscriber = DiamondSubscriber.getInstance();
        for (DiamondExtender diamondExtender : diamondExtenders) {
            DiamondStone diamondStone = new DiamondStone();
            diamondStone.setDiamondAxis(diamondExtender.diamondAxis());
            diamondStone.setContent(diamondSubscriber.getDiamond(diamondExtender.diamondAxis(), 3000));
            diamondExtender.accept(diamondStone);

            diamondSubscriber.addDiamondListener(diamondExtender.diamondAxis(), diamondExtender);
        }
    }
}
