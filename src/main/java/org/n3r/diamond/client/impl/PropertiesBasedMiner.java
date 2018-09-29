package org.n3r.diamond.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.*;

import java.util.Properties;

public class PropertiesBasedMiner extends AbstractMiner implements DiamondListener {
    private  Properties properties;

    public PropertiesBasedMiner(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStone(String group, String dataId) {
        String key = StringUtils.isBlank(group) ? dataId : (group + "." + dataId);
        String property = properties.getProperty(key);

        return DiamondSubstituter.substitute(property, true, group, dataId, null);
    }

    @Override
    public String getDefaultGroupName() {
        return "";
    }

    @Override
    public void accept(DiamondStone diamondStone) {
        Properties properties = DiamondUtils.parseStoneToProperties(diamondStone.getContent());
        this.properties = properties;
    }
}
