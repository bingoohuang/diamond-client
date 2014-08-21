package org.n3r.diamond.client.impl;

import com.google.common.base.Splitter;
import com.google.common.net.HostAndPort;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static java.io.File.separator;

public class ClientProperties {
    private static Logger log = LoggerFactory.getLogger(ClientProperties.class);

    private static Properties properties = new Properties();

    static {
        InputStream is = null;
        try {
            is = toInputStreamFromCdOrClasspath("diamond-client.properties", true);
            if (is != null) properties.load(is);
        } catch (IOException e) {
            log.error("load properties error", e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static HostAndPort readNameServerAddress() {
        String nameServerAddress = properties.getProperty(Constants.NAME_SERVER_ADDRESS);
        if (StringUtils.isNotEmpty(nameServerAddress)) {
            return HostAndPort.fromString(nameServerAddress).withDefaultPort(Constants.DEF_NAMESERVER_PORT);
        }

        return HostAndPort.fromParts(Constants.DEF_DOMAINNAME, Constants.DEF_NAMESERVER_PORT);
    }

    public static List<String> readDiamondServersAddress() {
        String diamondServersAddress = properties.getProperty(Constants.SERVER_ADDRESS, "");
        Splitter splitter = Splitter.onPattern("\\s+").omitEmptyStrings().trimResults();
        List<String> addresses = splitter.splitToList(diamondServersAddress);

        if (addresses.size() > 0)
            log.info("got diamond servers {} from config {}", addresses, Constants.SERVER_ADDRESS);

        return addresses;
    }

    public static NameServerMode readNameServerMode() {
        String nameServerAddress = properties.getProperty(Constants.NAME_SERVER_ADDRESS);
        if (StringUtils.isNotBlank(nameServerAddress)) return NameServerMode.ByAddressProperty;

        String serverAddress = properties.getProperty(Constants.SERVER_ADDRESS);
        if (StringUtils.isNotBlank(serverAddress)) return NameServerMode.Off;

        return NameServerMode.ByEtcHosts;
    }

    public static String getBasicAuth() {
        return properties.getProperty("BasicAuth");
    }

    public static enum NameServerMode {
        Off, ByEtcHosts, ByAddressProperty
    }

    public static InputStream toInputStreamFromCdOrClasspath(String pathname, boolean silent) {
        InputStream is = readFileFromCurrentDir(new File(pathname));
        if (is != null) return is;

        is = readFileFromDiamondClientHome(pathname);
        if (is != null) return is;

        is = getClassPathResourceAsStream(pathname);
        if (is != null || silent) return is;

        throw new RuntimeException("fail to find " + pathname + " in current dir or classpath");
    }

    private static InputStream readFileFromDiamondClientHome(String pathname) {
        String filePath = System.getProperty("user.home") + separator + ".diamond-client";
        File dir = new File(filePath);
        if (!dir.exists()) return null;

        File file = new File(dir, pathname);

        return readFileFromCurrentDir(file);
    }

    private static InputStream readFileFromCurrentDir(File file) {
        if (!file.exists()) return null;

        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            // This should not happened
            log.error("read file {} error", file, e);
            return null;
        }
    }

    public static InputStream getClassPathResourceAsStream(String resourceName) {
        return ClientProperties.class.getClassLoader().getResourceAsStream(resourceName);
    }

}
