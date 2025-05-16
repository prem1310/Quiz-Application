package Admin_package;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.*;
import java.util.Optional;

public class AdminViewResultsPage {
    private Connection connection;
    private ComboBox<Category> categoryComboBox;
    private TableView<ResultEntry> resultTable;
    private Label resultCountLabel;
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2ecc71";
    private final String ACCENT_COLOR = "#e74c3c";
    private final String BACKGROUND_COLOR = "#f8f9fa";
    private final String CARD_COLOR = "#ffffff";
    private final String TEXT_COLOR = "#2c3e50";
    private final String LIGHT_TEXT_COLOR = "#7f8c8d";

    public AdminViewResultsPage() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Database Connection Error", "Failed to connect to the database: " + e.getMessage());
        }
    }

    public Parent getView() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox headerBox = createHeaderSection();
        mainLayout.setTop(headerBox);
        
        // Filter section
        HBox filterBox = createFilterSection();
        
        // Results count label
        resultCountLabel = new Label();
        resultCountLabel.setFont(Font.font("Segoe UI", 14));
        resultCountLabel.setTextFill(Color.web(LIGHT_TEXT_COLOR));
        
        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(0, 20, 10, 20));
        topSection.getChildren().addAll(filterBox, resultCountLabel);
        
        // Table section
        VBox tableSection = createTableSection();
        
        // Combine sections
        VBox contentBox = new VBox(15);
        contentBox.getChildren().addAll(topSection, tableSection);
        mainLayout.setCenter(contentBox);
        
        // Load data
        loadCategories();
        loadResults(); // Load all results initially
        
        return mainLayout;
    }
    
    private VBox createHeaderSection() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20, 20, 15, 20));
        headerBox.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 3);");
        
        Label heading = new Label("Admin View: Quiz Results");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        heading.setTextFill(Color.WHITE);
        
        Label subheading = new Label("View and manage all user quiz results");
        subheading.setFont(Font.font("Segoe UI", 14));
        subheading.setTextFill(Color.web("#ffffff", 0.8));
        
        headerBox.getChildren().addAll(heading, subheading);
        return headerBox;
    }
    
    private HBox createFilterSection() {
        HBox filterBox = new HBox(15);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        Label filterLabel = new Label("Filter by Category:");
        filterLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        filterLabel.setTextFill(Color.web(TEXT_COLOR));
        
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("All Categories");
        categoryComboBox.setMinWidth(200);
        categoryComboBox.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 4px; " +
            "-fx-background-color: white; " +
            "-fx-border-color: #dcdcdc; " +
            "-fx-border-radius: 4px;"
        );
        
        Button loadBtn = new Button("Apply Filter");
        loadBtn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        loadBtn.setPrefHeight(35);
        loadBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        
        // Add hover effect
        loadBtn.setOnMouseEntered(e -> 
            loadBtn.setStyle(
                "-fx-background-color: #2980b9; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 4px; " +
                "-fx-cursor: hand;"
            )
        );
        loadBtn.setOnMouseExited(e -> 
            loadBtn.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 4px; " +
                "-fx-cursor: hand;"
            )
        );
        
        loadBtn.setOnAction(e -> loadResults());
        
        Button resetBtn = new Button("Reset");
        resetBtn.setFont(Font.font("Segoe UI", 14));
        resetBtn.setPrefHeight(35);
        resetBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + PRIMARY_COLOR + "; " +
            "-fx-border-color: " + PRIMARY_COLOR + "; " +
            "-fx-border-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        
        resetBtn.setOnAction(e -> {
            categoryComboBox.setValue(null);
            loadResults();
        });
        
        filterBox.getChildren().addAll(filterLabel, categoryComboBox, loadBtn, resetBtn);
        return filterBox;
    }
    
    private VBox createTableSection() {
        VBox tableBox = new VBox();
        tableBox.setPadding(new Insets(0, 20, 20, 20));
        
        // Create table with styling
        resultTable = new TableView<>();
        resultTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        resultTable.setStyle(
            "-fx-background-color: " + CARD_COLOR + "; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        
        // Username column
        TableColumn<ResultEntry, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        userCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                }
            }
        });
        
        // Category column
        TableColumn<ResultEntry, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px;");
                }
            }
        });
        
        // Result ID column
        TableColumn<ResultEntry, Integer> idCol = new TableColumn<>("Result ID");
        idCol.setCellValueFactory(data -> data.getValue().resultIdProperty().asObject());
        idCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 14px; -fx-alignment: center;");
                }
            }
        });
        
        // Score column
        TableColumn<ResultEntry, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(data -> data.getValue().scoreProperty().asObject());
        scoreCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 14px; -fx-alignment: center; -fx-font-weight: bold;");
                }
            }
        });
        
        // Date column
        TableColumn<ResultEntry, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data -> data.getValue().attemptedAtProperty());
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px;");
                }
            }
        });
        
        // View Answers column
        TableColumn<ResultEntry, Void> viewCol = new TableColumn<>("View Answers");
        viewCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle(
                    "-fx-background-color: " + PRIMARY_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 4px; " +
                    "-fx-cursor: hand;"
                );
                
                viewBtn.setOnAction(e -> {
                    ResultEntry result = getTableView().getItems().get(getIndex());
                    showAnswers(result);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });
        
        // Edit column
        TableColumn<ResultEntry, Void> editCol = new TableColumn<>("Edit");
        editCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            {
                editBtn.setStyle(
                    "-fx-background-color: " + SECONDARY_COLOR + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 4px; " +
                    "-fx-cursor: hand;"
                );
                
                editBtn.setOnAction(e -> {
                    ResultEntry result = getTableView().getItems().get(getIndex());
                    editResult(result);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editBtn);
            }
        });
        
        // Add columns to table
        resultTable.getColumns().addAll(userCol, categoryCol, idCol, scoreCol, dateCol, viewCol, editCol);
        
        // Set minimum height
        resultTable.setPrefHeight(500);
        
        tableBox.getChildren().add(resultTable);
        return tableBox;
    }

    private void loadCategories() {
        categoryComboBox.getItems().clear();
        
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM category ORDER BY category_name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                categoryComboBox.getItems().add(new Category(rs.getInt("category_id"), rs.getString("category_name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void loadResults() {
        Category selectedCategory = categoryComboBox.getValue();
        ObservableList<ResultEntry> results = FXCollections.observableArrayList();

        String sql = """
            SELECT r.result_id, r.score, r.attempted_at, r.total_questions, u.username, c.category_name
            FROM result r
            JOIN users u ON r.username = u.username
            JOIN category c ON r.category_id = c.category_id
        """;

        if (selectedCategory != null) {
            sql += " WHERE c.category_id = ? AND u.role = 'user' ORDER BY r.attempted_at DESC";
        } else {
            sql += " WHERE u.role = 'user' ORDER BY r.attempted_at DESC";
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            if (selectedCategory != null) {
                stmt.setInt(1, selectedCategory.categoryId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(new ResultEntry(
                            rs.getInt("result_id"),
                            rs.getInt("score"),
                            rs.getTimestamp("attempted_at").toString(),
                            rs.getInt("total_questions"),
                            rs.getString("username"),
                            rs.getString("category_name")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load results: " + e.getMessage());
        }

        resultTable.setItems(results);
        
        // Update count label
        String categoryText = selectedCategory != null ? " for " + selectedCategory.categoryName() : "";
        resultCountLabel.setText("Showing " + results.size() + " results" + categoryText);
    }

    private void showAnswers(ResultEntry result) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Detailed Answers");
        
        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20, 20, 15, 20));
        headerBox.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");
        
        Label heading = new Label("Quiz Answers");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        heading.setTextFill(Color.WHITE);
        
        Label subheading = new Label(
            "User: " + result.usernameProperty().get() + 
            " | Category: " + result.categoryProperty().get() + 
            " | Score: " + result.getScore() + "/" + result.getTotalQuestions()
        );
        subheading.setFont(Font.font("Segoe UI", 14));
        subheading.setTextFill(Color.web("#ffffff", 0.9));
        
        headerBox.getChildren().addAll(heading, subheading);
        mainLayout.setTop(headerBox);
        
        // Create table with styling
        TableView<AnswerEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle(
            "-fx-background-color: " + CARD_COLOR + "; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );
        
        // Question ID column
        TableColumn<AnswerEntry, Integer> qidCol = new TableColumn<>("Question ID");
        qidCol.setCellValueFactory(data -> data.getValue().questionIdProperty().asObject());
        qidCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("-fx-font-size: 14px; -fx-alignment: center;");
                }
            }
        });
        
        // Question text column
        TableColumn<AnswerEntry, String> questionCol = new TableColumn<>("Question");
        questionCol.setCellValueFactory(data -> data.getValue().questionTextProperty());
        questionCol.setPrefWidth(250);
        questionCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-wrap-text: true;");
                }
            }
        });
        
        // Selected option column
        TableColumn<AnswerEntry, String> selectedCol = new TableColumn<>("Selected");
        selectedCol.setCellValueFactory(data -> data.getValue().selectedOptionProperty());
        selectedCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Get the correct option for this row
                    AnswerEntry entry = getTableView().getItems().get(getIndex());
                    String correctOption = entry.correctOptionProperty().get();
                    
                    // Style based on correctness
                    if (item.equals(correctOption)) {
                        setStyle("-fx-font-size: 14px; -fx-text-fill: " + SECONDARY_COLOR + "; -fx-font-weight: bold;");
                    } else if (item.equals("X")) {
                        setStyle("-fx-font-size: 14px; -fx-text-fill: " + LIGHT_TEXT_COLOR + "; -fx-font-style: italic;");
                        setText("Not answered");
                    } else {
                        setStyle("-fx-font-size: 14px; -fx-text-fill: " + ACCENT_COLOR + ";");
                    }
                }
            }
        });
        
        // Correct option column
        TableColumn<AnswerEntry, String> correctCol = new TableColumn<>("Correct");
        correctCol.setCellValueFactory(data -> data.getValue().correctOptionProperty());
        correctCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-size: 14px; -fx-text-fill: " + SECONDARY_COLOR + "; -fx-font-weight: bold;");
                }
            }
        });
        
        table.getColumns().addAll(qidCol, questionCol, selectedCol, correctCol);
        
        // Load answers
        ObservableList<AnswerEntry> answers = FXCollections.observableArrayList();
        
        try (PreparedStatement stmt = connection.prepareStatement("""
            SELECT aq.question_id, q.question_text, aq.selected_option, aq.correct_option 
            FROM answered_questions aq
            JOIN questions q ON aq.question_id = q.question_id
            WHERE aq.result_id = ?
        """)) {
            stmt.setInt(1, result.getResultId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    answers.add(new AnswerEntry(
                            rs.getInt("question_id"),
                            rs.getString("question_text"),
                            rs.getString("selected_option"),
                            rs.getString("correct_option")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showErrorAlert("Error", "Failed to load answers: " + e.getMessage());
        }
        
        table.setItems(answers);
        
        // Add table to a container with padding
        VBox tableContainer = new VBox(table);
        tableContainer.setPadding(new Insets(20));
        mainLayout.setCenter(tableContainer);
        
        // Footer with close button
        HBox footerBox = new HBox();
        footerBox.setPadding(new Insets(0, 20, 20, 20));
        footerBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button closeBtn = new Button("Close");
        closeBtn.setPrefWidth(100);
        closeBtn.setPrefHeight(35);
        closeBtn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        closeBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> popup.close());
        
        footerBox.getChildren().add(closeBtn);
        mainLayout.setBottom(footerBox);
        
        // Set scene and show
        Scene scene = new Scene(mainLayout, 700, 600);
        popup.setScene(scene);
        
        // Add drop shadow to the stage
        popup.initStyle(StageStyle.DECORATED);
        
        popup.showAndWait();
    }

    private void editResult(ResultEntry result) {
        // Create custom dialog
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Score");
        dialog.initStyle(StageStyle.DECORATED);
        
        // Create layout
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        // Header
        Label headerLabel = new Label("Edit Quiz Score");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.web(TEXT_COLOR));
        
        // Info text
        VBox infoBox = new VBox(5);
        infoBox.setStyle(
            "-fx-background-color: rgba(52, 152, 219, 0.1); " +
            "-fx-padding: 10; " +
            "-fx-background-radius: 4;"
        );
        
        Label userLabel = new Label("User: " + result.usernameProperty().get());
        userLabel.setFont(Font.font("Segoe UI", 14));
        
        Label categoryLabel = new Label("Category: " + result.categoryProperty().get());
        categoryLabel.setFont(Font.font("Segoe UI", 14));
        
        Label resultIdLabel = new Label("Result ID: " + result.getResultId());
        resultIdLabel.setFont(Font.font("Segoe UI", 14));
        
        infoBox.getChildren().addAll(userLabel, categoryLabel, resultIdLabel);
        
        // Score input
        HBox scoreBox = new HBox(10);
        scoreBox.setAlignment(Pos.CENTER_LEFT);
        
        Label scoreLabel = new Label("New Score:");
        scoreLabel.setFont(Font.font("Segoe UI", 14));
        
        TextField scoreField = new TextField(String.valueOf(result.getScore()));
        scoreField.setPrefWidth(100);
        scoreField.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 4px; " +
            "-fx-border-radius: 4px; " +
            "-fx-border-color: #dcdcdc;"
        );
        
        Label maxLabel = new Label("/ " + result.getTotalQuestions());
        maxLabel.setFont(Font.font("Segoe UI", 14));
        
        scoreBox.getChildren().addAll(scoreLabel, scoreField, maxLabel);
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(100);
        cancelBtn.setPrefHeight(35);
        cancelBtn.setFont(Font.font("Segoe UI", 14));
        cancelBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: " + TEXT_COLOR + "; " +
            "-fx-border-color: #dcdcdc; " +
            "-fx-border-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialog.close());
        
        Button saveBtn = new Button("Save");
        saveBtn.setPrefWidth(100);
        saveBtn.setPrefHeight(35);
        saveBtn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        saveBtn.setStyle(
            "-fx-background-color: " + SECONDARY_COLOR + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 4px; " +
            "-fx-cursor: hand;"
        );
        
        saveBtn.setOnAction(e -> {
            try {
                int newScore = Integer.parseInt(scoreField.getText().trim());
                
                // Validate score
                if (newScore < 0 || newScore > result.getTotalQuestions()) {
                    showErrorAlert("Invalid Score", 
                        "Score must be between 0 and " + result.getTotalQuestions());
                    return;
                }
                
                // Update in database
                try (PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE result SET score = ? WHERE result_id = ?")) {
                    stmt.setInt(1, newScore);
                    stmt.setInt(2, result.getResultId());
                    stmt.executeUpdate();
                    
                    // Update in table
                    result.setScore(newScore);
                    resultTable.refresh();
                    
                    dialog.close();
                }
            } catch (NumberFormatException ex) {
                showErrorAlert("Invalid Input", "Please enter a valid number");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showErrorAlert("Database Error", "Failed to update score: " + ex.getMessage());
            }
        });
        
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);
        
        // Add all to layout
        layout.getChildren().addAll(headerLabel, infoBox, scoreBox, buttonBox);
        
        // Set scene and show
        Scene scene = new Scene(layout, 400, 300);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + CARD_COLOR + ";");
        
        alert.showAndWait();
    }

    public record Category(int categoryId, String categoryName) {
        @Override public String toString() { return categoryName; }
    }

    public static class ResultEntry {
        private final SimpleIntegerProperty resultId;
        private final SimpleIntegerProperty score;
        private final SimpleIntegerProperty totalQuestions;
        private final SimpleStringProperty attemptedAt;
        private final SimpleStringProperty username;
        private final SimpleStringProperty category;

        public ResultEntry(int resultId, int score, String attemptedAt, int totalQuestions, String username, String category) {
            this.resultId = new SimpleIntegerProperty(resultId);
            this.score = new SimpleIntegerProperty(score);
            this.totalQuestions = new SimpleIntegerProperty(totalQuestions);
            this.attemptedAt = new SimpleStringProperty(attemptedAt);
            this.username = new SimpleStringProperty(username);
            this.category = new SimpleStringProperty(category);
        }

        public int getResultId() { return resultId.get(); }
        public SimpleIntegerProperty resultIdProperty() { return resultId; }
        
        public int getScore() { return score.get(); }
        public void setScore(int score) { this.score.set(score); }
        public SimpleIntegerProperty scoreProperty() { return score; }
        
        public int getTotalQuestions() { return totalQuestions.get(); }
        public SimpleIntegerProperty totalQuestionsProperty() { return totalQuestions; }
        
        public SimpleStringProperty attemptedAtProperty() { return attemptedAt; }
        public SimpleStringProperty usernameProperty() { return username; }
        public SimpleStringProperty categoryProperty() { return category; }
    }

    public static class AnswerEntry {
        private final SimpleIntegerProperty questionId;
        private final SimpleStringProperty questionText;
        private final SimpleStringProperty selectedOption;
        private final SimpleStringProperty correctOption;

        public AnswerEntry(int questionId, String questionText, String selectedOption, String correctOption) {
            this.questionId = new SimpleIntegerProperty(questionId);
            this.questionText = new SimpleStringProperty(questionText);
            this.selectedOption = new SimpleStringProperty(selectedOption);
            this.correctOption = new SimpleStringProperty(correctOption);
        }

        public SimpleIntegerProperty questionIdProperty() { return questionId; }
        public SimpleStringProperty questionTextProperty() { return questionText; }
        public SimpleStringProperty selectedOptionProperty() { return selectedOption; }
        public SimpleStringProperty correctOptionProperty() { return correctOption; }
    }
}