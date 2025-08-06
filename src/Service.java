import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Service extends Application {
    private StackPane mainPane;
    private TableView<Result> tableView;
    Button quizStart;
    private Label timerLabel;
    private Timeline timeline;
    private int timeRemaining = 92; // 1.5 minutes++
    private List<Ques> questionList;
    private List<ToggleGroup> answerGroups;
    private List<Integer> correctAnswers;
    private long startTime, submitCount = 0;
    Button exitBtn, resetBtn, submitBtn;

    private AnchorPane homePane, quizPane;
    public void start(Stage primaryStage) {
        mainPane = new StackPane();
        setupHomePane();
        //setupQuizPane();

        mainPane.getChildren().add(homePane);
        primaryStage.setTitle("Quiz Test");
        primaryStage.setScene(new Scene(mainPane, 1000, 600));
        primaryStage.show();
    }

    private void setupHomePane() {
        homePane = new AnchorPane();
        homePane.setPadding(new Insets(20));
        homePane.setStyle("-fx-background-color: lightblue;");

        Label homeLabel = new Label("Welcome to Quiz Test App");
        homeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: Arial;");
        homeLabel.setLayoutX(400);
        homeLabel.setLayoutY(20);

        quizStart = new Button("Start Quiz");
        quizStart.setLayoutX(445);
        quizStart.setLayoutY(100);
        styleGreenButton(quizStart);
        quizStart.setOnAction(this::handleAction);

        Label leaderboard = new Label("Leaderboard: ");
        leaderboard.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-font-family: Arial;");
        leaderboard.setLayoutX(120);
        leaderboard.setLayoutY(160);

        tableView = new TableView<>();

        TableColumn<Result, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Result, Integer> marksCol = new TableColumn<>("Score");
        marksCol.setCellValueFactory(new PropertyValueFactory<>("marks"));

        TableColumn<Result, Double> timeCol = new TableColumn<>("Taken Time (Second)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("takenTime"));

        tableView.getColumns().addAll(nameCol, marksCol, timeCol);
        //tableView.setPrefHeight(150);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setLayoutX(290);
        tableView.setLayoutY(200);
        nameCol.setPrefWidth(200);
        marksCol.setPrefWidth(100);
        timeCol.setPrefWidth(160);

        tableView.setPlaceholder(new Label("No data available"));

        loadLeaderBoard();

        homePane.getChildren().addAll(homeLabel, quizStart, leaderboard, tableView);

    }

    private void setupQuizPane() {
        quizPane = new AnchorPane();
        quizPane.setStyle("-fx-background-color: #F5F5F5;");
        quizPane.setPadding(new Insets(20));

        timerLabel = new Label("Time: 1:30");
        timerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        AnchorPane.setTopAnchor(timerLabel, 10.0);
        AnchorPane.setRightAnchor(timerLabel, 20.0);

        VBox quizBox = new VBox(15);
        quizBox.setPadding(new Insets(50, 20, 20, 20));

        answerGroups = new ArrayList<>();
        correctAnswers = new ArrayList<>(); // <-- Add this list to track correct answers

        questionList = List.of(
                new Ques("নিচের কোনটি বিশেষ্য পদ?", "লাল", "দৌড়ানো", "ছাত্র", "দ্রুত", 3),
                new Ques("নিচের কোনটি ক্রিয়া পদ?", "খাওয়া", "লাল", "সুন্দর", "ছাত্র", 1)
        );

        for (Ques q : questionList) {
            Label qLabel = new Label(q.ques);
            qLabel.setWrapText(true);
            qLabel.setStyle("-fx-font-weight: bold;");

            ToggleGroup group = new ToggleGroup();
            RadioButton rb1 = new RadioButton(q.op1);
            RadioButton rb2 = new RadioButton(q.op2);
            RadioButton rb3 = new RadioButton(q.op3);
            RadioButton rb4 = new RadioButton(q.op4);

            rb1.setToggleGroup(group);
            rb2.setToggleGroup(group);
            rb3.setToggleGroup(group);
            rb4.setToggleGroup(group);

            VBox qPane = new VBox(5, qLabel, rb1, rb2, rb3, rb4);
            qPane.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 5;");
            quizBox.getChildren().add(qPane);
            answerGroups.add(group);
            correctAnswers.add(q.ans); // Store correct answer (1–4)
        }

        // Buttons
        exitBtn = new Button("Exit");
        resetBtn = new Button("Reset");
        submitBtn = new Button("Submit");

        exitBtn.setOnAction(this::handleAction);
        resetBtn.setOnAction(this::handleAction);
        submitBtn.setOnAction(e -> submitQuiz());

        HBox buttons = new HBox(20, exitBtn, resetBtn, submitBtn);
        buttons.setAlignment(Pos.CENTER);
        quizBox.getChildren().add(buttons);

        ScrollPane scrollPane = new ScrollPane(quizBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(500);

        quizPane.getChildren().addAll(scrollPane, timerLabel);

        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);

        startTimer();
    }


    private void loadLeaderBoard(){
        tableView.getItems().clear();
        try(Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM quiz_result order by marks desc, time asc");
            int row = 0;
            while(rs.next()) {
                String name = rs.getString("stuname");
                int marks = rs.getInt("marks");
                double time = rs.getDouble("time");

                tableView.getItems().add(new Result(name, marks, time));
                row++;
            }
            double rowHeight = 25;
            double headerHeight = 30;
            tableView.setPrefHeight(headerHeight + (row * rowHeight));

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void startTimer() {
        timeRemaining = 92;
        startTime = System.currentTimeMillis();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeRemaining--;
            int min = timeRemaining / 60;
            int sec = timeRemaining % 60;
            timerLabel.setText(String.format("Time Remaining: %d:%02d", min, sec));

            if (timeRemaining <= 0) {
                timeline.stop();
                submitQuiz();
            }
        }));
        timeline.setCycleCount(timeRemaining);
        timeline.play();
    }

//    private List<Quiz> fetchRandomQuestions() {
//        List<Quiz> list = new ArrayList<>();
//        try (Connection conn = Database.getConnection()) {
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM quiz_ques ORDER BY RAND() LIMIT 5");
//            while (rs.next()) {
//                list.add(new Quiz(
//                        rs.getInt("qid"),
//                        rs.getString("ques"),
//                        rs.getString("op1"),
//                        rs.getString("op2"),
//                        rs.getString("op3"),
//                        rs.getInt("ans")
//                ));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }


    private void submitQuiz() {
        if (submitCount > 0) return;
        timeline.stop(); // Assuming you have a timer
        submitCount++;

        int correct = 0;

        for (int i = 0; i < questionList.size(); i++) {
            ToggleGroup group = answerGroups.get(i);
            Ques q = questionList.get(i);
            int correctAns = q.ans;

            // Reset all buttons' styles first
            for (Toggle toggle : group.getToggles()) {
                RadioButton rb = (RadioButton) toggle;
                rb.setStyle("-fx-background-color: transparent; -fx-text-fill: black;");
            }

            // Highlight the correct answer in green
            RadioButton correctBtn = (RadioButton) group.getToggles().get(correctAns - 1);
            correctBtn.setStyle("-fx-text-fill: green;");

            Toggle selected = group.getSelectedToggle();
            if (selected != null) {
                RadioButton selectedBtn = (RadioButton) selected;
                int selectedIndex = group.getToggles().indexOf(selectedBtn);

                if (selectedIndex == correctAns - 1) {
                    correct++;
                } else {
                    // Wrong answer — style it red
                    selectedBtn.setStyle("-fx-background-color: yellow; -fx-text-fill: red;");
                }
            }
        }

        // Calculate and format time taken
        double totalTimeTaken = ((System.currentTimeMillis() - startTime) / 1000.0);
        double timeTakenMinute = Math.round(totalTimeTaken * 100.00) / 100.00;
        int finalCorrect = correct;

        Platform.runLater(() -> {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setHeaderText("Enter your name to see your score:");
            Optional<String> result = nameDialog.showAndWait();

            result.ifPresent(name -> saveResultToDB(name, finalCorrect, totalTimeTaken));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Quiz Result");
            alert.setHeaderText("You scored: " + finalCorrect + " out of " + questionList.size());
            alert.setContentText("Correct options are marked in green.\nWrong choices are marked in red.\nTime taken: " + totalTimeTaken + " seconds.");
            alert.show();
        });
    }


    private void saveResultToDB(String name, int marks, double time) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO quiz_result(stuname, marks, time) VALUES (?, ?, ?)");
            ps.setString(1, name);
            ps.setInt(2, marks);
            ps.setDouble(3, time);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAction(ActionEvent event){
        if(event.getSource() == quizStart){
            mainPane.getChildren().clear();
            if (timeline != null) timeline.stop();
            setupQuizPane();
            //mainPane.getChildren().add(quizPane);
            mainPane.getChildren().addAll(quizPane);
        }
        else if(event.getSource() == exitBtn){
            mainPane.getChildren().clear();
            if(timeline != null) timeline.stop();
            loadLeaderBoard();
            mainPane.getChildren().addAll(homePane);
            submitCount = 0;
            timeRemaining = 92;
        }
        else if(event.getSource() == resetBtn){
            mainPane.getChildren().clear();
            if(timeline != null) timeline.stop();
            //loadLeaderBoard();
            mainPane.getChildren().clear();
            setupQuizPane();
            mainPane.getChildren().addAll(quizPane);
            submitCount = 0;
            timeRemaining = 92;
        }
    }

    private void styleGreenButton(Button button){
        button.setStyle("-fx-background-color: #04AA6D; -fx-pref-width: 100; -fx-text-fill: white; -fx-font-weight:bold; -fx-font-size: 14px; -fx-padding: 10px;");
    }
}
