package org.n3r.diamond.client.loglevel;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ClassUtils;
import org.joor.Reflect;

import java.util.List;

public class LoggerLevelChanger implements LoggerLevelChangable {
    private List<LoggerLevelChangable> impls = Lists.newArrayList();

    public LoggerLevelChanger() {
        try {
            ClassUtils.getClass("org.apache.log4j.Logger", false);
            String changerClassName = "org.n3r.diamond.client.loglevel.Log4jLevelChanger";
            LoggerLevelChangable changer = Reflect.on(changerClassName).create().get();
            impls.add(changer);
        } catch (ClassNotFoundException e) {
        }

        try {
            ClassUtils.getClass("ch.qos.logback.classic.Logger", false);
            String changerClassName = "org.n3r.diamond.client.loglevel.LogbackLevelChanger";
            LoggerLevelChangable changer = Reflect.on(changerClassName).create().get();
            impls.add(changer);
        } catch (ClassNotFoundException e) {
        }
    }

    @Override
    public void changeAll(LoggerLevel loggerLevel) {
        for (LoggerLevelChangable impl : impls) {
            impl.changeAll(loggerLevel);
        }
    }

    @Override
    public void change(String loggerName, LoggerLevel loggerLevel) {
        for (LoggerLevelChangable impl : impls) {
            impl.change(loggerName, loggerLevel);
        }
    }

    @Override
    public void changeSome(String loggerWildcard, LoggerLevel loggerLevel) {
        for (LoggerLevelChangable impl : impls) {
            impl.changeSome(loggerWildcard, loggerLevel);
        }
    }
}
