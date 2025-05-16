package User_package;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.sql.*;

public class DashboardPage {

    private final String username;
    private TextField nameField, emailField, genderField, ageField, mobileField;
    private Button editSaveBtn;
    private VBox mainLayout;
    private VBox activitySection;
    private VBox activityCard;

    public DashboardPage(String username) {
        this.username = username;
    }

    public VBox getView() {
        mainLayout = new VBox(30);
        mainLayout.setPadding(new Insets(30));
        mainLayout.setStyle("-fx-background-color: #F4F6F8;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Header with improved styling
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label title = new Label("Dashboard");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2C3E50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Profile button with avatar
        StackPane profileBtn = createProfileIcon(username);
        profileBtn.setOnMouseClicked(e -> showProfileDialog());

        header.getChildren().addAll(title, spacer, profileBtn);

        // Stats overview section
        VBox statsSection = new VBox(15);
        Label statsTitle = new Label("Your Statistics");
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        statsTitle.setTextFill(Color.web("#2C3E50"));

        // Dashboard Cards with improved layout
        HBox cardsLayout = new HBox(25);
        cardsLayout.setAlignment(Pos.CENTER);

        // Get data for cards
        int rank = getUserRank(username);
        int attempts = getQuizAttempts(username);
        int avgScore = getAverageScore(username);

        cardsLayout.getChildren().addAll(
                createStatsCard("Leaderboard Rank", rank == -1 ? "N/A" : String.valueOf(rank), "🏆", "#4ECDC4"),
                createStatsCard("Quiz Attempts", String.valueOf(attempts), "📝", "#FF6B6B"),
                createStatsCard("Average Score", avgScore + "%", "📊", "#FFD166")
        );

        statsSection.getChildren().addAll(statsTitle, cardsLayout);

        // Recent activity section
        activitySection = new VBox(15);
        activitySection.setPadding(new Insets(20, 0, 0, 0));

        Label activityTitle = new Label("Recent Activity");
        activityTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));
        activityTitle.setTextFill(Color.web("#2C3E50"));

        activityCard = new VBox(12);
        activityCard.setPadding(new Insets(20));
        activityCard.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Add shadow to card
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        activityCard.setEffect(shadow);

        // Get recent activities from database
        activityCard.getChildren().addAll(
                getRecentActivities()
        );

        activitySection.getChildren().addAll(activityTitle, activityCard);

        // Add all sections to main layout
        mainLayout.getChildren().addAll(header, statsSection, activitySection);

        // Add listener for resizing
        mainLayout.widthProperty().addListener((observable, oldValue, newValue) -> {
            adjustActivitySectionWidth(newValue.doubleValue());
        });

        return mainLayout;
    }

    private void adjustActivitySectionWidth(double dashboardWidth) {
        if (activityCard != null) {
            // Calculate desired width for the activity card based on dashboard width
            double desiredWidth = dashboardWidth - 60; // Subtract padding of mainLayout

            // Set the preferred width of the activity card
            activityCard.setPrefWidth(desiredWidth);
            activityCard.setMaxWidth(Region.USE_COMPUTED_SIZE); // Allow resizing if needed
        }
    }
    
    private VBox[] getRecentActivities() {
        VBox[] activities = new VBox[3]; // Default to 3 placeholder activities
        
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
             PreparedStatement ps = con.prepareStatement(
                     "SELECT category_name, score, attempted_at FROM result r " +
                     "JOIN category c ON r.category_id = c.category_id " +
                     "WHERE username = ? ORDER BY attempted_at DESC LIMIT 3")) {
            
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            
            int i = 0;
            while (rs.next() && i < 3) {
                String category = rs.getString("category_name");
                int score = rs.getInt("score");
                Timestamp dateTime = rs.getTimestamp("attempted_at");
                
                activities[i] = createActivityItem(
                    "Completed " + category + " Quiz", 
                    "Score: " + score , 
                    formatTimestamp(dateTime)
                );
                i++;
            }
            
            // Fill remaining slots with placeholder if needed
            while (i < 3) {
                if (i == 0) {
                    // No activities at all
                    activities[0] = createEmptyActivityItem("No quiz activities yet", 
                                                          "Take your first quiz to see your results here");
                    i = 3; // Skip the rest
                } else {
                    activities[i] = createEmptyActivityItem("", "");
                    i++;
                }
            }
            
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Create placeholder activities on error
            activities[0] = createEmptyActivityItem("Could not load activities", 
                                                  "Please check your database connection");
            activities[1] = createEmptyActivityItem("", "");
            activities[2] = createEmptyActivityItem("", "");
        }
        
        return activities;
    }
    
    private String formatTimestamp(Timestamp ts) {
        if (ts == null) return "";
        
        java.util.Date now = new java.util.Date();
        long diffInMillis = now.getTime() - ts.getTime();
        long diffInSeconds = diffInMillis / 1000;
        long diffInMinutes = diffInSeconds / 60;
        long diffInHours = diffInMinutes / 60;
        long diffInDays = diffInHours / 24;
        
        if (diffInDays > 0) {
            return diffInDays + (diffInDays == 1 ? " day ago" : " days ago");
        } else if (diffInHours > 0) {
            return diffInHours + (diffInHours == 1 ? " hour ago" : " hours ago");
        } else if (diffInMinutes > 0) {
            return diffInMinutes + (diffInMinutes == 1 ? " minute ago" : " minutes ago");
        } else {
            return "Just now";
        }
    }
    
    private VBox createActivityItem(String title, String detail, String time) {
        VBox item = new VBox(5);
        item.setPadding(new Insets(10, 5, 10, 10));
        item.setStyle("-fx-border-color: transparent transparent #f0f0f0 transparent; -fx-border-width: 0 0 1 0;");
        
        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        
        // Activity indicator
        Rectangle indicator = new Rectangle(3, 20);
        indicator.setFill(Color.web("#4ECDC4"));
        indicator.setArcWidth(3);
        indicator.setArcHeight(3);
        
        VBox textContent = new VBox(3);
        textContent.setPadding(new Insets(0, 0, 0, 10));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        HBox detailsBox = new HBox(15);
        
        Label detailLabel = new Label(detail);
        detailLabel.setFont(Font.font("Segoe UI", 12));
        detailLabel.setTextFill(Color.web("#7F8C8D"));
        
        Label timeLabel = new Label(time);
        timeLabel.setFont(Font.font("Segoe UI", 12));
        timeLabel.setTextFill(Color.web("#BDC3C7"));
        
        detailsBox.getChildren().addAll(detailLabel, timeLabel);
        textContent.getChildren().addAll(titleLabel, detailsBox);
        
        content.getChildren().addAll(indicator, textContent);
        item.getChildren().add(content);
        
        return item;
    }
    
    private VBox createEmptyActivityItem(String title, String message) {
        VBox item = new VBox(5);
        item.setPadding(new Insets(15));
        item.setAlignment(Pos.CENTER);
        
        if (!title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
            titleLabel.setTextFill(Color.web("#2C3E50"));
            
            Label messageLabel = new Label(message);
            messageLabel.setFont(Font.font("Segoe UI", 12));
            messageLabel.setTextFill(Color.web("#7F8C8D"));
            
            item.getChildren().addAll(titleLabel, messageLabel);
        }
        
        return item;
    }
    
    private StackPane createProfileIcon(String username) {
        StackPane profilePane = new StackPane();
        profilePane.setCursor(javafx.scene.Cursor.HAND);
        
        // Create circular background
        Circle circle = new Circle(18);
        circle.setFill(Color.web("#00ccaa"));
        
        // Create label with first letter of username
        Label initial = new Label(username.substring(0, 1).toUpperCase());
        initial.setTextFill(Color.WHITE);
        initial.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        profilePane.getChildren().addAll(circle, initial);
        
        // Add hover effect
        profilePane.setOnMouseEntered(e -> {
            circle.setFill(Color.web("#00e6bf"));
            // Add subtle grow effect
            profilePane.setScaleX(1.1);
            profilePane.setScaleY(1.1);
        });
        
        profilePane.setOnMouseExited(e -> {
            circle.setFill(Color.web("#00ccaa"));
            profilePane.setScaleX(1.0);
            profilePane.setScaleY(1.0);
        });
        
        return profilePane;
    }

    private VBox createStatsCard(String title, String value, String icon, String color) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setPrefWidth(220);
        card.setPrefHeight(180);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setOffsetX(0);
        shadow.setOffsetY(2);
        shadow.setRadius(10);
        card.setEffect(shadow);
        
        Rectangle colorBar = new Rectangle(50, 5);
        colorBar.setFill(Color.web(color));
        colorBar.setArcWidth(5);
        colorBar.setArcHeight(5);
        
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI", 24));
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        titleLabel.setTextFill(Color.web("#2C3E50"));
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        valueLabel.setTextFill(Color.web("#2C3E50"));
        
        card.getChildren().addAll(colorBar, iconLabel, titleLabel, valueLabel);
        
        // Add hover effect
        card.setOnMouseEntered(e -> {
            shadow.setRadius(15);
            shadow.setColor(Color.rgb(0, 0, 0, 0.2));
            card.setTranslateY(-2);
        });
        
        card.setOnMouseExited(e -> {
            shadow.setRadius(10);
            shadow.setColor(Color.rgb(0, 0, 0, 0.1));
            card.setTranslateY(0);
        });
        
        return card;
    }

    private void showProfileDialog() {
        // Create a new stage for the profile dialog
        Stage profileStage = new Stage();
        profileStage.initModality(Modality.APPLICATION_MODAL);
        profileStage.initStyle(StageStyle.UNDECORATED);
        profileStage.setTitle("User Profile");

        // Create the dialog content
        VBox dialogContent = new VBox(20);
        dialogContent.setPadding(new Insets(30));
        dialogContent.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        dialogContent.setMinWidth(550); // Set minimum width
        dialogContent.setMaxWidth(550); // Set maximum width for a wider pop-up

        // Add drop shadow to dialog
        DropShadow dialogShadow = new DropShadow();
        dialogShadow.setColor(Color.rgb(0, 0, 0, 0.3));
        dialogShadow.setRadius(20);
        dialogContent.setEffect(dialogShadow);

        // Dialog header
        HBox dialogHeader = new HBox();
        dialogHeader.setAlignment(Pos.CENTER_LEFT);

        // Profile avatar
        StackPane avatar = new StackPane();
        Circle avatarCircle = new Circle(35);
        avatarCircle.setFill(Color.web("#00ccaa"));

        String initialText = "";
        if (username != null && !username.isEmpty()) {
            initialText = username.substring(0, 1).toUpperCase();
        }
        Label avatarInitial = new Label(initialText);
        avatarInitial.setTextFill(Color.WHITE);
        avatarInitial.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));

        avatar.getChildren().addAll(avatarCircle, avatarInitial);

        // Dialog title
        VBox titleBox = new VBox(5);
        Label dialogTitle = new Label("Profile Details");
        dialogTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        Label userLabel = new Label(username);
        userLabel.setFont(Font.font("Segoe UI", 16));
        userLabel.setTextFill(Color.web("#666666"));

        titleBox.getChildren().addAll(dialogTitle, userLabel);

        // Close button
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #666666; -fx-font-size: 16;");
        closeButton.setOnAction(e -> profileStage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        dialogHeader.getChildren().addAll(avatar, new Region() {{ setMinWidth(15); }}, titleBox, spacer, closeButton);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Form fields with improved styling
        GridPane formGrid = new GridPane();
        formGrid.setHgap(20); // Increased horizontal gap
        formGrid.setVgap(15);

        // Name field
        nameField = createStyledTextField("Full Name");
        GridPane.setConstraints(nameField, 0, 0, 2, 1); // Span two columns

        // Email field
        emailField = createStyledTextField("Email Address");
        GridPane.setConstraints(emailField, 0, 1, 2, 1); // Span two columns

        // Gender field
        genderField = createStyledTextField("Gender");
        GridPane.setConstraints(genderField, 0, 2);

        // Age field
        ageField = createStyledTextField("Age");
        GridPane.setConstraints(ageField, 1, 2);

        // Mobile field
        mobileField = createStyledTextField("Mobile Number");
        GridPane.setConstraints(mobileField, 0, 3, 2, 1); // Span two columns

        formGrid.getChildren().addAll(new Label("Full Name"), nameField,
                                    new Label("Email Address"), emailField,
                                    new Label("Gender"), genderField,
                                    new Label("Age"), ageField,
                                    new Label("Mobile Number"), mobileField);
        int rowIndex = 0;
        for (javafx.scene.Node node : formGrid.getChildren()) {
            if (node instanceof Label) {
                GridPane.setRowIndex(node, rowIndex);
                GridPane.setColumnIndex(node, 0);
                rowIndex++;
            } else if (node instanceof TextField) {
                GridPane.setRowIndex(node, rowIndex - 1);
                GridPane.setColumnIndex(node, 1);
                GridPane.setColumnSpan(node, 1);
                if (rowIndex - 1 == 0 || rowIndex - 1 == 1 || rowIndex - 1 == 3) {
                    GridPane.setColumnSpan(node, 2);
                }
            }
        }


        // Load profile data
        loadProfileData();
        disableFields(true);

        // Action buttons
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelButton = new Button("Cancel");
        cancelButton.setPrefHeight(40);
        cancelButton.setPrefWidth(120);
        cancelButton.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #333333; -fx-background-radius: 5;");
        cancelButton.setOnAction(e -> {
            if (editSaveBtn.getText().equals("Save Changes")) {
                // If in edit mode, cancel and reload data
                loadProfileData();
                disableFields(true);
                editSaveBtn.setText("Edit Profile");
            } else {
                // Just close the dialog
                profileStage.close();
            }
        });

        editSaveBtn = new Button("Edit Profile");
        editSaveBtn.setPrefHeight(40);
        editSaveBtn.setPrefWidth(150);
        editSaveBtn.setStyle("-fx-background-color: #00ccaa; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        editSaveBtn.setOnAction(e -> {
            if (editSaveBtn.getText().equals("Edit Profile")) {
                disableFields(false);
                editSaveBtn.setText("Save Changes");
            } else {
                if (validateFields()) {
                    updateProfileData();
                    disableFields(true);
                    editSaveBtn.setText("Edit Profile");
                }
            }
        });

        actionButtons.getChildren().addAll(cancelButton, editSaveBtn);

        // Add all components to dialog
        VBox formContainer = new VBox(15);
        formContainer.getChildren().addAll(formGrid);

        dialogContent.getChildren().addAll(dialogHeader, separator, formContainer, actionButtons);

        // Create scene and show dialog
        Scene dialogScene = new Scene(dialogContent);
        profileStage.setScene(dialogScene);

        // Center on screen, below the top 50px
        if (mainLayout.getScene() != null && mainLayout.getScene().getWindow() != null) {
            javafx.stage.Window ownerWindow = mainLayout.getScene().getWindow();
            profileStage.setX(ownerWindow.getX() + (ownerWindow.getWidth() / 2) - (dialogContent.getPrefWidth() / 2));
            profileStage.setY(ownerWindow.getY() + 50);
        } else {
            profileStage.centerOnScreen();
            profileStage.setY(50); // Fallback to below top 50px
        }

        profileStage.showAndWait();
    }
    
    private TextField createStyledTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefHeight(35);
        textField.setFont(Font.font("Segoe UI", 14));
        textField.setStyle("-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;");
        return textField;
    }
    
    private void disableFields(boolean disable) {
        nameField.setDisable(disable);
        emailField.setDisable(disable);
        genderField.setDisable(disable);
        ageField.setDisable(disable);
        mobileField.setDisable(disable);
        
        // Change style based on state
        String style = disable ? 
            "-fx-background-color: #f8f8f8; -fx-border-color: #e0e0e0; -fx-border-radius: 5; -fx-background-radius: 5;" :
            "-fx-background-color: white; -fx-border-color: #00ccaa; -fx-border-radius: 5; -fx-background-radius: 5;";
        
        nameField.setStyle(style);
        emailField.setStyle(style);
        genderField.setStyle(style);
        ageField.setStyle(style);
        mobileField.setStyle(style);
    }

    private boolean validateFields() {
        StringBuilder errors = new StringBuilder();
        
        if (nameField.getText().trim().isEmpty()) {
            errors.append("- Name cannot be empty\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("- Email cannot be empty\n");
        } else if (!emailField.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            errors.append("- Invalid email format\n");
        }
        
        if (!ageField.getText().trim().isEmpty()) {
            try {
                int age = Integer.parseInt(ageField.getText().trim());
                if (age < 1 || age > 120) {
                    errors.append("- Age must be between 1 and 120\n");
                }
            } catch (NumberFormatException e) {
                errors.append("- Age must be a number\n");
            }
        }
        
        if (!mobileField.getText().trim().isEmpty() && !mobileField.getText().matches("^[0-9]{10}$")) {
            errors.append("- Mobile number should be 10 digits\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
            return false;
        }
        
        return true;
    }

    private void loadProfileData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users_profiles WHERE username = ?")) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                emailField.setText(rs.getString("email"));
                genderField.setText(rs.getString("gender"));
                ageField.setText(rs.getString("age"));
                mobileField.setText(rs.getString("mobile"));
            } else {
                // No profile data yet, set defaults
                nameField.setText(username);
                emailField.setText("");
                genderField.setText("");
                ageField.setText("");
                mobileField.setText("");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to load profile data.");
        }
    }

    private void updateProfileData() {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root")) {
            String sql = "REPLACE INTO users_profiles (username, name, email, gender, age, mobile) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, nameField.getText().trim());
            ps.setString(3, emailField.getText().trim());
            ps.setString(4, genderField.getText().trim());
            ps.setString(5, ageField.getText().trim());
            ps.setString(6, mobileField.getText().trim());
            ps.executeUpdate();
            
            showSuccessMessage("Profile Updated", "Your profile has been updated successfully.");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to update profile: " + ex.getMessage());
        }
    }
    
    private void showSuccessMessage(String title, String message) {
        // Create a custom success message that auto-dismisses
        Stage messageStage = new Stage();
        messageStage.initStyle(StageStyle.UNDECORATED);
        messageStage.initModality(Modality.APPLICATION_MODAL);

        VBox messageBox = new VBox(15);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPadding(new Insets(25));
        messageBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        // Add shadow
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(10);
        messageBox.setEffect(shadow);

        Label checkmark = new Label("✓");
        checkmark.setFont(Font.font("Segoe UI", 40));
        checkmark.setTextFill(Color.web("#00ccaa"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        Label messageLabel = new Label(message);
        messageLabel.setFont(Font.font("Segoe UI", 14));
        messageLabel.setTextFill(Color.web("#666666"));

        messageBox.getChildren().addAll(checkmark, titleLabel, messageLabel);

        Scene scene = new Scene(messageBox);
        messageStage.setScene(scene);

        // Center on screen, below the top 50px
        if (mainLayout.getScene() != null && mainLayout.getScene().getWindow() != null) {
            javafx.stage.Window ownerWindow = mainLayout.getScene().getWindow();
            messageStage.setX(ownerWindow.getX() + (ownerWindow.getWidth() / 2) - (messageBox.getPrefWidth() / 2));
            messageStage.setY(ownerWindow.getY() + 50 + (ownerWindow.getHeight() / 2) - (messageBox.getPrefHeight() / 2));
        } else {
            messageStage.centerOnScreen();
            messageStage.setY(50); // Fallback to below top 50px
        }

        // Show message and auto-dismiss after 2 seconds
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(1), messageBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> messageStage.close());

        messageStage.show();
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> fadeOut.play());
        delay.play();
    }

    private int getQuizAttempts(String user) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM result WHERE username = ?")) {
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getUserRank(String user) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
             PreparedStatement ps = con.prepareStatement(
                     "SELECT username, MAX(score) AS max_score FROM result GROUP BY username ORDER BY max_score DESC")) {
            ResultSet rs = ps.executeQuery();
            int position = 1;
            while (rs.next()) {
                if (rs.getString("username").equals(user)) {
                    return position;
                }
                position++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }
    
    private int getAverageScore(String user) {
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
             PreparedStatement ps = con.prepareStatement("SELECT AVG(score) FROM result WHERE username = ?")) {
            ps.setString(1, user);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? (int)Math.round(rs.getDouble(1)) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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
        
        alert.showAndWait();
    }
}