package com.github.games647.colorconsole;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.logging.Level;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.bukkit.plugin.java.JavaPlugin;

public class ColorConsole extends JavaPlugin {

    private Layout<? extends Serializable> oldLayout;

    @Override
    public void onLoad() {
        //try to run it as early as possible
        installLogFormat();
    }

    @Override
    public void onEnable() {
        installLogFormat();
    }

    @Override
    public void onDisable() {
        Appender terminalAppender = getTerminalAppender();
        Logger rootLogger = ((Logger) LogManager.getRootLogger());

        ColorPluginAppender colorPluginAppender = null;
        for (Appender value : rootLogger.getAppenders().values()) {
            if (value instanceof ColorPluginAppender) {
                colorPluginAppender = (ColorPluginAppender) value;
                break;
            }
        }

        if (colorPluginAppender != null) {
            rootLogger.removeAppender(terminalAppender);
            rootLogger.addAppender(colorPluginAppender.getOldAppender());
        }

        setLayout(oldLayout);
    }

    private void installLogFormat() {
        Appender terminalAppender = getTerminalAppender();

        oldLayout = terminalAppender.getLayout();
        PatternLayout layout = PatternLayout
                .createLayout("%highlight{[%d{HH:mm:ss} %-5level]: %msg%n}{FATAL=red blink, ERROR=red, "
                        + "WARN=yellow bold, INFO=gray, DEBUG=green bold, TRACE=blue}", new DefaultConfiguration()
                        , null, Charset.defaultCharset().name(), "true");
        setLayout(layout);

        Logger rootLogger = ((Logger) LogManager.getRootLogger());

        ColorPluginAppender pluginAppender = new ColorPluginAppender(terminalAppender);
        pluginAppender.start();

        rootLogger.removeAppender(terminalAppender);
        rootLogger.addAppender(pluginAppender);
    }

    private void setLayout(Layout<? extends Serializable> layout) {
        Appender terminalAppender = getTerminalAppender();

        try {
            Field field = terminalAppender.getClass().getSuperclass().getDeclaredField("layout");
            field.setAccessible(true);
            field.set(terminalAppender, layout);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Failed to install log format", ex);
        }
    }

    private Appender getTerminalAppender() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration conf = ctx.getConfiguration();

        return conf.getAppenders().get("TerminalConsole");
    }
}
