package org.n3r.diamond.client.loglevel;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;

public class LogbackLevelChanger implements LoggerLevelChangable {
    private Level transToLogback(LoggerLevel loggerLevel) {
        switch (loggerLevel) {
            case DEBUG:
                return Level.DEBUG;
            case INFO:
                return Level.INFO;
            case WARN:
                return Level.WARN;
            case ERROR:
                return Level.ERROR;
        }

        throw new RuntimeException("should not reach here");
    }

    @Override
    public void changeAll(LoggerLevel loggerLevel) {
        ILoggerFactory loggerFactory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) return;

        Level newLevel = transToLogback(loggerLevel);
        for (Logger logger : ((LoggerContext) loggerFactory).getLoggerList()) {
            changeToNewLevel(newLevel, logger);
        }
    }

    @Override
    public void change(String loggerName, LoggerLevel loggerLevel) {
        Level newLevel = transToLogback(loggerLevel);
        Logger logger = (Logger) LoggerFactory.getLogger(loggerName);

        changeToNewLevel(newLevel, logger);
    }

    private void changeToNewLevel(Level newLevel, Logger logger) {
        if (logger.getLevel() == newLevel) return;
        logger.setLevel(newLevel);
    }

    @Override
    public void changeSome(String loggerPrefix, LoggerLevel loggerLevel) {
        ILoggerFactory loggerFactory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (!(loggerFactory instanceof LoggerContext)) return;

        Level newLevel = transToLogback(loggerLevel);
        for (Logger logger : ((LoggerContext) loggerFactory).getLoggerList()) {
            if (logger.getName().startsWith(loggerPrefix))
                changeToNewLevel(newLevel, logger);
        }
    }
}
