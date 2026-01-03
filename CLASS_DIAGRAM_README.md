# Diagramma delle Classi - JTressette

## Descrizione

Questo documento descrive il diagramma delle classi del progetto JTressette, un'applicazione Java per giocare a Tressette (un popolare gioco di carte italiano) seguendo il pattern architetturale **Model-View-Controller (MVC)**.

## Visualizzazione del Diagramma

Il diagramma è stato creato utilizzando PlantUML ed è disponibile in diversi formati:
- **PNG**: `JTressette Class Diagram.png` (immagine raster, buona per visualizzazione rapida)
- **SVG**: `JTressette Class Diagram.svg` (immagine vettoriale, migliore qualità)
- **PUML**: `class-diagram.puml` (codice sorgente PlantUML)

### Visualizzazione Diretta
Apri i file PNG o SVG con qualsiasi visualizzatore di immagini o browser web.

### Modifica del Diagramma

#### Opzione 1: Online
1. Apri [PlantUML Online Editor](http://www.plantuml.com/plantuml/uml/)
2. Copia e incolla il contenuto del file `class-diagram.puml`
3. Il diagramma verrà generato automaticamente

#### Opzione 2: Locale
1. Installa PlantUML: [Download](https://plantuml.com/download)
2. Esegui: `plantuml class-diagram.puml`
3. Verrà generata un'immagine PNG con il diagramma

#### Opzione 3: IDE Integration
- **IntelliJ IDEA**: Installa il plugin "PlantUML Integration"
- **Eclipse**: Installa il plugin "PlantUML Eclipse Plugin"
- **VS Code**: Installa l'estensione "PlantUML"

## Struttura del Progetto (Pattern MVC)

Il progetto è organizzato secondo il pattern MVC con una chiara separazione delle responsabilità:

### 🟢 MODEL (Verde)
Gestisce la logica di business e i dati dell'applicazione:

#### Model.Game
- **Giocatore** (interfaccia): Definisce il comportamento di base di tutti i giocatori
- **GiocatoreUmano**: Implementazione per il giocatore umano con gestione input tramite BlockingQueue
- **Bot**: Implementazione dell'intelligenza artificiale con tre livelli di difficoltà (EASY, MEDIUM, HARD)
- **Cards**: Rappresenta una carta con seme e valore
- **Mazzo**: Gestisce il mazzo completo di 40 carte italiane
- **GameState**: Mantiene lo stato completo della partita (punteggi, mani, turni, prese)
- **GameStateObserver**: Pattern Observer per notificare cambiamenti di stato

#### Model.Profile
- **UserProfile**: Profilo utente con statistiche, livello ed esperienza
- **GamesRecord**: Storico delle partite giocate
- **StorageProfile**: Persistenza del profilo su file

#### Model.Audio
- **AudioManager**: Gestisce tutti i suoni e la musica del gioco

#### Model.Impostazioni
- **MenuImpostazioni**: Singleton per le impostazioni globali (volume, fullscreen, effetti)

#### Model.Util
- **CardImageLoader**: Cache e caricamento ottimizzato delle immagini delle carte

### 🔵 VIEW (Azzurro)
Gestisce l'interfaccia utente e la visualizzazione:

#### View.Game
- **GameView**: Finestra principale del gioco con tavolo, mani dei giocatori e animazioni
- **GameSetup**: Schermata di configurazione pre-partita

#### View.Menu
- **MenuFrame**: Frame principale con navigazione a card layout
- **HomeMenu**: Menu principale con opzioni di gioco
- **MenuOption**: Singola opzione del menu con effetti hover

#### View.Profile
- **ProfileMenu**: Schermata del profilo utente con avatar e statistiche

#### View.Rules
- **RulesPage**: Schermata delle regole del gioco

#### View.Impostazioni
- **ViewImpostazioni**: Interfaccia per modificare le impostazioni

### 🟠 CONTROLLER (Arancione)
Coordina Model e View, gestendo la logica di controllo:

#### Controller.Game
- **GameController**: Coordina il loop di gioco, gestisce i turni e gli eventi
- **Cursor**: Gestisce la selezione nel menu

#### Controller.Profile
- **ProfileController** (interfaccia): Definisce le operazioni sul profilo
- **ProfileControllerImpl**: Implementazione con salvataggio asincrono
- **ProfileListener**: Listener per notifiche di aggiornamento profilo

## Relazioni tra Classi

### Gerarchie di Ereditarietà
- **Giocatore** ◁── `GiocatoreUmano`, `Bot`
- **ProfileController** ◁── `ProfileControllerImpl`
- **JFrame** ◁── `GameView`, `GameSetup`, `MenuFrame`, `ViewImpostazioni`
- **JPanel** ◁── `HomeMenu`, `ProfileMenu`, `RulesPage`

### Implementazioni di Interfacce
- **MenuImpostazioni.SettingsListener** ◁── `MenuFrame`, `GameController`, `ViewImpostazioni`
- **ProfileListener** ◁── `HomeMenu`, `ProfileMenu`
- **GameStateObserver** ◁── varie classi che osservano lo stato

### Composizioni e Aggregazioni
- **GameState** contiene `Mazzo`, gestisce lista di `Giocatore`
- **UserProfile** contiene lista di `GamesRecord`
- **GameView** contiene `GameState`, `GiocatoreUmano`, `GameController`
- **MenuFrame** contiene `HomeMenu`, `ProfileMenu`, `RulesPage`

## Caratteristiche Architetturali

### Pattern Utilizzati
1. **Model-View-Controller (MVC)**: Separazione netta delle responsabilità
2. **Observer Pattern**: Per notifiche di cambiamenti (GameStateObserver, ProfileListener, SettingsListener)
3. **Singleton**: MenuImpostazioni
4. **Strategy Pattern**: Bot con diverse strategie in base alla difficoltà

### Thread Safety
- **GameState**: Usa `CopyOnWriteArrayList` per observers e `AtomicBoolean` per flag
- **ProfileControllerImpl**: Salvataggio asincrono con SwingWorker
- **GameController**: ExecutorService per il game loop

### Gestione Audio
- Supporto per musica di sottofondo (menu e partita)
- Effetti sonori (carte giocate, pescata, vittoria/sconfitta)
- Controllo volume e fade in/out

### Sistema di Punteggio
Le carte hanno valori specifici:
- ASSO, TRE, DUE: 3 punti
- RE, CAVALLO, ALFIERE: 1 punto
- Altre carte: 0 punti

I punti vengono scalati in base 3 (es. 9 punti = 3, 10 punti = 3⅓).

## Flusso di Esecuzione

1. **JTressette.main()** avvia l'applicazione
2. Crea **MenuFrame** con **HomeMenu**
3. L'utente seleziona "Gioca" → apre **GameSetup**
4. **GameSetup** crea lista di `Giocatore` (umano + bot)
5. Viene creato **GameController** che:
   - Inizializza **GameState**
   - Crea **GameView**
   - Avvia il game loop in background
6. Durante il gioco:
   - **GameController** gestisce i turni
   - **GameView** aggiorna la visualizzazione
   - **GameState** mantiene lo stato
7. Fine partita:
   - Calcolo vincitore e punteggi
   - Salvataggio statistiche nel profilo
   - Ritorno al menu

## Note sul Diagramma

### Colori
- 🟢 **Verde**: Classi del Model (logica e dati)
- 🔵 **Azzurro**: Classi della View (interfaccia grafica)
- 🟠 **Arancione**: Classi del Controller (coordinamento)

### Simboli delle Relazioni
- `──────>` Associazione semplice
- `──────|>` Implementazione di interfaccia
- `──────▷` Estensione (extends)
- `◆──────` Composizione (ciclo di vita dipendente)
- `○──────` Aggregazione (riferimento)

## File Sorgenti

Il codice sorgente è organizzato in:
```
JTressette/src/
├── Controller/
│   ├── Game/
│   └── Profile/
├── Model/
│   ├── Audio/
│   ├── Game/
│   ├── Impostazioni/
│   ├── Profile/
│   └── Util/
├── View/
│   ├── Game/
│   ├── Impostazioni/
│   ├── Menu/
│   ├── Profile/
│   └── Rules/
└── main/
    └── JTressette.java
```

## Autori

Progetto JTressette - Implementazione del gioco di carte italiano Tressette

## Licenza

[Specificare la licenza del progetto]
