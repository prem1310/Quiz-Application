package User_package;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class QuizQuestionsPage {

    private Connection connection;
    private List<QuestionItem> questions;
    private Map<Integer, Character> userAnswers;
    private LocalDateTime startTime;
    private Label timerLabel;
    private Timeline timeline;
    private Stage stage;
    private VBox questionBox;
    private List<VBox> questionBoxes;

    private final int QUIZ_DURATION_MINUTES = 15;
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2ecc71";
    private final String ACCENT_COLOR = "#e74c3c";
    private final String BACKGROUND_COLOR = "#f8f9fa";
    private final String QUESTION_BG_COLOR = "#ffffff";
    private final String TEXT_COLOR = "#2c3e50";

    public Parent getView(String userId, String categoryName) {
        startTime = LocalDateTime.now();
        questionBoxes = new ArrayList<>();
        
        // Main container with gradient background
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox headerBox = createHeaderSection(categoryName);
        mainLayout.setTop(headerBox);
        
        // Questions section
        ScrollPane scrollPane = createQuestionsSection(categoryName);
        mainLayout.setCenter(scrollPane);
        
        // Footer with buttons
        HBox footerBox = createFooterSection(userId, categoryName);
        mainLayout.setBottom(footerBox);
        
        return mainLayout;
    }
    
    private VBox createHeaderSection(String categoryName) {
        VBox headerBox = new VBox(15);
        headerBox.setPadding(new Insets(20, 20, 10, 20));
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        // Quiz title
        Label heading = new Label("Quiz: " + categoryName);
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.WHITE);
        
        // Timer with styling
        timerLabel = new Label("Time Remaining: 15:00");
        timerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        timerLabel.setTextFill(Color.WHITE);
        timerLabel.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 5 15; -fx-background-radius: 20;");
        
        headerBox.getChildren().addAll(heading, timerLabel);
        startTimer();
        
        return headerBox;
    }
    
    private ScrollPane createQuestionsSection(String categoryName) {
        // Create scrollable area for questions
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + BACKGROUND_COLOR + "; -fx-background-color: transparent;");
        scrollPane.setPadding(new Insets(0));
        
        // Container for all questions
        questionBox = new VBox(20);
        questionBox.setPadding(new Insets(20));
        scrollPane.setContent(questionBox);
        
        userAnswers = new HashMap<>();
        
        // Load questions from database
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
            questions = loadQuestions(categoryName);
            
            // Create UI for each question
            for (int i = 0; i < questions.size(); i++) {
                QuestionItem q = questions.get(i);
                VBox qBox = createQuestionBox(q, i);
                questionBox.getChildren().add(qBox);
                questionBoxes.add(qBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading questions: " + e.getMessage());
            errorLabel.setTextFill(Color.RED);
            questionBox.getChildren().add(errorLabel);
        }
        
        return scrollPane;
    }
    
    private VBox createQuestionBox(QuestionItem q, int index) {
        // Question container with card-like styling
        VBox qBox = new VBox(12);
        qBox.setPadding(new Insets(20));
        qBox.setStyle(
            "-fx-background-color: " + QUESTION_BG_COLOR + "; " +
            "-fx-background-radius: 8; " +
            "-fx-border-radius: 8; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        // Question number and text
        Label questionNumber = new Label("Question " + (index + 1));
        questionNumber.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        questionNumber.setTextFill(Color.web(PRIMARY_COLOR));
        
        Label questionText = new Label(q.questionText);
        questionText.setFont(Font.font("Segoe UI", 16));
        questionText.setTextFill(Color.web(TEXT_COLOR));
        questionText.setWrapText(true);
        
        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(5, 0, 5, 0));
        
        // Options group
        ToggleGroup group = new ToggleGroup();
        
        // Create styled radio buttons for options
        VBox optionsBox = new VBox(10);
        RadioButton[] options = new RadioButton[4];
        
        options[0] = createStyledRadioButton("A", q.opt1, group);
        options[1] = createStyledRadioButton("B", q.opt2, group);
        options[2] = createStyledRadioButton("C", q.opt3, group);
        options[3] = createStyledRadioButton("D", q.opt4, group);
        
        optionsBox.getChildren().addAll(options);
        
        // Add listener to track selected answer
        int questionId = q.questionId;
        group.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                String selected = ((RadioButton) newToggle).getUserData().toString();
                userAnswers.put(questionId, selected.charAt(0));
            }
        });
        
        qBox.getChildren().addAll(questionNumber, questionText, separator, optionsBox);
        return qBox;
    }
    
    private RadioButton createStyledRadioButton(String optionLetter, String optionText, ToggleGroup group) {
        RadioButton rb = new RadioButton(optionText);
        rb.setFont(Font.font("Segoe UI", 14));
        rb.setTextFill(Color.web(TEXT_COLOR));
        rb.setToggleGroup(group);
        rb.setUserData(optionLetter);
        rb.setPadding(new Insets(5, 10, 5, 10));
        rb.setWrapText(true);
        
        // Style the radio button
        rb.setStyle("-fx-cursor: hand;");
        
        // Add hover effect
        rb.setOnMouseEntered(e -> 
            rb.setStyle("-fx-cursor: hand; -fx-background-color: rgba(52, 152, 219, 0.1); -fx-background-radius: 5;")
        );
        rb.setOnMouseExited(e -> 
            rb.setStyle("-fx-cursor: hand;")
        );
        
        return rb;
    }
    
    private HBox createFooterSection(String userId, String categoryName) {
        HBox footerBox = new HBox(15);
        footerBox.setPadding(new Insets(15, 20, 20, 20));
        footerBox.setAlignment(Pos.CENTER);
        
        // Back button
        Button backButton = new Button("Back to Dashboard");
        backButton.setPrefHeight(40);
        backButton.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        backButton.setStyle(
            "-fx-background-color: " + ACCENT_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect
        backButton.setOnMouseEntered(e -> 
            backButton.setStyle(
                "-fx-background-color: #c0392b; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        backButton.setOnMouseExited(e -> 
            backButton.setStyle(
                "-fx-background-color: " + ACCENT_COLOR + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        
        backButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Exit");
            alert.setHeaderText("Are you sure you want to return to the dashboard?");
            alert.setContentText("All progress will be lost.");
            
            // Style the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
            dialogPane.getStyleClass().add("custom-alert");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (timeline != null) timeline.stop();
                Stage currentStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
                new UserDashboard(userId).start(currentStage);
            }
        });
        
        // Submit button
        Button submitBtn = new Button("Submit Quiz");
        submitBtn.setPrefHeight(40);
        submitBtn.setPrefWidth(150);
        submitBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        submitBtn.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect
        submitBtn.setOnMouseEntered(e -> 
            submitBtn.setStyle(
                "-fx-background-color: #27ae60; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        submitBtn.setOnMouseExited(e -> 
            submitBtn.setStyle(
                "-fx-background-color: " + SECONDARY_COLOR + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        
        submitBtn.setOnAction(e -> {
            if (timeline != null) timeline.stop();
            Stage currentStage = (Stage) ((Button) e.getSource()).getScene().getWindow();
            submitQuiz(userId, categoryName, currentStage);
        });
        
        // Add spacer to push buttons to opposite sides
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        footerBox.getChildren().addAll(backButton, spacer, submitBtn);
        return footerBox;
    }

    private void startTimer() {
        long endTimeMillis = System.currentTimeMillis() + QUIZ_DURATION_MINUTES * 60 * 1000;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long remainingMillis = endTimeMillis - System.currentTimeMillis();
            if (remainingMillis <= 0) {
                timerLabel.setText("Time's up!");
                timerLabel.setTextFill(Color.web(ACCENT_COLOR));
                timerLabel.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-padding: 5 15; -fx-background-radius: 20;");
                timeline.stop();
                
                // Auto-submit when time is up
                if (stage != null) {
                    submitQuiz("timeout", "timeout", stage);
                }
            } else {
                long minutes = (remainingMillis / 1000) / 60;
                long seconds = (remainingMillis / 1000) % 60;
                
                // Change color when time is running low (less than 2 minutes)
                if (minutes < 2) {
                    timerLabel.setTextFill(Color.web(ACCENT_COLOR));
                    
                    // Flash effect when less than 1 minute
                    if (minutes < 1) {
                        timerLabel.setStyle("-fx-background-color: rgba(231, 76, 60, 0.3); -fx-padding: 5 15; -fx-background-radius: 20;");
                    }
                }
                
                timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private List<QuestionItem> loadQuestions(String category) throws SQLException {
        List<QuestionItem> list = new ArrayList<>();
        String query = """
            SELECT q.* FROM questions q
            INNER JOIN category c ON q.category_id = c.category_id
            WHERE c.category_name = ?
            ORDER BY RAND()
            LIMIT 20
        """;
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, category);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            list.add(new QuestionItem(
                    rs.getInt("question_id"),
                    rs.getString("question_text"),
                    rs.getString("opt1"),
                    rs.getString("opt2"),
                    rs.getString("opt3"),
                    rs.getString("opt4"),
                    rs.getString("correct_option").charAt(0)
            ));
        }
        return list;
    }

    private void submitQuiz(String userId, String category, Stage currentStage) {
        int score = 0;
        int total = questions.size();
        
        // Calculate score and highlight correct/incorrect answers
        for (int i = 0; i < questions.size(); i++) {
            QuestionItem q = questions.get(i);
            char selected = userAnswers.getOrDefault(q.questionId, 'X');
            
            if (selected == q.correctOption) {
                score++;
            }
        }

        try {
            connection.setAutoCommit(false);
            int categoryId = getCategoryId(category);
            
            // Save results to database
            PreparedStatement resultStmt = connection.prepareStatement(
                    "INSERT INTO result (username, category_id, score, total_questions, start_time, end_time) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            resultStmt.setString(1, userId);
            resultStmt.setInt(2, categoryId);
            resultStmt.setInt(3, score);
            resultStmt.setInt(4, total);
            resultStmt.setTimestamp(5, Timestamp.valueOf(startTime));
            resultStmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            resultStmt.executeUpdate();

            ResultSet rs = resultStmt.getGeneratedKeys();
            rs.next();
            int resultId = rs.getInt(1);

            // Save individual answers
            PreparedStatement answerStmt = connection.prepareStatement(
                    "INSERT INTO answered_questions (result_id, question_id, selected_option, correct_option) VALUES (?, ?, ?, ?)"
            );
            for (QuestionItem q : questions) {
                char selected = userAnswers.getOrDefault(q.questionId, 'X');
                answerStmt.setInt(1, resultId);
                answerStmt.setInt(2, q.questionId);
                answerStmt.setString(3, String.valueOf(selected));
                answerStmt.setString(4, String.valueOf(q.correctOption));
                answerStmt.addBatch();
            }
            answerStmt.executeBatch();
            connection.commit();

            // Show results dialog with styling
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quiz Completed");
            
            // Calculate percentage
            double percentage = (double) score / total * 100;
            String resultMessage;
            String resultColor;
            
            // Determine message and color based on score
            if (percentage >= 80) {
                resultMessage = "Excellent!";
                resultColor = "#2ecc71"; // Green
            } else if (percentage >= 60) {
                resultMessage = "Good job!";
                resultColor = "#3498db"; // Blue
            } else if (percentage >= 40) {
                resultMessage = "Not bad!";
                resultColor = "#f39c12"; // Orange
            } else {
                resultMessage = "Keep practicing!";
                resultColor = "#e74c3c"; // Red
            }
            
            alert.setHeaderText(resultMessage);
            alert.setContentText(String.format("You scored %d out of %d (%.1f%%)", score, total, percentage));
            
            // Style the alert
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
            dialogPane.getStyleClass().add("custom-alert");
            
            // Custom header with color
            Label headerLabel = new Label(resultMessage);
            headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            headerLabel.setTextFill(Color.web(resultColor));
            dialogPane.setHeader(headerLabel);

            // Wait for user to click OK
            Optional<ButtonType> result = alert.showAndWait();

            // After OK is clicked, redirect
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    new UserDashboard(userId).start(currentStage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            
            // Show error alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to submit quiz");
            errorAlert.setContentText("An error occurred: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private int getCategoryId(String category) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("SELECT category_id FROM category WHERE category_name = ?");
        stmt.setString(1, category);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        return rs.getInt("category_id");
    }

    private static class QuestionItem {
        int questionId;
        String questionText, opt1, opt2, opt3, opt4;
        char correctOption;

        public QuestionItem(int questionId, String questionText, String opt1, String opt2, String opt3, String opt4, char correctOption) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.opt1 = opt1;
            this.opt2 = opt2;
            this.opt3 = opt3;
            this.opt4 = opt4;
            this.correctOption = correctOption;
        }
    }
}