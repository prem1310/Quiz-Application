package User_package;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.text.TextAlignment;

import java.sql.*;

import Admin_package.AdminDashboard;

public class LoginRegistrationPage extends Application {

    private boolean isLoginMode = true;
    private Connection connection;
    private StackPane formContainer;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;
    private ComboBox<String> roleComboBox;
    private Button actionButton;
    private Button toggleButton;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("QuizApp - Login / Register");
        
        // Initialize database connection
        initializeDatabase();

        // Main container
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #3498db, #8e44ad);");

        // Create a split layout with decorative side panel
        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER);
        
        // Left decorative panel
        VBox leftPanel = createDecorativePanel();
        
        // Right form panel
        VBox rightPanel = createFormPanel();
        
        // Add both panels to the content box
        contentBox.getChildren().addAll(leftPanel, rightPanel);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);
        
        root.setCenter(contentBox);

        // Create scene and set to stage
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        // Initial animations
        animateEntrance(leftPanel, rightPanel);
        
        // Setup event handlers
        setupEventHandlers(primaryStage);
    }
    
    private VBox createDecorativePanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 0 20 20 0;");
        
        // App logo/icon
        Text iconText = new Text("Q");
        iconText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 80));
        iconText.setFill(Color.WHITE);
        
        StackPane iconContainer = new StackPane(iconText);
        iconContainer.setStyle("-fx-background-color: #2980b9; -fx-background-radius: 50%;");
        iconContainer.setPrefSize(120, 120);
        iconContainer.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));
        
        // App title
        Text appTitle = new Text("Quiz Application");
        appTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        appTitle.setFill(Color.WHITE);
        
        // App description
        Text appDesc = new Text("Test your knowledge with\ninteractive quizzes");
        appDesc.setFont(Font.font("Segoe UI", 16));
        appDesc.setFill(Color.WHITE);
        appDesc.setTextAlignment(TextAlignment.CENTER);
        
        // Decorative elements
        VBox decorElements = new VBox(15);
        decorElements.setAlignment(Pos.CENTER);
        
        for (int i = 0; i < 3; i++) {
            Rectangle rect = new Rectangle(60 + (i * 40), 8);
            rect.setFill(Color.rgb(255, 255, 255, 0.4));
            rect.setArcWidth(8);
            rect.setArcHeight(8);
            decorElements.getChildren().add(rect);
        }
        
        panel.getChildren().addAll(iconContainer, appTitle, appDesc, new Separator(), decorElements);
        return panel;
    }
    
    private VBox createFormPanel() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(40));
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 20 0 0 20;");
        
        // Form title
        Text formTitle = new Text("Welcome Back");
        formTitle.setId("formTitle");
        formTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        formTitle.setFill(Color.web("#2c3e50"));
        
        // Form subtitle
        Text formSubtitle = new Text("Sign in to continue");
        formSubtitle.setId("formSubtitle");
        formSubtitle.setFont(Font.font("Segoe UI", 16));
        formSubtitle.setFill(Color.web("#7f8c8d"));
        
        // Status label for errors/messages
        statusLabel = new Label("");
        statusLabel.setTextFill(Color.RED);
        statusLabel.setVisible(false);
        
        // Create form container
        formContainer = new StackPane();
        VBox formContent = createFormContent();
        formContainer.getChildren().add(formContent);
        
        panel.getChildren().addAll(formTitle, formSubtitle, statusLabel, formContainer);
        return panel;
    }
    
    private VBox createFormContent() {
        VBox form = new VBox(20);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(350);
        
        // Role selection
        Label roleLabel = new Label("Select Role");
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("admin", "user");
        roleComboBox.setValue("user");
        roleComboBox.setPrefWidth(350);
        roleComboBox.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
        
        // Username field
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
        
        // Password field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
        
        // Confirm password field (initially hidden)
        Label confirmPasswordLabel = new Label("Confirm Password");
        confirmPasswordLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        confirmPasswordLabel.setVisible(false);
        
        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Re-enter your password");
        confirmPasswordField.setPrefHeight(40);
        confirmPasswordField.setStyle("-fx-font-size: 14px; -fx-background-radius: 5;");
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setManaged(false);
        confirmPasswordLabel.setManaged(false);
        
        // Action buttons
        actionButton = new Button("Login");
        actionButton.setPrefWidth(350);
        actionButton.setPrefHeight(45);
        actionButton.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        styleButton(actionButton, "#3498db", "#2980b9");
        
        toggleButton = new Button("Create an Account");
        toggleButton.setPrefWidth(350);
        toggleButton.setPrefHeight(45);
        toggleButton.setFont(Font.font("Segoe UI", 14));
        toggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-radius: 5;");
        
        // Add all components to form
        form.getChildren().addAll(
            roleLabel, roleComboBox,
            usernameLabel, usernameField,
            passwordLabel, passwordField,
            confirmPasswordLabel, confirmPasswordField,
            new Separator(Orientation.HORIZONTAL),
            actionButton, toggleButton
        );
        
        return form;
    }
    
    private void styleButton(Button button, String baseColor, String hoverColor) {
        button.setStyle(
            "-fx-background-color: " + baseColor + "; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 5; " +
            "-fx-cursor: hand;"
        );
        
        button.setOnMouseEntered(e -> 
            button.setStyle(
                "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        
        button.setOnMouseExited(e -> 
            button.setStyle(
                "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
        
        button.setOnMousePressed(e -> 
            button.setStyle(
                "-fx-background-color: " + hoverColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand; " +
                "-fx-translate-y: 1px;"
            )
        );
        
        button.setOnMouseReleased(e -> 
            button.setStyle(
                "-fx-background-color: " + baseColor + "; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            )
        );
    }
    
    private void animateEntrance(Node leftPanel, Node rightPanel) {
        // Animate left panel
        TranslateTransition leftTransition = new TranslateTransition(Duration.millis(800), leftPanel);
        leftTransition.setFromX(-300);
        leftTransition.setToX(0);
        
        FadeTransition leftFade = new FadeTransition(Duration.millis(800), leftPanel);
        leftFade.setFromValue(0);
        leftFade.setToValue(1);
        
        // Animate right panel
        TranslateTransition rightTransition = new TranslateTransition(Duration.millis(800), rightPanel);
        rightTransition.setFromX(300);
        rightTransition.setToX(0);
        
        FadeTransition rightFade = new FadeTransition(Duration.millis(800), rightPanel);
        rightFade.setFromValue(0);
        rightFade.setToValue(1);
        
        // Play all animations
        ParallelTransition parallelTransition = new ParallelTransition(
            leftTransition, leftFade, rightTransition, rightFade
        );
        parallelTransition.play();
    }
    
    private void setupEventHandlers(Stage primaryStage) {
        // Toggle between login and register modes
        toggleButton.setOnAction(e -> {
            isLoginMode = !isLoginMode;
            
            // Update UI elements
            Text formTitle = (Text) ((VBox) formContainer.getParent()).getChildren().get(0);
            Text formSubtitle = (Text) ((VBox) formContainer.getParent()).getChildren().get(1);
            
            // Animate form transition
            FadeTransition fade = new FadeTransition(Duration.millis(200), formContainer);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setOnFinished(event -> {
                // Update text and visibility based on mode
                if (isLoginMode) {
                    formTitle.setText("Welcome Back");
                    formSubtitle.setText("Sign in to continue");
                    actionButton.setText("Login");
                    toggleButton.setText("Create an Account");
                    confirmPasswordField.setVisible(false);
                    confirmPasswordField.setManaged(false);
                    ((Label) ((VBox) formContainer.getChildren().get(0)).getChildren().get(6)).setVisible(false);
                    ((Label) ((VBox) formContainer.getChildren().get(0)).getChildren().get(6)).setManaged(false);
                    roleComboBox.setDisable(false);
                } else {
                    formTitle.setText("Create Account");
                    formSubtitle.setText("Sign up to get started");
                    actionButton.setText("Register");
                    toggleButton.setText("Back to Login");
                    confirmPasswordField.setVisible(true);
                    confirmPasswordField.setManaged(true);
                    ((Label) ((VBox) formContainer.getChildren().get(0)).getChildren().get(6)).setVisible(true);
                    ((Label) ((VBox) formContainer.getChildren().get(0)).getChildren().get(6)).setManaged(true);
                    roleComboBox.setDisable(true);
                    roleComboBox.setValue("user");
                }
                
                // Clear fields
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
                statusLabel.setVisible(false);
                
                // Fade back in
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), formContainer);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fade.play();
        });
        
        // Handle login/register action
        actionButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String confirmPassword = confirmPasswordField.getText().trim();
            String selectedRole = roleComboBox.getValue();
            
            if (isLoginMode) {
                // Login logic
                if (username.isEmpty() || password.isEmpty()) {
                    showStatus("Please fill in all fields", true);
                    shakeNode(formContainer);
                } else {
                    String actualRole = validateLogin(username, password);
                    if (actualRole == null) {
                        showStatus("Invalid username or password", true);
                        shakeNode(formContainer);
                    } else if (!actualRole.equalsIgnoreCase(selectedRole)) {
                        showStatus("Incorrect role selected", true);
                        shakeNode(formContainer);
                    } else {
                        showStatus("Login successful!", false);
                        
                        // Successful login animation
                        ScaleTransition scale = new ScaleTransition(Duration.millis(200), actionButton);
                        scale.setToX(0.95);
                        scale.setToY(0.95);
                        scale.setAutoReverse(true);
                        scale.setCycleCount(2);
                        scale.setOnFinished(event -> {
                            if (actualRole.equals("admin")) {
                                AdminDashboard.launchDashboard();
                                primaryStage.close();
                            } else {
                                UserDashboard userDashboard = new UserDashboard(username);
                                userDashboard.start(primaryStage);
                            }
                        });
                        scale.play();
                    }
                }
            } else {
                // Register logic
                if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    showStatus("Please fill in all fields", true);
                    shakeNode(formContainer);
                } else if (!password.equals(confirmPassword)) {
                    showStatus("Passwords do not match", true);
                    shakeNode(formContainer);
                } else if (password.length() < 6) {
                    showStatus("Password must be at least 6 characters", true);
                    shakeNode(formContainer);
                } else {
                    if (registerUser(username, password)) {
                        showStatus("Registration successful!", false);
                        
                        // Success animation and switch to login
                        ScaleTransition scale = new ScaleTransition(Duration.millis(200), actionButton);
                        scale.setToX(0.95);
                        scale.setToY(0.95);
                        scale.setAutoReverse(true);
                        scale.setCycleCount(2);
                        scale.setOnFinished(event -> toggleButton.fire());
                        scale.play();
                    } else {
                        showStatus("Username already exists", true);
                        shakeNode(formContainer);
                    }
                }
            }
        });
    }
    
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setTextFill(isError ? Color.RED : Color.GREEN);
        statusLabel.setVisible(true);
        
        // Auto-hide after 5 seconds if success message
        if (!isError) {
            FadeTransition fade = new FadeTransition(Duration.millis(5000), statusLabel);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            fade.setDelay(Duration.seconds(3));
            fade.play();
        }
    }
    
    private void shakeNode(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setFromX(0f);
        tt.setByX(10f);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
            String tableSQL = "CREATE TABLE IF NOT EXISTS users (" +
                    "username VARCHAR(100) PRIMARY KEY," +
                    "password VARCHAR(100)," +
                    "role VARCHAR(20) DEFAULT 'user')";
            Statement stmt = connection.createStatement();
            stmt.execute(tableSQL);

            String checkAdmin = "SELECT * FROM users WHERE username='admin'";
            ResultSet rs = stmt.executeQuery(checkAdmin);
            if (!rs.next()) {
                String insertAdmin = "INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'admin')";
                stmt.execute(insertAdmin);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showStatus("Database connection error", true);
        }
    }

    private boolean registerUser(String username, String password) {
        try {
            String query = "INSERT INTO users (username, password, role) VALUES (?, ?, 'user')";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private String validateLogin(String username, String password) {
        try {
            String query = "SELECT role FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void launchDashboard() {
        new LoginRegistrationPage().start(new Stage());
    }

    public static void main(String[] args) {
        launch(args);
    }
}