package com.maple;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class App {
    private static final Logger logger = LogManager.getLogger(App.class);
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    public static void main(String[] args) {
      logger.info("starting");
      Util.init();
      ImageProcessor imageProcessor = new ImageProcessor(WIDTH, HEIGHT);
      imageProcessor.init();
      imageProcessor.execute();
    }
}
