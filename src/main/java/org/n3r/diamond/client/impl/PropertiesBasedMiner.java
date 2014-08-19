package org.n3r.diamond.client.impl;

import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.AbstractMiner;

import java.util.Properties;

public class PropertiesBasedMiner extends AbstractMiner {
    private final Properties properties;

    public PropertiesBasedMiner(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getStone(String group, String dataId) {
        String key = StringUtils.isBlank(group) ? dataId : (group + "." + dataId);
        String property = properties.getProperty(key);

        return DiamondSubstituter.substitute(property, true);
    }

    @Override
    public String getDefaultGroupName() {
        return "";
    }
}
