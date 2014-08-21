package org.n3r.diamond.client.impl;

import com.google.common.collect.Lists;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.joor.Reflect;
import org.n3r.diamond.client.cache.ParamsAppliable;
import org.n3r.diamond.client.cache.Spec;
import org.n3r.diamond.client.cache.SpecParser;
import org.n3r.diamond.client.security.Pbe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.n3r.diamond.client.impl.Constants.LINE_SEPARATOR;

public class DiamondUtils {
    private static Logger log = LoggerFactory.getLogger(DiamondUtils.class);

    public static <T> T parseObject(String specContent, Class<T> clazz) {
        if (StringUtils.isBlank(specContent)) return null;

        try {
            Spec spec = SpecParser.parseSpecLeniently(specContent);
            return createObject(clazz, spec);
        } catch (Exception e) {
            log.error("parse object {} failed by {}", specContent, e.getMessage());
        }

        return null;
    }

    private static <T> T createObject(Class<T> clazz, Spec spec) {
        Object object = Reflect.on(spec.getName()).create().get();
        if (!clazz.isInstance(object)) return null;

        if (object instanceof ParamsAppliable)
            ((ParamsAppliable) object).applyParams(spec.getParams());

        return (T) object;
    }

    public static <T> List<T> parseObjects(String specContent, Class<T> clazz) {
        List<T> result = Lists.newArrayList();
        if (StringUtils.isBlank(specContent)) return result;

        try {
            Spec[] specs = SpecParser.parseSpecs(specContent);
            for (Spec spec : specs) {
                T object = createObject(clazz, spec);
                if (object != null) result.add(object);
            }
        } catch (Exception e) {
            log.error("parse object {} failed by {}", specContent, e.getMessage());
        }

        return result;
    }


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

    public static StringBuilder padding(String s, char letter, int repeats) {
        StringBuilder sb = new StringBuilder(s);
        while (repeats-- > 0) {
            sb.append(letter);
        }

        return sb;
    }

    public static String paddingBase64(String s) {
        return padding(s, '=', s.length() % 4).toString();
    }

    static Pattern encryptPattern = Pattern.compile("\\{(...)\\}");

    public static String tryDecrypt(String original, String dataId) {
        if (original == null) return null;

        Matcher matcher = encryptPattern.matcher(original);
        if (!matcher.find() || matcher.start() != 0) return original;

        String encrypted = original.substring(5);
        String algrithm = matcher.group(1);
        if ("PBE".equalsIgnoreCase(algrithm)) {
            return Pbe.decrypt(paddingBase64(encrypted), dataId);
        }

        throw new RuntimeException(algrithm + " is not supported now");
    }

    public static Properties tryDecrypt(Properties properties) {
        Properties newProperties = new Properties();

        for (String key : properties.stringPropertyNames()) {
            String property = properties.getProperty(key);
            newProperties.put(key, tryDecrypt(property, key));
        }

        return newProperties;
    }
}
