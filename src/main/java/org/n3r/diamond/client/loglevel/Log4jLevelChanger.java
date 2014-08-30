package org.n3r.diamond.client.loglevel;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Enumeration;

@SuppressWarnings("unchecked")
public class Log4jLevelChanger implements LoggerLevelChangable {
    private Level transToLog4j(LoggerLevel loggerLevel) {
        switch (loggerLevel) {
            case OFF:
                return Level.OFF;
            case TRACE:
                return Level.TRACE;
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
            case ALL:
                return Level.ALL;
        }

        throw new RuntimeException("should not reach here");
    }

    @Override
    public void changeAll(LoggerLevel loggerLevel) {
        Level newLevel = transToLog4j(loggerLevel);
        Enumeration<Logger> currentLoggers = LogManager.getCurrentLoggers();

        while (currentLoggers.hasMoreElements()) {
            changeToNewLevel(newLevel, currentLoggers.nextElement());
        }

        changeToNewLevel(newLevel, LogManager.getRootLogger());
    }

    @Override
    public void change(String loggerName, LoggerLevel loggerLevel) {
        Level newLevel = transToLog4j(loggerLevel);
        Logger logger = Logger.getLogger(loggerName);

        changeToNewLevel(newLevel, logger);
    }

    @Override
    public void changeSome(String loggerWildcard, LoggerLevel loggerLevel) {
        Level newLevel = transToLog4j(loggerLevel);
        Enumeration<Logger> currentLoggers = LogManager.getCurrentLoggers();

        while (currentLoggers.hasMoreElements()) {
            Logger logger = currentLoggers.nextElement();
            if (FilenameUtils.wildcardMatch(logger.getName(), loggerWildcard))
                changeToNewLevel(newLevel, logger);
        }

        Logger logger = LogManager.getRootLogger();
        if (FilenameUtils.wildcardMatch(logger.getName(), loggerWildcard))
            changeToNewLevel(newLevel, logger);
    }

    private void changeToNewLevel(Level newLevel, Logger logger) {
        if (logger.getLevel() == newLevel) return;
        logger.setLevel(newLevel);
    }
}
