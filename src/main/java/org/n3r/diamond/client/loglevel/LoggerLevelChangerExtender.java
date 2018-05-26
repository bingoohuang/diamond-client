package org.n3r.diamond.client.loglevel;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondExtender;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondStone;

import java.util.Properties;

import static org.n3r.diamond.client.impl.DiamondUtils.parseStoneToProperties;

@Slf4j
public class LoggerLevelChangerExtender implements DiamondExtender, DiamondListener {
    LoggerLevelChanger loggerLevelChanger = new LoggerLevelChanger();

    @Override
    public DiamondAxis diamondAxis() {
        return DiamondAxis.makeAxis("diamond.extender", "logger.levels");
    }

    @Override
    public void accept(DiamondStone diamondStone) {
        Properties nameAndLevels = parseStoneToProperties(diamondStone.getContent());

        for (String loggerName : nameAndLevels.stringPropertyNames()) {
            String level = nameAndLevels.getProperty(loggerName);
            LoggerLevel loggerLevel = parseLoggerLevel(level);
            if (loggerLevel == null) continue;

            if ("_all_".equals(loggerName)) {
                loggerLevelChanger.changeAll(loggerLevel);
            } else if (loggerName.indexOf('*') >= 0 || loggerName.indexOf('?') >= 0) {
                String loggerWildcard = loggerName.substring(0, loggerName.length() - 1);
                loggerLevelChanger.changeSome(loggerWildcard, loggerLevel);
            } else {
                loggerLevelChanger.change(loggerName, loggerLevel);
            }
        }
    }

    private LoggerLevel parseLoggerLevel(String level) {
        if (StringUtils.isBlank(level)) return null;

        try {
            return LoggerLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("log level {} is invalid", level);
            return null;
        }
    }
}
