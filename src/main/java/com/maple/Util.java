package com.maple;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Util {

    private static final Logger logger = LogManager.getLogger(Util.class);
    private static Robot screenRobot;
    public static String pathToResources = "src/main/resources/";

    public static void init() {
      try {
        screenRobot = new Robot();
      } catch (AWTException e) {
        logger.error("Could not create screen robot: {}", e.getMessage());
      }
    }

    public static BufferedImage takeScreenshot(String filename) {
        Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return takeScreenshot(screenRect.getX(), screenRect.getY(), screenRect.getWidth(), screenRect.getHeight(), filename);
    }

    public static BufferedImage takeScreenshot(double x, double y, double width, double height, String filename) {
      BufferedImage image = null;
      try {
        image = screenRobot.createScreenCapture(new Rectangle((int) x, (int) y, (int) width, (int) height));
        String currentTime = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        ImageIO.write(image, "png", new File(pathToResources + filename + "_" + currentTime + ".png"));
        return image;
      } catch (IOException e) {
        logger.error("Error taking screenshot: {}", e.getMessage());
      }
      return image;
    }

    public static int toGray(Color pixel) {
      int r = pixel.getRed();
      int g = pixel.getGreen();
      int b = pixel.getBlue();
      return (r + g + b) / 3;
    }

    public static BufferedImage read(String filename) {
        try {
            return ImageIO.read(new File(filename));
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage readScreenshot(String filename) {
      return read(pathToResources + "screenshots/" + filename);
    }

    public static BufferedImage readIndicator(String filename) {
      return read(pathToResources + "indicators/" + filename);
    }

    public static void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {

        }
    }
}
