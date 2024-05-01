package xyz.destiall.tabheads;

import org.slf4j.Logger;
import xyz.destiall.tabheads.core.TabLogger;

public class SLF4JLogger implements TabLogger {
    private final Logger logger;
    public SLF4JLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warning(String message) {
        logger.warn(message);
    }

    @Override
    public void severe(String message) {
        logger.error(message);
    }
}
