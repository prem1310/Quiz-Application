package Admin_package;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import java.sql.*;
import java.util.*;

public class CreateQuizPage {

    // UI Components
    private ComboBox<Category> categoryComboBox = new ComboBox<>();
    private TextField questionField = new TextField();
    private TextField option1 = new TextField();
    private TextField option2 = new TextField();
    private TextField option3 = new TextField();
    private TextField option4 = new TextField();
    private RadioButton opt1 = new RadioButton("Correct");
    private RadioButton opt2 = new RadioButton("Correct");
    private RadioButton opt3 = new RadioButton("Correct");
    private RadioButton opt4 = new RadioButton("Correct");
    private Button submitBtn = new Button("Save Question");
    private Button clearBtn = new Button("Clear Form");
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2c3e50";
    private final String BACKGROUND_COLOR = "#f5f5f5";
    private final String CARD_COLOR = "#ffffff";
    private final String SUCCESS_COLOR = "#2ecc71";
    private final String ERROR_COLOR = "#e74c3c";

    public Pane getView() {
        // Main container with background
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header section
        VBox header = createHeader();
        mainLayout.setTop(header);
        
        // Content area
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);
        
        // Form card
        VBox formCard = createQuizForm();
        
        // Add form to content
        content.getChildren().add(formCard);
        mainLayout.setCenter(content);
        
        // Load categories from database
        loadCategories();
        
        // Initially disable form until category is selected
        setFormDisabled(true);
        
        return mainLayout;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 15, 0));
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label heading = new Label("Create Quiz Question");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setStyle("-fx-text-fill: white;");
        
        Label subheading = new Label("Add new questions to your quiz categories");
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
    
    private VBox createQuizForm() {
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setMaxWidth(700);
        formCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                         "-fx-background-radius: 8;" +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        // Category selection section
        VBox categorySection = new VBox(10);
        Label categoryLabel = new Label("Select Category");
        categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        categoryLabel.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        categoryComboBox.setPromptText("Choose a category for this question");
        categoryComboBox.setPrefWidth(400);
        categoryComboBox.setPrefHeight(35);
        categoryComboBox.setStyle("-fx-font-size: 14px;");
        
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean enable = newVal != null;
            setFormDisabled(!enable);
        });
        
        categorySection.getChildren().addAll(categoryLabel, categoryComboBox);
        
        // Question section
        VBox questionSection = new VBox(10);
        Label questionLabel = new Label("Question Text");
        questionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        questionLabel.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        questionField.setPromptText("Enter your question here");
        questionField.setPrefHeight(60);
        questionField.setStyle("-fx-font-size: 14px;");
        
        questionSection.getChildren().addAll(questionLabel, questionField);
        
        // Options section
        VBox optionsSection = new VBox(15);
        Label optionsLabel = new Label("Answer Options");
        optionsLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        optionsLabel.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Create toggle group for radio buttons
        ToggleGroup correctGroup = new ToggleGroup();
        opt1.setToggleGroup(correctGroup);
        opt2.setToggleGroup(correctGroup);
        opt3.setToggleGroup(correctGroup);
        opt4.setToggleGroup(correctGroup);
        
        // Style radio buttons
        styleRadioButton(opt1);
        styleRadioButton(opt2);
        styleRadioButton(opt3);
        styleRadioButton(opt4);
        
        // Style text fields
        styleOptionField(option1, "Option A");
        styleOptionField(option2, "Option B");
        styleOptionField(option3, "Option C");
        styleOptionField(option4, "Option D");
        
        // Create option rows with proper alignment
        GridPane optionsGrid = new GridPane();
        optionsGrid.setHgap(15);
        optionsGrid.setVgap(15);
        optionsGrid.setPadding(new Insets(5, 0, 5, 0));
        
        // Option labels
        Label optALabel = new Label("A:");
        Label optBLabel = new Label("B:");
        Label optCLabel = new Label("C:");
        Label optDLabel = new Label("D:");
        
        styleOptionLabel(optALabel);
        styleOptionLabel(optBLabel);
        styleOptionLabel(optCLabel);
        styleOptionLabel(optDLabel);
        
        // Add components to grid
        optionsGrid.add(optALabel, 0, 0);
        optionsGrid.add(option1, 1, 0);
        optionsGrid.add(opt1, 2, 0);
        
        optionsGrid.add(optBLabel, 0, 1);
        optionsGrid.add(option2, 1, 1);
        optionsGrid.add(opt2, 2, 1);
        
        optionsGrid.add(optCLabel, 0, 2);
        optionsGrid.add(option3, 1, 2);
        optionsGrid.add(opt3, 2, 2);
        
        optionsGrid.add(optDLabel, 0, 3);
        optionsGrid.add(option4, 1, 3);
        optionsGrid.add(opt4, 2, 3);
        
        // Set column constraints for proper alignment
        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(30);
        
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        fieldCol.setFillWidth(true);
        
        ColumnConstraints radioCol = new ColumnConstraints();
        radioCol.setMinWidth(100);
        
        optionsGrid.getColumnConstraints().addAll(labelCol, fieldCol, radioCol);
        
        optionsSection.getChildren().addAll(optionsLabel, optionsGrid);
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        submitBtn = createStyledButton("Save Question", SUCCESS_COLOR);
        clearBtn = createStyledButton("Clear Form", PRIMARY_COLOR);
        
        buttonBox.getChildren().addAll(clearBtn, submitBtn);
        
        // Add all sections to form card
        formCard.getChildren().addAll(categorySection, questionSection, optionsSection, buttonBox);
        
        // Event handlers
        submitBtn.setOnAction(e -> saveQuiz());
        
        clearBtn.setOnAction(e -> {
            questionField.clear();
            option1.clear();
            option2.clear();
            option3.clear();
            option4.clear();
            opt1.setSelected(false);
            opt2.setSelected(false);
            opt3.setSelected(false);
            opt4.setSelected(false);
        });
        
        return formCard;
    }
    
    private void styleOptionLabel(Label label) {
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
        label.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
    }
    
    private void styleOptionField(TextField field, String prompt) {
        field.setPromptText(prompt);
        field.setPrefHeight(35);
        field.setStyle("-fx-font-size: 14px;");
    }
    
    private void styleRadioButton(RadioButton radio) {
        radio.setStyle("-fx-font-size: 14px;");
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(40);
        button.setPrefWidth(150);
        button.setStyle("-fx-background-color: " + color + ";" +
                       "-fx-text-fill: white;" +
                       "-fx-font-weight: bold;" +
                       "-fx-font-size: 14px;" +
                       "-fx-background-radius: 4;");
        
        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: derive(" + color + ", 20%);" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 14px;" +
                           "-fx-background-radius: 4;"));
        
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: " + color + ";" +
                           "-fx-text-fill: white;" +
                           "-fx-font-weight: bold;" +
                           "-fx-font-size: 14px;" +
                           "-fx-background-radius: 4;"));
        
        return button;
    }
    
    private void setFormDisabled(boolean disable) {
        questionField.setDisable(disable);
        option1.setDisable(disable);
        option2.setDisable(disable);
        option3.setDisable(disable);
        option4.setDisable(disable);
        opt1.setDisable(disable);
        opt2.setDisable(disable);
        opt3.setDisable(disable);
        opt4.setDisable(disable);
        submitBtn.setDisable(disable);
        clearBtn.setDisable(disable);
    }

    private void loadCategories() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_id, category_name FROM category")) {

            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                categoryComboBox.getItems().add(new Category(id, name));
            }
            
            // Show message if no categories found
            if (categoryComboBox.getItems().isEmpty()) {
                showAlert(AlertType.WARNING, "No Categories", 
                         "No categories found. Please create a category first before adding questions.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to load categories: " + e.getMessage());
        }
    }

    private void saveQuiz() {
        Category selectedCategory = categoryComboBox.getValue();
        if (selectedCategory == null) {
            showAlert(AlertType.ERROR, "Error", "Please select a category.");
            return;
        }

        String question = questionField.getText().trim();
        String[] options = { 
            option1.getText().trim(), 
            option2.getText().trim(),
            option3.getText().trim(), 
            option4.getText().trim() 
        };
        
        char correctOption = ' ';
        if (opt1.isSelected()) correctOption = 'A';
        else if (opt2.isSelected()) correctOption = 'B';
        else if (opt3.isSelected()) correctOption = 'C';
        else if (opt4.isSelected()) correctOption = 'D';

        // Validate inputs
        if (question.isEmpty()) {
            showAlert(AlertType.ERROR, "Validation Error", "Please enter a question.");
            questionField.requestFocus();
            return;
        }
        
        for (int i = 0; i < options.length; i++) {
            if (options[i].isEmpty()) {
                showAlert(AlertType.ERROR, "Validation Error", 
                         "Please enter text for Option " + (char)('A' + i) + ".");
                return;
            }
        }
        
        if (correctOption == ' ') {
            showAlert(AlertType.ERROR, "Validation Error", 
                     "Please select which option is correct.");
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO questions (question_text, opt1, opt2, opt3, opt4, correct_option, category_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, question);
            stmt.setString(2, options[0]);
            stmt.setString(3, options[1]);
            stmt.setString(4, options[2]);
            stmt.setString(5, options[3]);
            stmt.setString(6, String.valueOf(correctOption));
            stmt.setInt(7, selectedCategory.getId());

            stmt.executeUpdate();
            conn.commit();

            showAlert(AlertType.INFORMATION, "Success", "Question added successfully to category: " + 
                     selectedCategory.getName());
            
            // Clear form for next question
            questionField.clear(); 
            option1.clear(); 
            option2.clear(); 
            option3.clear(); 
            option4.clear();
            opt1.setSelected(false); 
            opt2.setSelected(false); 
            opt3.setSelected(false); 
            opt4.setSelected(false);
            
            // Keep category selected for convenience when adding multiple questions
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to save question: " + ex.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            // Make sure the driver is loaded
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        // Style the alert based on type
        DialogPane dialogPane = alert.getDialogPane();
        
        switch (type) {
            case ERROR:
                dialogPane.setStyle("-fx-border-color: " + ERROR_COLOR + "; -fx-border-width: 2px;");
                break;
            case WARNING:
                dialogPane.setStyle("-fx-border-color: #f39c12; -fx-border-width: 2px;");
                break;
            case INFORMATION:
                dialogPane.setStyle("-fx-border-color: " + SUCCESS_COLOR + "; -fx-border-width: 2px;");
                break;
            default:
                break;
        }
        
        alert.showAndWait();
    }

    // Category model for ComboBox
    public static class Category {
        private final int id;
        private final String name;
        
        public Category(int id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public int getId() { return id; }
        public String getName() { return name; }
        
        @Override
        public String toString() { return name; }
    }
}