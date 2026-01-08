# Design Patterns e Tecnologie nel Progetto JTressette

## Indice
1. [Design Pattern Model-View-Controller (MVC)](#1-design-pattern-model-view-controller-mvc)
2. [Design Pattern Observer/Observable](#2-design-pattern-observerobservable)
3. [Java Swing](#3-java-swing)
4. [Stream API per le Funzioni](#4-stream-api-per-le-funzioni)
5. [Gestione Audio](#5-gestione-audio)
6. [Effetti Grafici](#6-effetti-grafici)

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
        observers.addIfAbsent(observer);
    }
}

private void notifyCardPlayed(Giocatore player, Cards card) {
    observers.forEach(observer -> observer.onCardPlayed(player, card));
}
```

**Esempio pratico** nel metodo `playCard()`:
```java
public Cards playCard(Giocatore p, int handIndex) {
    // ... logica di gioco ...
    notifyCardPlayed(p, c);
    
    if (trickCards.size() == players.size()) {
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

## Conclusione

Il progetto JTressette dimostra un'applicazione completa e professionale di design patterns e tecnologie moderne Java:

- **MVC**: Separazione netta tra dati, presentazione e logica
- **Observer**: Notifiche reattive per aggiornamenti automatici
- **Swing**: UI ricca con componenti personalizzati
- **Stream API**: Codice funzionale e manutenibile
- **Audio avanzato**: Sistema completo con fade e volume dinamico
- **Grafica 2D**: Rendering professionale con animazioni ed effetti

Ogni aspetto del progetto segue best practices e principi SOLID, risultando in un codebase ben strutturato, estensibile e manutenibile.
