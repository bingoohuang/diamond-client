package org.n3r.diamond.client;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.Configurator;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.util.Loader;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;
import lombok.val;
import org.n3r.diamond.client.impl.DiamondSubscriber;

@AutoService(Configurator.class)
public class TestDiamondConfigurator extends ContextAwareBase implements Configurator {

    @SneakyThrows
    @Override
    public void configure(LoggerContext loggerContext) {
        DiamondSubscriber.getInstance().start();

        val myClassLoader = Loader.getClassLoaderOfObject(this);
        val xmlURL = Loader.getResource("logback-original.xml", myClassLoader);
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        configurator.doConfigure(xmlURL);
    }
}
