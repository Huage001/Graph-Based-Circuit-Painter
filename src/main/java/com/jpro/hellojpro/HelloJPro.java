package com.jpro.hellojpro;

import com.jpro.webapi.JProApplication;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class HelloJPro extends JProApplication {

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage stage)
    {
        Label label = new Label("Hello JPro!");
        label.setFont(new Font(50));
        label.setAlignment(Pos.TOP_CENTER);
        stage.setScene(new Scene(label));
        stage.show();
    }
}
