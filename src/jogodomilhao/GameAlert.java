/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jogodomilhao;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Button;



public class GameAlert {

    public static void show(String message) {
        // Cria a janela modal
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UNDECORATED); // sem bordas
        dialog.setAlwaysOnTop(true);

        // Texto centralizado
        Label label = new Label(message);
        label.setTextFill(Color.GOLD);
        label.setFont(Font.font("Arial Black", 28));
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);

        // Botão OK estilizado
        Button okButton = new Button("OK");
        okButton.setFont(Font.font("Arial Black", 22));
        okButton.setTextFill(Color.WHITE);
        okButton.setStyle("-fx-background-color: red; -fx-cursor: hand; -fx-background-radius: 12;");
        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: green; -fx-cursor: hand; -fx-background-radius: 12;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: red; -fx-cursor: hand; -fx-background-radius: 12;"));
        okButton.setOnAction(e -> dialog.close());

        // Layout principal
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(label, okButton);
        layout.setStyle("-fx-background-color: #002B7A; -fx-border-color: gold; -fx-border-width: 4; -fx-border-radius: 12; -fx-background-radius: 12;");
        layout.setPrefSize(600, 300);

        // Cena
        Scene scene = new Scene(layout);
        dialog.setScene(scene);

        // Centraliza dinamicamente na tela
        dialog.setOnShown(e -> {
            dialog.setX((javafx.stage.Screen.getPrimary().getBounds().getWidth() - dialog.getWidth()) / 2);
            dialog.setY((javafx.stage.Screen.getPrimary().getBounds().getHeight() - dialog.getHeight()) / 2);
        });

        // Efeito fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.6), layout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Mostra e espera a interação
        dialog.showAndWait();
    }
}
