package xyz.destiall.tabheads;

import xyz.destiall.tabheads.core.TabLogger;

import java.util.logging.Logger;

public class JavaLogger implements TabLogger {
    private final Logger logger;

    public JavaLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warning(message);
    }

    @Override
    public void severe(String message) {
        logger.severe(message);
    }
}
