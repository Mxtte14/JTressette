# Guida Rapida - Diagramma delle Classi JTressette

## 🎯 Cosa è stato creato?

Un diagramma delle classi completo del progetto JTressette che mostra:
- **35 classi** organizzate secondo il pattern MVC
- **Colori diversi** per Model (🟢 verde), View (🔵 azzurro) e Controller (🟠 arancione)
- **Tutte le relazioni** tra le classi (extends, implements, associazioni)

## 📁 File Disponibili

| File | Dimensione | Descrizione |
|------|------------|-------------|
| `JTressette Class Diagram.png` | 498 KB | Immagine PNG ad alta risoluzione (6221×2183 px) |
| `JTressette Class Diagram.svg` | 140 KB | Immagine vettoriale (migliore qualità) |
| `class-diagram.puml` | 14 KB | Codice sorgente PlantUML (modificabile) |
| `CLASS_DIAGRAM_README.md` | 7.3 KB | Documentazione completa in italiano |
| `DIAGRAMMA_CLASSI.md` | 5.1 KB | Riepilogo dettagliato delle classi |

## 👀 Come Visualizzare

### Metodo più semplice:
1. Apri il file `JTressette Class Diagram.svg` con un browser web o visualizzatore di immagini
2. Usa lo zoom per vedere i dettagli

### Alternative:
- Apri il PNG per una visualizzazione rapida
- Usa un IDE con supporto PlantUML per modificare il file `.puml`

## 🎨 Legenda dei Colori

Il diagramma usa colori diversi per distinguere i componenti MVC:

### 🟢 MODEL (Verde chiaro - #90EE90)
**21 classi** che gestiscono la logica di business e i dati:
- `Model.Game`: Giocatore, Bot, Cards, Mazzo, GameState (9 classi)
- `Model.Profile`: UserProfile, GamesRecord, StorageProfile (3 classi)
- `Model.Audio`: AudioManager (1 classe)
- `Model.Impostazioni`: MenuImpostazioni, SettingsListener (2 classi)
- `Model.Util`: CardImageLoader (1 classe)

### 🔵 VIEW (Azzurro - #87CEEB)
**9 classi** che gestiscono l'interfaccia utente:
- `View.Game`: GameView, GameSetup (2 classi)
- `View.Menu`: MenuFrame, HomeMenu, MenuOption (3 classi)
- `View.Profile`: ProfileMenu (1 classe)
- `View.Rules`: RulesPage (1 classe)
- `View.Impostazioni`: ViewImpostazioni (1 classe)

### 🟠 CONTROLLER (Arancione - #FFB347)
**5 classi** che coordinano Model e View:
- `Controller.Game`: GameController, Cursor (2 classi)
- `Controller.Profile`: ProfileController, ProfileControllerImpl, ProfileListener (3 classi)

## 📖 Simboli delle Relazioni

Nel diagramma troverai questi simboli:

- `──────>` **Associazione**: Una classe usa un'altra
- `──────|>` **Implementazione**: Una classe implementa un'interfaccia
- `──────▷` **Estensione**: Una classe estende un'altra classe
- `◆──────` **Composizione**: La vita di un oggetto dipende dall'altro
- `○──────` **Aggregazione**: Relazione più debole, vite indipendenti

## 🔑 Gerarchie Principali

### Interfaccia Giocatore
```
Giocatore (interfaccia)
├── GiocatoreUmano (implementa)
└── Bot (implementa)
```

### Swing Components
```
JFrame
├── GameView
├── GameSetup
├── MenuFrame
└── ViewImpostazioni

JPanel
├── HomeMenu
├── ProfileMenu
└── RulesPage
```

### Profile Management
```
ProfileController (interfaccia)
└── ProfileControllerImpl (implementa)
```

## 📝 Note Importanti

### Pattern Architetturali Evidenziati:
1. **MVC**: Chiara separazione tra Model, View e Controller
2. **Observer**: Per notifiche di cambiamenti (GameStateObserver, ProfileListener)
3. **Singleton**: MenuImpostazioni è un singleton
4. **Strategy**: Bot usa strategie diverse in base alla difficoltà (EASY, MEDIUM, HARD)

### Thread Safety:
- GameState usa `CopyOnWriteArrayList` per gli observers
- ProfileControllerImpl salva in modo asincrono con `SwingWorker`
- GameController usa `ExecutorService` per il loop di gioco

## 🚀 Prossimi Passi

1. **Visualizza il diagramma**: Apri il file SVG o PNG
2. **Leggi la documentazione**: Consulta `CLASS_DIAGRAM_README.md` per dettagli completi
3. **Esplora le classi**: Usa `DIAGRAMMA_CLASSI.md` per una lista completa
4. **Modifica se necessario**: Edita `class-diagram.puml` e rigenera con PlantUML

## 💡 Suggerimenti

- **Per presentazioni**: Usa il file PNG
- **Per stampa**: Usa il file SVG (scalabile senza perdita di qualità)
- **Per modifiche**: Edita il file `.puml` e rigenera
- **Per studio**: Segui i colori per capire la separazione MVC

## 📚 Documentazione Completa

Per una comprensione approfondita, leggi:
1. **CLASS_DIAGRAM_README.md** - Guida dettagliata con esempi
2. **DIAGRAMMA_CLASSI.md** - Lista completa delle classi e relazioni

---

**Creato:** 3 Gennaio 2026  
**Autore:** GitHub Copilot  
**Formato:** PlantUML → PNG + SVG  
**Classi:** 35 totali (21 Model + 9 View + 5 Controller)
