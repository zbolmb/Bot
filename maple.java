import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.*;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javafx.scene.*;
import javafx.scene.text.*;

import java.io.*;
import java.util.*;
import java.util.function.BooleanSupplier;

import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import javax.imageio.ImageIO;

/**
 * A few unfortunate noteables:
   1) Your minimap must be BIG (meaning you have to be able to see the town icon)
   2) If the program seems to crash on startup (or as soon as it tries to autosuicide), then update the "indicators/mapleIcon.png" with a similar image.
      Some computers have different backgrounds
   3) Your item inventory must NOT cover the map and also must NOT cover the middle. It must also NOT be expanded (expanded makes it transparent, no bueno)
   4) You may have to update facingLeft and facingRight with your own gear, but in theory the program should still work without it (just less accurate when trying to return to ByeBye)
   5) Your hyperteleport rock must have Fantasy Theme Park 3 and Corridor H01 showing at the TOP (otherwise update the images mesofarmspot and suicidespot)





 * Update the variables directly below as you need to.
 * Run the program to start running. To pause, you can either press the pause button, or just press space.
 * Pressing space again will automatically switch back to the MapleStory window for you.
 * <p>
 * Rune procedure:
 * 1. Alt-tab and press space (or just press the pause button)
 * 2. Alt-tab back to MapleStory and do the rune
 * 3. Alt-tab and press space
 * - You will now be resumed in MapleStory, with buff times preserved as well.
 */
   public class maple extends Application {

  /**
   * This you might want to change
   */

  private static final int TELEPORT_KEY = KeyEvent.VK_E;
  private static final int MANA_BALANCE_KEY = KeyEvent.VK_D;
  private static final int HAKU_KEY = KeyEvent.VK_F2;
  private static final int BLACKHEART_KEY = KeyEvent.VK_F4;
  // Buffs are of form: name (just for printing), time between casts (in seconds), and key
  private static Spell[] BUFFS = {
      // Kishin lasts for 150 seconds and is the most important for rebuffs.
  	new Spell("Kishin", 60, 0, KeyEvent.VK_F1, true),
      // Domain has a 220s CD, but is very very important and does NOT spend mana
      // for failed attempts. So, try more often than is necessary.
  	new Spell("Domain", 40, 0, KeyEvent.VK_F5, false),
      // Booster lasts for 240 seconds, so as long as there are no consecutive failures,
      // booster should always be active.
  	new Spell("Booster", 100, 0, KeyEvent.VK_F3, false),
      // Yuki has a 90s CD, but it also does not spend mana for failed attempts.
  	new Spell("Yuki", 30, 0, KeyEvent.VK_F4, false),
      // Sengoku Forces
  	new Spell("Sengoku", 30, 0, KeyEvent.VK_F6, false),
      // Haku has a 900s duration, but missing haku for 5 minutes could be deadly, so
      // try a bit more frequently.
  	new Spell("Haku", 300, 0, KeyEvent.VK_F2, true),
    new Spell("Decent HS", 100, 0, KeyEvent.VK_F4, false),
    new Spell("Blossom Barrier", 90, 0, KeyEvent.VK_F, false)
  };
  private static Spell[] SPELLS = {
      // new Spell("Teleport", 0, 0, KeyEvent.VK_E, true),
  	new Spell("Shikigami Haunting", 0, 5, KeyEvent.VK_Q, false),
  	new Spell("Demon's Fury", 0, 3, KeyEvent.VK_W, false)
  };
  // How long the bot runs before pausing. This is in case you AFK for too long.
  private static final int RUNTIME_MINUTES = 20;
  // Self-imposed cooldown between buffs. Gives time for mana regen
  private static final int BUFF_CD_SECONDS = 5;

  /**
   * FX GUI Block
   */

  public static void main(String[] args) throws Exception {
    // This is the robot for teleporting: it includes a delay.
  	robot = new Robot();
  	robot.setAutoWaitForIdle(true);
  	robot.setAutoDelay(100);
  	screenRobot = new Robot();
    // This hold robot is used to press and hold keys. This will be used for buffing.
  	holdRobot = new Robot();
  	holdRobot.setAutoWaitForIdle(true);
  	buff = now();
  	start = now();
  	lastPrint = now();
  	lastBuffed = new long[BUFFS.length];
  	lastUsed = new long[SPELLS.length];
  	PrintStream out = new PrintStream(new File("switch.vbs"));
  	out.println("set WshShell = WScript.CreateObject(\"Wscript.Shell\")");
  	out.println("WshShell.AppActivate \"MapleStory\"");
  	mapleLoc = getLoc(fullScreenshot(), mapleIcon);
  	if (mapleLoc == null) {
  		System.out.println("Cannot find Maplestory window, will try again");
  	} else {
  		System.out.println("Maplestory window found!");
  	}
  	out.close();

  	launch(args);
  }

  private static VBox vbox;
  private static HBox tmpbox;
  private static Button btn;
  private static ArrayList<CheckBox> toggles = new ArrayList<>();
  @Override
  public void start(Stage primaryStage) {
  	primaryStage.setTitle("Maple?");
  	CheckBox chk;

  	for (int i = 0; i < BUFFS.length; i++) {
      //Create new checkbox and add to toggles
  		chk = new CheckBox(BUFFS[i].name + " : " + KeyEvent.getKeyText(BUFFS[i].key));
  		chk.setSelected(BUFFS[i].selected);
  		toggles.add(chk);
  	}

  	for (int i = 0; i < SPELLS.length; i++) {
      //Create new checkbox and add to toggles
  		chk = new CheckBox(SPELLS[i].name + " : " + KeyEvent.getKeyText(SPELLS[i].key));
  		chk.setSelected(SPELLS[i].selected);
  		toggles.add(chk);
  	}

  	chk = new CheckBox("Teleport : " + KeyEvent.getKeyText(TELEPORT_KEY));
  	// chk.setSelected(true);
  	toggles.add(chk);
    chk = new CheckBox("Leech (No autosuicide)");
    chk.setSelected(true);
    toggles.add(chk);
  	btn = new Button();
  	btn.setText("Pause");
  	btn.setOnAction(
  		new EventHandler<ActionEvent>() {
  			@Override
  			public void handle(ActionEvent event) {
  				pause = !pause;
  				start = now();
  				if (!pause) {
  					switchToMaplestory();
  				}
  			}
  		});

  	BorderPane root = new BorderPane();
  	root.getChildren().add(btn);
  	vbox = new VBox();
  	for (CheckBox buff_checkbox : toggles) {
  		tmpbox = new HBox();
  		tmpbox.setSpacing(10);
  		tmpbox.getChildren().add(buff_checkbox);
  		tmpbox.getChildren().add(new Text());
  		vbox.getChildren().add(tmpbox);
  	}
  	vbox.getChildren().add(new TextFlow(new Text()));
  	vbox.getChildren().add(new TextFlow(new Text()));
  	vbox.getChildren().add(btn);

  	Scene scene = new Scene(vbox, 450, 350);
  	scene.setOnKeyPressed(new EventHandler<javafx.scene.input.KeyEvent>() {
  		@Override
  		public void handle(javafx.scene.input.KeyEvent event) {
  			if (event.getCode() == javafx.scene.input.KeyCode.SPACE) {
  				pause = !pause;
  				if (!pause) {
  					switchToMaplestory();
  				}
  			}
  		}
  	});
  	primaryStage.setScene(scene);
  	primaryStage.show();
  	new AnimationTimer() {
  		@Override
  		public void handle(long now) {
  			oneLoop();
  		}
  	}.start();
  	new AnimationTimer() {
  		@Override
  		public void handle(long now) {
  			updateGui();
  		}
  	}.start();
  	Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
  	primaryStage.setX((primScreenBounds.getWidth() - primaryStage.getWidth()));
  	switchToMaplestory();
  }

  /**
   * Actual code
   */

  private static Robot robot, holdRobot;

  private static boolean pause = false;
  // Time to hold down a button in ms
  private static final int BUTTON_HOLD = 500;

  private static long buff;
  private static long start;
  private static long lastPrint;
  private static long[] lastBuffed;
  private static long[] lastUsed;


  private static class Spell {
  	public final String name;
      // Buff duration and frequency in seconds
  	public final int duration;
  	public final int freq;
  	public final int key;
  	public long lastUsed;
  	public boolean selected;
  	public Spell(String name, int duration, int freq, int key, boolean selected) {
  		this.name = name;
  		this.duration = duration;
  		this.freq = freq;
  		this.key = key;
  		this.lastUsed = 0;
  		this.selected = selected;
  	}
  }

  private static void pressButton(int keyEvent, long length) {
  	holdRobot.keyPress(keyEvent);
  	pause(length);
  	holdRobot.keyRelease(keyEvent);
  }

  private static void pressButton(int keyEvent) {
  	pressButton(keyEvent, BUTTON_HOLD);
  }

  private static void shortButtonPress(int keyEvent) {
  	pressButton(keyEvent, BUTTON_HOLD / 2);
  }

  private static long now() {
  	return System.currentTimeMillis();
  }

  private static long timeSince(long time) {
  	return now() - time;
  }

  private static void switchToMaplestory() {
  	try {
  		Runtime.getRuntime().exec("cmd /c switch.vbs");
  	} catch (Exception e) {
  		System.out.println(e);
  	}
  }

  private static void updateGui() {
  	ObservableList<Node> children = vbox.getChildren();
  	HBox tmp;
  	for (int i = 0; i < BUFFS.length; i++) {
  		tmp = (HBox) children.get(i);
  		Text text = (Text) tmp.getChildren().get(1);
  		long timeToNext = BUFFS[i].duration - timeSince(BUFFS[i].lastUsed) / 1000;
  		if (timeToNext > 0) {
  			text.setText(String.format("Buffing in %d seconds.", timeToNext));
  		} else {
  			text.setText("Buffing as soon as possible");
  		}
  	}
  	((TextFlow) children.get(toggles.size())).setStyle(String.format("-fx-background-color: #C0C0C0;"));
  	((Text) ((TextFlow) children.get(toggles.size())).getChildren().get(0))
  	.setText(
  		!pause
  		? String.format("Has been running for %d minutes %d seconds. Will terminate at %d minutes", timeSince(start)/1000/60, timeSince(start)/1000%60, RUNTIME_MINUTES)
  		: String.format("Program is paused, but will run for %d minutes upon restart", RUNTIME_MINUTES));

  	((TextFlow) children.get(toggles.size() + 1)).setStyle(String.format("-fx-background-color: #C0C0C0;"));
  	((Text) ((TextFlow) children.get(toggles.size() + 1)).getChildren().get(0)).setText("Current exp: " + (curExp * 100));
  	Button button = (Button) vbox.getChildren().get(toggles.size() + 2);

  	button.setText(pause ? "Unpause" : "Pause");
    vbox.setStyle(String.format("-fx-background-color: #%s;", pause ? "800000" : "008800"));
  }

  private static long lastExp = 0;
  private static double curExp = 0;

  private static void oneLoop() {
  	if (timeSince(start) > RUNTIME_MINUTES * 60 * 1000 || pause) {
  		pause = true;
  		return;
  	}
  	Boolean is_leeching = toggles.get(BUFFS.length + SPELLS.length + 1).isSelected();
  	if (!is_leeching && timeSince(lastExp) > 2000) {
  		lastExp = now();
  		curExp = getExp();
  		if (curExp > 0.6) {
  		// if (true) {
  			pause = true;
  			resetExp();
  		}
  	}
      // Respect the universal Buff CD (self-imposed) for mana regen and such
  	if (timeSince(buff) > BUFF_CD_SECONDS * 1000) {
         // Check if any buffs need to be refreshed.
         // Since buffs can fail due to lag, buff more frequently than needed
  		for (int x = 0; x < BUFFS.length; x++) {
  			if (toggles.get(x).isSelected() && timeSince(BUFFS[x].lastUsed) / 1000 > BUFFS[x].duration) {
  				buff = now();
  				BUFFS[x].lastUsed = buff;
  				pressButton(BUFFS[x].key);
  				break;
  			}
  		}
  	}

  	for (int y = 0; y < SPELLS.length; y++) {
  		if (toggles.get(BUFFS.length + y).isSelected() && timeSince(SPELLS[y].lastUsed) / 1000 > SPELLS[y].freq) {
  			SPELLS[y].lastUsed = now();
  			robot.keyPress(SPELLS[y].key);
  			robot.keyRelease(SPELLS[y].key);
  		}
  	}

  	if (toggles.get(BUFFS.length + SPELLS.length).isSelected()) {
  		robot.keyPress(TELEPORT_KEY);
  		robot.keyRelease(TELEPORT_KEY);
  	}

  }


  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private static Robot screenRobot;

  // TODO: get more accurate xp by finding like last occurrence? or smth
  // currently once it passes the middle, it'll have a much lower value because of the
  // numbers in the middle.
  private static double getExp() {
  	BufferedImage screen = mapleScreenshot();
  	int height = HEIGHT - 1;
  	int counter = 0;
//    clickIfValid(new Pair(20, height + mapleIcon.getHeight() + 3), 1);
  	for (int x = 0; x < WIDTH; x++) {
  		int pixel = screen.getRGB(x, height);
  		int max = Math.max(Math.max(getRed(pixel), getGreen(pixel)), getBlue(pixel));
  		int min = Math.min(Math.min(getRed(pixel), getGreen(pixel)), getBlue(pixel));
  		if (getGreen(pixel) == max && max - min > 50 && getBlue(pixel) < 30 && getGreen(pixel) > 150) {
  			counter++;
//        System.out.println(x + " " + getRed(pixel) + " " + getGreen(pixel) + " " + getBlue(pixel));
  		}
  	}
//    System.out.println(((double) counter)/WIDTH);
  	return ((double) counter) / (WIDTH);
  }

  private static int getRed(int pixel) {
  	return (pixel >> 16) & 0xff;
  }

  private static int getBlue(int pixel) {
  	return (pixel) & 0xff;
  }

  private static int getGreen(int pixel) {
  	return (pixel >> 8) & 0xff;
  }

  private static void resetExp() {
  	resetFamiliar();
    // Getting out of the cash shop has lag
  	waitForLoad();
    // TODO: just for loop over initial XP. note that xp is wrong atm
  	while (getExp() > 0) {
  		kms();
  		System.out.println("Shorter waiting for load");
  		doUntilImageIsFound(() -> pause(200), cashShopIcon);
  	}
  	waitForLoad();
  	moveToMesoMap();
    // TODO: add a resume method to call instead
  	pause = false;
  	start = now();
  }

  private static void waitForLoad() {
  	System.out.println("Waiting for load");
  	doUntilImageIsFound(() -> pause(200), cashShopIcon);
  	pause(3000);
  	System.out.println("Fully loaded");
  }

  private static BufferedImage inventoryIcon = read("indicators/item.png");
  private static BufferedImage teleportRockIcon = read("indicators/hypertele.png");
  private static BufferedImage teleportRockSelect = read("indicators/move.png");
  private static BufferedImage teleportRockOk = read("indicators/hyperteleok.png");
  private static BufferedImage teleportRockCancel = read("indicators/hypertelecancel.png");
  private static BufferedImage cashShopIcon = read("indicators/cashshop.png");
  private static BufferedImage inCashShop = read("indicators/incashshop.png");
  private static BufferedImage suicideSpot = read("indicators/suicidespot.png");
  private static BufferedImage mesoSpot = read("indicators/mesofarmspot.png");
  private static BufferedImage revive = read("indicators/deathok.png");
  private static BufferedImage dead = read("indicators/dead.png");
  private static BufferedImage blackheart = read("indicators/blackheart.png");
  private static BufferedImage haku = read("indicators/haku.png");
  private static BufferedImage mapleIcon = read("indicators/mapleIcon.png");
  private static BufferedImage portalLocation = read("indicators/portalLocation.png");
  private static BufferedImage onFantasyThemePark = read("indicators/fantasythemepark.png");
  private static BufferedImage facingLeft = read("indicators/facingLeft.png");
  private static BufferedImage facingRight = read("indicators/facingRight.png");
  private static BufferedImage fullHealth = read("indicators/fullhealth.png");
  private static BufferedImage notFullHealth = read("indicators/notfullhealth.png");
  private static final Pair toClick = new Pair(-1, -1);
  private static int counter = 0;
  private static int limit = 20;

  private static void resetFamiliar() {
    // Turn off all buffs by right clicking across the top of the screen
    // TODO: make sure that blackheart is OFF. That's the only thing that really matters
  	System.out.println("Turning off buffs");
  	Pair start = new Pair(mapleLoc.a, mapleLoc.b);
  	int startX = start.a;
  	start.b += 50;
  	start.a += WIDTH * 1 / 2;
  	while (start.a < startX + WIDTH) {
  		rightClickIfValid(start);
  		start.a += 25;
  	}
    // Until we are in the cash shop, we spam click cash shop and enter
  	System.out.println("Entering cash shop");
  	doUntilImageIsFound(
  		() -> {
  			findAndClickImage(cashShopIcon);
  			pressButton(KeyEvent.VK_ENTER);
  		},
  		inCashShop);
    // We spam click exit and enter to leave the cash shop
  	System.out.println("Leaving cash shop");
  	doUntilCondition(() -> findAndClickImage(inCashShop), () -> getLoc(mapleScreenshot(), inCashShop) == null);

  }

  private static void kms() {
  	hyperTeleport(suicideSpot, "suicide spot");
    // Press OK to revive, and if not dead, random teleport
  	System.out.println("Waiting for death");
  	doUntilImageIsFound(
  		() -> {
          // Teleport
  			shortButtonPress(TELEPORT_KEY);
          // Mana balance
  			shortButtonPress(MANA_BALANCE_KEY);
  		},
  		revive);
    // Clicking the revive button
  	System.out.println("Reviving");
  	doUntilCondition(
  		() -> findAndClickImage(revive),
  		() -> getLoc(mapleScreenshot(), dead) == null);
  }

  private static int countBuffs() {
  	BufferedImage screen = mapleScreenshot();
  	int buffCounter = 0;
    // buff dimension is 32x32
  	int dim = 32;
  	loop:for (int x = screen.getWidth() / 2; x < screen.getWidth() - dim; x++) {
  		for (int y = 0; y < 20; y++) {
//      int y = 9;
  			int count = 0;
  			for (int a = 3; a <= dim - 3; a++) {
  				if (isBlack(screen.getRGB(x + a, y))) count++;
  				if (isBlack(screen.getRGB(x + a, y + dim - 1))) count++;
  				if (isBlack(screen.getRGB(x, y + a))) count++;
  				if (isBlack(screen.getRGB(x + dim - 1, y + a))) count++;
//          if (!isBlack(screen.getRGB(x + a, y))
//              || !isBlack(screen.getRGB(x + dim - 1, y + a))
//              || !isBlack(screen.getRGB(x + a, y + dim - 1))
//              || !isBlack(screen.getRGB(x, y + a))) {
//            continue loop;
//          }
  			}
  			if (x == 952) {
  				System.out.println("top row" );
  				for (int a = 3; a < dim - 3; a++) {
  					System.out.println(pixelToString(screen.getRGB(x + a, y)));
  				}
  				System.out.println("bottom row" );
  				for (int a = 3; a < dim - 3; a++) {
  					System.out.println(pixelToString(screen.getRGB(x + a, y + dim - 1)));
  				}
  				System.out.println("left column" );
  				for (int a = 3; a < dim - 3; a++) {
  					System.out.println(pixelToString(screen.getRGB(x, y + a)));
  				}
  				System.out.println("right column" );
  				for (int a = 3; a < dim - 3; a++) {
  					System.out.println(pixelToString(screen.getRGB(x + dim - 1, y + a)));
  				}
  			}
  			if (count > 50 && x == 952) {
  				System.out.println("Buff at: " + x + " " + y + ": " + count);
  				buffCounter++;
//          clickIfValid(new Pair(mapleLoc.a + x, mapleLoc.b + mapleIcon.getHeight() + 3 + y), 1);
//          x += dim;
  			}
  		}
  	}
  	return buffCounter;
  }

  private static String pixelToString(int pixel) {
  	return getRed(pixel) + " " + getGreen(pixel) + " " + getBlue(pixel);
  }

  private static boolean isBlack(int pixel) {
  	int threshold = 150;
  	return getRed(pixel) > threshold && getBlue(pixel) > threshold && getGreen(pixel) > threshold;
  }

  private static void moveToMesoMap() {
    // Turn on Blackheart before returning
  	System.out.println("Turning on blackheart");
  	doUntilCondition(
  		() -> {
  			pressButton(BLACKHEART_KEY);
  			pause(500);
  		},
  		() -> getKindaLoc(mapleScreenshot(), blackheart) != null);
    // Cast Haku to heal
    // CRITICAL TODO: fix this condition this doesn't work. This only checks for haku, so there is a chance
    // that we have not healed even if we did click the power elixir button
  	System.out.println("Casting Haku to heal before going back to byebye");
  	doUntilCondition(
  		() -> {
  			pressButton(HAKU_KEY);
  			pressButton(KeyEvent.VK_C);
  		},
  		() -> getKindaLoc(mapleScreenshot(), haku) != null);
//    doUntilCondition(
//        () -> pressButton(BUFFS[2].key), () -> getLoc(mapleScreenshot(), notFullHealth) == null);
    // TODO: check that health has been restored instead of just waiting for a second
  	pause(1000);
  	hyperTeleport(mesoSpot, "meso spot");
    // Wait for screen to load
  	doUntilImageIsFound(() -> pause(100), portalLocation);
  	// Close inventory
  	pressButton(KeyEvent.VK_I);
    // Enter the portal in the middle of the stage.
  	doUntilCondition(maple::moveToAndEnterByeBye, () -> getLoc(mapleScreenshot(), onFantasyThemePark) == null);
  	System.out.println("Inside byebye");
  }

  private static Pair mapleLoc = null;
  private static void moveToAndEnterByeBye() {
  	BufferedImage screen = mapleScreenshot();
    // We are looking at the position of our character relative to the center portal
  	Pair relativeLoc = getLoc(screen, portalLocation);
  	Pair facingLeftLoc = getLoc(screen, facingLeft);
  	Pair facingRightLoc = getLoc(screen, facingRight);
  	Pair curLoc = facingLeftLoc != null ? facingLeftLoc : facingRightLoc;
  	if (curLoc == null) {
  		curLoc = new Pair(mapleLoc.a + WIDTH / 2, -1 /* y doesn't matter */);
  	}
  	if (relativeLoc == null) {
  		pressButton(KeyEvent.VK_UP);
  		return;
  	}
    // The portal location image itself isn't centered, so add additional error factor (20)
  	relativeLoc.a += 40;
  	relativeLoc.b += 400;
//    clickIfValid(relativeLoc, 1);
  	System.out.println("Loc: " + relativeLoc.a + " CharLoc: " + curLoc.a);
  	int relative = curLoc.a - relativeLoc.a;
  	double speed = Math.abs(((double) relative) / WIDTH) / 0.5;
  	System.out.println("Loc: " + relativeLoc.a + " relative: " + relative);
  	System.out.println("Relative: " + relative + " Speed: " + speed);
    // Add another zone here? maybe between 0.1 and 0.2?
  	if (relative < 0) {
      // We are on the left side
  		if (speed > 0.5) {
  			System.out.println("Teleporting right");
  			holdRobot.keyPress(KeyEvent.VK_RIGHT);
  			holdRobot.keyPress(TELEPORT_KEY);
  			pause(150);
  			holdRobot.keyRelease(TELEPORT_KEY);
  			holdRobot.keyRelease(KeyEvent.VK_RIGHT);
  			pause(500);
  		} else {
  			System.out.println("Pressing right");
  			pressButton(KeyEvent.VK_RIGHT, (int) (Math.max(speed, 0.3) * 1000));
        // If we're somewhat close, since the map moves slower, we might as well just try potentially going in
  			if (speed < 0.3) {
  				System.out.println("YOLO pressing up");
  				pressButton(KeyEvent.VK_UP);
  			}
  		}
  	} else {
      // We are on the right side
  		if (speed > 0.5) {
  			System.out.println("Teleporting left");
  			holdRobot.keyPress(KeyEvent.VK_LEFT);
  			holdRobot.keyPress(TELEPORT_KEY);
  			pause(150);
  			holdRobot.keyRelease(TELEPORT_KEY);
  			holdRobot.keyRelease(KeyEvent.VK_LEFT);
  			pause(500);
  		} else {
  			System.out.println("Pressing left");
  			pressButton(KeyEvent.VK_LEFT, (int) (Math.max(speed, 0.3) * 1000));
        // If we're somewhat close, since the map moves slower, we might as well just try potentially going in
  			if (speed < 0.3) {
  				System.out.println("YOLO pressing up");
  				pressButton(KeyEvent.VK_UP);
  			}
  		}
  	}
  }

  private static void hyperTeleport(BufferedImage image, String name) {
  	int inventory = KeyEvent.VK_I;
    // Click inventory until it shows up
  	System.out.println("Opening inventory");
  	doUntilImageIsFound(() -> shortButtonPress(inventory), inventoryIcon);
    // Tab to the correct tab (until we see the teleportRock)
  	System.out.println("Tabbing over to cash tab");
  	doUntilImageIsFound(() -> shortButtonPress(KeyEvent.VK_TAB), teleportRockIcon);
    // Click the teleportRock until the menu shows up
  	System.out.println("Clicking hyper tele rock");
  	doUntilImageIsFound(() -> findAndDoubleClickImage(teleportRockIcon), image);
    // Click the spot until the item is selected
  	System.out.println("Clicking " + name);
  	doUntilImageIsFound(() -> findAndClickImage(image), teleportRockSelect);
  	for (int x = 0; x < limit / 6; x++) {
      // Click move and enter until we are there (so the teleportRockMenu isn't there anymore)
  		System.out.println("Clicking move");
  		doUntilImageIsFound(() -> findAndClickImage(teleportRockSelect), teleportRockOk);
      // Click OK to finalize the move. This has no post condition so could fail, but really shouldn't.
  		System.out.println("Clicking OK to move to the map");
  		doUntilCondition(
  			() -> findAndClickImage(teleportRockOk), () -> getLoc(mapleScreenshot(), teleportRockCancel) == null);
  		if (getLoc(mapleScreenshot(), teleportRockOk) != null) {
        // It is possible for the teleport rock to fail for various reasons. If this is the case,
        // we want to click the OK button in the error message and then just try again.
        // We will confirm that we've clicked the teleport rock by having our cursor be
        // moved away from the move button and
  			System.out.println("An error occurred with teleport rock, trying again");
  			doUntilImageIsFound(() -> findAndClickImage(teleportRockOk), teleportRockSelect);
  		} else {
  			return;
  		}
  	}
  	endProgramWithError();
  }

  private static void findAndDoubleClickImage(BufferedImage image) {
  	findAndClickImageMultipleTimes(image, 2);
  }

  private static void findAndClickImage(BufferedImage image) {
  	findAndClickImageMultipleTimes(image, 1);
  }

  private static void findAndClickImageMultipleTimes(BufferedImage image, int count) {
  	if (toClick.a < 0) {
  		Pair temp = getLoc(mapleScreenshot(), image);
  		if (temp == null) {
  			return;
  		}
  		temp = new Pair(temp.a + image.getWidth() / 2, temp.b + image.getHeight() / 2);
  		if (temp != null) {
  			toClick.apply(temp);
  		}
  	}
  	clickIfValid(toClick, count);
  }

  private static void clickIfValidWithType(Pair c, int times, int inputEvent) {
  	if (c.a >= 0) {
      // There's a stupid bug where it's possible for the mouse to not move at first, just spam
  		for (int x = 0; x < 10; x++) {
  			holdRobot.mouseMove(c.a, c.b);
  		}
  		for (int x = 0; x < times; x++) {
  			holdRobot.mousePress(inputEvent);
  			pause(100);
  			holdRobot.mouseRelease(inputEvent);
  		}
  	}
  }

  private static void rightClickIfValid(Pair c) {
  	clickIfValidWithType(c, 1, InputEvent.BUTTON3_DOWN_MASK);
  }

  private static void clickIfValid(Pair c, int times) {
  	clickIfValidWithType(c, times, InputEvent.BUTTON1_DOWN_MASK);
  }

  private static void doUntilImageIsFound(Runnable toRun, BufferedImage image) {
  	doUntilCondition(toRun, () -> getLoc(mapleScreenshot(), image) != null);
  }

  private static void endProgramWithError() {
  	try {
  		throw new RuntimeException("");
  	} catch (Exception e) {
  		System.out.println("Stack trace: " + Arrays.asList(e.getStackTrace()));
  	}
  	writeImage(mapleScreenshot(), (int) (Math.random() * 99999999) + "");
  	System.exit(0);
  }

  private static void doUntilCondition(Runnable toRun, BooleanSupplier condition) {
  	while (!condition.getAsBoolean() && counter++ < limit) {
  		toRun.run();
  		pause(250);
  	}
  	if (counter >= limit) {
  		endProgramWithError();
  	}
  	counter = 0;
  	toClick.apply(new Pair(-1, -1));
  }

  private static void pause(long ms) {
  	try {
  		Thread.sleep(ms);
  	} catch (Exception e) {

  	}
  }

  private static BufferedImage mapleScreenshot() {
  	while (mapleLoc == null) {
  		System.out.println("retaking screenshot for maplestory");
  		pause(500);
  		mapleLoc = getLoc(fullScreenshot(), mapleIcon);
  		if (mapleLoc == null) {
  			System.out.println("Still cannot find maple icon, might need to retake screenshot of maplewindow icon");
  		}
  	}
    return screenshot(mapleLoc.a, mapleLoc.b + mapleIcon.getHeight() + 3, WIDTH, HEIGHT);
  }

  private static BufferedImage fullScreenshot() {
  	Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
  	return screenshot(screenRect.getX(), screenRect.getY(), screenRect.getWidth(), screenRect.getHeight());
  }

  private static BufferedImage screenshot(double startX, double startY, double width, double height) {
  	return screenRobot.createScreenCapture(new Rectangle((int) startX, (int) startY, (int) width, (int) height));
  }

  private static BufferedImage read(String filename) {
  	try {
  		return ImageIO.read(new File(filename));
  	} catch (Exception e) {
  		return null;
  	}
  }

  private static void writeImage(BufferedImage image, String fileName) {
  	try {
  		ImageIO.write(image, "png", new File(fileName + ".png"));
  	} catch (Exception e) {
  		System.out.println(e);
  	}
  }

  // x across, y down
  private static Pair getLoc(BufferedImage container, BufferedImage image) {
  	for (int x = 0; x < container.getWidth() - image.getWidth(); x++) {
  		loop:
  		for (int y = 0; y < container.getHeight() - image.getHeight(); y++) {
  			for (int a = 0; a < image.getWidth(); a++) {
  				for (int b = 0; b < image.getHeight(); b++) {
  					if (container.getRGB(x + a, y + b) != image.getRGB(a, b)) {
  						continue loop;
  					}
  				}
  			}
  			System.out.println("Image found: " + x + " " + y);
  			if (mapleLoc == null) {
  				return new Pair(x, y);
  			} else {
  				return new Pair(mapleLoc.a + x, mapleLoc.b + mapleIcon.getHeight() + y + 3);
  			}
  		}
  	}
  	System.out.println("Image not found");
  	return null;
  }

  private static double getPercentDiff(int a, int b) {
  	if (a == 0 || b == 0) {
  		return Math.max(a, b) / 255.0;
  	} else if (a == b) {
  		return 0;
  	} else {
  		return ((double) Math.max(a, b) - Math.min(a, b)) / Math.max(a, b);
  	}
  }

  private static Pair getKindaLoc(BufferedImage container, BufferedImage image) {
  	for (int x = container.getWidth() / 2; x < container.getWidth() - image.getWidth(); x++) {
  		loop:
  		for (int y = 0; y < 50; y++) {
  			double sum = 0;
  			for (int a = 0; a < image.getWidth(); a++) {
  				for (int b = 0; b < image.getHeight(); b++) {
  					int pixel1 = container.getRGB(x + a, y + b);
  					int pixel2 = image.getRGB(a, b);
  					sum += getPercentDiff(getRed(pixel1), getRed(pixel2));
  					sum += getPercentDiff(getGreen(pixel1), getGreen(pixel2));
  					sum += getPercentDiff(getBlue(pixel1), getBlue(pixel2));
  				}
  			}
//        System.out.println(x + " " + y + " with average diff: "
//            + sum / image.getWidth() / image.getHeight() / 3);
  			if (sum / image.getWidth() / image.getHeight() / 3 <= 0.25) {
  				System.out.println("Image found: " + x + " " + y + " with average diff: "
  					+ sum / image.getWidth() / image.getHeight() / 3);
  				if (mapleLoc == null) {
  					return new Pair(x, y);
  				} else {
  					return new Pair(mapleLoc.a + x, mapleLoc.b + mapleIcon.getHeight() + y + 3);
  				}
  			}
  		}
  	}
  	System.out.println("Image not found");
  	return null;

  }

  private static class Pair {
  	public int a, b;

  	public Pair(int a, int b) {
  		this.a = a;
  		this.b = b;
  	}

  	public void apply(Pair c) {
  		this.a = c.a;
  		this.b = c.b;
  	}

  	public String toString() {
  		return String.format("(%d, %d)", a, b);
  	}
  }

  private static void goToMiddle() {
  	holdRobot.keyPress(KeyEvent.VK_RIGHT);
  	for (int x = 0; x < 12; x++) {
  		holdRobot.keyPress(TELEPORT_KEY);
  		try {
  			Thread.sleep(250);
  		} catch (Exception e) {
  		}
  		holdRobot.keyRelease(TELEPORT_KEY);
  	}
  	holdRobot.keyRelease(KeyEvent.VK_RIGHT);
  	try {
  		Thread.sleep(500);
  	} catch (Exception e) {
  	}
  	holdRobot.keyPress(KeyEvent.VK_DOWN);
  	for (int x = 0; x < 6; x++) {
  		holdRobot.keyPress(TELEPORT_KEY);
  		try {
  			Thread.sleep(250);
  		} catch (Exception e) {
  		}
  		holdRobot.keyRelease(TELEPORT_KEY);
  	}
  	holdRobot.keyRelease(KeyEvent.VK_DOWN);
  	try {
  		Thread.sleep(500);
  	} catch (Exception e) {
  	}
  	holdRobot.keyPress(KeyEvent.VK_LEFT);
  	for (int x = 0; x < 2; x++) {
  		holdRobot.keyPress(TELEPORT_KEY);
  		try {
  			Thread.sleep(250);
  		} catch (Exception e) {
  		}
  		holdRobot.keyRelease(TELEPORT_KEY);
  	}
  	holdRobot.keyRelease(KeyEvent.VK_LEFT);
  }

}