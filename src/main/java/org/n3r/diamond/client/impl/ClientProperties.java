package org.n3r.diamond.client.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Sets;
import com.google.common.net.HostAndPort;
import org.apache.commons.lang3.StringUtils;

import java.util.Properties;
import java.util.Set;

import static org.n3r.diamond.client.impl.DiamondLogger.log;

public class ClientProperties {
    static Properties properties = Props.tryProperties("diamond-client.properties", ".diamond-client");

    public static HostAndPort readNameServerAddresses() {
        String nameServerAddress = properties.getProperty(Constants.NAME_SERVER_ADDRESS);
        if (StringUtils.isNotEmpty(nameServerAddress)) {
            return HostAndPort.fromString(nameServerAddress)
                    .withDefaultPort(Constants.DEFAULT_NAME_SERVER_PORT);
        }

        return HostAndPort.fromParts(Constants.DEFAULT_DIAMOND_SERVER_NAME,
                Constants.DEFAULT_NAME_SERVER_PORT);
    }

    public static boolean isPureLocalMode() {
        String pureLocalMode = properties.getProperty(Constants.PureLocalMode);
        return pureLocalMode != null && (pureLocalMode.equals("yes") || pureLocalMode.equals("on"));
    }

    public static Set<String> readDiamondServersAddress() {
        String diamondServersAddress = properties.getProperty(Constants.SERVER_ADDRESS, "");
        Splitter splitter = Splitter.onPattern("\\s+").omitEmptyStrings().trimResults();
        Set<String> addresses = Sets.newHashSet(splitter.splitToList(diamondServersAddress));

        if (addresses.size() > 0)
            log().info("got diamond servers {} from config {}", addresses, Constants.SERVER_ADDRESS);

        return addresses;
    }

    public static NameServerMode readNameServerMode() {
        String nameServerAddress = properties.getProperty(Constants.NAME_SERVER_ADDRESS);
        if (StringUtils.isNotBlank(nameServerAddress)) return NameServerMode.ByAddressProperty;

        String serverAddress = properties.getProperty(Constants.SERVER_ADDRESS);
        if (StringUtils.isNotBlank(serverAddress)) return NameServerMode.Off;

        return NameServerMode.ByEtcHosts;
    }

    public static String readDiamondExtenders() {
        return properties.getProperty(Constants.DIAMOND_EXTENDERS);
    }

    public static String getBasicAuth() {
        return properties.getProperty("BasicAuth");
    }

    public enum NameServerMode {
        Off, ByEtcHosts, ByAddressProperty
    }
}
