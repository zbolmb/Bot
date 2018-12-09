package com.maple;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessor {

  private static final Logger logger = LogManager.getLogger(ImageProcessor.class);
  private int RETRY_COUNT = 10;
  private int WIDTH;
  private int HEIGHT;
  private Robot screenRobot;
  private Pos mapleLoc;
  private ImageBank imageBank;

  public ImageProcessor(int width, int height) {
    WIDTH = width;
    HEIGHT = height;
    imageBank = new ImageBank();
  }

  public void execute() {
    // BufferedImage image = getMapleScreenshot();
    testFindLoc();
  }

  public void init() {
    try {
      screenRobot = new Robot();
    } catch (AWTException e) {
      logger.error("Error creating screen robot: {}", e.getMessage());
    }
  }

  private BufferedImage getMapleScreenshot() {
    int count = 0;
    while (mapleLoc == null && count < RETRY_COUNT) {
      logger.info("Attempting to locate Maplestory Window ...");
      System.out.println("retaking screenshot for maplestory");
      Util.pause(500);
      mapleLoc = findLoc(Util.takeScreenshot("full"), imageBank.mapleIcon);
      Util.pause(500);
      if (mapleLoc == null) {
        logger.info("Could not found Maplestory Window, Retrying ...");
        count++;
      }
    }
    if (mapleLoc == null) {
      logger.info("Could not find Maplestory Window ...");
      return null;
    } else {
      // mapleIcon.getHeight() + 3 needs to be tweaked
      return Util.takeScreenshot(mapleLoc.x, mapleLoc.y + imageBank.mapleIcon.getHeight() + 3, WIDTH, HEIGHT, "maplestory_window");
    }
  }

  private Pos findLoc(BufferedImage source, BufferedImage target) {
    double minSAD = Integer.MAX_VALUE;
    Pos bestPair = null;
    int w1 = source.getWidth();
    int h1 = source.getHeight();
    int w2 = target.getWidth();
    int h2 = target.getHeight();
    // Loop through source image
    for (int x1 = 0; x1 <= w1 - w2; x1++) {
      loop:
      for (int y1 = 0; y1 <= h1 - h2; y1++) {
        double SAD = 0.0;
        // Loop through target image
        for (int x2 = 0; x2 < w2; x2++) {
          for (int y2 = 0; y2 < h2; y2++) {
            int p_source = Util.toGray(new Color(source.getRGB(x1 + x2, y1 + y2)));
            int p_target = Util.toGray(new Color(target.getRGB(x2, y2)));
            // SAD += Math.abs(p_source - p_target);
            if (p_source != p_target) {
              continue loop;
            }
          }
        }
        logger.info("Image found");
        return new Pos(x1, y1);
        // // save best position
        // if (SAD < minSAD) {
        //   minSAD = SAD;
        //   bestPair = new Pos(x1, y1);
        // }
      }
    }
    logger.info("Could not find image");
    return null;
  }

  private void testFindLoc() {
    logger.info("starting");
    BufferedImage source = Util.readScreenshot("test1.png");
    logger.debug("Read source");
    BufferedImage target = Util.readIndicator("fantasythemepark.png");
    logger.debug("Read target");
    Pos loc = findLoc(source, target);
    logger.info("Target found at (" + loc.x + ", " + loc.y + ")");
  }
}
