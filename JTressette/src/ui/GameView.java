package ui;

import game.Cards;
import game.Giocatore;
import game.GiocatoreUmano;
import game.GameState;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * GameView: JavaFX-based view for the card game following MVC pattern.
 * Displays the table, player's hand, opponent hands (face down), and cards played.
 * Styled like an online poker server with a green felt table.
 */
public class GameView {

    // Colors inspired by poker table felt
    private static final Color FELT_GREEN = Color.rgb(26, 117, 65);
    private static final Color FELT_DARK = Color.rgb(18, 85, 47);
    private static final Color FELT_BORDER = Color.rgb(100, 70, 40);
    private static final Color CARD_BACK = Color.rgb(30, 60, 120);
    private static final Color TEXT_GOLD = Color.rgb(255, 215, 0);
    private static final Color TEXT_WHITE = Color.WHITE;

    private final Stage stage;
    private final GameState gameState;
    private final GiocatoreUmano humanPlayer;
    private final GameController controller;

    // UI components that need updating
    private HBox playerHandBox;
    private HBox tableCardsBox;
    private VBox opponentArea;
    private Label statusLabel;
    private Label scoreLabel;
    private VBox logArea;
    private Button playButton;

    private int selectedCardIndex = -1;
    private List<StackPane> cardViews = new ArrayList<>();

    public GameView(Stage stage, GameState gameState, GiocatoreUmano humanPlayer, GameController controller) {
        this.stage = stage;
        this.gameState = gameState;
        this.humanPlayer = humanPlayer;
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        BorderPane root = new BorderPane();
        root.setBackground(createTableBackground());

        // Top: Opponent area (face-down cards)
        opponentArea = createOpponentArea();
        root.setTop(opponentArea);
        BorderPane.setMargin(opponentArea, new Insets(20));

        // Center: Table with played cards
        StackPane tableCenter = createTableCenter();
        root.setCenter(tableCenter);

        // Bottom: Player's hand
        VBox bottomArea = createPlayerArea();
        root.setBottom(bottomArea);
        BorderPane.setMargin(bottomArea, new Insets(10, 20, 20, 20));

        // Right: Log/info panel
        VBox rightPanel = createInfoPanel();
        root.setRight(rightPanel);
        BorderPane.setMargin(rightPanel, new Insets(20));

        Scene scene = new Scene(root, 1100, 750);
        stage.setScene(scene);
        stage.setTitle("JTressette - Partita in Corso");
        stage.setResizable(false);

        // Initial refresh
        refresh();
    }

    private Background createTableBackground() {
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, FELT_DARK),
                new Stop(0.5, FELT_GREEN),
                new Stop(1, FELT_DARK)
        );
        return new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY));
    }

    private VBox createOpponentArea() {
        VBox area = new VBox(10);
        area.setAlignment(Pos.CENTER);

        // Show each opponent's cards (face down)
        HBox opponentsRow = new HBox(40);
        opponentsRow.setAlignment(Pos.CENTER);

        for (Giocatore player : gameState.getPlayers()) {
            if (player != humanPlayer) {
                VBox opponentBox = createOpponentBox(player);
                opponentsRow.getChildren().add(opponentBox);
            }
        }

        area.getChildren().add(opponentsRow);
        return area;
    }

    private VBox createOpponentBox(Giocatore player) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(player.getName());
        nameLabel.setTextFill(TEXT_WHITE);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Face-down cards in a horizontal row
        HBox cardsBox = new HBox(-25); // Negative spacing for overlapping cards
        cardsBox.setAlignment(Pos.CENTER);

        List<Cards> hand = gameState.getHand(player);
        for (int i = 0; i < hand.size(); i++) {
            StackPane cardBack = createCardBack();
            cardsBox.getChildren().add(cardBack);
        }

        box.getChildren().addAll(nameLabel, cardsBox);
        return box;
    }

    private StackPane createTableCenter() {
        StackPane center = new StackPane();
        center.setAlignment(Pos.CENTER);

        // Create an oval table area
        Rectangle tableOval = new Rectangle(500, 250);
        tableOval.setArcWidth(100);
        tableOval.setArcHeight(100);
        tableOval.setFill(Color.rgb(35, 130, 75));
        tableOval.setStroke(FELT_BORDER);
        tableOval.setStrokeWidth(8);

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.BLACK);
        shadow.setRadius(15);
        shadow.setOffsetY(5);
        tableOval.setEffect(shadow);

        // Cards on the table (played in current trick)
        tableCardsBox = new HBox(15);
        tableCardsBox.setAlignment(Pos.CENTER);

        VBox tableContent = new VBox(10);
        tableContent.setAlignment(Pos.CENTER);

        Label tableLabel = new Label("Tavolo");
        tableLabel.setTextFill(TEXT_GOLD);
        tableLabel.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        tableContent.getChildren().addAll(tableLabel, tableCardsBox);

        center.getChildren().addAll(tableOval, tableContent);
        return center;
    }

    private VBox createPlayerArea() {
        VBox area = new VBox(10);
        area.setAlignment(Pos.CENTER);

        // Player name and score
        Label playerLabel = new Label("La tua mano - " + humanPlayer.getName());
        playerLabel.setTextFill(TEXT_WHITE);
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        // Player's hand
        playerHandBox = new HBox(10);
        playerHandBox.setAlignment(Pos.CENTER);
        playerHandBox.setPadding(new Insets(10));

        // Play button
        playButton = new Button("Gioca Carta");
        playButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        playButton.setStyle("-fx-background-color: #c8a000; -fx-text-fill: white; -fx-padding: 10 20;");
        playButton.setDisable(true);
        playButton.setOnAction(e -> onPlayCard());

        area.getChildren().addAll(playerLabel, playerHandBox, playButton);
        return area;
    }

    private VBox createInfoPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));
        panel.setMinWidth(220);
        panel.setMaxWidth(220);
        panel.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.5), new CornerRadii(10), Insets.EMPTY)));

        Label titleLabel = new Label("Info Partita");
        titleLabel.setTextFill(TEXT_GOLD);
        titleLabel.setFont(Font.font("Serif", FontWeight.BOLD, 18));

        statusLabel = new Label("In attesa...");
        statusLabel.setTextFill(TEXT_WHITE);
        statusLabel.setFont(Font.font("Arial", 13));
        statusLabel.setWrapText(true);

        scoreLabel = new Label("Punteggio: 0");
        scoreLabel.setTextFill(TEXT_WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label logTitle = new Label("Log:");
        logTitle.setTextFill(TEXT_GOLD);
        logTitle.setFont(Font.font("Serif", FontWeight.BOLD, 14));

        logArea = new VBox(3);
        logArea.setMaxHeight(300);

        Button backButton = new Button("Esci dalla Partita");
        backButton.setStyle("-fx-background-color: #8b0000; -fx-text-fill: white;");
        backButton.setOnAction(e -> controller.onExitGame());

        panel.getChildren().addAll(titleLabel, statusLabel, scoreLabel, logTitle, logArea, backButton);
        return panel;
    }

    private StackPane createCardBack() {
        StackPane card = new StackPane();

        Rectangle cardRect = new Rectangle(50, 75);
        cardRect.setArcWidth(8);
        cardRect.setArcHeight(8);
        cardRect.setFill(CARD_BACK);
        cardRect.setStroke(Color.WHITE);
        cardRect.setStrokeWidth(1);

        // Pattern on card back
        Rectangle innerRect = new Rectangle(40, 65);
        innerRect.setArcWidth(4);
        innerRect.setArcHeight(4);
        innerRect.setFill(Color.TRANSPARENT);
        innerRect.setStroke(Color.rgb(200, 180, 100));
        innerRect.setStrokeWidth(1);

        card.getChildren().addAll(cardRect, innerRect);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(3);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        card.setEffect(shadow);

        return card;
    }

    private StackPane createCardFace(Cards card, int index, boolean isPlayable) {
        StackPane cardPane = new StackPane();
        cardPane.setUserData(index);

        Rectangle cardRect = new Rectangle(70, 100);
        cardRect.setArcWidth(10);
        cardRect.setArcHeight(10);
        cardRect.setFill(Color.WHITE);
        cardRect.setStroke(Color.GRAY);
        cardRect.setStrokeWidth(1);

        VBox content = new VBox(2);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(5));

        // Card rank
        String rankStr = getRankSymbol(card.getRank());
        Text rankText = new Text(rankStr);
        rankText.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        rankText.setFill(getSuitColor(card.getSegno()));

        // Card suit
        String suitStr = getSuitSymbol(card.getSegno());
        Text suitText = new Text(suitStr);
        suitText.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        suitText.setFill(getSuitColor(card.getSegno()));

        content.getChildren().addAll(rankText, suitText);

        cardPane.getChildren().addAll(cardRect, content);

        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        cardPane.setEffect(shadow);

        if (isPlayable) {
            cardPane.setOnMouseEntered(e -> {
                cardPane.setTranslateY(-10);
                cardRect.setStroke(TEXT_GOLD);
                cardRect.setStrokeWidth(2);
            });
            cardPane.setOnMouseExited(e -> {
                if (selectedCardIndex != index) {
                    cardPane.setTranslateY(0);
                    cardRect.setStroke(Color.GRAY);
                    cardRect.setStrokeWidth(1);
                }
            });
            cardPane.setOnMouseClicked(e -> selectCard(index));
        }

        return cardPane;
    }

    private String getRankSymbol(Cards.Rank rank) {
        return switch (rank) {
            case ASSO -> "A";
            case DUE -> "2";
            case TRE -> "3";
            case QUATTRO -> "4";
            case CINQUE -> "5";
            case SEI -> "6";
            case SETTE -> "7";
            case ALFIERE -> "J";
            case CAVALLO -> "Q";
            case RE -> "K";
        };
    }

    private String getSuitSymbol(Cards.Segno segno) {
        return switch (segno) {
            case DENARA -> "♦";
            case SPADE -> "♠";
            case BASTONI -> "♣";
            case COPPE -> "♥";
        };
    }

    private Color getSuitColor(Cards.Segno segno) {
        return switch (segno) {
            case DENARA, COPPE -> Color.RED;
            case SPADE, BASTONI -> Color.BLACK;
        };
    }

    private void selectCard(int index) {
        // Deselect previous
        if (selectedCardIndex >= 0 && selectedCardIndex < cardViews.size()) {
            StackPane prev = cardViews.get(selectedCardIndex);
            prev.setTranslateY(0);
            Rectangle rect = (Rectangle) prev.getChildren().get(0);
            rect.setStroke(Color.GRAY);
            rect.setStrokeWidth(1);
        }

        // Select new
        selectedCardIndex = index;
        if (index >= 0 && index < cardViews.size()) {
            StackPane current = cardViews.get(index);
            current.setTranslateY(-15);
            Rectangle rect = (Rectangle) current.getChildren().get(0);
            rect.setStroke(TEXT_GOLD);
            rect.setStrokeWidth(3);
        }

        playButton.setDisable(false);
    }

    private void onPlayCard() {
        if (selectedCardIndex >= 0) {
            int[] legalMoves = gameState.getLegalMoves(humanPlayer);
            boolean isLegal = false;
            for (int legal : legalMoves) {
                if (legal == selectedCardIndex) {
                    isLegal = true;
                    break;
                }
            }

            if (isLegal) {
                controller.onCardPlayed(selectedCardIndex);
                selectedCardIndex = -1;
                playButton.setDisable(true);
            } else {
                log("Mossa non valida! Devi seguire il seme se possibile.");
            }
        }
    }

    /**
     * Refresh the view to reflect current game state.
     */
    public void refresh() {
        Platform.runLater(() -> {
            updatePlayerHand();
            updateTableCards();
            updateOpponentArea();
            updateScores();
            updateStatus();
        });
    }

    private void updatePlayerHand() {
        playerHandBox.getChildren().clear();
        cardViews.clear();

        List<Cards> hand = gameState.getHand(humanPlayer);
        int[] legalMoves = gameState.getLegalMoves(humanPlayer);
        Giocatore current = gameState.getCurrentPlayer();
        boolean isMyTurn = current == humanPlayer;

        for (int i = 0; i < hand.size(); i++) {
            Cards card = hand.get(i);
            boolean isLegal = false;
            for (int legal : legalMoves) {
                if (legal == i) {
                    isLegal = true;
                    break;
                }
            }
            StackPane cardView = createCardFace(card, i, isMyTurn && isLegal);
            cardViews.add(cardView);
            playerHandBox.getChildren().add(cardView);
        }

        selectedCardIndex = -1;
        playButton.setDisable(!isMyTurn);
    }

    private void updateTableCards() {
        tableCardsBox.getChildren().clear();

        // Get trick cards from game state using the new methods
        List<Cards> trickCards = gameState.getTrickCards();
        List<Giocatore> trickPlayers = gameState.getTrickPlayers();

        for (int i = 0; i < trickCards.size(); i++) {
            Cards card = trickCards.get(i);
            Giocatore player = trickPlayers.get(i);

            VBox playedCard = new VBox(3);
            playedCard.setAlignment(Pos.CENTER);

            Label nameLabel = new Label(player.getName());
            nameLabel.setTextFill(TEXT_WHITE);
            nameLabel.setFont(Font.font("Arial", 10));

            StackPane cardFace = createCardFace(card, -1, false);
            playedCard.getChildren().addAll(nameLabel, cardFace);

            tableCardsBox.getChildren().add(playedCard);
        }

        // Display message if no cards on table
        if (tableCardsBox.getChildren().isEmpty()) {
            Label noCards = new Label("Nessuna carta giocata");
            noCards.setTextFill(Color.rgb(200, 200, 200, 0.7));
            noCards.setFont(Font.font("Arial", 14));
            tableCardsBox.getChildren().add(noCards);
        }
    }

    private void updateOpponentArea() {
        opponentArea.getChildren().clear();

        HBox opponentsRow = new HBox(40);
        opponentsRow.setAlignment(Pos.CENTER);

        for (Giocatore player : gameState.getPlayers()) {
            if (player != humanPlayer) {
                VBox opponentBox = createOpponentBox(player);
                opponentsRow.getChildren().add(opponentBox);
            }
        }

        opponentArea.getChildren().add(opponentsRow);
    }

    private void updateScores() {
        int score = gameState.getScore(humanPlayer);
        scoreLabel.setText("Punteggio: " + score);
    }

    private void updateStatus() {
        Giocatore current = gameState.getCurrentPlayer();
        if (current == humanPlayer) {
            statusLabel.setText("È il tuo turno!\nScegli una carta da giocare.");
            statusLabel.setTextFill(TEXT_GOLD);
        } else {
            statusLabel.setText("Turno di: " + current.getName());
            statusLabel.setTextFill(TEXT_WHITE);
        }
    }

    /**
     * Add a log message to the info panel.
     */
    public void log(String message) {
        Platform.runLater(() -> {
            Label logEntry = new Label("• " + message);
            logEntry.setTextFill(TEXT_WHITE);
            logEntry.setFont(Font.font("Arial", 11));
            logEntry.setWrapText(true);
            logEntry.setMaxWidth(190);

            logArea.getChildren().add(0, logEntry);

            // Keep only last 15 messages
            if (logArea.getChildren().size() > 15) {
                logArea.getChildren().remove(15, logArea.getChildren().size());
            }
        });
    }

    /**
     * Show a card being played on the table.
     */
    public void showCardPlayed(Giocatore player, Cards card) {
        Platform.runLater(() -> {
            // Remove "no cards" message if present
            tableCardsBox.getChildren().removeIf(node -> node instanceof Label);

            VBox playedCard = new VBox(3);
            playedCard.setAlignment(Pos.CENTER);

            Label nameLabel = new Label(player.getName());
            nameLabel.setTextFill(TEXT_WHITE);
            nameLabel.setFont(Font.font("Arial", 10));

            StackPane cardFace = createCardFace(card, -1, false);
            playedCard.getChildren().addAll(nameLabel, cardFace);

            tableCardsBox.getChildren().add(playedCard);
        });
    }

    /**
     * Clear the table when a trick is completed.
     */
    public void clearTable() {
        Platform.runLater(() -> {
            tableCardsBox.getChildren().clear();
        });
    }

    /**
     * Show game over dialog.
     */
    public void showGameOver(String result) {
        Platform.runLater(() -> {
            statusLabel.setText("PARTITA TERMINATA\n" + result);
            statusLabel.setTextFill(TEXT_GOLD);
            playButton.setDisable(true);
            log("Partita terminata: " + result);
        });
    }

    public Stage getStage() {
        return stage;
    }
}
