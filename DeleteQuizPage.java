package Admin_package;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.scene.Node;

import java.sql.*;
import java.util.Optional;

public class DeleteQuizPage {

    private TextField searchField;
    private ComboBox<String> categoryComboBox;
    private TableView<QuestionRow> tableView;
    private Button deleteButton;
    private Connection connection;
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2c3e50";
    private final String BACKGROUND_COLOR = "#f5f5f5";
    private final String CARD_COLOR = "#ffffff";
    private final String SUCCESS_COLOR = "#2ecc71";
    private final String ERROR_COLOR = "#e74c3c";
    private final String WARNING_COLOR = "#f39c12";

    public Parent getView() {
        // Main container with background
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox header = createHeader();
        mainLayout.setTop(header);
        
        // Content area
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Search card
        VBox searchCard = createSearchCard();
        
        // Table card
        VBox tableCard = createTableCard();
        
        // Action buttons
        HBox actionButtons = createActionButtons();
        
        content.getChildren().addAll(searchCard, tableCard, actionButtons);
        mainLayout.setCenter(content);
        
        try {
            // Initialize database connection
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
            loadCategories();
            loadAllQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Connection Error", 
                     "Unable to connect to the database. Please check your connection settings.", 
                     "The application couldn't establish a connection to the quiz database.");
        }
        
        return mainLayout;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 15, 0));
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label heading = new Label("Delete Quiz Questions");
        heading.setFont(Font.font("System", FontWeight.BOLD, 28));
        heading.setStyle("-fx-text-fill: white;");
        
        Label subheading = new Label("Search and remove questions from your quiz database");
        subheading.setFont(Font.font("System", FontWeight.NORMAL, 14));
        subheading.setStyle("-fx-text-fill: white;");
        
        header.getChildren().addAll(heading, subheading);
        
        // Add shadow effect to header
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetY(2);
        shadow.setRadius(5);
        header.setEffect(shadow);
        
        return header;
    }
    
    private VBox createSearchCard() {
        VBox searchCard = new VBox(15);
        searchCard.setPadding(new Insets(20));
        searchCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                           "-fx-background-radius: 8;" +
                           "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label searchTitle = new Label("Search Questions");
        searchTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        searchTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Search controls
        GridPane searchGrid = new GridPane();
        searchGrid.setHgap(15);
        searchGrid.setVgap(10);
        searchGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Question search field
        Label searchLabel = new Label("Question Text:");
        searchLabel.setStyle("-fx-font-weight: bold;");
        
        searchField = new TextField();
        searchField.setPromptText("Enter keywords to search...");
        searchField.setPrefHeight(35);
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 4;");
        
        // Category dropdown
        Label categoryLabel = new Label("Category:");
        categoryLabel.setStyle("-fx-font-weight: bold;");
        
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("All Categories");
        categoryComboBox.setPrefHeight(35);
        categoryComboBox.setPrefWidth(200);
        categoryComboBox.setStyle("-fx-background-radius: 4;");
        
        // Search button
        Button searchButton = createStyledButton("Search", PRIMARY_COLOR);
        searchButton.setGraphic(createIcon("search", 16));
        searchButton.setOnAction(e -> {
            animateNode(searchButton);
            searchQuestions(searchField.getText(), categoryComboBox.getValue());
        });
        
        // Reset button
        Button resetButton = createStyledButton("Reset", "#95a5a6");
        resetButton.setOnAction(e -> {
            searchField.clear();
            categoryComboBox.setValue(null);
            loadAllQuestions();
        });
        
        // Add components to grid
        searchGrid.add(searchLabel, 0, 0);
        searchGrid.add(searchField, 1, 0);
        searchGrid.add(categoryLabel, 2, 0);
        searchGrid.add(categoryComboBox, 3, 0);
        searchGrid.add(searchButton, 4, 0);
        searchGrid.add(resetButton, 5, 0);
        
        // Set column constraints
        ColumnConstraints labelCol1 = new ColumnConstraints();
        labelCol1.setMinWidth(100);
        
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setMinWidth(250);
        
        ColumnConstraints labelCol2 = new ColumnConstraints();
        labelCol2.setMinWidth(80);
        
        ColumnConstraints comboCol = new ColumnConstraints();
        comboCol.setMinWidth(150);
        
        ColumnConstraints buttonCol = new ColumnConstraints();
        buttonCol.setMinWidth(100);
        
        searchGrid.getColumnConstraints().addAll(labelCol1, fieldCol, labelCol2, comboCol, buttonCol, buttonCol);
        
        searchCard.getChildren().addAll(searchTitle, searchGrid);
        
        return searchCard;
    }
    
    private VBox createTableCard() {
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(20));
        tableCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                          "-fx-background-radius: 8;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        // Table header with title and count
        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("Question List");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        Label countLabel = new Label("0 questions found");
        countLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        HBox.setHgrow(tableTitle, Priority.ALWAYS);
        tableHeader.getChildren().addAll(tableTitle, countLabel);
        
        // Configure table
        tableView = new TableView<>();
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPrefHeight(400);
        tableView.setPlaceholder(new Label("No questions found. Try adjusting your search criteria."));
        
        // Style the table
        tableView.setStyle("-fx-background-color: transparent;" +
                          "-fx-border-color: transparent;");
        
        // Create columns
        TableColumn<QuestionRow, String> questionCol = new TableColumn<>("Question");
        questionCol.setCellValueFactory(cell -> cell.getValue().questionTextProperty());
        questionCol.setPrefWidth(400);
        
        TableColumn<QuestionRow, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cell -> cell.getValue().categoryNameProperty());
        categoryCol.setPrefWidth(150);
        
        TableColumn<QuestionRow, String> correctCol = new TableColumn<>("Correct Answer");
        correctCol.setCellValueFactory(cell -> cell.getValue().correctAnswerProperty());
        correctCol.setPrefWidth(120);
        
        tableView.getColumns().addAll(questionCol, categoryCol, correctCol);
        
        // Style the rows
        tableView.setRowFactory(tv -> {
            TableRow<QuestionRow> row = new TableRow<>();
            
            // Add hover effect
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) {
                    row.setStyle("-fx-background-color: derive(" + PRIMARY_COLOR + ", 80%);");
                }
            });
            
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) {
                    if (row.getIndex() % 2 == 0) {
                        row.setStyle("-fx-background-color: transparent;");
                    } else {
                        row.setStyle("-fx-background-color: derive(" + CARD_COLOR + ", -2%);");
                    }
                }
            });
            
            return row;
        });
        
        // Update count label when items change
        tableView.itemsProperty().addListener((obs, oldItems, newItems) -> {
            if (newItems != null) {
                countLabel.setText(newItems.size() + " questions found");
            } else {
                countLabel.setText("0 questions found");
            }
        });
        
        tableCard.getChildren().addAll(tableHeader, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        
        return tableCard;
    }
    
    private HBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        deleteButton = createStyledButton("Delete Selected Question", ERROR_COLOR);
        deleteButton.setGraphic(createIcon("trash", 16));
        deleteButton.setPrefWidth(250);
        deleteButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        
        // Disable delete button when no row is selected
        deleteButton.setDisable(true);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            deleteButton.setDisable(newSelection == null);
        });
        
        deleteButton.setOnAction(e -> deleteSelectedQuestion());
        
        buttonBox.getChildren().add(deleteButton);
        
        return buttonBox;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setStyle("-fx-background-color: " + color + ";" +
                       "-fx-text-fill: white;" +
                       "-fx-font-weight: bold;" +
                       "-fx-background-radius: 4;");
        
        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: derive(" + color + ", 20%);" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;" +
                           "-fx-background-radius: 4;"));
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: " + color + ";" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;" +
                           "-fx-background-radius: 4;"));
        
        return button;
    }
    
    private ImageView createIcon(String name, int size) {
        // This is a placeholder - in a real app, you would load actual icons
        // For this example, we'll return null to avoid errors
        return null;
        
        // In a real application with actual icon resources:
        // Image image = new Image(getClass().getResourceAsStream("/icons/" + name + ".png"));
        // ImageView imageView = new ImageView(image);
        // imageView.setFitHeight(size);
        // imageView.setFitWidth(size);
        // return imageView;
    }
    
    private void animateNode(Node node) {
        FadeTransition fade = new FadeTransition(Duration.millis(100), node);
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setCycleCount(2);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void loadCategories() {
        try {
            if (categoryComboBox.getItems().isEmpty()) {
                PreparedStatement stmt = connection.prepareStatement("SELECT DISTINCT category_name FROM category ORDER BY category_name");
                ResultSet rs = stmt.executeQuery();
                
                // Add "All Categories" option
                categoryComboBox.getItems().add("All Categories");
                
                while (rs.next()) {
                    categoryComboBox.getItems().add(rs.getString("category_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", 
                     "Unable to load categories", 
                     "The application encountered an error while trying to load quiz categories.");
        }
    }

    private void loadAllQuestions() {
        searchQuestions("", null);
    }

    private void searchQuestions(String query, String category) {
        ObservableList<QuestionRow> rows = FXCollections.observableArrayList();

        try {
            StringBuilder sql = new StringBuilder(
                "SELECT q.question_id, q.question_text, q.correct_option, c.category_name " +
                "FROM questions q INNER JOIN category c ON q.category_id = c.category_id WHERE 1=1");
            
            if (category != null && !category.isEmpty() && !category.equals("All Categories")) {
                sql.append(" AND c.category_name = ?");
            }
            
            if (!query.isEmpty()) {
                sql.append(" AND q.question_text LIKE ?");
            }
            
            sql.append(" ORDER BY c.category_name, q.question_text");

            PreparedStatement stmt = connection.prepareStatement(sql.toString());
            int index = 1;
            
            if (category != null && !category.isEmpty() && !category.equals("All Categories")) {
                stmt.setString(index++, category);
            }
            
            if (!query.isEmpty()) {
                stmt.setString(index, "%" + query + "%");
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rows.add(new QuestionRow(
                    rs.getInt("question_id"),
                    rs.getString("question_text"),
                    rs.getString("correct_option"),
                    rs.getString("category_name")
                ));
            }

            tableView.setItems(rows);
            
            // Show message if no results found
            if (rows.isEmpty() && (!query.isEmpty() || (category != null && !category.isEmpty() && !category.equals("All Categories")))) {
                showAlert(AlertType.INFORMATION, "No Results", 
                         "No questions match your search criteria", 
                         "Try using different keywords or selecting a different category.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Search Error", 
                     "Unable to search questions", 
                     "The application encountered an error while searching for questions: " + e.getMessage());
        }
    }

    private void deleteSelectedQuestion() {
        QuestionRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(AlertType.WARNING, "No Selection", 
                     "No question selected", 
                     "Please select a question from the table to delete.");
            return;
        }

        // Create a custom confirmation dialog
        Optional<ButtonType> result = showCustomConfirmDialog(
            "Confirm Question Deletion",
            "Are you sure you want to delete this question?",
            "This action cannot be undone. The question will be permanently removed from the database.",
            selected.getQuestionText()
        );
        
        result.ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    PreparedStatement stmt = connection.prepareStatement("DELETE FROM questions WHERE question_id = ?");
                    stmt.setInt(1, selected.getId());
                    int rowsAffected = stmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        tableView.getItems().remove(selected);
                        showSuccessAlert("Question Deleted", 
                                        "The question has been successfully deleted from the database.");
                    } else {
                        showAlert(AlertType.ERROR, "Deletion Failed", 
                                 "Failed to delete the question", 
                                 "The question may have already been deleted or does not exist.");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(AlertType.ERROR, "Database Error", 
                             "Failed to delete the question", 
                             "The application encountered an error: " + e.getMessage());
                }
            }
        });
    }
    
    private Optional<ButtonType> showCustomConfirmDialog(String title, String header, String content, String questionText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        
        // Create a custom dialog pane
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                           "-fx-border-color: " + WARNING_COLOR + ";" +
                           "-fx-border-width: 2px;");
        
        // Set header style
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + SECONDARY_COLOR + ";");
        }
        
        // Create content with question text in a styled box
        VBox contentBox = new VBox(15);
        
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        VBox questionBox = new VBox(5);
        questionBox.setStyle("-fx-background-color: #f8f9fa;" +
                            "-fx-padding: 10px;" +
                            "-fx-background-radius: 5px;" +
                            "-fx-border-color: #dee2e6;" +
                            "-fx-border-radius: 5px;");
        
        Label questionLabel = new Label("Question:");
        questionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label questionTextLabel = new Label(questionText);
        questionTextLabel.setWrapText(true);
        questionTextLabel.setStyle("-fx-font-size: 14px;");
        
        questionBox.getChildren().addAll(questionLabel, questionTextLabel);
        contentBox.getChildren().addAll(contentLabel, questionBox);
        
        dialogPane.setContent(contentBox);
        
        // Custom button types
        ButtonType yesButton = new ButtonType("Yes, Delete", ButtonBar.ButtonData.YES);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, cancelButton);
        
        // Style buttons
        Button yesBtn = (Button) dialogPane.lookupButton(yesButton);
        yesBtn.setStyle("-fx-background-color: " + ERROR_COLOR + ";" +
                       "-fx-text-fill: white;" +
                       "-fx-font-weight: bold;");
        
        Button cancelBtn = (Button) dialogPane.lookupButton(cancelButton);
        cancelBtn.setStyle("-fx-background-color: #6c757d;" +
                          "-fx-text-fill: white;");
        
        // Add hover effects
        yesBtn.setOnMouseEntered(e -> 
            yesBtn.setStyle("-fx-background-color: derive(" + ERROR_COLOR + ", 20%);" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;"));
        
        yesBtn.setOnMouseExited(e -> 
            yesBtn.setStyle("-fx-background-color: " + ERROR_COLOR + ";" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;"));
        
        cancelBtn.setOnMouseEntered(e -> 
            cancelBtn.setStyle("-fx-background-color: derive(#6c757d, 20%);" +
                              "-fx-text-fill: white;"));
        
        cancelBtn.setOnMouseExited(e -> 
            cancelBtn.setStyle("-fx-background-color: #6c757d;" +
                              "-fx-text-fill: white;"));
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetY(2);
        shadow.setRadius(5);
        dialogPane.setEffect(shadow);
        
        return alert.showAndWait();
    }
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        // Style the alert
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                           "-fx-border-color: " + SUCCESS_COLOR + ";" +
                           "-fx-border-width: 2px;");
        
        // Set header style
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + SUCCESS_COLOR + ";");
        }
        
        // Style content
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-font-size: 14px;");
        }
        
        // Style button
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + ";" +
                         "-fx-text-fill: white;" +
                         "-fx-font-weight: bold;");
        
        // Add hover effect
        okButton.setOnMouseEntered(e -> 
            okButton.setStyle("-fx-background-color: derive(" + SUCCESS_COLOR + ", 20%);" +
                             "-fx-text-fill: white;" +
                             "-fx-font-weight: bold;"));
        
        okButton.setOnMouseExited(e -> 
            okButton.setStyle("-fx-background-color: " + SUCCESS_COLOR + ";" +
                             "-fx-text-fill: white;" +
                             "-fx-font-weight: bold;"));
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetY(2);
        shadow.setRadius(5);
        dialogPane.setEffect(shadow);
        
        alert.showAndWait();
    }

    private void showAlert(AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        
        // Style the alert based on type
        DialogPane dialogPane = alert.getDialogPane();
        String borderColor;
        
        switch (type) {
            case ERROR:
                borderColor = ERROR_COLOR;
                break;
            case WARNING:
                borderColor = WARNING_COLOR;
                break;
            case INFORMATION:
                borderColor = PRIMARY_COLOR;
                break;
            default:
                borderColor = SECONDARY_COLOR;
                break;
        }
        
        dialogPane.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                           "-fx-border-color: " + borderColor + ";" +
                           "-fx-border-width: 2px;");
        
        // Set header style
        Label headerLabel = (Label) dialogPane.lookup(".header-panel .label");
        if (headerLabel != null) {
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: " + borderColor + ";");
        }
        
        // Style content
        Label contentLabel = (Label) dialogPane.lookup(".content.label");
        if (contentLabel != null) {
            contentLabel.setStyle("-fx-font-size: 14px;");
        }
        
        // Style button
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setStyle("-fx-background-color: " + borderColor + ";" +
                         "-fx-text-fill: white;" +
                         "-fx-font-weight: bold;");
        
        // Add hover effect
        okButton.setOnMouseEntered(e -> 
            okButton.setStyle("-fx-background-color: derive(" + borderColor + ", 20%);" +
                             "-fx-text-fill: white;" +
                             "-fx-font-weight: bold;"));
        
        okButton.setOnMouseExited(e -> 
            okButton.setStyle("-fx-background-color: " + borderColor + ";" +
                             "-fx-text-fill: white;" +
                             "-fx-font-weight: bold;"));
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setOffsetY(2);
        shadow.setRadius(5);
        dialogPane.setEffect(shadow);
        
        alert.showAndWait();
    }

    public static class QuestionRow {
        private final int id;
        private final SimpleStringProperty questionText;
        private final SimpleStringProperty correctAnswer;
        private final SimpleStringProperty categoryName;

        public QuestionRow(int id, String questionText, String correctAnswer, String categoryName) {
            this.id = id;
            this.questionText = new SimpleStringProperty(questionText);
            this.correctAnswer = new SimpleStringProperty(correctAnswer);
            this.categoryName = new SimpleStringProperty(categoryName);
        }

        public int getId() {
            return id;
        }

        public String getQuestionText() {
            return questionText.get();
        }

        public SimpleStringProperty questionTextProperty() {
            return questionText;
        }

        public String getCorrectAnswer() {
            return correctAnswer.get();
        }

        public SimpleStringProperty correctAnswerProperty() {
            return correctAnswer;
        }

        public String getCategoryName() {
            return categoryName.get();
        }

        public SimpleStringProperty categoryNameProperty() {
            return categoryName;
        }
    }
}