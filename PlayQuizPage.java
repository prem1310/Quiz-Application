package User_package;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayQuizPage {

    private Connection connection;
    
    // Color constants
    private final String BG_COLOR = "#f8f9fa";
    private final String CARD_BG_COLOR = "#ffffff";
    private final String HEADING_COLOR = "#2c3e50";
    private final String TITLE_COLOR = "#34495e";
    private final String DESC_COLOR = "#7f8c8d";
    private final String AVAILABLE_COLOR = "#27ae60";
    private final String UNAVAILABLE_COLOR = "#e74c3c";
    private final String BUTTON_TEXT_COLOR = "#ffffff";

    public Parent getView(String userId, StackPane rootPane) {
        // Main container with subtle background
        VBox container = new VBox(25);
        container.setPadding(new Insets(30));
        container.setStyle("-fx-background-color: " + BG_COLOR + ";");
        container.setAlignment(Pos.TOP_CENTER);

        // Page heading with icon
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        Label headingIcon = new Label("🎯");
        headingIcon.setFont(Font.font("Segoe UI", 30));
        
        Label heading = new Label("Choose Your Quiz Category");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        heading.setTextFill(Color.web(HEADING_COLOR));
        
        headerBox.getChildren().addAll(headingIcon, heading);
        
        // Instruction text
        Label instruction = new Label("Select a category to start a quiz. Green cards have questions available.");
        instruction.setFont(Font.font("Segoe UI", 14));
        instruction.setTextFill(Color.web(DESC_COLOR));
        instruction.setTextAlignment(TextAlignment.CENTER);

        // Card container with improved layout
        FlowPane cardPane = new FlowPane();
        cardPane.setHgap(25);
        cardPane.setVgap(25);
        cardPane.setPadding(new Insets(15));
        cardPane.setPrefWrapLength(900);
        cardPane.setAlignment(Pos.TOP_CENTER);
        cardPane.setStyle("-fx-background-color: transparent;");

        // Scrollable area
        ScrollPane scrollPane = new ScrollPane(cardPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setPannable(true); // Enable touch panning

        container.getChildren().addAll(headerBox, instruction, scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
            loadCategories(cardPane, userId, rootPane);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Unable to connect to the database.");
        }

        return container;
    }

    private void loadCategories(FlowPane cardPane, String userId, StackPane rootPane) {
        try {
            // Get all categories
            PreparedStatement stmt = connection.prepareStatement(
                "SELECT c.category_id, c.category_name, c.description, " +
                "(SELECT COUNT(*) FROM questions q WHERE q.category_id = c.category_id) AS question_count " +
                "FROM category c"
            );
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int categoryId = rs.getInt("category_id");
                String categoryName = rs.getString("category_name");
                String description = rs.getString("description");
                int questionCount = rs.getInt("question_count");
                
                // Create card with appropriate styling based on question availability
                VBox card = createCategoryCard(categoryId, categoryName, description, questionCount, userId, rootPane);
                
                // Add fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), card);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                cardPane.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load quiz categories.");
        }
    }

    private VBox createCategoryCard(int categoryId, String categoryName, String description, 
                                   int questionCount, String userId, StackPane rootPane) {
        // Determine if category has questions
        boolean hasQuestions = questionCount > 0;
        
        // Card container with conditional styling
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setPrefHeight(200);
        
        // Base card style
        String cardStyle = "-fx-background-color: " + CARD_BG_COLOR + "; " +
                          "-fx-background-radius: 12; " +
                          "-fx-border-radius: 12; ";
        
        // Add border color based on question availability
        if (hasQuestions) {
            cardStyle += "-fx-border-color: " + AVAILABLE_COLOR + "; " +
                        "-fx-border-width: 2; ";
        } else {
            cardStyle += "-fx-border-color: " + UNAVAILABLE_COLOR + "; " +
                        "-fx-border-width: 2; ";
        }
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(0.0);
        shadow.setOffsetY(3.0);
        shadow.setColor(Color.color(0, 0, 0, 0.1));
        card.setEffect(shadow);
        
        card.setStyle(cardStyle);
        
        // Status indicator
        HBox statusBox = new HBox(10);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        
        Circle statusCircle = new Circle(6);
        statusCircle.setFill(hasQuestions ? Color.web(AVAILABLE_COLOR) : Color.web(UNAVAILABLE_COLOR));
        
        Label statusLabel = new Label(hasQuestions ? 
                                     "Available (" + questionCount + " questions)" : 
                                     "No questions available");
        statusLabel.setFont(Font.font("Segoe UI", 12));
        statusLabel.setTextFill(hasQuestions ? Color.web(AVAILABLE_COLOR) : Color.web(UNAVAILABLE_COLOR));
        
        statusBox.getChildren().addAll(statusCircle, statusLabel);
        
        // Category title
        Label title = new Label(categoryName);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setTextFill(Color.web(TITLE_COLOR));
        title.setWrapText(true);
        
        // Description
        Label desc = new Label(description != null ? description : "No description available.");
        desc.setWrapText(true);
        desc.setTextFill(Color.web(DESC_COLOR));
        desc.setFont(Font.font("Segoe UI", 14));
        desc.setPrefHeight(60);
        
        // Start button with conditional styling
        Button startBtn = new Button("Start Quiz");
        startBtn.setPrefWidth(240);
        startBtn.setPrefHeight(40);
        startBtn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        if (hasQuestions) {
            startBtn.setStyle("-fx-background-color: " + AVAILABLE_COLOR + "; " +
                             "-fx-text-fill: " + BUTTON_TEXT_COLOR + "; " +
                             "-fx-background-radius: 6; " +
                             "-fx-cursor: hand;");
            
            // Add hover effect
            startBtn.setOnMouseEntered(e -> 
                startBtn.setStyle("-fx-background-color: #219653; " +
                                 "-fx-text-fill: " + BUTTON_TEXT_COLOR + "; " +
                                 "-fx-background-radius: 6; " +
                                 "-fx-cursor: hand;")
            );
            startBtn.setOnMouseExited(e -> 
                startBtn.setStyle("-fx-background-color: " + AVAILABLE_COLOR + "; " +
                                 "-fx-text-fill: " + BUTTON_TEXT_COLOR + "; " +
                                 "-fx-background-radius: 6; " +
                                 "-fx-cursor: hand;")
            );
            
            // Set action to navigate to quiz
            startBtn.setOnAction(e -> {
                QuizQuestionsPage quizPage = new QuizQuestionsPage();
                Parent quizView = quizPage.getView(userId, categoryName);
                rootPane.getChildren().setAll(quizView);
            });
        } else {
            // Disabled style for categories without questions
            startBtn.setStyle("-fx-background-color: #bdc3c7; " +
                             "-fx-text-fill: " + BUTTON_TEXT_COLOR + "; " +
                             "-fx-background-radius: 6; " +
                             "-fx-opacity: 0.7;");
            startBtn.setDisable(true);
        }
        
        // Add components to card
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        card.getChildren().addAll(statusBox, title, desc, spacer, startBtn);
        return card;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        
        // Style the alert
        alert.getDialogPane().setStyle("-fx-background-color: " + CARD_BG_COLOR + ";");
        
        alert.showAndWait();
    }
}