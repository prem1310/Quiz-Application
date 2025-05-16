package Admin_package;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EditQuizPage {

    private TextField searchField;
    private TableView<QuestionRow> tableView;
    private Button saveButton;
    private ComboBox<String> categoryComboBox;

    private Connection connection;
    private Map<String, Integer> categoryMap = new HashMap<>(); // Map category_name -> category_id

    public Parent getView() {
        // Main container with gradient background
        VBox mainLayout = new VBox(25);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #f5f7fa, #e4e8f0);");

        // Header with shadow
        VBox headerBox = new VBox(5);
        headerBox.setAlignment(Pos.CENTER);
        
        Label heading = new Label("Edit Quiz Questions");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        heading.setTextFill(Color.web("#2c3e50"));
        
        Label subheading = new Label("Modify questions and options below");
        subheading.setFont(Font.font("Segoe UI", 14));
        subheading.setTextFill(Color.web("#7f8c8d"));
        
        headerBox.getChildren().addAll(heading, subheading);

        // Card for search controls
        VBox searchCard = new VBox(15);
        searchCard.setPadding(new Insets(20));
        searchCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        
        // Add shadow to card
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        searchCard.setEffect(shadow);

        // Category selection
        HBox categoryBox = new HBox(15);
        categoryBox.setAlignment(Pos.CENTER_LEFT);
        
        Label categoryLabel = new Label("Category:");
        categoryLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select category");
        categoryComboBox.setPrefWidth(200);
        categoryComboBox.setStyle("-fx-font-size: 14px;");
        categoryComboBox.setOnAction(e -> loadQuestions());
        
        categoryBox.getChildren().addAll(categoryLabel, categoryComboBox);

        // Search controls
        HBox searchBox = new HBox(15);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        Label searchLabel = new Label("Search:");
        searchLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        
        searchField = new TextField();
        searchField.setPromptText("Enter question text...");
        searchField.setPrefHeight(35);
        searchField.setPrefWidth(300);
        
        Button searchButton = new Button("Search");
        searchButton.setPrefHeight(35);
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        searchButton.setOnAction(e -> searchQuestions());
        
        Button clearButton = new Button("Clear");
        clearButton.setPrefHeight(35);
        clearButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #555; -fx-background-radius: 4;");
        clearButton.setOnAction(e -> {
            searchField.clear();
            searchQuestions();
        });
        
        searchBox.getChildren().addAll(searchLabel, searchField, searchButton, clearButton);
        
        // Add both rows to search card
        searchCard.getChildren().addAll(categoryBox, searchBox);

        // Table setup with improved styling
        tableView = new TableView<>();
        tableView.setEditable(true);
        tableView.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8;");
        tableView.setEffect(shadow);
        
        // Make table fill available width
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setMinHeight(400);

        // Create columns with better sizing
        TableColumn<QuestionRow, String> questionCol = createEditableColumn("Question", QuestionRow::questionTextProperty, QuestionRow::setQuestionText);
        questionCol.setPrefWidth(250);
        
        TableColumn<QuestionRow, String> option1Col = createEditableColumn("Option 1", QuestionRow::option1Property, QuestionRow::setOption1);
        TableColumn<QuestionRow, String> option2Col = createEditableColumn("Option 2", QuestionRow::option2Property, QuestionRow::setOption2);
        TableColumn<QuestionRow, String> option3Col = createEditableColumn("Option 3", QuestionRow::option3Property, QuestionRow::setOption3);
        TableColumn<QuestionRow, String> option4Col = createEditableColumn("Option 4", QuestionRow::option4Property, QuestionRow::setOption4);
        TableColumn<QuestionRow, String> correctCol = createEditableColumn("Correct Answer", QuestionRow::correctAnswerProperty, QuestionRow::setCorrectAnswer);
        
        // Set equal width for option columns
        option1Col.setPrefWidth(120);
        option2Col.setPrefWidth(120);
        option3Col.setPrefWidth(120);
        option4Col.setPrefWidth(120);
        correctCol.setPrefWidth(120);

        tableView.getColumns().addAll(questionCol, option1Col, option2Col, option3Col, option4Col, correctCol);

        // Action buttons with better styling
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(120);
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        cancelButton.setOnAction(e -> {
            // Reload questions to discard changes
            loadQuestions();
        });
        
        saveButton = new Button("Save Changes");
        saveButton.setPrefHeight(40);
        saveButton.setPrefWidth(150);
        saveButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 4;");
        saveButton.setOnAction(e -> confirmSaveChanges());
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);

        // Add all components to main layout
        mainLayout.getChildren().addAll(headerBox, searchCard, tableView, buttonBox);

        // Connect to database
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
            loadCategories();
            loadQuestions();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to connect to database.");
        }

        return mainLayout;
    }

    private void loadCategories() throws SQLException {
        categoryComboBox.getItems().clear();
        categoryMap.clear();

        // Add "All Categories" option
        categoryComboBox.getItems().add("All Categories");

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT category_id, category_name FROM category");

        while (rs.next()) {
            int id = rs.getInt("category_id");
            String name = rs.getString("category_name");
            categoryComboBox.getItems().add(name);
            categoryMap.put(name, id);
        }
    }

    private void loadQuestions() {
        searchQuestions(); // load all or by category
    }

    private TableColumn<QuestionRow, String> createEditableColumn(String title,
            javafx.util.Callback<QuestionRow, SimpleStringProperty> property,
            java.util.function.BiConsumer<QuestionRow, String> setter) {

        TableColumn<QuestionRow, String> col = new TableColumn<>(title);
        col.setCellValueFactory(cellData -> property.call(cellData.getValue()));
        
        // Create a StringConverter for the TextFieldTableCell
        StringConverter<String> stringConverter = new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object == null ? "" : object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };
        
        // Use the StringConverter in the cell factory
        col.setCellFactory(column -> {
            TextFieldTableCell<QuestionRow, String> cell = new TextFieldTableCell<>(stringConverter);
            cell.setStyle("-fx-padding: 10 5 10 5;");
            return cell;
        });
        
        col.setOnEditCommit(event -> setter.accept(event.getRowValue(), event.getNewValue()));
        return col;
    }

    private void searchQuestions() {
        ObservableList<QuestionRow> rows = FXCollections.observableArrayList();

        String search = searchField.getText().trim();
        String selectedCategory = categoryComboBox.getValue();

        try {
            String sql = "SELECT q.question_id, q.question_text, q.opt1, q.opt2, q.opt3, q.opt4, q.correct_option " +
                         "FROM questions q INNER JOIN category c ON q.category_id = c.category_id WHERE 1=1";
            
            if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("All Categories")) {
                sql += " AND c.category_name = ?";
            }
            if (!search.isEmpty()) {
                sql += " AND q.question_text LIKE ?";
            }

            PreparedStatement stmt = connection.prepareStatement(sql);
            int paramIndex = 1;

            if (selectedCategory != null && !selectedCategory.isEmpty() && !selectedCategory.equals("All Categories")) {
                stmt.setString(paramIndex++, selectedCategory);
            }
            if (!search.isEmpty()) {
                stmt.setString(paramIndex, "%" + search + "%");
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(new QuestionRow(
                        rs.getInt("question_id"),
                        rs.getString("question_text"),
                        rs.getString("opt1"),
                        rs.getString("opt2"),
                        rs.getString("opt3"),
                        rs.getString("opt4"),
                        rs.getString("correct_option")
                ));
            }

            tableView.setItems(rows);
            
            // Show message if no results found
            if (rows.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Results", "No questions found matching your criteria.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to fetch questions.");
        }
    }

    private void confirmSaveChanges() {
        // Create confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Save");
        confirmAlert.setHeaderText("Save Changes");
        confirmAlert.setContentText("Are you sure you want to save these changes?");
        
        // Get the result of the dialog
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        // If user clicked OK, save changes
        if (result.isPresent() && result.get() == ButtonType.OK) {
            saveChanges();
        }
    }

    private void saveChanges() {
        try {
            connection.setAutoCommit(false);
            
            for (QuestionRow row : tableView.getItems()) {
                PreparedStatement stmt = connection.prepareStatement(
                        "UPDATE questions SET question_text = ?, opt1 = ?, opt2 = ?, opt3 = ?, opt4 = ?, correct_option = ? WHERE question_id = ?");
                stmt.setString(1, row.getQuestionText());
                stmt.setString(2, row.getOption1());
                stmt.setString(3, row.getOption2());
                stmt.setString(4, row.getOption3());
                stmt.setString(5, row.getOption4());
                stmt.setString(6, row.getCorrectAnswer());
                stmt.setInt(7, row.getId());
                stmt.executeUpdate();
            }
            
            connection.commit();
            connection.setAutoCommit(true);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Changes saved successfully.");
        } catch (SQLException e) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error saving changes: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Style the dialog
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white;");
        
        // Show the alert
        alert.showAndWait();
    }

    // =========================== QuestionRow ===========================
    public static class QuestionRow {
        private final int id;
        private final SimpleStringProperty questionText;
        private final SimpleStringProperty option1;
        private final SimpleStringProperty option2;
        private final SimpleStringProperty option3;
        private final SimpleStringProperty option4;
        private final SimpleStringProperty correctAnswer;

        public QuestionRow(int id, String questionText, String opt1, String opt2, String opt3, String opt4, String correct) {
            this.id = id;
            this.questionText = new SimpleStringProperty(questionText);
            this.option1 = new SimpleStringProperty(opt1 != null ? opt1 : "");
            this.option2 = new SimpleStringProperty(opt2 != null ? opt2 : "");
            this.option3 = new SimpleStringProperty(opt3 != null ? opt3 : "");
            this.option4 = new SimpleStringProperty(opt4 != null ? opt4 : "");
            this.correctAnswer = new SimpleStringProperty(correct != null ? correct : "");
        }

        public int getId() {
            return id;
        }

        public String getQuestionText() {
            return questionText.get();
        }

        public void setQuestionText(String text) {
            questionText.set(text);
        }

        public SimpleStringProperty questionTextProperty() {
            return questionText;
        }

        public String getOption1() {
            return option1.get();
        }

        public void setOption1(String a) {
            option1.set(a);
        }

        public SimpleStringProperty option1Property() {
            return option1;
        }

        public String getOption2() {
            return option2.get();
        }

        public void setOption2(String b) {
            option2.set(b);
        }

        public SimpleStringProperty option2Property() {
            return option2;
        }

        public String getOption3() {
            return option3.get();
        }

        public void setOption3(String c) {
            option3.set(c);
        }

        public SimpleStringProperty option3Property() {
            return option3;
        }

        public String getOption4() {
            return option4.get();
        }

        public void setOption4(String d) {
            option4.set(d);
        }

        public SimpleStringProperty option4Property() {
            return option4;
        }

        public String getCorrectAnswer() {
            return correctAnswer.get();
        }

        public void setCorrectAnswer(String correct) {
            correctAnswer.set(correct);
        }

        public SimpleStringProperty correctAnswerProperty() {
            return correctAnswer;
        }
    }
}