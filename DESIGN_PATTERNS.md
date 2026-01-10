# Design Patterns e Tecnologie nel Progetto JTressette

## Indice
1. [Design Pattern Model-View-Controller (MVC)](#1-design-pattern-model-view-controller-mvc)
2. [Design Pattern Observer/Observable](#2-design-pattern-observerobservable)
3. [Java Swing](#3-java-swing)
4. [Stream API per le Funzioni](#4-stream-api-per-le-funzioni)
5. [Gestione Audio](#5-gestione-audio)
6. [Effetti Grafici](#6-effetti-grafici)
7. [Design Pattern Singleton](#7-design-pattern-singleton)
8. [Utility Class Pattern (Static Helper)](#8-utility-class-pattern-static-helper)
9. [Strategy Pattern](#9-strategy-pattern)
10. [Polymorphism e Interface-based Design](#10-polymorphism-e-interface-based-design)
11. [Repository/DAO Pattern](#11-repositorydao-pattern)
12. [Thread Safety e Concorrenza](#12-thread-safety-e-concorrenza)
13. [Immutability e Defensive Copying](#13-immutability-e-defensive-copying)

---

## 1. Design Pattern Model-View-Controller (MVC)

### Descrizione
Il pattern MVC è stato applicato in modo rigoroso per separare la logica di business (Model), la presentazione (View) e il controllo del flusso dell'applicazione (Controller).

### Implementazione nel Progetto

#### **Model**
Il Model gestisce i dati e la logica di business dell'applicazione. Le classi principali si trovano in `src/Model/`:

- **`GameState.java`** (`Model/Game/GameState.java`): 
  - Classe centrale che mantiene lo stato completo della partita
  - Gestisce: giocatori, mani di carte, punteggi, mazzo, prese correnti
  - Contiene la logica di gioco: distribuzione carte, determinazione vincitore, calcolo punteggi
  - **Esempio chiave**: Il metodo `playCard()` (linee 194-242) gestisce tutta la logica quando viene giocata una carta

- **`Giocatore.java` e sottoclassi** (`Model/Game/`):
  - `GiocatoreUmano`: rappresenta il giocatore umano
  - `Bot`: rappresenta i giocatori controllati dall'IA con difficoltà variabile

- **`Cards.java`** e **`Mazzo.java`**: gestiscono le carte e il mazzo

- **`UserProfile.java`** (`Model/Profile/`): gestisce i dati del profilo utente (nome, avatar, statistiche)

- **`MenuImpostazioni.java`** (`Model/Impostazioni/`): gestisce le impostazioni dell'applicazione

- **`AudioManager.java`** (`Model/Audio/`): gestisce la riproduzione dell'audio

#### **View**
Le View si occupano esclusivamente della presentazione grafica in `src/View/`:

- **`GameView.java`** (`View/Game/GameView.java`): Vista principale del gioco che estende `JFrame`
- **`HomeMenu.java`** (`View/Menu/HomeMenu.java`): menu principale del gioco
- **`ProfileMenu.java`** (`View/Profile/ProfileMenu.java`): visualizzazione del profilo utente
- **`RulesPage.java`** (`View/Rules/RulesPage.java`): pagina delle regole del gioco

#### **Controller**
I Controller fanno da intermediari tra Model e View:

- **`GameController.java`** (`Controller/Game/GameController.java`): Controller principale che coordina il flusso di gioco
- **`ProfileController.java`** (`Controller/Profile/`): Gestisce il profilo utente

### Vantaggi dell'MVC nel Progetto
- **Separazione delle responsabilità**
- **Manutenibilità**: modifiche alla UI non impattano la logica
- **Testabilità**: il Model può essere testato indipendentemente
- **Riusabilità**: stesso Model con View diverse

---

## 2. Design Pattern Observer/Observable

### Descrizione
Il pattern Observer permette a un oggetto di notificare automaticamente i suoi osservatori quando il suo stato cambia.

### Implementazione nel Progetto

#### **L'Interfaccia Observer**
**`GameStateObserver.java`** (`Model/Game/GameStateObserver.java`):
```java
public interface GameStateObserver {
    void onCardPlayed(Giocatore player, Cards card);
    void onTrickCompleted(Giocatore winner, int cardsWon);
    void onCardsDealt();
    void onTurnChanged(Giocatore currentPlayer);
    void onGameFinished();
}
```

#### **Il Subject (Observable)**
**`GameState.java`** mantiene una lista di observers:
```java
private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();

public void addObserver(GameStateObserver observer) {
    if (observer != null) {
        ((CopyOnWriteArrayList<GameStateObserver>) observers).addIfAbsent(observer);
    }
}

private void notifyCardPlayed(Giocatore player, Cards card) {
    observers.forEach(observer -> observer.onCardPlayed(player, card));
}
```

**Esempio pratico** nel metodo `playCard()`:
```java
public Cards playCard(Giocatore p, int handIndex) {
    // Rimuove la carta dalla mano
    Cards c = hand.remove(handIndex);
    trickCards.add(c);
    
    // Notifica gli observers
    notifyCardPlayed(p, c);
    
    // Se la presa è completa
    if (trickCards.size() == players.size()) {
        int winnerPos = determineTrickWinner();
        Giocatore winner = trickPlayers.get(winnerPos);
        int cardsWon = trickCards.size();
        
        notifyTrickCompleted(winner, cardsWon);
        notifyTurnChanged(getCurrentPlayer());
    }
}
```

### Vantaggi del Pattern Observer
- **Disaccoppiamento**: GameState non conosce i dettagli delle View
- **Estensibilità**: facile aggiungere nuovi observers
- **Thread-safety**: uso di `CopyOnWriteArrayList`
- **Reattività**: la UI si aggiorna automaticamente

---

## 3. Java Swing

### Descrizione
Java Swing è il framework utilizzato per creare l'interfaccia grafica dell'applicazione.

### Componenti Swing Utilizzati

#### **Finestre Principali (JFrame)**
- **`GameView.java`** estende `JFrame` per la finestra di gioco
- **`MenuFrame.java`** per il menu principale

#### **Layout Managers**
1. **BorderLayout** - struttura principale
2. **GridBagLayout** - per centrare componenti
3. **FlowLayout** - per disporre carte orizzontalmente
4. **BoxLayout** - per layout verticali
5. **CardLayout** - per navigare tra schermate

#### **Pannelli Personalizzati (JPanel)**

**GradientPanel** - pannello con sfondo sfumato:
```java
private static class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(
            0, 0, FELT_DARK,
            0, getHeight(), FELT_GREEN
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
```

**TablePanel** - tavolo da gioco ovale con ombreggiatura:
```java
@Override
protected void paintComponent(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
    // Ombra
    g2d.fill(new RoundRectangle2D.Double(6, 6, width-12, height-12, 90, 90));
    // Tavolo
    g2d.fill(new RoundRectangle2D.Double(4, 4, width-8, height-8, 90, 90));
}
```

#### **Componenti Interattivi**
- **JButton** con styling custom
- **JLabel** per testi e immagini
- **JScrollPane** per scorrimento
- **JLayeredPane** per sovrapporre componenti

#### **Event Handling**
**MouseListener** per interazioni:
```java
addMouseListener(new MouseAdapter() {
    @Override
    public void mousePressed(MouseEvent e) {
        initialClick[0] = e.getPoint();
    }
});
```

---

## 4. Stream API per le Funzioni

### Descrizione
Le Stream API di Java 8+ sono utilizzate estensivamente per operazioni su collezioni.

### Esempi di Utilizzo nel Progetto

#### **1. Creazione del Mazzo di Carte**
**`Mazzo.java`**:
```java
cards = Arrays.stream(Cards.Segno.values())
        .flatMap(segno -> Arrays.stream(Cards.Rank.values())
                .map(rank -> new Cards(segno, rank)))
        .collect(Collectors.toCollection(() -> new ArrayList<>(52)));
```

#### **2. Calcolo Punteggio di una Presa**
**`GameState.java`**:
```java
int trickPoints = trickCards.stream()
        .mapToInt(card -> CARD_POINTS.getOrDefault(card.getRank(), 0))
        .sum();
```

#### **3. Determinazione del Vincitore**
**`GameController.java`**:
```java
var winner = scores.entrySet().stream()
        .max(Comparator.comparingInt(Map.Entry::getValue))
        .map(Map.Entry::getKey)
        .orElse(null);
```

#### **4. Filtraggio Giocatori**
```java
String opponentNames = gameState.getPlayers().stream()
        .filter(p -> p != humanPlayer)
        .map(Giocatore::getName)
        .collect(Collectors.joining(","));
```

#### **5. Verifica Fine Partita**
```java
boolean finished = hands.values().stream().allMatch(List::isEmpty);
```

#### **6. Determinazione Mosse Legali**
```java
int[] sameSuit = IntStream.range(0, hand.size())
        .filter(i -> hand.get(i).getSegno() == leadSuit)
        .toArray();
```

#### **7. Strategia Bot**
```java
int bestWinIdx = Arrays.stream(legal)
        .boxed()
        .filter(idx -> canWin(hand.get(idx), lead))
        .min(Comparator.comparingInt(idx -> hand.get(idx).getPriority()))
        .orElse(-1);
```

### Vantaggi delle Stream API
- **Codice più conciso**
- **Leggibilità migliorata**
- **Operazioni dichiarative**
- **Immutabilità**: le stream non modificano le collezioni originali

---

## 5. Gestione Audio

### Descrizione
Il sistema audio è gestito dalla classe `AudioManager` che utilizza le API Java Sound (`javax.sound.sampled`).

### Architettura Audio

#### **Classe AudioManager**
**`AudioManager.java`** (`Model/Audio/AudioManager.java`):

**File audio disponibili**:
```java
public static final int BACKGROUND_MENU = 0;      // Musica menu
public static final int MENU_SELECTION_CLICK = 1; // Click
public static final int CARD_PLAYED = 2;          // Carta giocata
public static final int BACKGROUND_GAME = 3;      // Musica partita
public static final int CARD_DRAW = 4;            // Pesca carta
public static final int CARD_DEALING = 5;         // Distribuzione
public static final int VICTORY = 6;              // Vittoria
public static final int DEFEAT = 7;               // Sconfitta
```

**Caricamento risorse**:
```java
public AudioManager() {
    soundURL[BACKGROUND_MENU] = getClass().getResource("/res/audio/backgroundMenu.wav");
    soundURL[CARD_PLAYED] = getClass().getResource("/res/audio/cardPlayed.wav");
    // ... altri file ...
}
```

### Funzionalità Audio Implementate

#### **1. Riproduzione Base**
```java
public void setFile(int i) {
    AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
    clip = AudioSystem.getClip();
    clip.open(ais);
    setVolume(currentVolume);
}

public void start() {
    clip.setFramePosition(0);
    clip.start();
}

public void loop() {
    clip.loop(Clip.LOOP_CONTINUOUSLY);
}
```

#### **2. Controllo Volume Dinamico**
```java
public void setVolume(float volume) {
    currentVolume = Math.max(0.0f, Math.min(0.4f, volume));
    FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
    // Converti volume lineare in decibel
    float dB = (float) (Math.log(Math.max(currentVolume, 0.0001)) / Math.log(10.0) * 20.0);
    gainControl.setValue(dB);
}
```

#### **3. Effetti Fade (Dissolvenza)**

**Fade-Out**:
```java
public void fadeOut(int durationMs, Runnable onComplete) {
    final int steps = 20;
    final float volumeStep = startVolume / steps;
    
    fadeTimer = new Timer(durationMs / steps, null);
    fadeTimer.addActionListener(e -> {
        currentStep[0]++;
        float newVolume = startVolume - (volumeStep * currentStep[0]);
        setVolume(Math.max(0, newVolume));
        
        if (currentStep[0] >= steps) {
            fadeTimer.stop();
            if (onComplete != null) onComplete.run();
        }
    });
    fadeTimer.start();
}
```

**Fade-In**:
```java
public void fadeIn(int durationMs, float targetVolume) {
    setVolume(0);
    start();
    
    fadeTimer = new Timer(durationMs / steps, null);
    fadeTimer.addActionListener(e -> {
        currentStep[0]++;
        setVolume(Math.min(targetVolume, volumeStep * currentStep[0]));
    });
    fadeTimer.start();
}
```

#### **4. Effetti Sonori One-Shot**
```java
public void playSoundEffect(int soundIndex) {
    AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[soundIndex]);
    Clip effectClip = AudioSystem.getClip();
    effectClip.open(ais);
    
    // Auto-chiude il clip quando finisce
    effectClip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            effectClip.close();
        }
    });
    
    effectClip.start();
}
```

### Utilizzo dell'Audio nel Gioco

#### **Musica di Background**:
```java
audioManager.setFile(AudioManager.BACKGROUND_MENU);
audioManager.loop();
```

#### **Durante la Partita con Fade-In**:
```java
float volume = settings.getVolume() / 100.0f * AudioManager.MAX_VOLUME_SCALE;
audioManager.setFile(AudioManager.BACKGROUND_GAME);
audioManager.fadeIn(800, volume);
audioManager.loop();
```

### Architettura delle Risorse Audio
File in `/JTressette/src/res/audio/`:
- `backgroundMenu.wav` - Musica menu
- `backGame.wav` - Musica partita
- `cardPlayed.wav` - Suono carta giocata
- `cardDraw.wav` - Suono pesca carta
- `dealing.wav` - Suono distribuzione
- `SelectionClick.wav` - Click menu
- `victory.wav` - Vittoria
- `defeat.wav` - Sconfitta

---

## 6. Effetti Grafici

### Descrizione
Il progetto implementa numerosi effetti grafici avanzati per migliorare l'esperienza utente.

### Categorie di Effetti Grafici

#### **1. Effetti di Trasparenza e Fade**

**Fade-In Finestra**:
```java
public void fadeIn() {
    Timer fadeTimer = new Timer(16, null);  // ~60 FPS
    final float[] alpha = {0.0f};
    
    fadeTimer.addActionListener(e -> {
        alpha[0] += 0.05f;
        if (alpha[0] >= 1.0f) {
            alpha[0] = 1.0f;
            fadeTimer.stop();
        }
        setOpacity(alpha[0]);
    });
    fadeTimer.start();
}
```

#### **2. Animazioni delle Carte**

**Animazione Giocata Carta**:
```java
public void showCardPlayed(Giocatore player, Cards card, Runnable onComplete) {
    // Posizione iniziale e finale
    Point startPos = getPlayerCardStartPosition(player, playerIdx);
    int[] targetPos = positions[playerIdx];
    
    // Timer per animazione fluida
    Timer animationTimer = new Timer(16, null);
    animationTimer.addActionListener(e -> {
        frame[0]++;
        float progress = (float) frame[0] / totalFrames;
        
        // Interpolazione lineare
        int currentX = (int) (startPos.x + (targetPos[0] - startPos.x) * progress);
        int currentY = (int) (startPos.y + (targetPos[1] - startPos.y) * progress);
        
        animatedCard.setLocation(currentX, currentY);
    });
    animationTimer.start();
}
```

**Effetto Glow sul Testo**:
```java
JLabel label = new JLabel("Distribuzione carte...") {
    private float glowAlpha = 0f;
    
    @Override
    protected void paintComponent(Graphics g) {
        // Effetto alone luminoso pulsante
        g2.setColor(new Color(255, 215, 0, (int)(100 * glowAlpha)));
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                g2.drawString(getText(), x + i, y + j);
            }
        }
        // Testo principale
        g2.setColor(TEXT_GOLD);
        g2.drawString(getText(), x, y);
    }
};
```

#### **3. Effetti di Hover e Interazione**

**Hover sulle Carte**:
```java
@Override
protected void paintComponent(Graphics g) {
    if (isHovered && isPlayable) {
        // Calcola offset verso l'alto
        int offsetY = -(int) (20 * animationProgress);
        
        // Scala leggermente la carta
        int scaledWidth = (int) (drawWidth * (1 + 0.05 * animationProgress));
        
        // Disegna ombra
        g2d.fill(new RoundRectangle2D.Double(3, 3, scaledWidth, scaledHeight, 10, 10));
        
        // Bordo dorato pulsante
        g2d.setColor(new Color(255, 215, 0, (int)(255 * animationProgress)));
        g2d.drawRoundRect(offsetX, offsetY, scaledWidth-1, scaledHeight-1, 10, 10);
    }
}
```

#### **4. Gradienti e Sfondi**

**Sfondo con Gradiente Verticale**:
```java
GradientPaint gp1 = new GradientPaint(
    0, 0, FELT_DARK,
    0, getHeight() / 2, FELT_GREEN
);
g2d.setPaint(gp1);
g2d.fillRect(0, 0, getWidth(), getHeight() / 2);
```

#### **5. Ombre e Profondità**

**Tavolo con Ombra**:
```java
// Ombra
g2d.setColor(new Color(0, 0, 0, 80));
g2d.fill(new RoundRectangle2D.Double(6, 6, width-12, height-12, 90, 90));

// Tavolo sopra l'ombra
g2d.setColor(new Color(35, 130, 75));
g2d.fill(new RoundRectangle2D.Double(4, 4, width-8, height-8, 90, 90));
```

#### **6. Effetti Overlay**

**Overlay con Fade-In**:
```java
JPanel overlay = new JPanel() {
    private float alpha = 0f;
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, Math.min(1f, alpha) * 0.92f));
        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRect(0, 0, getWidth(), getHeight());
    }
};
```

#### **7. Rendering Avanzato**

**Antialiasing e Qualità**:
```java
g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
g2d.setRenderingHint(RenderingHints.KEY_RENDERING, VALUE_RENDER_QUALITY);
```

### Tecniche Utilizzate
- **Timer-based Animation**: Frame smooth con `javax.swing.Timer`
- **Layered Rendering**: `JLayeredPane` per profondità visiva
- **Custom Painting**: Override di `paintComponent()`
- **Interpolazione**: Animazioni fluide
- **Alpha Compositing**: Trasparenza e blending
- **Shape API**: `RoundRectangle2D` per forme arrotondate

---


## 7. Design Pattern Singleton

### Descrizione
Il pattern Singleton garantisce che una classe abbia una sola istanza nell'intera applicazione e fornisce un punto di accesso globale a questa istanza.

### Implementazione nel Progetto

#### **MenuImpostazioni (Singleton Thread-Safe)**
**`MenuImpostazioni.java`** (`Model/Impostazioni/MenuImpostazioni.java`, linee 29-86):

```java
public class MenuImpostazioni {
    /** Istanza singleton */
    private static MenuImpostazioni instance;
    
    /** Costruttore privato per pattern Singleton */
    private MenuImpostazioni() {
        String home = System.getProperty("user.home");
        settingsDir = Paths.get(home, DIR_NAME);
        settingsFile = settingsDir.resolve(FILE_NAME);
        load();
    }
    
    /**
     * Restituisce l'istanza singleton delle impostazioni.
     * Metodo thread-safe con lazy initialization.
     */
    public static synchronized MenuImpostazioni getInstance() {
        if (instance == null) {
            instance = new MenuImpostazioni();
        }
        return instance;
    }
}
```

**Caratteristiche**:
- **Costruttore privato**: impedisce la creazione di istanze dall'esterno
- **Metodo getInstance() sincronizzato**: garantisce thread-safety
- **Lazy initialization**: l'istanza viene creata solo quando necessaria
- **Persistenza automatica**: carica le impostazioni da file al primo accesso

**Gestisce**:
- Volume audio (0-100)
- Abilitazione effetti sonori
- Visualizzazione punteggi e messaggi
- Modalità fullscreen

**Integrazione con Observer Pattern**:
```java
public interface SettingsListener {
    void onSettingsChanged(MenuImpostazioni settings);
}

private void notifyListeners() {
    for (SettingsListener listener : listeners) {
        listener.onSettingsChanged(this);
    }
}
```
- Combina Singleton con Observer per notificare cambiamenti globali

### Vantaggi del Singleton
- **Unica fonte di verità**: tutte le parti dell'applicazione accedono alle stesse impostazioni
- **Gestione centralizzata**: modifiche alle impostazioni si propagano automaticamente
- **Risparmio risorse**: una sola istanza invece di multiple copie
- **Thread-safe**: sincronizzazione garantisce correttezza in ambiente multi-thread

---

## 8. Utility Class Pattern (Static Helper)

### Descrizione
Classi che forniscono metodi statici di utilità senza mantenere stato. Il costruttore è privato per prevenire istanziazione.

### Implementazione nel Progetto

#### **CardImageLoader (Utility Class con Cache)**
**`CardImageLoader.java`** (`Model/Util/CardImageLoader.java`, linee 31-42):

```java
public class CardImageLoader {
    /** Costruttore privato per impedire l'instanziazione */
    private CardImageLoader() {
        throw new UnsupportedOperationException(
            "CardImageLoader è una classe utility e non può essere istanziata.");
    }
    
    /** Cache statica delle immagini caricate */
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    
    /** Immagine del retro carta (singleton interno) */
    private static BufferedImage cardBackImage;
}
```

**Funzionalità principali**:

1. **Caricamento con cache** (linee 152-162):
```java
private static BufferedImage getCardImage(String imageName) {
    if (imageCache.containsKey(imageName)) {
        return imageCache.get(imageName);
    }
    
    BufferedImage image = loadImage(imageName);
    if (image != null) {
        imageCache.put(imageName, image);
    }
    return image;
}
```

2. **Scaling ottimizzato**:
```java
public static Image getScaledCardImage(Cards card, int width, int height) {
    BufferedImage original = getCardImage(card);
    if (original == null) {
        return createPlaceholderImage(card, width, height);
    }
    return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
}
```

3. **Preload per performance** (linee 324-332):
```java
public static void preloadImages() {
    for (Cards.Segno segno : Cards.Segno.values()) {
        for (Cards.Rank rank : Cards.Rank.values()) {
            Cards card = new Cards(segno, rank);
            getCardImage(card);
        }
    }
    getCardBackImage();
}
```

4. **Fallback con placeholder**:
```java
private static Image createPlaceholderImage(Cards card, int width, int height) {
    // Genera immagine con Graphics2D quando il file non è disponibile
    BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    // ... rendering del placeholder ...
    return img;
}
```

### Vantaggi Utility Class
- **Nessuno stato condiviso**: solo metodi statici, nessuna istanza
- **Cache trasparente**: le immagini vengono caricate una sola volta
- **Resilienza**: genera placeholder se le risorse non sono disponibili
- **Performance**: preload opzionale per gioco fluido

---

## 9. Strategy Pattern

### Descrizione
Il pattern Strategy definisce una famiglia di algoritmi, li incapsula e li rende intercambiabili. L'algoritmo può variare indipendentemente dai client che lo usano.

### Implementazione nel Progetto

#### **Difficoltà Bot (Strategy con Enum)**
**`Bot.java`** (`Model/Game/Bot.java`, linee 67-105):

```java
@Override
public int chooseCard(GameState state) {
    int[] legal = state.getLegalMoves(this);
    if (legal == null || legal.length == 0) return -1;
    
    switch (difficulty) {
        case EASY:
            return legal[rnd.nextInt(legal.length)];
            
        case MEDIUM:
            // Strategia: vinci con carta minima o scarta la più debole
            Cards lead = state.getLeadCard();
            if (lead != null) {
                List<Cards> hand = state.getHand(this);
                int bestWinIdx = Arrays.stream(legal)
                        .boxed()
                        .filter(idx -> canWin(hand.get(idx), lead))
                        .min(Comparator.comparingInt(idx -> hand.get(idx).getPriority()))
                        .orElse(-1);
                if (bestWinIdx >= 0) return bestWinIdx;
            }
            return findWeakestLegal(state, legal);
            
        case HARD:
            return chooseCardHard(state, legal);
    }
    return 0;
}
```

**Strategie implementate**:

1. **EASY - Strategia Casuale**:
   - Scelta completamente random tra le mosse legali
   - Nessuna valutazione tattica

2. **MEDIUM - Strategia Tattica Base**:
   - Cerca di vincere la presa con la carta minima necessaria
   - Se non può vincere, scarta la carta più debole
   - Usa Stream API per trovare la mossa ottimale

3. **HARD - Strategia Avanzata con Euristica** (linee 122-213):
   - Analizza le carte già giocate
   - Calcola i punti nella presa corrente
   - Stima le carte punto rimanenti nel seme
   - Usa scoring euristico complesso:
```java
if (wouldWin) {
    score = 2000 - candidate.getPriority();
    score += trickPoints * 100;
    score += remainingPointCardsInLead * 50;
} else {
    score = -candidate.getPriority();
    if (trickPoints == 0 && remainingPointCardsInLead == 0) {
        score += 20;  // bonus per scartare carte inutili
    }
}
```

### Vantaggi Strategy Pattern
- **Algoritmi intercambiabili**: facile cambiare difficoltà senza modificare Bot
- **Estensibilità**: semplice aggiungere nuove difficoltà
- **Testabilità**: ogni strategia può essere testata indipendentemente
- **Leggibilità**: logiche separate e ben identificabili

---

## 10. Polymorphism e Interface-based Design

### Descrizione
Utilizzo di interfacce per definire contratti comuni e implementazioni polimorfiche per comportamenti diversi.

### Implementazione nel Progetto

#### **Giocatore Interface**
**`Giocatore.java`** (`Model/Game/Giocatore.java`, linee 8-33):

```java
public interface Giocatore {
    String getName();
    boolean isBot();
    int chooseCard(GameState state) throws InterruptedException;
}
```

**Implementazioni**:

1. **GiocatoreUmano** - Interazione con UI:
```java
public class GiocatoreUmano implements Giocatore {
    private final String name;
    private final BlockingQueue<Integer> cardChoiceQueue = new LinkedBlockingQueue<>();
    
    @Override
    public int chooseCard(GameState state) throws InterruptedException {
        // Aspetta input dall'utente tramite queue thread-safe
        Integer choice = cardChoiceQueue.take();
        return choice;
    }
    
    public void submitCardChoice(int cardIndex) {
        cardChoiceQueue.offer(cardIndex);
    }
}
```
- Utilizza `BlockingQueue` per comunicazione thread-safe tra UI e game loop
- `take()` blocca fino a quando l'utente fa una scelta

2. **Bot** - IA con strategia:
```java
public class Bot implements Giocatore {
    private final String name;
    private final Difficoltà difficulty;
    
    @Override
    public int chooseCard(GameState state) {
        // Scelta immediata basata su algoritmo (vedi Strategy Pattern)
        int[] legal = state.getLegalMoves(this);
        return applyStrategy(state, legal);
    }
}
```

**Utilizzo polimorfico** nel GameController:
```java
while (!gameState.isFinished()) {
    Giocatore current = gameState.getCurrentPlayer();
    
    // Chiamata polimorfica - funziona sia per umano che bot
    int idx = current.chooseCard(gameState);
    
    Cards played = gameState.playCard(current, idx);
    // ...
}
```

### Vantaggi Polymorphism
- **Codice generico**: GameController non distingue tra umano e bot
- **Estensibilità**: facile aggiungere nuovi tipi di giocatori (es. rete)
- **Manutenibilità**: modifiche a un tipo non impattano gli altri
- **Testabilità**: possibile creare mock player per test

---

## 11. Repository/DAO Pattern

### Descrizione
Pattern che astrae l'accesso ai dati, separando la logica di persistenza dalla logica di business.

### Implementazione nel Progetto

#### **StorageProfile (Repository per UserProfile)**
**`StorageProfile.java`** (`Model/Profile/StorageProfile.java`):

**Caratteristiche principali**:

1. **Separazione delle responsabilità**:
   - `UserProfile`: entità di dominio (POJO)
   - `StorageProfile`: logica di persistenza

2. **Salvataggio atomico** (linee 257-301):
```java
public synchronized void save(UserProfile profile) throws IOException {
    // Crea Properties con tutti i dati
    Properties props = new Properties();
    props.setProperty(KEY_USERNAME, profile.getUsername());
    props.setProperty(KEY_EXPERIENCE, String.valueOf(profile.getExperience()));
    // ... altre proprietà ...
    
    // Salva in file temporaneo
    Path tmp = profileDir.resolve(FILE_NAME + ".tmp");
    try (OutputStream os = Files.newOutputStream(tmp, ...)) {
        props.store(os, "JTressette User Profile");
    }
    
    // Move atomico per prevenire corruzione
    Files.move(tmp, profileFile, 
               StandardCopyOption.REPLACE_EXISTING, 
               StandardCopyOption.ATOMIC_MOVE);
}
```
- **Atomicità**: salva prima in `.tmp`, poi sposta atomicamente
- **Thread-safe**: metodo sincronizzato
- **Resilienza**: previene corruzione dati in caso di crash

3. **Caricamento con fallback** (linee 88-104):
```java
public UserProfile loadOrCreateDefault() {
    try {
        if (Files.notExists(profileFile)) {
            // Crea profilo default e lo salva
            UserProfile defaultProfile = new UserProfile("Giocatore");
            save(defaultProfile);
            return defaultProfile;
        } else {
            return loadFromFile();
        }
    } catch (IOException e) {
        // Fallback su profilo default senza salvare
        return new UserProfile("Giocatore");
    }
}
```

4. **Serializzazione complessa con Stream API** (linee 192-221):
```java
private List<GamesRecord> loadGamesFromProperties(Properties props) {
    int count = Integer.parseInt(props.getProperty(KEY_GAMES_COUNT, "0"));
    
    return IntStream.range(0, count)
            .mapToObj(i -> {
                String prefix = KEY_GAME_PREFIX + i + ".";
                String date = props.getProperty(prefix + "date", "");
                String opponent = props.getProperty(prefix + "opponent", "");
                // ... altri campi ...
                
                GamesRecord record = new GamesRecord(date, opponent, ...);
                return record;
            })
            .collect(Collectors.toList());
}
```

### Vantaggi Repository Pattern
- **Disaccoppiamento**: il Model non conosce i dettagli di persistenza
- **Testabilità**: facile creare mock repository per test
- **Flessibilità**: possibile cambiare storage (file → database) senza impattare il Model
- **Affidabilità**: salvataggio atomico previene perdita dati

---

## 12. Thread Safety e Concorrenza

### Descrizione
Tecniche e pattern utilizzati per garantire correttezza in ambiente multi-threaded.

### Implementazioni nel Progetto

#### **1. CopyOnWriteArrayList per Observers**
**`GameState.java`** (linea 70):
```java
private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();
```
- **Thread-safe**: modifiche creano una copia dell'array interno
- **Iterazione sicura**: nessun ConcurrentModificationException
- **Ideale per**: molte letture, poche scritture (tipico pattern Observer)

#### **2. AtomicBoolean per Flag**
**`GameState.java`** (linea 73):
```java
private final AtomicBoolean gameFinished = new AtomicBoolean(false);

public boolean isFinished() {
    boolean finished = hands.values().stream().allMatch(List::isEmpty);
    if (finished && gameFinished.compareAndSet(false, true)) {
        // Solo il primo thread notifica
        notifyGameFinished();
    }
    return finished;
}
```
- **Operazione atomica**: `compareAndSet` è thread-safe
- **Garanzia**: notifica una sola volta anche con chiamate concorrenti

#### **3. BlockingQueue per Comunicazione Thread**
**`GiocatoreUmano.java`**:
```java
private final BlockingQueue<Integer> cardChoiceQueue = new LinkedBlockingQueue<>();

public int chooseCard(GameState state) throws InterruptedException {
    // Blocca finché non arriva una scelta dall'UI thread
    Integer choice = cardChoiceQueue.take();
    return choice;
}

public void submitCardChoice(int cardIndex) {
    // Chiamato dall'UI thread
    cardChoiceQueue.offer(cardIndex);
}
```
- **Comunicazione sicura**: tra game loop thread e UI thread
- **Blocking**: `take()` aspetta senza busy-waiting
- **Non-blocking**: `offer()` non blocca l'UI

#### **4. Synchronized Methods**
**`MenuImpostazioni.java`** (linea 81):
```java
public static synchronized MenuImpostazioni getInstance() {
    if (instance == null) {
        instance = new MenuImpostazioni();
    }
    return instance;
}
```

**`StorageProfile.java`** (linea 257):
```java
public synchronized void save(UserProfile profile) throws IOException {
    // Salvataggio thread-safe del profilo
}
```

#### **5. SwingUtilities.invokeLater per UI Updates**
**`GameController.java`**:
```java
private void runGameLoop() {
    while (!gameState.isFinished()) {
        // Game logic nel background thread
        Cards played = gameState.playCard(current, idx);
        
        // Aggiornamenti UI sempre nell'Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            view.showCardPlayed(current, played, ...);
            view.refresh();
        });
    }
}
```
- **Thread confinement**: tutta la UI in un solo thread (EDT)
- **Previene**: race conditions e deadlock nella GUI

#### **6. Volatile e Final per Visibilità**
**`GameController.java`** (linea 46):
```java
private volatile boolean gameRunning = false;
```
- **Volatile**: garantisce visibilità tra thread
- **Final**: campi immutabili dopo costruzione

### Tecniche Thread-Safety Utilizzate
- **Immutability**: oggetti immutabili sono automaticamente thread-safe
- **Thread confinement**: UI operations solo nell'EDT
- **Synchronization**: per sezioni critiche
- **Concurrent collections**: CopyOnWriteArrayList, BlockingQueue
- **Atomic variables**: AtomicBoolean per flag
- **Defensive copying**: Collections.unmodifiableList/Map

---

## 13. Immutability e Defensive Copying

### Descrizione
Utilizzo di oggetti immutabili e copie difensive per prevenire modifiche indesiderate e garantire thread-safety.

### Implementazioni nel Progetto

#### **1. Collections Immutabili**
**`GameState.java`**:

```java
// Restituisce copia immutabile
public List<Giocatore> getPlayers() { 
    return Collections.unmodifiableList(players); 
}

public List<Cards> getHand(Giocatore p) {
    List<Cards> hand = hands.get(p);
    return (hand == null) ? List.of() : Collections.unmodifiableList(hand);
}

public Map<Giocatore, Integer> getScores() { 
    return Collections.unmodifiableMap(scores); 
}

public List<Cards> getTrickCards() {
    return Collections.unmodifiableList(trickCards);
}
```
- **Protezione**: impossibile modificare stato interno dall'esterno
- **Sicurezza**: nessuna reference leak
- **API pulita**: interfaccia di sola lettura

#### **2. Mappa Immutabile di Costanti**
**`GameState.java`** (linee 81-95):
```java
private static final Map<Cards.Rank, Integer> CARD_POINTS;
static {
    Map<Cards.Rank, Integer> m = new EnumMap<>(Cards.Rank.class);
    m.put(Cards.Rank.ASSO, 3);
    m.put(Cards.Rank.TRE, 1);
    m.put(Cards.Rank.DUE, 1);
    // ...
    CARD_POINTS = Collections.unmodifiableMap(m);
}
```
- **Inizializzazione statica**: valori costanti condivisi
- **Immutabile**: nessuna modifica possibile dopo inizializzazione

#### **3. Final Fields**
Quasi tutti i campi sono `final` quando possibile:
```java
private final List<Giocatore> players;
private final Map<Giocatore, List<Cards>> hands = new LinkedHashMap<>();
private final Mazzo deck;
private final AudioManager audioManager;
private final List<GameStateObserver> observers = new CopyOnWriteArrayList<>();
```
- **Garanzia**: reference non può cambiare dopo costruzione
- **Thread-safety**: campi final sono safely published

#### **4. Defensive Copy nei Getter**
**`Mazzo.java`** (linea 79):
```java
public List<Cards> snapshot() {
    return List.copyOf(cards);
}
```
- **Copia profonda**: modifiche alla copia non impattano l'originale

### Vantaggi Immutability
- **Thread-safety**: oggetti immutabili sono automaticamente thread-safe
- **Prevedibilità**: lo stato non cambia inaspettatamente
- **Caching sicuro**: possibile condividere reference senza rischi
- **Debugging facilitato**: meno stati mutabili da tracciare

---


## Conclusione

Il progetto JTressette dimostra un'applicazione completa e professionale di design patterns e tecnologie moderne Java:

### Design Patterns Implementati
- **MVC**: Separazione netta tra Model, View e Controller
- **Observer**: Notifiche reattive per aggiornamenti automatici della UI
- **Singleton**: Gestione centralizzata di impostazioni globali (MenuImpostazioni)
- **Strategy**: Algoritmi intercambiabili per difficoltà bot (Easy, Medium, Hard)
- **Utility Class**: Helper statici con cache per risorse (CardImageLoader)
- **Repository/DAO**: Astrazione della persistenza (StorageProfile)
- **Polymorphism**: Design basato su interfacce (Giocatore → Bot, GiocatoreUmano)

### Tecnologie e Best Practices
- **Java Swing**: UI ricca con 5 layout managers e componenti personalizzati
- **Stream API**: Programmazione funzionale per operazioni su collezioni
- **Thread Safety**: CopyOnWriteArrayList, BlockingQueue, AtomicBoolean, synchronized
- **Immutability**: Collections immutabili e defensive copying
- **Audio System**: Gestione completa con fade, volume dinamico ed effetti
- **Grafica 2D**: Rendering professionale con animazioni a 60fps, trasparenze ed effetti

### Architettura e Qualità del Codice
- **Principi SOLID**: Single Responsibility, Open/Closed, Interface Segregation
- **Separation of Concerns**: Logica di business separata da presentazione e persistenza
- **Concurrent Programming**: Comunicazione sicura tra UI thread e game loop thread
- **Error Handling**: Gestione errori con fallback e resilienza
- **Resource Management**: Cache intelligente e preload opzionale per performance

Il progetto risulta in un codebase ben strutturato, estensibile, manutenibile e thread-safe, ideale sia per uso pratico che per studio accademico dei design patterns.
