package User_package;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.sql.*;

public class LeaderboardPage {

    private Connection connection;
    private ComboBox<Category> categoryComboBox;
    private TableView<LeaderboardEntry> table;
    private Label totalPlayersLabel;

    public LeaderboardPage() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/quizapp_db", "root", "root");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Parent getView() {
        // Main container with gradient background
        VBox layout = new VBox(25);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #f9fbfd, #e8eff7);");
        
        // Header section
        HBox headerBox = createHeader();
        
        // Filter section
        HBox filterBox = createFilterSection();
        
        // Create table with styling
        createTable();
        
        // Add components to layout
        layout.getChildren().addAll(headerBox, filterBox, table);
        
        // Default to All Categories
        refreshLeaderboard();
        
        return layout;
    }
    
    private HBox createHeader() {
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        
        // Trophy icon
        Text trophyIcon = new Text("🏆");
        trophyIcon.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        
        // Title with shadow effect
        Label heading = new Label("Leaderboard");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        heading.setTextFill(Color.web("#2c3e50"));
        
        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(3.0);
        dropShadow.setOffsetX(1.0);
        dropShadow.setOffsetY(1.0);
        dropShadow.setColor(Color.color(0, 0, 0, 0.3));
        heading.setEffect(dropShadow);
        
        headerBox.getChildren().addAll(trophyIcon, heading);
        
        return headerBox;
    }
    
    private HBox createFilterSection() {
        HBox filterBox = new HBox(20);
        filterBox.setAlignment(Pos.CENTER);
        filterBox.setPadding(new Insets(10, 0, 20, 0));
        
        // Category label
        Label categoryLabel = new Label("Filter by Category:");
        categoryLabel.setFont(Font.font("Segoe UI", FontWeight.MEDIUM, 14));
        
        // Category dropdown with styling
        categoryComboBox = new ComboBox<>();
        categoryComboBox.setPromptText("Select Category");
        categoryComboBox.setMinWidth(250);
        categoryComboBox.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-radius: 5px; " +
            "-fx-background-color: white; " +
            "-fx-border-color: #dcdcdc; " +
            "-fx-border-radius: 5px;"
        );
        categoryComboBox.setOnAction(e -> refreshLeaderboard());
        
        // Load categories
        loadCategories();
        
        // Total players counter
        totalPlayersLabel = new Label();
        totalPlayersLabel.setFont(Font.font("Segoe UI", 14));
        totalPlayersLabel.setTextFill(Color.web("#34495e"));
        totalPlayersLabel.setTextAlignment(TextAlignment.RIGHT);
        
        // Add components to filter box
        filterBox.getChildren().addAll(categoryLabel, categoryComboBox, totalPlayersLabel);
        HBox.setHgrow(totalPlayersLabel, Priority.ALWAYS);
        totalPlayersLabel.setAlignment(Pos.CENTER_RIGHT);
        
        return filterBox;
    }
    
    private void createTable() {
        table = new TableView<>();
        table.setPrefHeight(500);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply table styling
        table.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-color: #dcdcdc; " +
            "-fx-border-radius: 10px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);"
        );
        
        // Rank column with medal emojis
        TableColumn<LeaderboardEntry, String> rankCol = new TableColumn<>("Rank");
        rankCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                    setStyle("");
                } else {
                    int index = getIndex();
                    
                    // Style based on rank
                    switch (index) {
                        case 0 -> {
                            setText("🥇");
                            setStyle("-fx-font-size: 18px; -fx-alignment: center; -fx-font-weight: bold; -fx-text-fill: #D4AF37;");
                        }
                        case 1 -> {
                            setText("🥈");
                            setStyle("-fx-font-size: 18px; -fx-alignment: center; -fx-font-weight: bold; -fx-text-fill: #C0C0C0;");
                        }
                        case 2 -> {
                            setText("🥉");
                            setStyle("-fx-font-size: 18px; -fx-alignment: center; -fx-font-weight: bold; -fx-text-fill: #CD7F32;");
                        }
                        default -> {
                            setText(String.valueOf(index + 1));
                            setStyle("-fx-font-size: 14px; -fx-alignment: center;");
                        }
                    }
                }
            }
        });
        rankCol.setPrefWidth(80);
        rankCol.setStyle("-fx-alignment: CENTER;");
        
        // Username column
        TableColumn<LeaderboardEntry, String> userCol = new TableColumn<>("Username");
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
                    
                    // Highlight top 3 players
                    int index = getIndex();
                    if (index < 3) {
                        setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    } else {
                        setStyle("-fx-font-size: 14px;");
                    }
                }
            }
        });
        
        // Score column
        TableColumn<LeaderboardEntry, Integer> scoreCol = new TableColumn<>("Score");
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
                    
                    // Highlight top 3 players
                    int index = getIndex();
                    if (index < 3) {
                        setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-font-size: 14px;");
                    } else {
                        setStyle("-fx-alignment: center; -fx-font-size: 14px;");
                    }
                }
            }
        });
        scoreCol.setStyle("-fx-alignment: CENTER;");
        
        // Add columns to table
        table.getColumns().addAll(rankCol, userCol, scoreCol);
        
        // Style the table header
        table.setStyle(table.getStyle() + "-fx-font-size: 14px;");
    }

    private void loadCategories() {
        categoryComboBox.getItems().clear();
        categoryComboBox.getItems().add(new Category(0, "All Categories"));

        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM category");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("category_id");
                String name = rs.getString("category_name");
                categoryComboBox.getItems().add(new Category(id, name));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        categoryComboBox.getSelectionModel().selectFirst();
    }

    private void refreshLeaderboard() {
        Category selected = categoryComboBox.getSelectionModel().getSelectedItem();
        ObservableList<LeaderboardEntry> list = FXCollections.observableArrayList();

        String query;
        if (selected.categoryId() == 0) {
            query = """
                SELECT u.username, COALESCE(SUM(r.score), 0) AS total_score
                FROM users u
                LEFT JOIN result r ON u.username = r.username
                where u.role = 'user'
                GROUP BY u.username
                ORDER BY total_score DESC
            """;
        } else {
            query = """
                SELECT u.username, COALESCE(SUM(r.score), 0) AS total_score
                FROM users u
                LEFT JOIN result r ON u.username = r.username AND r.category_id = ?
                where u.role = 'user'
                GROUP BY u.username
                ORDER BY total_score DESC
            """;
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            if (selected.categoryId() != 0) {
                stmt.setInt(1, selected.categoryId());
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String user = rs.getString("username");
                    int score = rs.getInt("total_score");
                    list.add(new LeaderboardEntry(user, score));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        table.setItems(list);
        totalPlayersLabel.setText("👥 Total Players: " + list.size());
    }

    // Leaderboard entry model
    public static class LeaderboardEntry {
        private final SimpleStringProperty username;
        private final SimpleIntegerProperty score;

        public LeaderboardEntry(String username, int score) {
            this.username = new SimpleStringProperty(username);
            this.score = new SimpleIntegerProperty(score);
        }

        public SimpleStringProperty usernameProperty() {
            return username;
        }

        public SimpleIntegerProperty scoreProperty() {
            return score;
        }
    }

    // Category model
    public record Category(int categoryId, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}