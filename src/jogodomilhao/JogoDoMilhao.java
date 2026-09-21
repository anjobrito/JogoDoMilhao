package jogodomilhao;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Jogo do Milhão - versão JavaFX 8
 *
 * Arquivo completo com as correções solicitadas: - pular não altera prize e não
 * mostra transição - highlight só no acerto - timer pausado ao clicar
 * Universitários - timer pausado ao selecionar uma alternativa (primeiro
 * clique) - transição mostra valor da rodada atual
 *
 * Ajuste paths/sons/perguntas conforme seu ambiente.
 */
public class JogoDoMilhao extends Application {

    // ------- Inner Question -------
    public enum Difficulty {
        EASY, MEDIUM, HARD, EXPERT
    }

    public static class Question {

        String text, a, b, c, d;
        char correct;
        Difficulty difficulty;

        public Question(String text, String a, String b, String c, String d, char correct) {
            this(text, a, b, c, d, correct, Difficulty.MEDIUM);
        }

        public Question(String text, String a, String b, String c, String d, char correct, Difficulty difficulty) {
            this.text = text;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.correct = correct;
            this.difficulty = difficulty;
        }
    }

    // fields
    private Stage primaryStage;
    private Scene startScene;
    private MediaPlayer introPlayer, clickPlayer, terrorPlayer, errorPlayer, correctPlayer, areYouSurePlayer, transitionPlayer;
    private String chosenLanguage = "pt";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        prepareSounds();
        showStartScreen();
    }

    private void prepareSounds() {
        try {
            Path base = Paths.get(System.getProperty("user.dir"), "sounds");
            Path intro = base.resolve("intro.mp3");
            Path click = base.resolve("click.mp3");
            Path trans = base.resolve("transition.mp3");
            Path error = base.resolve("error.mp3");
            Path correct = base.resolve("correct.mp3");
            Path areyousure = base.resolve("areyousure.mp3");
            Path terror = base.resolve("terror.mp3");

            if (Files.exists(intro)) {
                introPlayer = new MediaPlayer(new Media(intro.toUri().toString()));
                introPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            }
            if (Files.exists(click)) {
                clickPlayer = new MediaPlayer(new Media(click.toUri().toString()));
            }
            if (Files.exists(correct)) {
                correctPlayer = new MediaPlayer(new Media(correct.toUri().toString()));
            }
            if (Files.exists(terror)) {
                terrorPlayer = new MediaPlayer(new Media(terror.toUri().toString()));
            }
            if (Files.exists(areyousure)) {
                areYouSurePlayer = new MediaPlayer(new Media(areyousure.toUri().toString()));
            }
            if (Files.exists(error)) {
                errorPlayer = new MediaPlayer(new Media(error.toUri().toString()));
            }
            if (Files.exists(trans)) {
                transitionPlayer = new MediaPlayer(new Media(trans.toUri().toString()));
            }
        } catch (Exception e) {
            System.out.println("Aviso: não foi possível carregar sons: " + e.getMessage());
        }
    }

    // Start screen
    private void showStartScreen() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #001850, #000a24);");
        Label title = new Label("💰 JOGO DO MILHÃO 💰");
        title.setFont(Font.font("Arial Black", FontWeight.BOLD, 56));
        title.setTextFill(Color.GOLD);
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        VBox menu = new VBox(18);
        menu.setAlignment(Pos.CENTER);
        Button btnStart = createMenuButton("▶ Jogar");
        Button btnExit = createMenuButton("❌ Sair");
        final ComboBox<String> cmbLang = new ComboBox<>();
        cmbLang.getItems().addAll("Português 🇧🇷", "English 🇺🇸");
        cmbLang.setValue("Português 🇧🇷");
        cmbLang.setPrefWidth(260);
        cmbLang.setOnAction(e -> {
            playClick();
            if (cmbLang.getValue().startsWith("English")) {
                chosenLanguage = "en";
            } else {
                chosenLanguage = "pt";
            }
        });

        btnStart.setOnAction(e -> {
            playClick();
            if (introPlayer != null) {
                introPlayer.stop();
            }
            showGameScreen(chosenLanguage);
        });
        btnExit.setOnAction(e -> {
            playClick();
            if (introPlayer != null) {
                introPlayer.stop();
            }
            primaryStage.close();
        });

        menu.getChildren().addAll(btnStart, btnExit,cmbLang);
        root.setCenter(menu);
        startScene = new Scene(root, 1100, 700);
        primaryStage.setScene(startScene);
        primaryStage.setTitle("Jogo do Milhão");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        primaryStage.show();
        if (introPlayer != null) {
            introPlayer.play();
        }
    }

    private Button createMenuButton(String text) {
        Button b = new Button(text);
        b.setFont(Font.font("Arial Black", FontWeight.BOLD, 30));
        b.setTextFill(Color.WHITE);
        b.setPrefWidth(300);
        b.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #f44336; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;"));
        return b;
    }

    private void playClick() {
        if (clickPlayer == null) {
            return;
        }
        try {
            clickPlayer.stop();
            clickPlayer.play();
        } catch (Exception ignored) {
        }
    }

    private void showGameScreen(String language) {
        GameScreen game = new GameScreen(language);
        game.start(primaryStage, this::showStartScreen);
    }

    // -------------------- GameScreen --------------------
    private class GameScreen {

        private final String language;
        private List<Question> questions = new ArrayList<>();
        private final List<Question> questionPool = new ArrayList<>();
        private final Set<String> usedQuestionTexts = new HashSet<>();
        private final Random random = new Random();
        private final List<Integer> prizes = new ArrayList<>();
        private int currentQuestion = 0;
        private int currentPrize = 0;
        private int skipsLeft = 2;
        private boolean fiftyUsed = false;
        private boolean universitariosUsed = false;
        private boolean awaitingConfirmation = false;
        private Button lastSelectedButton = null;

        // UI
        private Label lblPrize;
        private Label lblQuestion;
        private Button btnA, btnB, btnC, btnD;
        private Button btnFiftyRef, btnSkipRef, btnUniversitariosRef;
        private ListView<String> prizeListView;
        private BorderPane rootGame;
        private Scene sceneGame;

        // Timer
        private static final int TIMER_SECONDS = 30;
        private int remainingSeconds = TIMER_SECONDS;
        private Timeline countdown;
        private Label timerLabel;
        private Region timerBar;
        private DoubleProperty timerProgress = new SimpleDoubleProperty(1.0);

        private Runnable onGameEndRef;

        public GameScreen(String lang) {
            this.language = lang;
        }

        public void start(Stage stage, final Runnable onGameEnd) {
            this.onGameEndRef = onGameEnd;
            loadPrizes();
            loadQuestions();

            rootGame = new BorderPane();
            rootGame.setPadding(new Insets(12));
            rootGame.setStyle("-fx-background-color: linear-gradient(to bottom, #001850, #000a24);");

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label title = new Label("💰 Jogo do Milhão 💰");
            title.setFont(Font.font("Arial Black", FontWeight.BOLD, 46));
            title.setTextFill(Color.GOLD);
            lblPrize = new Label("Prêmio atual: R$ 0");
            lblPrize.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            lblPrize.setTextFill(Color.WHITE);

            ComboBox<String> cmbLang = new ComboBox<>();
            cmbLang.getItems().addAll("Português", "English");
            cmbLang.setValue(language.equals("en") ? "English" : "Português");
            cmbLang.setOnAction(e -> {
                playClick();
                String val = cmbLang.getValue();
                if (val.equals("English")) {
                    questions.clear();
                    loadQuestionsFromFile("questions_EN.txt");
                } else {
                    questions.clear();
                    loadQuestionsFromFile("questions_PT.txt");
                }
                resetGame();
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            top.getChildren().addAll(title, spacer, lblPrize, cmbLang);
            rootGame.setTop(top);

            VBox center = new VBox(20);
            center.setAlignment(Pos.TOP_CENTER);
            center.setPadding(new Insets(30));

            lblQuestion = new Label("Carregando perguntas...");
            lblQuestion.setWrapText(true);
            lblQuestion.setFont(Font.font("Arial Black", FontWeight.BOLD, 30));
            lblQuestion.setTextFill(Color.WHITE);
            lblQuestion.setStyle("-fx-background-color: #c62828; -fx-background-radius: 15;");
            lblQuestion.setPadding(new Insets(15));
            lblQuestion.setAlignment(Pos.CENTER);
            lblQuestion.setMaxWidth(Double.MAX_VALUE);
            lblQuestion.setMinHeight(Region.USE_PREF_SIZE);

            btnA = createAnswerButton("A)");
            btnB = createAnswerButton("B)");
            btnC = createAnswerButton("C)");
            btnD = createAnswerButton("D)");

            VBox botoesBox = new VBox(15, btnA, btnB, btnC, btnD);
            botoesBox.setAlignment(Pos.CENTER);
            center.getChildren().addAll(lblQuestion, botoesBox);
            rootGame.setCenter(center);

            VBox right = new VBox(8);
            right.setPadding(new Insets(8));
            right.setAlignment(Pos.TOP_CENTER);
            Label ladderTitle = new Label("Escada de prêmios");
            ladderTitle.setFont(Font.font("Arial", FontWeight.BOLD, 28));
            ladderTitle.setTextFill(Color.GOLD);
            prizeListView = new ListView<>();
            prizeListView.setPrefWidth(180);
            for (int i = prizes.size() - 1; i >= 0; i--) {
                prizeListView.getItems().add((i + 1) + " - R$ " + String.format("%,d", prizes.get(i)).replace(',', '.'));
            }
            right.getChildren().addAll(ladderTitle, prizeListView);
            rootGame.setRight(right);

            // Bottom: lifelines + timer + controls
            HBox bottom = new HBox(10);
            bottom.setAlignment(Pos.CENTER_LEFT);
            bottom.setPadding(new Insets(10));
            Button btnFifty = new Button("50:50");
            Button btnSkip = new Button("Pular (" + skipsLeft + ")");
            Button btnUniversitarios = new Button("🎓 Universitários");
            Button btnExit = new Button("Desistir");
            Button btnNext = new Button("Próxima");

            this.btnFiftyRef = btnFifty;
            this.btnSkipRef = btnSkip;
            this.btnUniversitariosRef = btnUniversitarios;

            Region spacerBottom = new Region();
            HBox.setHgrow(spacerBottom, Priority.ALWAYS);
            bottom.getChildren().addAll(btnFifty, btnSkip, btnUniversitarios, spacerBottom, btnExit, btnNext);

            // Timer bar
            StackPane timerContainer = new StackPane();
            timerContainer.setPrefHeight(36);
            timerContainer.setMaxWidth(Double.MAX_VALUE);
            timerContainer.setStyle("-fx-background-color: #333; -fx-background-radius: 6; -fx-padding:4;");

            timerBar = new Region();
            timerBar.setStyle("-fx-background-color: gold; -fx-background-radius: 6;");
            timerBar.setPrefHeight(36);
            timerBar.prefWidthProperty().bind(timerContainer.widthProperty().multiply(timerProgress));

            timerLabel = new Label();
            timerLabel.setFont(Font.font("Arial Black", FontWeight.BOLD, 48));
            timerLabel.setTextFill(Color.RED);

            timerContainer.getChildren().addAll(timerBar, timerLabel);
            BorderPane.setAlignment(timerContainer, Pos.CENTER);
            rootGame.setBottom(new VBox(timerContainer, bottom));

            // Handlers
            btnA.setOnAction(e -> handleAnswerClick(btnA, 'A', onGameEnd));
            btnB.setOnAction(e -> handleAnswerClick(btnB, 'B', onGameEnd));
            btnC.setOnAction(e -> handleAnswerClick(btnC, 'C', onGameEnd));
            btnD.setOnAction(e -> handleAnswerClick(btnD, 'D', onGameEnd));

            btnExit.setOnAction(e -> {
                playClick();
                stopTimer();
                GameAlert.show("Você desistiu! \n Você saiu do jogo com R$ " + currentPrize);
                onGameEnd.run();
            });

            btnNext.setOnAction(e -> {
                playClick();
                moveToNextQuestion(); // mantém comportamento anterior (com transição)
            });

            // SKIP: não muda prize, não mostra transição, só troca pergunta e reinicia timer
            btnSkip.setOnAction(e -> {
                playClick();
                if (skipsLeft > 0) {
                    skipsLeft--;
                    btnSkip.setText("Pular (" + skipsLeft + ")");
                    skipQuestion(); // novo método simples
                } else {
                    GameAlert.show("Você não tem mais pulos!");
                }
            });

            btnFifty.setOnAction(e -> {
                playClick();
                if (fiftyUsed) {
                    GameAlert.show("Você já usou o 50:50!");
                } else {
                    fiftyUsed = true;
                    useFifty();
                    btnFifty.setDisable(true);
                }
            });

            // Universitários: pausar timer e desabilitar
            btnUniversitarios.setOnAction(e -> {
                playClick();
                stopTimer(); // PAUSA o cronômetro aqui
                universitariosUsed = true;
                btnUniversitarios.setDisable(true);
            });

            rootGame.setCenter(center);
            sceneGame = new Scene(rootGame, 1100, 700);
            sceneGame.widthProperty().addListener((obs, oldValue, newValue) -> applyResponsiveFonts());
            sceneGame.heightProperty().addListener((obs, oldValue, newValue) -> applyResponsiveFonts());
            primaryStage.setScene(sceneGame);
            primaryStage.setTitle("Jogo do Milhão");
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
            primaryStage.show();

            // start: if questions exist, show transition for first round, then question 0
            if (!questions.isEmpty()) {
                int firstRoundPrize = prizes.get(0);
                showTransitionScreen(firstRoundPrize, () -> showQuestion(0, true));
            } else {
                lblQuestion.setText("Nenhuma pergunta encontrada no arquivo.");
            }

            sceneGame.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    moveToNextQuestion();
                }
            });
        }

        private Button createAnswerButton(String text) {
            Button b = new Button(text);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setMinHeight(52);
            b.setWrapText(true);
            b.setFont(Font.font("Arial Black", FontWeight.BOLD, 30));
            b.setAlignment(Pos.CENTER_LEFT);
            b.setTextFill(Color.WHITE);
            b.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
            b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #f44336; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;"));
            b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;"));
            return b;
        }

        private void loadPrizes() {
            prizes.clear();
            int[] v = {15000, 30000, 45000, 60000, 75000, 90000, 105000, 120000, 135000, 150000};
            for (int x : v) {
                prizes.add(x);
            }
        }

        private void loadQuestions() {
            String filename = language.equals("en") ? "questions_EN.txt" : "questions_PT.txt";
            loadQuestionsFromFile(filename);
        }

        private void loadQuestionsFromFile(String filename) {
            questions.clear();
            questionPool.clear();
            usedQuestionTexts.clear();

            Path p = Paths.get(System.getProperty("user.dir"), filename);
            if (!Files.exists(p)) {
                showAlert("Erro", "Arquivo " + filename + " não encontrado no diretório do projeto!");
                return;
            }

            try {
                List<String> lines = Files.readAllLines(p);
                for (String ln : lines) {
                    ln = ln.trim();
                    if (ln.isEmpty() || ln.startsWith("#")) {
                        continue;
                    }

                    String[] parts = ln.split("\\|", -1);

                    // New format:
                    // DIFFICULTY|Question|A|B|C|D|CORRECT
                    if (parts.length >= 7) {
                        Difficulty difficulty = parseDifficulty(parts[0]);
                        questionPool.add(new Question(
                                parts[1].trim(),
                                parts[2].trim(),
                                parts[3].trim(),
                                parts[4].trim(),
                                parts[5].trim(),
                                parts[6].trim().toUpperCase().charAt(0),
                                difficulty
                        ));
                    } else if (parts.length >= 6) {
                        // Backward compatibility with the original question bank.
                        questionPool.add(new Question(
                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim(),
                                parts[3].trim(),
                                parts[4].trim(),
                                parts[5].trim().toUpperCase().charAt(0),
                                Difficulty.MEDIUM
                        ));
                    }
                }

                buildQuestionsForGame();
            } catch (IOException e) {
                showAlert("Erro", "Falha ao ler " + filename + ": " + e.getMessage());
            }
        }

        private Difficulty parseDifficulty(String value) {
            try {
                return Difficulty.valueOf(value.trim().toUpperCase());
            } catch (Exception ignored) {
                return Difficulty.MEDIUM;
            }
        }

        private Difficulty difficultyForRound(int round) {
            if (round <= 2) {
                return Difficulty.EASY;
            }
            if (round <= 6) {
                return Difficulty.MEDIUM;
            }
            if (round <= 8) {
                return Difficulty.HARD;
            }
            return Difficulty.EXPERT;
        }

        private void buildQuestionsForGame() {
            questions.clear();
            usedQuestionTexts.clear();

            for (int round = 0; round < prizes.size(); round++) {
                Question selected = selectUnusedQuestion(difficultyForRound(round));
                if (selected == null) {
                    break;
                }
                questions.add(selected);
            }
        }

        private Question selectUnusedQuestion(Difficulty difficulty) {
            List<Question> candidates = new ArrayList<>();

            for (Question q : questionPool) {
                if (q.difficulty == difficulty && !usedQuestionTexts.contains(questionKey(q))) {
                    candidates.add(q);
                }
            }

            // Safe fallback: never repeat a question even if a difficulty pool is exhausted.
            if (candidates.isEmpty()) {
                for (Question q : questionPool) {
                    if (!usedQuestionTexts.contains(questionKey(q))) {
                        candidates.add(q);
                    }
                }
            }

            if (candidates.isEmpty()) {
                return null;
            }

            Question selected = candidates.get(random.nextInt(candidates.size()));
            usedQuestionTexts.add(questionKey(selected));
            return selected;
        }

        private String questionKey(Question q) {
            return q.text.trim().toLowerCase(Locale.ROOT);
        }

        /**
         * showQuestion com controle de highlight: highlight == true ->
         * seleciona na escada highlight == false -> NÃO altera highlight
         */
        private void showQuestion(int index, boolean highlight) {
            resetTimer();
            resetButtonColors();
            if (terrorPlayer != null) try {
                terrorPlayer.play();
            } catch (Exception ignored) {
            }
            if (index < 0 || index >= questions.size()) {
                return;
            }
            Question q = questions.get(index);
            lblQuestion.setText(q.text);
            btnA.setText("A) " + q.a);
            btnB.setText("B) " + q.b);
            btnC.setText("C) " + q.c);
            btnD.setText("D) " + q.d);
            Platform.runLater(this::applyResponsiveFonts);
            lblPrize.setText("Prêmio atual: R$ " + String.format("%,d", currentPrize).replace(',', '.'));
            if (highlight) {
                highlightPrize(index);
            }
            btnA.setDisable(false);
            btnB.setDisable(false);
            btnC.setDisable(false);
            btnD.setDisable(false);
            awaitingConfirmation = false;
            lastSelectedButton = null;
        }

        // Skip keeps the player in the same round and replaces only the current question.
        // The replacement uses the same difficulty and can never repeat a question already seen.
        private void skipQuestion() {
            stopTimer();

            Question replacement = selectUnusedQuestion(difficultyForRound(currentQuestion));
            if (replacement == null) {
                GameAlert.show("Não há mais perguntas disponíveis para esta partida.");
                return;
            }

            resetButtonColors();
            questions.set(currentQuestion, replacement);
            showQuestion(currentQuestion, false);
        }

        // moveToNextQuestion (used by Próxima): keeps transition behavior (announces prizeNext)
        private void moveToNextQuestion() {
            stopTimer();
            int next = currentQuestion + 1;
            if (next >= questions.size()) {
                GameAlert.show("Não há mais perguntas.");
                return;
            }
            int prizeNext = prizes.get(Math.min(next, prizes.size() - 1));
            showTransitionScreen(prizeNext, () -> {
                currentQuestion = next;
                showQuestion(currentQuestion, true);
            });
        }

        // answer used rarely; main flow is handleAnswerClick which has confirmation behavior
        private void answer(char chosen, Runnable onGameEnd) {
            if (questions.isEmpty()) {
                return;
            }
            Question q = questions.get(currentQuestion);
            if (q.correct == chosen) {
                if (correctPlayer != null) try {
                    correctPlayer.play();
                } catch (Exception ignored) {
                }
                currentPrize = prizes.get(Math.min(currentQuestion, prizes.size() - 1));
                lblPrize.setText("Prêmio atual: R$ " + String.format("%,d", currentPrize).replace(',', '.'));
                highlightPrize(currentQuestion);
                // after correct, announce next round prize or finish
                int nextIndex = currentQuestion + 1;
                if (nextIndex < questions.size() && currentPrize < 150000 ) {
                    int prizeNext = prizes.get(Math.min(nextIndex, prizes.size() - 1));
                    if (terrorPlayer != null) try {
                        terrorPlayer.stop();
                    } catch (Exception ignored) {
                    }
                    playTransitionSound();
                    resetButtonColors();
                    stopTimer();
                    showTransitionScreen(prizeNext, () -> {
                        currentQuestion = nextIndex;
                        showQuestion(currentQuestion, true);
                    });
                } else {
                    GameAlert.show("Parabéns! \n Você venceu o jogo com R$ " + currentPrize);
                    onGameEnd.run();
                }
            } else {
                if (errorPlayer != null) try {
                    errorPlayer.play();
                } catch (Exception ignored) {
                }
                if (terrorPlayer != null) try {
                    terrorPlayer.stop();
                } catch (Exception ignored) {
                }
                stopTimer();
                int loss = (currentQuestion == 0) ? 0 : prizes.get(Math.max(0, currentQuestion - 1));
                GameAlert.show(" Fim de jogo. \n Você saiu com R$ " + loss);
                onGameEnd.run();
            }
        }

        private int guaranteedPrize(int questionIndex) {
            if (questionIndex >= 5) {
                return prizes.get(4);
            }
            if (questionIndex >= 2) {
                return prizes.get(1);
            }
            return 0;
        }

        private void highlightPrize(int qIndex) {
            int idx = prizes.size() - 1 - qIndex;
            prizeListView.getSelectionModel().clearSelection();
            if (idx >= 0 && idx < prizeListView.getItems().size()) {
                prizeListView.getSelectionModel().select(idx);
            }
        }

        private void useFifty() {
            Question q = questions.get(currentQuestion);
            List<Button> all = Arrays.asList(btnA, btnB, btnC, btnD);
            Map<Button, Character> map = new HashMap<>();
            map.put(btnA, 'A');
            map.put(btnB, 'B');
            map.put(btnC, 'C');
            map.put(btnD, 'D');
            List<Button> wrong = new ArrayList<>();
            for (Button b : all) {
                if (map.get(b) != q.correct) {
                    wrong.add(b);
                }
            }
            Collections.shuffle(wrong);
            int removed = 0;
            for (Button b : wrong) {
                if (removed >= 2) {
                    break;
                }
                b.setDisable(true);
                removed++;
            }
        }

        // showTransitionScreen: overlay + fade, plays transition sound
        private void showTransitionScreen(int valor, final Runnable after) {
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
            Label lbl = new Label(" Valendo R$ " + String.format("%,d", valor).replace(',', '.'));
            lbl.setFont(Font.font("Arial Black", FontWeight.BOLD, 50));
            lbl.setTextFill(Color.GOLD);
            overlay.getChildren().add(lbl);
            rootGame.setCenter(overlay);
            resetButtonColors();
            FadeTransition ft = new FadeTransition(Duration.seconds(1.6), overlay);
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setDelay(Duration.seconds(1.0));
            ft.setOnFinished(e -> {
                VBox botoesBox = new VBox(15, btnA, btnB, btnC, btnD);
                botoesBox.setAlignment(Pos.CENTER);
                VBox center = new VBox(20, lblQuestion, botoesBox);
                center.setAlignment(Pos.CENTER);
                center.setPadding(new Insets(30));
                rootGame.setCenter(center);
                after.run();
            });
            ft.play();

            if (terrorPlayer != null) try {
                terrorPlayer.stop();
            } catch (Exception ignored) {
            }
            playTransitionSound();
        }

        private void playTransitionSound() {
            if (transitionPlayer == null) {
                return;
            }
            try {
                transitionPlayer.stop();
                transitionPlayer.play();
            } catch (Exception ignored) {
            }
        }

        private void showAlert(String title, String message) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }

        // reset to initial state and show first question with transition
        private void resetGame() {
            currentQuestion = 0;
            currentPrize = 0;
            skipsLeft = 2;
            fiftyUsed = false;
            universitariosUsed = false;
            if (btnSkipRef != null) {
                btnSkipRef.setText("Pular (" + skipsLeft + ")");
            }
            if (btnSkipRef != null) {
                btnSkipRef.setDisable(false);
            }
            if (btnFiftyRef != null) {
                btnFiftyRef.setDisable(false);
            }
            if (btnUniversitariosRef != null) {
                btnUniversitariosRef.setDisable(false);
            }
            loadQuestions();
            if (!questions.isEmpty()) {
                int firstRoundPrize = prizes.get(0);
                showTransitionScreen(firstRoundPrize, () -> showQuestion(0, true));
            } else {
                lblQuestion.setText("Nenhuma pergunta encontrada no arquivo.");
            }
        }

        /**
         * Quando o usuário clica em uma alternativa: - Primeiro clique: pausa
         * timer, destaca (aguarda confirmação) - Segundo clique no mesmo botão:
         * confirma e processa resposta
         */
        private void handleAnswerClick(Button btn, char chosen, Runnable onGameEnd) {
            playClick();

            // Primeiro clique: pausa e marca aguardando confirmação
            if (!awaitingConfirmation || lastSelectedButton != btn) {
                stopTimer(); // PAUSA O RELÓGIO no primeiro clique
                resetButtonColors();
                lastSelectedButton = btn;
                awaitingConfirmation = true;
                btn.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
                if (areYouSurePlayer != null) try {
                    areYouSurePlayer.stop();
                    areYouSurePlayer.play();
                } catch (Exception ignored) {
                }
                return;
            }

            // confirmação (segundo clique no mesmo botão)
            awaitingConfirmation = false;
            lastSelectedButton = null;
            stopTimer(); // assegura timer parado enquanto processa
            Question q = questions.get(currentQuestion);

            if (q.correct == chosen) {
                if (correctPlayer != null) try {
                    correctPlayer.stop();
                    correctPlayer.play();
                } catch (Exception ignored) {
                }
                blinkButton(btn, "#4CAF50");
                // concede o prêmio desta pergunta
                currentPrize = prizes.get(Math.min(currentQuestion, prizes.size() - 1));
                lblPrize.setText("Prêmio atual: R$ " + String.format("%,d", currentPrize).replace(',', '.'));
                highlightPrize(currentQuestion);

                if (terrorPlayer != null) try {
                    terrorPlayer.stop();
                } catch (Exception ignored) {
                }
                // prepara próxima rodada: anunciar o próximo prêmio (se houver)
                int nextIndex = currentQuestion + 1;
                if (nextIndex < questions.size() && currentPrize < 150000 ) {
                    int prizeNext = prizes.get(Math.min(nextIndex, prizes.size() - 1));
                    playTransitionSound();
                    resetButtonColors();
                    showTransitionScreen(prizeNext, () -> {
                        currentQuestion = nextIndex;
                        showQuestion(currentQuestion, true);
                    });
                } else {
                    Platform.runLater(() -> {
                        GameAlert.show(" Parabéns! \n Você venceu o jogo com R$ " + currentPrize);
                        onGameEndRef.run();
                    });
                }
            } else {
                if (errorPlayer != null) try {
                    errorPlayer.stop();
                    errorPlayer.play();
                } catch (Exception ignored) {
                }
                btn.setStyle("-fx-background-color: #FF0000; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
                int loss = (currentQuestion == 0) ? 0 : prizes.get(Math.max(0, currentQuestion - 1));
                GameAlert.show("Resposta errada \n Fim de jogo. Você saiu com R$ " + loss);
                if (terrorPlayer != null) try {
                    terrorPlayer.stop();
                } catch (Exception ignored) {
                }
                onGameEnd.run();
            }
        }

        private void applyResponsiveFonts() {
            if (sceneGame == null || lblQuestion == null) {
                return;
            }

            double width = Math.max(sceneGame.getWidth(), 800);
            double height = Math.max(sceneGame.getHeight(), 600);
            double scale = Math.min(width / 1920.0, height / 1080.0);
            scale = Math.max(0.72, Math.min(scale, 1.20));

            double contentWidth = Math.max(520, width - 320);
            double questionWidth = Math.min(contentWidth * 0.92, 1250);
            double answerWidth = Math.min(contentWidth * 0.82, 1100);

            fitLabelFont(lblQuestion, 34 * scale, 16, questionWidth, Math.max(100, height * 0.20));
            fitButtonFont(btnA, 30 * scale, 14, answerWidth);
            fitButtonFont(btnB, 30 * scale, 14, answerWidth);
            fitButtonFont(btnC, 30 * scale, 14, answerWidth);
            fitButtonFont(btnD, 30 * scale, 14, answerWidth);
        }

        private void fitLabelFont(Label label, double preferredSize, double minSize,
                                  double maxWidth, double maxHeight) {
            double size = Math.max(minSize, preferredSize);

            while (size > minSize) {
                Font font = Font.font("Arial Black", FontWeight.BOLD, size);
                Text probe = new Text(label.getText());
                probe.setFont(font);
                probe.setWrappingWidth(Math.max(100, maxWidth - 30));

                if (probe.getLayoutBounds().getHeight() <= maxHeight) {
                    label.setFont(font);
                    return;
                }
                size -= 1.0;
            }

            label.setFont(Font.font("Arial Black", FontWeight.BOLD, minSize));
        }

        private void fitButtonFont(Button button, double preferredSize, double minSize, double maxWidth) {
            double size = Math.max(minSize, preferredSize);
            double usableWidth = Math.max(200, maxWidth - 50);

            while (size > minSize) {
                Font font = Font.font("Arial Black", FontWeight.BOLD, size);
                Text probe = new Text(button.getText());
                probe.setFont(font);

                if (probe.getLayoutBounds().getWidth() <= usableWidth) {
                    button.setFont(font);
                    return;
                }
                size -= 1.0;
            }

            button.setFont(Font.font("Arial Black", FontWeight.BOLD, minSize));
        }

        private void resetButtonColors() {
            btnA.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
            btnB.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
            btnC.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
            btnD.setStyle("-fx-background-color: #d32f2f; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;");
        }

        private void blinkButton(Button btn, String colorHex) {
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(Duration.seconds(0.2), e -> btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;")),
                    new javafx.animation.KeyFrame(Duration.seconds(0.4), e -> btn.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 20; -fx-border-color: white; -fx-border-width: 2;"))
            );
            timeline.setCycleCount(6);
            timeline.play();
        }

        // TIMER
        private void resetTimer() {
            stopTimer();
            remainingSeconds = TIMER_SECONDS;
            timerProgress.set(1.0);
            updateTimerLabel();
            startTimer();
        }

        private void startTimer() {
            if (countdown != null) {
                countdown.stop();
            }
            countdown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                remainingSeconds--;
                double prog = Math.max(0.0, remainingSeconds / (double) TIMER_SECONDS);
                timerProgress.set(prog);
                updateTimerLabel();
                if (remainingSeconds <= 0) {
                    stopTimer();
                    onTimeExpired();
                }
            }));
            countdown.setCycleCount(TIMER_SECONDS);
            countdown.playFromStart();
        }

        private void stopTimer() {
            if (countdown != null) {
                countdown.stop();
            }
        }

        private void updateTimerLabel() {
            int s = Math.max(0, remainingSeconds);
            String fmt = String.format("00:00:%02d", s);
            timerLabel.setText(fmt);
        }

        private void onTimeExpired() {
            if (errorPlayer != null) try {
                errorPlayer.stop();
                errorPlayer.play();
            } catch (Exception ignored) {
            }
            if (terrorPlayer != null) try {
                terrorPlayer.stop();
            } catch (Exception ignored) {
            }
            Platform.runLater(() -> {
                GameAlert.show(" Tempo esgotado \n ⏰ Tempo esgotado! Isso conta como resposta errada.");
                onGameEndRef.run();
            });
        }
    } // end GameScreen

    // helpers
    private void playClickSoundOnce() {
        playClick();
    }

    private void playClickSilentSafe() {
        playClick();
    }

    private void playClickPlayerReset() {
    }

    private void playClickPlayerAgain() {
    }

    private void playClickIfExists() {
        playClick();
    }

    private void showGameScreenPlaceholder() {
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private List<Question> loadQuestionsFromFileSimple(String filename) {
        List<Question> list = new ArrayList<>();
        Path p = Paths.get(System.getProperty("user.dir"), filename);
        if (!Files.exists(p)) {
            return list;
        }
        try {
            List<String> lines = Files.readAllLines(p);
            for (String ln : lines) {
                ln = ln.trim();
                if (ln.isEmpty() || ln.startsWith("#")) {
                    continue;
                }
                String[] parts = ln.split("\\|", -1);
                if (parts.length >= 6) {
                    list.add(new Question(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim().toUpperCase().charAt(0)));
                }
            }
        } catch (IOException e) {
            System.out.println(" Erro lendo " + filename + ": " + e.getMessage());
        }
        return list;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
