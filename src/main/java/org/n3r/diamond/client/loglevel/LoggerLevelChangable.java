package org.n3r.diamond.client.loglevel;

public interface LoggerLevelChangable {
    void changeAll(LoggerLevel loggerLevel);

    void change(String loggerName, LoggerLevel loggerLevel);

    void changeSome(String loggerWildcard, LoggerLevel loggerLevel);
}
