package com.maple;

import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author JingJing
 */
public class PropertiesReader {

  private static PropertiesReader instance = null;
  private Properties props = new Properties();
  private static Logger logger = LogManager.getLogger(PropertiesReader.class);

  private PropertiesReader() {
    InputStream input;
    try {
      // Load properties
      input = this.getClass().getResourceAsStream("configuration.properties");
      props.load(input);
      input.close();
    } catch (Exception e) {
      logger.error("Error reading properties:{}", e.getMessage());
    }
  }

  public static PropertiesReader instance() {
    if (instance == null) {
      instance = new PropertiesReader();
    }
    return instance;
  }

  public Properties getProperties() {
    return props;
  }
}
