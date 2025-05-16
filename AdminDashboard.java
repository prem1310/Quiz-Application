package Admin_package;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import java.sql.*;
import User_package.LeaderboardPage;
import User_package.LoginRegistrationPage;

public class AdminDashboard extends Application {

    private static final int WINDOW_WIDTH = 1500;
    private static final int WINDOW_HEIGHT = 750;
    private Connection connection;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Admin Dashboard - Quiz Application");
        primaryStage.setMinWidth(WINDOW_WIDTH);
        primaryStage.setMinHeight(WINDOW_HEIGHT);
        primaryStage.setMaxWidth(WINDOW_WIDTH);
        primaryStage.setMaxHeight(WINDOW_HEIGHT);

        // Sidebar with modern styling
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #1a2530); -fx-background-radius: 0 15 15 0;");
        sidebar.setPrefWidth(220);
        
        // Admin header with icon placeholder
        HBox adminHeader = new HBox(10);
        adminHeader.setAlignment(Pos.CENTER_LEFT);
        adminHeader.setPadding(new Insets(0, 0, 15, 0));
        
        Rectangle iconPlaceholder = new Rectangle(32, 32);
        iconPlaceholder.setFill(Color.web("#3498db"));
        iconPlaceholder.setArcWidth(8);
        iconPlaceholder.setArcHeight(8);
        
        Label adminTitle = new Label("Admin Dashboard");
        adminTitle.setTextFill(Color.WHITE);
        adminTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        adminHeader.getChildren().addAll(iconPlaceholder, adminTitle);
        
        // Separator
        Separator headerSeparator = new Separator();
        headerSeparator.setStyle("-fx-background-color: #34495e;");
        
        // Navigation buttons
        Button btnDashboard = createSidebarButton("Dashboard", true);
        Button btnUserMang = createSidebarButton("User Management", false);
        Button btnCreateCate = createSidebarButton("Create Category", false);
        Button btnCreateQuiz = createSidebarButton("Create Quiz", false);
        Button btnEditQuiz = createSidebarButton("Edit Quiz", false);
        Button btnDeleteQuiz = createSidebarButton("Delete Quiz", false);
        Button btnViewResults = createSidebarButton("View Results", false);
        Button btnLeaderboard = createSidebarButton("Leaderboard", false);
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        Button btnLogout = createSidebarButton("Logout", false);
        btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;");
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5;"));

        sidebar.getChildren().addAll(adminHeader, headerSeparator, btnDashboard, btnUserMang, 
                                    btnCreateCate, btnCreateQuiz, btnEditQuiz, btnDeleteQuiz, 
                                    btnViewResults, btnLeaderboard, spacer, btnLogout);

        // Main content with drop shadow
        StackPane contentWrapper = new StackPane();
        contentWrapper.setStyle("-fx-background-color: transparent;");
        
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(30));
        mainContent.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(0);
        dropShadow.setOffsetY(0);
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.2));
        mainContent.setEffect(dropShadow);
        
        Label welcomeText = new Label("Welcome, Admin!");
        welcomeText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        welcomeText.setTextFill(Color.web("#2c3e50"));

        Label instruction = new Label("Select an option from the sidebar to manage quizzes and view results.");
        instruction.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        instruction.setTextFill(Color.web("#7f8c8d"));
        
        mainContent.getChildren().addAll(welcomeText, instruction);
        contentWrapper.getChildren().add(mainContent);

        // Layout
        BorderPane root = new BorderPane();
        root.setLeft(sidebar);
        root.setCenter(contentWrapper);
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setPadding(new Insets(15));

        // Scene
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Button actions
        btnDashboard.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnDashboard.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            AdminDashboard dashboardPage = new AdminDashboard();
            Pane dashboardView = dashboardPage.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(dashboardView);
        });
        
        btnUserMang.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnUserMang.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            UserManagement usermang = new UserManagement();
            Pane usermant = usermang.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(usermant);
        });
        
        btnCreateCate.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnCreateCate.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            CategoryPage categorypage = new CategoryPage();
            Pane category = categorypage.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(category);
        });
        
        btnCreateQuiz.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnCreateQuiz.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            CreateQuizPage quizPage = new CreateQuizPage();
            Pane quizView = quizPage.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(quizView);
        });
        
        btnEditQuiz.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnEditQuiz.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            EditQuizPage editQuizPane = new EditQuizPage();
            Parent view = editQuizPane.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(view);
        });

        btnDeleteQuiz.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnDeleteQuiz.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            DeleteQuizPage deleteQuizPane = new DeleteQuizPage();
            Parent view = deleteQuizPane.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(view);
        });
        
        btnViewResults.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnViewResults.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            AdminViewResultsPage adminviewresult = new AdminViewResultsPage();
            Parent view = adminviewresult.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(view);
        });
        
        btnLeaderboard.setOnAction(e -> {
            resetButtonStyles(sidebar);
            btnLeaderboard.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
            LeaderboardPage leaderboardpage = new LeaderboardPage();
            Parent view = leaderboardpage.getView();
            contentWrapper.getChildren().clear();
            contentWrapper.getChildren().add(view);
        });
        
        btnLogout.setOnAction(e -> {
            LoginRegistrationPage.launchDashboard();
            primaryStage.close();
        });
    }

    // Reset all button styles
    private void resetButtonStyles(VBox sidebar) {
        for (int i = 2; i < sidebar.getChildren().size() - 1; i++) {
            if (sidebar.getChildren().get(i) instanceof Button) {
                Button btn = (Button) sidebar.getChildren().get(i);
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 5;");
            }
        }
    }

    public Pane getView() {
        VBox container = new VBox(25);
        container.setPadding(new Insets(20));
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 15;");

        Label title = new Label("Dashboard Overview");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#2c3e50"));

        // Stats cards with improved styling
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setPadding(new Insets(10, 0, 20, 0));

        // Fetch data from DB
        int cateCount = getCount("category");
        int userCount = getCount("users where role = 'user'");
        int questionCount = getCount("questions");

        VBox quizCard = createStatCard("Total Categories", cateCount, "#3498db");
        VBox userCard = createStatCard("Total Users", userCount, "#2ecc71");
        VBox questionCard = createStatCard("Total Questions", questionCount, "#9b59b6");

        statsBox.getChildren().addAll(quizCard, userCard, questionCard);
        
        // Recent activity section
        Label recentActivityLabel = new Label("Recent Activity");
        recentActivityLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        recentActivityLabel.setTextFill(Color.web("#2c3e50"));
        
        VBox activityContainer = new VBox(10);
        activityContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 15;");
        
        for (int i = 0; i < 3; i++) {
            HBox activityItem = new HBox(10);
            activityItem.setAlignment(Pos.CENTER_LEFT);
            
            Rectangle activityIcon = new Rectangle(8, 8);
            activityIcon.setFill(Color.web("#3498db"));
            activityIcon.setArcWidth(8);
            activityIcon.setArcHeight(8);
            
            Label activityText = new Label("Sample activity " + (i + 1));
            activityText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
            
            activityItem.getChildren().addAll(activityIcon, activityText);
            activityContainer.getChildren().add(activityItem);
        }

        container.getChildren().addAll(title, statsBox, recentActivityLabel, activityContainer);
        return container;
    }

    private VBox createStatCard(String title, int count, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefSize(200, 140);
        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        card.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        titleLabel.setTextFill(Color.web("#7f8c8d"));

        Label countLabel = new Label(String.valueOf(count));
        countLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        countLabel.setTextFill(Color.web(color));

        card.getChildren().addAll(titleLabel, countLabel);
        return card;
    }

    private int getCount(String table) {
        String query = "SELECT COUNT(*) FROM " + table;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
    }
    
    private Button createSidebarButton(String text, boolean isActive) {
        Button btn = new Button(text);
        btn.setPrefWidth(190);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 10));
        
        if (isActive) {
            btn.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-background-radius: 5;");
        } else {
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 5;");
        }
        
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#1abc9c")) {
                btn.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 5;");
            }
        });
        
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#1abc9c")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 5;");
            }
        });
        
        return btn;
    }

    public static void launchDashboard() {
        // Utility method to launch dashboard without command-line args
        new AdminDashboard().start(new Stage());
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}