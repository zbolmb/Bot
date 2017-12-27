import java.awt.*;
import java.awt.event.KeyEvent;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.*;
import javafx.scene.*;
import javafx.scene.text.*;
import java.io.*;
import java.util.*;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

/**
 * Update the variables directly below as you need to.
 * Run the program to start running. To pause, you can either press the pause button, or just press space.
 * Pressing space again will automatically switch back to the MapleStory window for you.
 *
 * Rune procedure:
 *	1. Alt-tab and press space (or just press the pause button)
 * 	2. Alt-tab back to MapleStory and do the rune
 *  3. Alt-tab and press space
 *		- You will now be resumed in MapleStory, with buff times preserved as well.
 */
public class maple extends Application {

   /**
    * This you might want to change
	*/

   private static final int TELEPORT_KEY = KeyEvent.VK_E;
   private static final int DEMON_FURY_KEY = KeyEvent.VK_W;
   // Buffs are of form: name (just for printing), time between casts (in seconds), and key
   private static Buff[] BUFFS = {
      // Kishin lasts for 150 seconds and is the most important for rebuffs.
      new Buff("Kishin", 60, KeyEvent.VK_F1),
      // Domain has a 220s CD, but is very very important and does NOT spend mana
      // for failed attempts. So, try more often than is necessary.
      // new Buff("Domain", 40, KeyEvent.VK_F5),
      // Booster lasts for 240 seconds, so as long as there are no consecutive failures,
      // booster should always be active.
      // new Buff("Booster", 100, KeyEvent.VK_F3),
      // Yuki has a 90s CD, but it also does not spend mana for failed attempts.
      // new Buff("Yuki", 30, KeyEvent.VK_F4),
      // Sengoku Forces
      // new Buff("Sengoku", 30, KeyEvent.VK_F6),
      // Haku has a 900s duration, but missing haku for 5 minutes could be deadly, so
      // try a bit more frequently.
      new Buff("Haku", 300, KeyEvent.VK_F2)
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
      robot.setAutoDelay(100);
      // This hold robot is used to press and hold keys. This will be used for buffing.
      holdRobot = new Robot();
      buff = now();
      start = now();
      lastPrint = now();
      lastBuffed = new long[BUFFS.length];
      PrintStream out = new PrintStream(new File("switch.vbs"));
      out.println("set WshShell = WScript.CreateObject(\"Wscript.Shell\")");
      out.println("WshShell.AppActivate \"MapleStory\"");
      out.close();
      launch(args);
   }
   private static VBox vbox;
   private static Button btn;

   @Override
    public void start(Stage primaryStage) {
      primaryStage.setTitle("Maple?");
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
      for (int x = 0; x < BUFFS.length; x++) {
         vbox.getChildren().add(new TextFlow(new Text()));
      }
      vbox.getChildren().add(new TextFlow(new Text()));
      vbox.getChildren().add(new TextFlow(new Text()));
      vbox.getChildren().add(btn);

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

	  Scene scene = new Scene(vbox, 450, 250);
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
   private static final int BUTTON_HOLD = 1000;

   private static long buff;
   private static long start;
   private static long lastPrint;
   private static long[] lastBuffed;


   private static class Buff {
      public final String name;
      // Buff duration in seconds
      public final int duration;
      public final int key;
      public Buff(String name, int duration, int key) {
         this.name = name;
         this.duration = duration;
         this.key = key;
      }
   }

   private static void pressButton(int keyEvent) {
      long startTime = now();
      while(timeSince(startTime) < BUTTON_HOLD){
         holdRobot.keyPress(keyEvent);
      }
      holdRobot.keyRelease(keyEvent);
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
      }
      catch (Exception e) {
         System.out.println(e);
      }
   }

   private static void updateGui() {
      ObservableList<Node> children = vbox.getChildren();
      for (int x = 0; x < BUFFS.length; x++) {
         TextFlow textFlow = (TextFlow) children.get(x);
         textFlow.setStyle(String.format("-fx-background-color: #C0C0C0;"));
         Text text =(Text) textFlow.getChildren().get(0);
         long timeToNext = BUFFS[x].duration - timeSince(lastBuffed[x]) / 1000;
         if (lastBuffed[x] == 0) {
            text.setText(String.format(
               "%s has not yet been cast. Will be cast as soon as possible.",
               BUFFS[x].name));
         }
         else if (timeToNext > 0) {
            text.setText(String.format(
               "%s was last cast %d seconds ago and will be cast again in %d seconds.",
               BUFFS[x].name,
               timeSince(lastBuffed[x]) / 1000,
               timeToNext));
         }
         else {
            text.setText(String.format("%s was last cast %d seconds ago and will be cast again as soon as possible.",
               BUFFS[x].name,
               timeSince(lastBuffed[x]) / 1000));
         }
      }
      String buffCdString =
         timeSince(buff) < BUFF_CD_SECONDS * 1000
            ? String.format("%d seconds remaining on buff CD", (BUFF_CD_SECONDS * 1000 - timeSince(buff)) / 1000)
            : "Next buff will be cast immediately.";
      ((Text) ((TextFlow) children.get(BUFFS.length)).getChildren().get(0)).setText(buffCdString);
      ((TextFlow) children.get(BUFFS.length)).setStyle(String.format("-fx-background-color: #C0C0C0;"));
      ((TextFlow) children.get(BUFFS.length + 1)).setStyle(String.format("-fx-background-color: #C0C0C0;"));
      ((Text) ((TextFlow) children.get(BUFFS.length + 1)).getChildren().get(0))
         .setText(
         	!pause
         		? String.format("Has been running for %d minutes %d seconds. Will terminate at %d minutes", timeSince(start)/1000/60, timeSince(start)/1000%60, RUNTIME_MINUTES)
         		: String.format("Program is paused, but will run for %d minutes upon restart", RUNTIME_MINUTES));
      Button button = (Button) vbox.getChildren().get(BUFFS.length + 2);

      button.setText(pause ? "Unpause" : "Pause");
      vbox.setStyle(String.format("-fx-background-color: #%s;", pause ? "800000" : "008800"));

   }

   private static void oneLoop() {
      if (timeSince(start) > RUNTIME_MINUTES * 60 * 1000 || pause) {
         pause = true;
         return;
      }
      // Respect the universal Buff CD (self-imposed) for mana regen and such
      if (timeSince(buff) > BUFF_CD_SECONDS * 1000) {
         // Check if any buffs need to be refreshed.
         // Since buffs can fail due to lag, buff more frequently than needed
         for (int x = 0; x < BUFFS.length; x++) {
            if (timeSince(lastBuffed[x]) / 1000 > BUFFS[x].duration) {
               buff = now();
               lastBuffed[x] = buff;
               pressButton(BUFFS[x].key);
               break;
            }
         }
      }
      // Teleport
      robot.keyPress(TELEPORT_KEY);
      robot.keyRelease(TELEPORT_KEY);
      // robot.keyPress(DEMON_FURY_KEY);
      // robot.keyRelease(DEMON_FURY_KEY);
   }

   private static void goToMiddle() {
      holdRobot.keyPress(KeyEvent.VK_RIGHT);
      for (int x = 0; x < 12; x++) {
         holdRobot.keyPress(TELEPORT_KEY);
         try {
            Thread.sleep(250);
         }
         catch (Exception e){}
         holdRobot.keyRelease(TELEPORT_KEY);
      }
      holdRobot.keyRelease(KeyEvent.VK_RIGHT);
      try {
         Thread.sleep(500);
      }
      catch (Exception e){}
      holdRobot.keyPress(KeyEvent.VK_DOWN);
      for (int x = 0; x < 6; x++) {
         holdRobot.keyPress(TELEPORT_KEY);
         try {
            Thread.sleep(250);
         }
         catch (Exception e){}
         holdRobot.keyRelease(TELEPORT_KEY);
      }
      holdRobot.keyRelease(KeyEvent.VK_DOWN);
      try {
         Thread.sleep(500);
      }
      catch (Exception e){}
      holdRobot.keyPress(KeyEvent.VK_LEFT);
      for (int x = 0; x < 2; x++) {
         holdRobot.keyPress(TELEPORT_KEY);
         try {
            Thread.sleep(250);
         }
         catch (Exception e){}
         holdRobot.keyRelease(TELEPORT_KEY);
      }
      holdRobot.keyRelease(KeyEvent.VK_LEFT);
   }

}