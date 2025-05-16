package User_package;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class UserDashboard {

    private final String username;

    public UserDashboard(String username) {
        this.username = username;
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // ==== Sidebar ====
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(30));
        sidebar.setStyle("-fx-background-color: #1F2A40;");
        sidebar.setPrefWidth(250);

        Label appTitle = new Label("QUIZ APP");
        appTitle.setTextFill(Color.WHITE);
        appTitle.setFont(Font.font("Segoe UI", 26));
        appTitle.setPadding(new Insets(0, 0, 20, 0));

        Button btnDashboard = createSidebarButton("📋", "Dashboard");
        Button btnPlayQuiz = createSidebarButton("🎮", "Play Quiz");
        Button btnResults = createSidebarButton("📊", "My Results");
        Button btnLeaderboard = createSidebarButton("🏆", "Leaderboard");
        Button btnLogout = createSidebarButton("🚪", "Logout");

        sidebar.getChildren().addAll(appTitle, btnDashboard, btnPlayQuiz, btnResults, btnLeaderboard, btnLogout);
        sidebar.setAlignment(Pos.TOP_LEFT);

        // ==== Header ====
        HBox header = new HBox();
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setStyle("-fx-background-color: #273549;");
        header.setAlignment(Pos.CENTER_RIGHT);

        Label welcomeLabel = new Label("Welcome, " + username);
        welcomeLabel.setTextFill(Color.WHITE);
        welcomeLabel.setFont(Font.font("Segoe UI", 16));
        header.getChildren().add(welcomeLabel);

        // ==== Main Content Area ====
        StackPane contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #F4F6F8;");
        contentArea.setPadding(new Insets(40));

        Label defaultLabel = new Label("📌 Select an option from the left to begin.");
        defaultLabel.setFont(Font.font("Segoe UI", 20));
        defaultLabel.setTextFill(Color.GRAY);
        contentArea.getChildren().add(defaultLabel);

        // ==== Button Actions ====
        btnDashboard.setOnAction(e -> contentArea.getChildren().setAll(new DashboardPage(username).getView()));
        btnPlayQuiz.setOnAction(e -> contentArea.getChildren().setAll(new PlayQuizPage().getView(username, contentArea)));
        btnResults.setOnAction(e -> contentArea.getChildren().setAll(new ViewResultsPage(username).getView()));
        btnLeaderboard.setOnAction(e -> contentArea.getChildren().setAll(new LeaderboardPage().getView()));
        btnLogout.setOnAction(e -> {
            LoginRegistrationPage.launchDashboard();
            stage.close();
        });

        // ==== Assemble Layout ====
        root.setLeft(sidebar);
        root.setTop(header);
        root.setCenter(contentArea);

        Scene scene = new Scene(root, 1400, 780);
        stage.setScene(scene);
        stage.setTitle("User Dashboard");
        stage.show();
    }

    private Button createSidebarButton(String icon, String text) {
        Button btn = new Button(icon + "  " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Segoe UI", 14));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-padding: 10 20 10 10;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #34495E; -fx-text-fill: #00ffcc; -fx-padding: 10 20 10 10;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-padding: 10 20 10 10;"));
        return btn;
    }
}
