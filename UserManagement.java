package Admin_package;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.*;
import javafx.util.converter.DefaultStringConverter;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.beans.binding.Bindings;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.sql.*;

public class UserManagement {

    private TableView<User> table = new TableView<>();
    private ObservableList<User> userList = FXCollections.observableArrayList();
    
    // Color scheme
    private final String PRIMARY_COLOR = "#3498db";
    private final String SECONDARY_COLOR = "#2c3e50";
    private final String BACKGROUND_COLOR = "#f5f5f5";
    private final String CARD_COLOR = "#ffffff";
    private final String SUCCESS_COLOR = "#2ecc71";
    private final String ERROR_COLOR = "#e74c3c";
    private final String WARNING_COLOR = "#f39c12";

    public Pane getView() {
        // Main container with background
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        // Header
        VBox header = createHeader();
        mainLayout.setTop(header);
        
        // Content area
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));
        
        // User form card
        VBox formCard = createUserForm();
        
        // Table container
        VBox tableContainer = createTableView();
        
        content.getChildren().addAll(formCard, tableContainer);
        mainLayout.setCenter(content);
        
        // Load users from database
        loadUsers();
        
        return mainLayout;
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(25, 0, 15, 0));
        header.setStyle("-fx-background-color: " + SECONDARY_COLOR + ";");
        
        Label heading = new Label("User Management");
        heading.setFont(Font.font("System", FontWeight.BOLD, 24));
        heading.setStyle("-fx-text-fill: white;");
        
        Label subheading = new Label("Add, update, or remove users");
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
    
    private VBox createUserForm() {
        VBox formCard = new VBox(20);
        formCard.setPadding(new Insets(25));
        formCard.setStyle("-fx-background-color: " + CARD_COLOR + ";" +
                         "-fx-background-radius: 8;" +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label formTitle = new Label("User Information");
        formTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        formTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setStyle("-fx-font-weight: bold;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setPrefHeight(35);
        usernameField.setStyle("-fx-background-radius: 4;");
        
        // Password fields
        Label passwordLabel = new Label("Password");
        passwordLabel.setStyle("-fx-font-weight: bold;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(35);
        passwordField.setStyle("-fx-background-radius: 4;");
        
        TextField showPasswordField = new TextField();
        showPasswordField.setPromptText("Enter password");
        showPasswordField.setPrefHeight(35);
        showPasswordField.setStyle("-fx-background-radius: 4;");
        showPasswordField.setVisible(false);
        showPasswordField.setManaged(false);
        
        CheckBox showPasswordCheckBox = new CheckBox("Show Password");
        showPasswordCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                showPasswordField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                showPasswordField.setVisible(true);
                showPasswordField.setManaged(true);
            } else {
                passwordField.setText(showPasswordField.getText());
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                showPasswordField.setVisible(false);
                showPasswordField.setManaged(false);
            }
        });
        
        // Bidirectional binding
        showPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (showPasswordCheckBox.isSelected()) {
                passwordField.setText(newVal);
            }
        });
        
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (showPasswordCheckBox.isSelected()) {
                showPasswordField.setText(newVal);
            }
        });
        
        // Role selection
        Label roleLabel = new Label("Role");
        roleLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("admin", "user");
        roleComboBox.setPromptText("Select role");
        roleComboBox.setValue("user");
        roleComboBox.setPrefHeight(35);
        roleComboBox.setPrefWidth(200);
        roleComboBox.setStyle("-fx-background-radius: 4;");
        
        // Form layout
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(10, 0, 10, 0));
        
        // Add form elements to grid
        formGrid.add(usernameLabel, 0, 0);
        formGrid.add(usernameField, 0, 1);
        
        formGrid.add(passwordLabel, 1, 0);
        formGrid.add(passwordField, 1, 1);
        formGrid.add(showPasswordField, 1, 1);
        formGrid.add(showPasswordCheckBox, 1, 2);
        
        formGrid.add(roleLabel, 2, 0);
        formGrid.add(roleComboBox, 2, 1);
        
        // Set column constraints
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33);
        formGrid.getColumnConstraints().addAll(col1, col2, col3);
        
        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        Button addButton = createStyledButton("Add User", SUCCESS_COLOR);
        Button updateButton = createStyledButton("Update User", PRIMARY_COLOR);
        Button deleteButton = createStyledButton("Delete User", ERROR_COLOR);
        Button viewDetailsButton = createStyledButton("View Details", WARNING_COLOR);
        
        buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, viewDetailsButton);
        
        // Disable buttons when no row is selected
        updateButton.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
        deleteButton.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
        viewDetailsButton.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));
        
        // Add all components to form card
        formCard.getChildren().addAll(formTitle, formGrid, buttonBox);
        
        // Event handlers
        addButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = showPasswordCheckBox.isSelected() ? 
                              showPasswordField.getText().trim() : 
                              passwordField.getText().trim();
            String role = roleComboBox.getValue() == null ? "user" : roleComboBox.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(AlertType.ERROR, "Validation Error", "Username and password cannot be empty.");
                return;
            }

            addUser(username, password, role);
            loadUsers();
            usernameField.clear();
            passwordField.clear();
            showPasswordField.clear();
            roleComboBox.setValue("user");
        });

        updateButton.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Select a user to update.");
                return;
            }
            
            String username = usernameField.getText().trim();
            String password = showPasswordCheckBox.isSelected() ? 
                              showPasswordField.getText().trim() : 
                              passwordField.getText().trim();
            String role = roleComboBox.getValue() == null ? "user" : roleComboBox.getValue();
            
            if (password.isEmpty()) {
                showAlert(AlertType.ERROR, "Validation Error", "Password cannot be empty.");
                return;
            }
            
            updateUser(username, password, role);
            loadUsers();
        });

        deleteButton.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Select a user to delete.");
                return;
            }
            Alert confirm = new Alert(AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Deletion");
            confirm.setHeaderText("Are you sure you want to delete this user?");
            confirm.setContentText("All results and answered questions related to this user will also be deleted.");
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    deleteUser(selected.getUsername());
                    loadUsers();
                    usernameField.clear();
                    passwordField.clear();
                    showPasswordField.clear();
                    roleComboBox.setValue("user");
                }
            });
        });

        viewDetailsButton.setOnAction(e -> {
            User selected = table.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert(AlertType.WARNING, "No Selection", "Select a user to view details.");
                return;
            }
            viewUserDetails(selected.getUsername());
        });
        
        // Handle row selection
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                usernameField.setText(newSelection.getUsername());
                passwordField.setText(newSelection.getPassword());
                if (showPasswordCheckBox.isSelected()) {
                    showPasswordField.setText(newSelection.getPassword());
                }
                roleComboBox.setValue(newSelection.getRole());
            }
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
        
        Label tableTitle = new Label("User List");
        tableTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        tableTitle.setStyle("-fx-text-fill: " + SECONDARY_COLOR + ";");
        
        // Configure table
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(400);
        
        // Style the table
        table.setStyle("-fx-background-color: transparent;" +
                      "-fx-border-color: transparent;");
        
        // Create columns
        TableColumn<User, String> userCol = new TableColumn<>("Username");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userCol.setMinWidth(150);
        userCol.setEditable(false);
        
        TableColumn<User, String> passCol = new TableColumn<>("Password");
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passCol.setMinWidth(150);
        passCol.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        passCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setPassword(event.getNewValue());
            updateUser(user.getUsername(), user.getPassword(), user.getRole());
        });
        
        TableColumn<User, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setMinWidth(100);
        roleCol.setCellFactory(ComboBoxTableCell.forTableColumn("admin", "user"));
        roleCol.setOnEditCommit(event -> {
            User user = event.getRowValue();
            user.setRole(event.getNewValue());
            updateUser(user.getUsername(), user.getPassword(), user.getRole());
        });
        
        table.getColumns().setAll(userCol, passCol, roleCol);
        table.setItems(userList);
        
        // Style the rows
        table.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            
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
        
        // Add search functionality
        TextField searchField = new TextField();
        searchField.setPromptText("Search by username...");
        searchField.setPrefHeight(35);
        searchField.setStyle("-fx-background-radius: 4;");
        
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                loadUsers();
            } else {
                filterUsers(newVal);
            }
        });
        
        tableCard.getChildren().addAll(tableTitle, searchField, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        
        return tableCard;
    }
    
    private void filterUsers(String searchText) {
        ObservableList<User> filteredList = FXCollections.observableArrayList();
        for (User user : userList) {
            if (user.getUsername().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(user);
            }
        }
        table.setItems(filteredList);
    }

    private void viewUserDetails(String username) {
        // Create a custom dialog for user details
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("User Quiz Details");
        dialog.setHeaderText("Quiz Results for: " + username);
        
        // Set the button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK);
        
        // Create a VBox for the content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setMinWidth(500);
        content.setMaxHeight(400);
        
        // Create a scroll pane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        
        // Create a VBox for the results
        VBox resultsBox = new VBox(10);
        resultsBox.setPadding(new Insets(10));
        
        String query = "SELECT c.category_name, r.score, r.total_questions, r.attempted_at " +
                       "FROM result r JOIN category c ON r.category_id = c.category_id " +
                       "WHERE r.username = ? ORDER BY r.attempted_at DESC";
        
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            boolean hasResults = false;
            
            while (rs.next()) {
                hasResults = true;
                
                VBox resultCard = new VBox(5);
                resultCard.setPadding(new Insets(15));
                resultCard.setStyle("-fx-background-color: white;" +
                                   "-fx-border-color: #e0e0e0;" +
                                   "-fx-border-radius: 5;");
                
                String categoryName = rs.getString("category_name");
                int score = rs.getInt("score");
                int totalQuestions = rs.getInt("total_questions");
                String attemptedAt = rs.getTimestamp("attempted_at").toString();
                
                // Calculate percentage
                double percentage = (double) score / totalQuestions * 100;
                
                Label categoryLabel = new Label("Category: " + categoryName);
                categoryLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                Label scoreLabel = new Label(String.format("Score: %d / %d (%.1f%%)", 
                                                          score, totalQuestions, percentage));
                
                // Color based on score
                String scoreColor;
                if (percentage >= 80) {
                    scoreColor = SUCCESS_COLOR;
                } else if (percentage >= 60) {
                    scoreColor = PRIMARY_COLOR;
                } else if (percentage >= 40) {
                    scoreColor = WARNING_COLOR;
                } else {
                    scoreColor = ERROR_COLOR;
                }
                
                scoreLabel.setStyle("-fx-text-fill: " + scoreColor + ";");
                
                Label dateLabel = new Label("Date: " + attemptedAt);
                dateLabel.setStyle("-fx-font-size: 12;");
                
                resultCard.getChildren().addAll(categoryLabel, scoreLabel, dateLabel);
                resultsBox.getChildren().add(resultCard);
            }
            
            if (!hasResults) {
                Label noResultsLabel = new Label("No quiz results available for this user.");
                noResultsLabel.setStyle("-fx-font-size: 14;");
                resultsBox.getChildren().add(noResultsLabel);
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            Label errorLabel = new Label("Error loading results: " + ex.getMessage());
            errorLabel.setStyle("-fx-text-fill: " + ERROR_COLOR + ";");
            resultsBox.getChildren().add(errorLabel);
        }
        
        scrollPane.setContent(resultsBox);
        content.getChildren().add(scrollPane);
        
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void addUser(String username, String password, String role) {
        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert(AlertType.INFORMATION, "Success", "User added successfully.");
            } else {
                showAlert(AlertType.WARNING, "Failed", "Failed to add user.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            showAlert(AlertType.ERROR, "Duplicate User", "Username already exists.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to add user: " + e.getMessage());
        }
    }

    private void updateUser(String username, String newPassword, String newRole) {
        String query = "UPDATE users SET password = ?, role = ? WHERE username = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, newRole);
            stmt.setString(3, username);
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                showAlert(AlertType.INFORMATION, "Success", "User updated successfully.");
            } else {
                showAlert(AlertType.ERROR, "Update Failed", "No rows updated. User may not exist.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to update user: " + e.getMessage());
        }
    }

    private void deleteUser(String username) {
        // First, delete associated data (results)
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Delete results first
                String deleteResultsQuery = "DELETE FROM result WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteResultsQuery)) {
                    stmt.setString(1, username);
                    stmt.executeUpdate();
                }
                
                // Then delete the user
                String deleteUserQuery = "DELETE FROM users WHERE username = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteUserQuery)) {
                    stmt.setString(1, username);
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        conn.commit();
                        showAlert(AlertType.INFORMATION, "Success", "User and associated data deleted successfully.");
                    } else {
                        conn.rollback();
                        showAlert(AlertType.WARNING, "Not Found", "User not found.");
                    }
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to delete user: " + e.getMessage());
        }
    }

    private void loadUsers() {
        userList.clear();
        String query = "SELECT username, password, role FROM users";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                userList.add(new User(rs.getString("username"), rs.getString("password"), rs.getString("role")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "Failed to load users: " + e.getMessage());
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
        alert.setHeaderText(null);
        alert.setContentText(msg);
        
        // Style the alert based on type
        DialogPane dialogPane = alert.getDialogPane();
        
        String headerStyle = "-fx-font-weight: bold; -fx-font-size: 16px;";
        String contentStyle = "-fx-font-size: 14px;";
        
        switch (type) {
            case ERROR:
                dialogPane.setStyle("-fx-border-color: " + ERROR_COLOR + ";");
                break;
            case WARNING:
                dialogPane.setStyle("-fx-border-color: " + WARNING_COLOR + ";");
                break;
            case INFORMATION:
                dialogPane.setStyle("-fx-border-color: " + SUCCESS_COLOR + ";");
                break;
            default:
                break;
        }
        
        dialogPane.lookup(".content.label").setStyle(contentStyle);
        
        alert.showAndWait();
    }

    public static class User {
        private final String username;
        private String password;
        private String role;

        public User(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}