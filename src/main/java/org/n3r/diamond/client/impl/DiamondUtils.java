package org.n3r.diamond.client.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.security.Pbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.n3r.diamond.client.impl.Constants.LINE_SEPARATOR;

public class DiamondUtils {
    private static Logger log = LoggerFactory.getLogger(DiamondUtils.class);

    public static Set<String> convertStringToSet(String modifiedDataIdsString) {
        if (StringUtils.isEmpty(modifiedDataIdsString)) return null;

        Set<String> modifiedDataIdSet = new HashSet<String>();

        try {
            modifiedDataIdsString = URLDecoder.decode(modifiedDataIdsString, "UTF-8");
        } catch (Exception e) {
            log.error("decode modifiedDataIdsString error", e);
        }

        if (log.isInfoEnabled() && modifiedDataIdsString != null) {
            String escaped = StringEscapeUtils.escapeJava(modifiedDataIdsString);
            if (!modifiedDataIdsString.startsWith("OK")) {
                log.info("changes detected {}", escaped);
            }
        }

        final String[] modifiedDataIdStrings = modifiedDataIdsString.split(LINE_SEPARATOR);
        for (String modifiedDataIdString : modifiedDataIdStrings) {
            if (!"".equals(modifiedDataIdString)) {
                modifiedDataIdSet.add(modifiedDataIdString);
            }
        }
        return modifiedDataIdSet;
    }

    public static boolean checkMd5(String configInfo, String md5) {
        String realMd5 = DigestUtils.md5Hex(configInfo);
        return realMd5 == null ? md5 == null : realMd5.equals(md5);
    }

    static Pattern encryptPattern = Pattern.compile("\\{(...)\\}");

    public static String tryDecrypt(String original, String dataId) {
        if (original == null) return null;

        Matcher matcher = encryptPattern.matcher(original);
        if (!matcher.find() || matcher.start() != 0) return original;

        String encrypted = original.substring(5);
        String algrithm = matcher.group(1);
        if ("PBE".equalsIgnoreCase(algrithm)) {
            return Pbe.decrypt(encrypted, dataId);
        }

        throw new RuntimeException(algrithm + " is not supported now");
    }

    public static Properties tryDecrypt(Properties properties, String dataId) {
        Properties newProperties = new Properties();

        for(Object okey : properties.keySet() ) {
            String key = (String) okey;
            String property = properties.getProperty(key);
            newProperties.put(key, tryDecrypt(property, key));
        }

        return newProperties;
    }
}
