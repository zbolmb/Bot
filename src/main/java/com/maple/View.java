package com.maple;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

public class View extends Application {

  private VBox outer;
  private VBox skillText;
  private VBox statusText;
  private VBox buttons;
  private String css = "-fx-border-color: red;\n" +
                       "-fx-border-insets: 5;\n" +
                       "-fx-border-width: 3;\n" +
                       "-fx-border-style: dashed;\n";

  public StackPane setup() {
    StackPane root = new StackPane();
    Button btn = new Button();
    btn.setText("Say 'Hello World'");
    btn.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent event) {
        System.out.println("Hello World!");
      }
    });

    Button skills = new Button();
    skills.setText("Skills");

    Button status = new Button();
    status.setText("Status");

    outer = new VBox();
    skillText = new VBox(skills);
    skillText.setStyle(css);
    statusText = new VBox(status);
    statusText.setStyle(css);
    buttons = new VBox(btn);
    buttons.setStyle(css);
    outer = new VBox(skillText, statusText, buttons);
    outer.setStyle(css);
    root.getChildren().add(outer);
    return root;
  }

  @Override
  public void start(Stage primaryStage) {

    StackPane root = setup();
    Scene scene = new Scene(root, 250, 300);

    primaryStage.setTitle("Hello World!");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
