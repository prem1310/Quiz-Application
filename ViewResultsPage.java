package User_package;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViewResultsPage {

    private final String username;
    private Connection connection;
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2c3e50";
    private final String BACKGROUND_COLOR = "#f5f5f5";
    private final String CARD_COLOR = "#ffffff";
    private final String SUCCESS_COLOR = "#2ecc71";
    private final String ERROR_COLOR = "#e74c3c";

    public ViewResultsPage(String username) {
        this.username = username;
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Parent getView() {
        // Main container
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox headerBox = createHeader();
        mainLayout.setTop(headerBox);
        
        // Center content
        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20, 30, 30, 30));
        contentBox.setAlignment(Pos.TOP_CENTER);
        
        // Category selector card
        VBox categoryCard = createCategorySelector();
        
        // Results table
        TableView<ResultItem> resultTable = new TableView<>();
        styleTable(resultTable);
        
        TableColumn<ResultItem, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cell -> cell.getValue().attemptedAtProperty());
        dateCol.setPrefWidth(200);
        
        TableColumn<ResultItem, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(cell -> cell.getValue().scoreProperty().asObject());
        scoreCol.setPrefWidth(100);
        
        TableColumn<ResultItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View Answers");
            
            {
                viewBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 15;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 4;");
                
                viewBtn.setOnMouseEntered(e -> 
                    viewBtn.setStyle("-fx-background-color: derive(" + PRIMARY_COLOR + ", 20%);" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 5 15;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-background-radius: 4;"));
                
                viewBtn.setOnMouseExited(e -> 
                    viewBtn.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 5 15;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-background-radius: 4;"));
                
                viewBtn.setOnAction(e -> {
                    ResultItem result = getTableView().getItems().get(getIndex());
                    showAnswersPopup(result.resultId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewBtn);
                }
            }
        });
        
        resultTable.getColumns().addAll(dateCol, scoreCol, actionCol);
        
        // Get the category combobox from the category card
        ComboBox<String> categoryBox = (ComboBox<String>) categoryCard.getChildren().get(1);
        
        categoryBox.setOnAction(e -> {
            String selectedCategory = categoryBox.getValue();
            if (selectedCategory != null) {
                List<ResultItem> results = loadResults(selectedCategory);
                resultTable.setItems(FXCollections.observableArrayList(results));
                
                // Show empty state message if no results
                if (results.isEmpty()) {
                    Label emptyLabel = new Label("No results found for this category");
                    emptyLabel.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";" +
                                       "-fx-font-size: 14px;");
                    resultTable.setPlaceholder(emptyLabel);
                }
            }
        });
        
        // Add components to content box
        contentBox.getChildren().addAll(categoryCard, resultTable);
        mainLayout.setCenter(contentBox);
        
        return mainLayout;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 15, 0));
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label heading = new Label("Your Quiz Results");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setStyle("-fx-text-fill: white;");
        
        Label usernameLabel = new Label("User: " + username);
        usernameLabel.setFont(Font.font("System", FontWeight.NORMAL, 14));
        usernameLabel.setStyle("-fx-text-fill: white;");
        
        header.getChildren().addAll(heading, usernameLabel);
        return header;
    }
    
    private VBox createCategorySelector() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setMaxWidth(600);
        card.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                     "-fx-background-radius: 8;" +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label title = new Label("Select Quiz Category");
        title.setFont(Font.font("System", FontWeight.BOLD, 16));
        title.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Choose a category to view results");
        categoryBox.setPrefWidth(400);
        categoryBox.setStyle("-fx-font-size: 14px;");
        loadCategories(categoryBox);
        
        card.getChildren().addAll(title, categoryBox);
        return card;
    }
    
    private void styleTable(TableView<ResultItem> table) {
        table.setPrefHeight(400);
        table.setMaxWidth(600);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply styles directly to the table and its components
        table.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                      "-fx-background-radius: 8;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        // Style the column headers
        table.setStyle(table.getStyle() + 
                      "-fx-table-header-background: " + SECONDARY_COLOR + ";" +
                      "-fx-table-header-border-color: transparent;");
        
        // Style for the table cells
        table.setRowFactory(tv -> {
            TableRow<ResultItem> row = new TableRow<>();
            row.setStyle("-fx-cell-size: 45px; -fx-padding: 5px;");
            
            // Add hover effect
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle(row.getStyle() + "-fx-background-color: derive(" + PRIMARY_COLOR + ", 80%);");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-cell-size: 45px; -fx-padding: 5px; -fx-background-color: " + CARD_COLOR + ";");
                    } else {
                        row.setStyle("-fx-cell-size: 45px; -fx-padding: 5px; -fx-background-color: derive(" + CARD_COLOR + ", -2%);");
                    }
                }
            });
            
            return row;
        });
        
        // Style for empty table message
        Label placeholder = new Label("Select a category to view results");
        placeholder.setStyle("-fx-text-fill: " + SECONDARY_COLOR + "; -fx-font-size: 14px;");
        table.setPlaceholder(placeholder);
    }

    private void loadCategories(ComboBox<String> comboBox) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT category_name FROM category");
            while (rs.next()) {
                comboBox.getItems().add(rs.getString("category_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<ResultItem> loadResults(String categoryName) {
        List<ResultItem> list = new ArrayList<>();
        try {
            String query = """
                SELECT r.result_id, r.score, r.attempted_at
                FROM result r
                JOIN category c ON r.category_id = c.category_id
                WHERE r.username = ? AND c.category_name = ?
                ORDER BY r.attempted_at DESC
            """;
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, categoryName);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ResultItem(
                        rs.getInt("result_id"),
                        rs.getInt("score"),
                        rs.getTimestamp("attempted_at").toString()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void showAnswersPopup(int resultId) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Quiz Answers");
        
        BorderPane popupLayout = new BorderPane();
        
        // Header
        VBox popupHeader = new VBox(5);
        popupHeader.setAlignment(Pos.CENTER);
        popupHeader.setPadding(new Insets(15));
        popupHeader.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label popupTitle = new Label("Your Answered Questions");
        popupTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        popupTitle.setStyle("-fx-text-fill: white;");
        
        Label popupSubtitle = new Label("Review your answers and see the correct ones");
        popupSubtitle.setFont(Font.font("System", FontWeight.NORMAL, 14));
        popupSubtitle.setStyle("-fx-text-fill: white;");
        
        popupHeader.getChildren().addAll(popupTitle, popupSubtitle);
        popupLayout.setTop(popupHeader);
        
        // Content
        VBox container = new VBox(15);
        container.setPadding(new Insets(25));
        container.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        try {
            String query = """
                SELECT q.question_text, q.opt1, q.opt2, q.opt3, q.opt4,
                       a.selected_option, a.correct_option
                FROM answered_questions a
                JOIN questions q ON a.question_id = q.question_id
                WHERE a.result_id = ?
            """;
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setInt(1, resultId);
            ResultSet rs = stmt.executeQuery();
            
            int questionNumber = 1;
            
            while (rs.next()) {
                VBox qBox = new VBox(10);
                qBox.setPadding(new Insets(20));
                qBox.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                             "-fx-background-radius: 8;" +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
                
                // Question number and text
                Label qNumber = new Label("Question " + questionNumber);
                qNumber.setFont(Font.font("System", FontWeight.BOLD, 14));
                qNumber.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
                
                Label qText = new Label(rs.getString("question_text"));
                qText.setFont(Font.font("System", FontWeight.NORMAL, 16));
                qText.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
                qText.setWrapText(true);
                
                // Options
                VBox optionsBox = new VBox(8);
                optionsBox.setPadding(new Insets(10, 0, 10, 0));
                
                String selectedOption = rs.getString("selected_option");
                String correctOption = rs.getString("correct_option");
                
                String[] optLabels = {"A", "B", "C", "D"};
                String[] options = {
                        rs.getString("opt1"),
                        rs.getString("opt2"),
                        rs.getString("opt3"),
                        rs.getString("opt4")
                };
                
                for (int i = 0; i < 4; i++) {
                    HBox optRow = new HBox(10);
                    optRow.setAlignment(Pos.CENTER_LEFT);
                    
                    // Option label (A, B, C, D)
                    StackPane optLabelBox = new StackPane();
                    optLabelBox.setPrefSize(30, 30);
                    
                    String optStyle = "-fx-background-radius: 15;";
                    
                    // Determine if this option was selected or correct
                    if (optLabels[i].equals(selectedOption) && optLabels[i].equals(correctOption)) {
                        // Correct answer and selected
                        optStyle += "-fx-background-color: " + SUCCESS_COLOR + ";";
                    } else if (optLabels[i].equals(selectedOption)) {
                        // Selected but incorrect
                        optStyle += "-fx-background-color: " + ERROR_COLOR + ";";
                    } else if (optLabels[i].equals(correctOption)) {
                        // Correct but not selected
                        optStyle += "-fx-background-color: " + SUCCESS_COLOR + ";";
                    } else {
                        // Neither selected nor correct
                        optStyle += "-fx-background-color: #e0e0e0;";
                    }
                    
                    optLabelBox.setStyle(optStyle);
                    
                    Label optLabel = new Label(optLabels[i]);
                    optLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                    optLabel.setStyle("-fx-text-fill: white;");
                    optLabelBox.getChildren().add(optLabel);
                    
                    // Option text
                    Label optText = new Label(options[i]);
                    optText.setFont(Font.font("System", FontWeight.NORMAL, 14));
                    optText.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
                    optText.setWrapText(true);
                    
                    optRow.getChildren().addAll(optLabelBox, optText);
                    optionsBox.getChildren().add(optRow);
                }
                
                // Result indicator
                HBox resultBox = new HBox(10);
                resultBox.setAlignment(Pos.CENTER_LEFT);
                resultBox.setPadding(new Insets(10, 0, 0, 0));
                
                Label resultLabel = new Label();
                resultLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                if (selectedOption.equals(correctOption)) {
                    resultLabel.setText("Correct Answer!");
                    resultLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + ";");
                } else {
                    resultLabel.setText("Incorrect. Correct answer: " + correctOption);
                    resultLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
                }
                
                resultBox.getChildren().add(resultLabel);
                
                qBox.getChildren().addAll(qNumber, qText, optionsBox, resultBox);
                container.getChildren().add(qBox);
                
                questionNumber++;
            }
            
            if (questionNumber == 1) {
                // No questions found
                Label noQuestionsLabel = new Label("No questions found for this result");
                noQuestionsLabel.setFont(Font.font("System", FontWeight.NORMAL, 16));
                noQuestionsLabel.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
                container.getChildren().add(noQuestionsLabel);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            
            // Show error message
            Label errorLabel = new Label("Error loading questions: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
            container.getChildren().add(errorLabel);
        }
        
        // Footer with close button
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(15));
        footer.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        Button closeButton = new Button("Close");
        closeButton.setPrefWidth(100);
        closeButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 10 20;" +
                            "-fx-cursor: hand;" +
                            "-fx-background-radius: 4;");
        
        closeButton.setOnMouseEntered(e -> 
            closeButton.setStyle("-fx-background-color: derive(" + SECONDARY_COLOR + ", 20%);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 4;"));
        
        closeButton.setOnMouseExited(e -> 
            closeButton.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 10 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 4;"));
        
        closeButton.setOnAction(e -> popup.close());
        
        footer.getChildren().add(closeButton);
        popupLayout.setBottom(footer);
        
        ScrollPane scroll = new ScrollPane(container);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + BACKGROUND_COLOR + ";" +
                       "-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        popupLayout.setCenter(scroll);
        
        Scene scene = new Scene(popupLayout, 650, 600);
        popup.setScene(scene);
        popup.showAndWait();
    }

    // Helper class for result table
    private static class ResultItem {
        private final int resultId;
        private final javafx.beans.property.IntegerProperty score;
        private final javafx.beans.property.SimpleStringProperty attemptedAt;

        public ResultItem(int resultId, int score, String attemptedAt) {
            this.resultId = resultId;
            this.score = new javafx.beans.property.SimpleIntegerProperty(score);
            this.attemptedAt = new javafx.beans.property.SimpleStringProperty(attemptedAt);
        }

        public int resultId() {
            return resultId;
        }

        public javafx.beans.property.IntegerProperty scoreProperty() {
            return score;
        }

        public javafx.beans.property.SimpleStringProperty attemptedAtProperty() {
            return attemptedAt;
        }
    }
}