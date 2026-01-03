# Diagramma delle Classi - JTressette

## 📊 Panoramica

È stato creato un diagramma delle classi completo per il progetto JTressette che mostra l'architettura MVC (Model-View-Controller) con colori distintivi:

- 🟢 **Model** (Verde): 21 classi - Logica di business e gestione dati
- 🔵 **View** (Azzurro): 9 classi - Interfaccia utente
- 🟠 **Controller** (Arancione): 5 classi - Coordinamento tra Model e View

**Totale: 35 classi visualizzate**

## 📁 File Creati

### 1. Diagramma
- **`JTressette-ClassDiagram.drawio`**
  - Formato Draw.io XML modificabile
  - Visualizza su https://app.diagrams.net/
  - Ideale per modifiche e esportazioni

### 2. Documentazione
- **`CLASS_DIAGRAM_README.md`**
  - Guida completa al diagramma
  - Spiegazione dettagliata di ogni componente
  - Istruzioni per visualizzazione e modifica

- **`DIAGRAMMA_CLASSI.md`** (questo file)
  - Lista completa delle classi
  - Organizzazione per package
  - Pattern e relazioni

- **`GUIDA_RAPIDA_DIAGRAMMA.md`**
  - Quick reference con simboli
  - Guida rapida all'uso
  - Tips e suggerimenti

## 🎨 Struttura del Diagramma

### Model (21 classi) - 🟢

#### Model.Game (9 classi)
- `Giocatore` (interfaccia)
- `GiocatoreUmano` (implementa Giocatore)
- `Bot` (implementa Giocatore)
- `Difficoltà` (enum: EASY, MEDIUM, HARD)
- `Cards` (con enum Segno e Rank)
- `Mazzo`
- `GameState`
- `GameStateObserver` (interfaccia)

#### Model.Profile (3 classi)
- `UserProfile`
- `GamesRecord`
- `StorageProfile`

#### Model.Audio (1 classe)
- `AudioManager`

#### Model.Impostazioni (2 classi)
- `MenuImpostazioni` (Singleton)
- `MenuImpostazioni.SettingsListener` (interfaccia)

#### Model.Util (1 classe)
- `CardImageLoader`

### View (9 classi) - 🔵

#### View.Game (2 classi)
- `GameView` (extends JFrame)
- `GameSetup` (extends JFrame)

#### View.Menu (3 classi)
- `MenuFrame` (extends JFrame)
- `HomeMenu` (extends JPanel)
- `MenuOption`

#### View.Profile (1 classe)
- `ProfileMenu` (extends JPanel)

#### View.Rules (1 classe)
- `RulesPage` (extends JPanel)

#### View.Impostazioni (1 classe)
- `ViewImpostazioni` (extends JFrame)

### Controller (5 classi) - 🟠

#### Controller.Game (2 classi)
- `GameController`
- `Cursor`

#### Controller.Profile (3 classi/interfacce)
- `ProfileController` (interfaccia)
- `ProfileControllerImpl` (implementa ProfileController)
- `ProfileListener` (interfaccia)

## 🔗 Relazioni Principali

### Gerarchie di Ereditarietà
- `Giocatore` ◁── `GiocatoreUmano`, `Bot`
- `ProfileController` ◁── `ProfileControllerImpl`
- `JFrame` ◁── `GameView`, `GameSetup`, `MenuFrame`, `ViewImpostazioni`
- `JPanel` ◁── `HomeMenu`, `ProfileMenu`, `RulesPage`

### Pattern Implementati
1. **MVC Pattern**: Separazione netta tra Model, View e Controller
2. **Observer Pattern**: GameStateObserver, ProfileListener, SettingsListener
3. **Singleton Pattern**: MenuImpostazioni
4. **Strategy Pattern**: Bot con strategie diverse per difficoltà

## 📖 Come Visualizzare

### Metodo Rapido
Apri il diagramma in Draw.io:
```bash
# Online
1. Vai su https://app.diagrams.net/
2. File → Open from → Device
3. Seleziona JTressette-ClassDiagram.drawio

# Desktop (se installato)
drawio JTressette-ClassDiagram.drawio
```

### Esportazione
Da Draw.io puoi esportare in:
- **PNG** - per presentazioni
- **SVG** - vettoriale scalabile
- **PDF** - per stampa
- **JPEG**, **HTML**, **XML**

## 📚 Lettura della Legenda

### Simboli delle Relazioni
- `──────>` Associazione (una classe usa un'altra)
- `──────|>` Implementazione (classe implementa interfaccia)
- `──────▷` Estensione (classe estende altra classe)
- `◆──────` Composizione (ciclo di vita dipendente)
- `○──────` Aggregazione (riferimento, ciclo di vita indipendente)

### Colori dei Componenti
- **🟢 Verde chiaro (#90EE90)**: Classi del Model
- **🔵 Azzurro (#87CEEB)**: Classi della View
- **🟠 Arancione (#FFB347)**: Classi del Controller

## 🎯 Punti Chiave dell'Architettura

### Separazione MVC
Il progetto segue rigorosamente il pattern MVC:
- **Model** gestisce i dati e la logica (es. GameState, UserProfile)
- **View** visualizza l'interfaccia (es. GameView, MenuFrame)
- **Controller** coordina le interazioni (es. GameController, ProfileController)

### Thread Safety
- `GameState` usa strutture thread-safe per observers
- `ProfileControllerImpl` usa SwingWorker per salvataggio asincrono
- `GameController` usa ExecutorService per il game loop

### Gestione Eventi
- Pattern Observer per notifiche di cambiamenti
- Listener pattern per eventi UI
- Callback per gestione asincrona

## 🔍 Per Maggiori Dettagli

Consulta il file **`CLASS_DIAGRAM_README.md`** per:
- Descrizione dettagliata di ogni classe
- Spiegazione delle responsabilità
- Flusso di esecuzione dell'applicazione
- Esempi di utilizzo dei pattern

---

**Generato il:** 3 Gennaio 2026  
**Strumento:** Draw.io (diagrams.net)  
**Formato:** Draw.io XML modificabile  
**Approccio:** Diagramma architetturale semplificato
