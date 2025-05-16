package Admin_package;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.control.Alert.AlertType;
import java.sql.*;
import javafx.collections.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class CategoryPage {

    private TableView<Category> table = new TableView<>();
    private ObservableList<Category> categoryList = FXCollections.observableArrayList();
    
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
        
        // Category form card
        VBox formCard = createCategoryForm();
        
        // Table container
        VBox tableContainer = createTableView();
        
        content.getChildren().addAll(formCard, tableContainer);
        mainLayout.setCenter(content);
        
        // Load categories from database
        loadCategories();
        
        return mainLayout;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 15, 0));
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label heading = new Label("Category Management");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setStyle("-fx-text-fill: white;");
        
        Label subheading = new Label("Add and manage quiz categories");
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
    
    private VBox createCategoryForm() {
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                         "-fx-background-radius: 8;" +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label formTitle = new Label("Add New Category");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Form fields
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Category name field
        Label nameLabel = new Label("Category Name");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField categoryNameField = new TextField();
        categoryNameField.setPromptText("Enter category name");
        categoryNameField.setPrefHeight(35);
        categoryNameField.setPrefWidth(250);
        categoryNameField.setStyle("-fx-background-radius: 4;");
        
        // Category description field
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-weight: bold;");
        TextField categoryDescField = new TextField();
        categoryDescField.setPromptText("Enter category description (optional)");
        categoryDescField.setPrefHeight(35);
        categoryDescField.setPrefWidth(350);
        categoryDescField.setStyle("-fx-background-radius: 4;");
        
        // Add form elements to grid
        formGrid.add(nameLabel, 0, 0);
        formGrid.add(categoryNameField, 0, 1);
        formGrid.add(descLabel, 1, 0);
        formGrid.add(categoryDescField, 1, 1);
        
        // Set column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(40);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(60);
        formGrid.getColumnConstraints().addAll(col1, col2);
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button addButton = createStyledButton("Add Category", SUCCESS_COLOR);
        Button clearButton = createStyledButton("Clear Form", PRIMARY_COLOR);
        
        buttonBox.getChildren().addAll(clearButton, addButton);
        
        // Add all components to form card
        formCard.getChildren().addAll(formTitle, formGrid, buttonBox);
        
        // Event handlers
        addButton.setOnAction(e -> {
            String name = categoryNameField.getText().trim();
            String desc = categoryDescField.getText().trim();

            if (name.isEmpty()) {
                showAlert(AlertType.ERROR, "Validation Error", "Category name cannot be empty.");
                return;
            }

            addCategory(name, desc);
            categoryNameField.clear();
            categoryDescField.clear();
            loadCategories();
        });
        
        clearButton.setOnAction(e -> {
            categoryNameField.clear();
            categoryDescField.clear();
        });
        
        return formCard;
    }
    
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setPrefHeight(35);
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
    
    private VBox createTableView() {
        VBox tableCard = new VBox(15);
        tableCard.setPadding(new Insets(25));
        tableCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                          "-fx-background-radius: 8;" +
                          "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        // Table header with title and actions
        HBox tableHeader = new HBox();
        tableHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label tableTitle = new Label("Available Categories");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search categories...");
        searchField.setPrefHeight(35);
        searchField.setPrefWidth(250);
        searchField.setStyle("-fx-background-radius: 4;");
        
        // Remove button
        Button removeButton = createStyledButton("Remove Selected", ERROR_COLOR);
        removeButton.setDisable(true); // Initially disabled
        
        HBox.setHgrow(tableTitle, Priority.ALWAYS);
        tableHeader.getChildren().addAll(tableTitle, searchField, removeButton);
        tableHeader.setSpacing(15);
        
        // Configure table
        table.setEditable(false);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);
        
        // Style the table
        table.setStyle("-fx-background-color: transparent;" +
                      "-fx-border-color: transparent;");
        
        // Create columns
        TableColumn<Category, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setMinWidth(80);
        idCol.setMaxWidth(100);
        
        TableColumn<Category, String> nameCol = new TableColumn<>("Category Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setMinWidth(200);
        
        TableColumn<Category, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setMinWidth(300);
        
        table.getColumns().setAll(idCol, nameCol, descCol);
        table.setItems(categoryList);
        
        // Style the rows
        table.setRowFactory(tv -> {
            TableRow<Category> row = new TableRow<>();
            
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
        
        // Enable/disable remove button based on selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            removeButton.setDisable(newSelection == null);
        });
        
        // Search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadCategories();
            } else {
                filterCategories(newVal);
            }
        });
        
        // Remove button action
        removeButton.setOnAction(e -> {
            Category selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Please select a category to remove.");
                return;
            }

            Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Deletion");
            confirmAlert.setHeaderText("Delete Category");
            confirmAlert.setContentText("This will delete the category \"" + selected.getName() + 
                                       "\" and all associated questions. This action cannot be undone.");
            
            // Style the confirmation dialog
            DialogPane dialogPane = confirmAlert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                               "-fx-border-color: " + ERROR_COLOR + ";" +
                               "-fx-border-width: 2px;");
            
            ButtonType deleteButton = new ButtonType("Delete", ButtonBar.ButtonData.YES);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(deleteButton, cancelButton);

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == deleteButton) {
                    deleteCategory(selected.getId());
                    loadCategories();
                }
            });
        });
        
        // Add components to table card
        tableCard.getChildren().addAll(tableHeader, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return tableCard;
    }
    
    private void filterCategories(String searchText) {
        ObservableList<Category> filteredList = FXCollections.observableArrayList();
        for (Category category : categoryList) {
            if (category.getName().toLowerCase().contains(searchText.toLowerCase()) ||
                category.getDescription().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(category);
            }
        }
        table.setItems(filteredList);
    }

    private void addCategory(String name, String description) {
        String query = "INSERT INTO category (category_name, description) VALUES (?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, description.isEmpty() ? null : description);
            stmt.executeUpdate();
            showAlert(AlertType.INFORMATION, "Success", "Category added successfully.");
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(AlertType.ERROR, "Duplicate", "Category already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to add category.");
        }
    }

    private void deleteCategory(int id) {
        String query = "DELETE FROM category WHERE category_id = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "Category and related questions deleted successfully.");
            } else {
                showAlert(AlertType.ERROR, "Failure", "Failed to delete category.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to delete category.");
        }
    }

    private void loadCategories() {
        categoryList.clear();
        String query = "SELECT category_id, category_name, description FROM category";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                categoryList.add(new Category(
                    rs.getInt("category_id"),
                    rs.getString("category_name"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to load categories.");
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

    private void showAlert(AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(msg);
        
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

    // Category model class
    public static class Category {
        private final Integer id;
        private final String name;
        private final String description;

        public Category(Integer id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description == null ? "" : description;
        }

        public Integer getId() { return id; }
        public String getName() { return name; }
        public String getDescription() { return description; }
    }
}