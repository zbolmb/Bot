import java.awt.*;
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
import javafx.scene.*;
import javafx.scene.text.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

   // Spells have format, (name, duration, frequency, key, selected).
   // For Buffs, set frequency = 0
   // For Spells, like teleport, set duration to 0
   // Set selected to true if you want it to be checked on default
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
      new Spell("Haku", 300, 0, KeyEvent.VK_F2, true)
   };
   private static Spell[] SPELLS = {
      // new Spell("Teleport", 0, 0, KeyEvent.VK_E, true),
      new Spell("Shikigami Haunting", 0, 5, KeyEvent.VK_Q, false),
      new Spell("Demon's Fury", 0, 3, KeyEvent.VK_W, false)
   };
   private static final int TELEPORT = KeyEvent.VK_E;
   // How long the bot runs before pausing. This is in case you AFK for too long.
   private static final int RUNTIME_MINUTES = 20;
   // Self-imposed cooldown between buffs. Gives time for mana regen
   private static final int BUFF_CD_SECONDS = 5;

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
      lastUsed = new long[SPELLS.length];
      PrintStream out = new PrintStream(new File("switch.vbs"));
      out.println("set WshShell = WScript.CreateObject(\"Wscript.Shell\")");
      out.println("WshShell.AppActivate \"MapleStory\"");
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

      chk = new CheckBox("Teleport : " + KeyEvent.getKeyText(TELEPORT));
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

      Button button = (Button) vbox.getChildren().get(toggles.size() + 1);

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
         robot.keyPress(TELEPORT);
         robot.keyRelease(TELEPORT);
      }
   }
}