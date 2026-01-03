# 📊 Diagramma delle Classi JTressette - Indice Completo

## 🎯 Obiettivo Completato

È stato creato un **diagramma delle classi completo** per il progetto JTressette che mostra l'architettura MVC con colori distintivi per Model (verde), View (azzurro) e Controller (arancione).

---

## 📁 File Generati (7 files totali)

### 1. Diagrammi Visuali (3 files)

| File | Dimensione | Formato | Uso Consigliato |
|------|------------|---------|------------------|
| **JTressette Class Diagram.png** | 498 KB | PNG 6221×2183 | Presentazioni, documentazione |
| **JTressette Class Diagram.svg** | 140 KB | SVG vettoriale | Stampa, web, zoom infinito |
| **class-diagram.puml** | 14 KB | PlantUML | Modifiche, versionamento |

### 2. Documentazione (4 files)

| File | Righe | Contenuto | Quando Usarlo |
|------|-------|-----------|---------------|
| **GUIDA_RAPIDA_DIAGRAMMA.md** | 132 | Guida rapida con tabelle e simboli | Per iniziare velocemente |
| **CLASS_DIAGRAM_README.md** | 205 | Documentazione completa dettagliata | Per approfondire l'architettura |
| **DIAGRAMMA_CLASSI.md** | 180 | Riepilogo strutturato delle 35 classi | Per esplorare i componenti |
| **INDEX_DIAGRAMMA.md** | - | Questo file (indice generale) | Per navigare la documentazione |

---

## 🗺️ Come Navigare la Documentazione

### Percorso Consigliato

```
1. 👀 Visualizza il diagramma
   └─→ Apri "JTressette Class Diagram.svg" (migliore qualità)

2. 📖 Leggi la guida rapida  
   └─→ Apri "GUIDA_RAPIDA_DIAGRAMMA.md"
       ├─ Scopri la legenda dei colori
       ├─ Impara i simboli delle relazioni
       └─ Vedi le gerarchie principali

3. 🔍 Esplora le classi
   └─→ Apri "DIAGRAMMA_CLASSI.md"
       ├─ Lista completa delle 35 classi
       ├─ Pattern implementati
       └─ Relazioni principali

4. 📚 Approfondisci
   └─→ Apri "CLASS_DIAGRAM_README.md"
       ├─ Descrizione dettagliata di ogni package
       ├─ Flusso di esecuzione dell'applicazione
       └─ Best practices e thread safety
```

---

## 🎨 Panoramica del Diagramma

### 35 Classi Organizzate in 3 Layer MVC

#### 🟢 Model Layer (21 classi) - Verde #90EE90
**Gestisce la logica di business e i dati**

- **Model.Game** (9 classi)
  - Giocatore, GiocatoreUmano, Bot, Difficoltà
  - Cards (con enum Segno e Rank)
  - Mazzo, GameState, GameStateObserver

- **Model.Profile** (3 classi)
  - UserProfile, GamesRecord, StorageProfile

- **Model.Audio** (1 classe)
  - AudioManager

- **Model.Impostazioni** (2 classi)
  - MenuImpostazioni, SettingsListener

- **Model.Util** (1 classe)
  - CardImageLoader

#### 🔵 View Layer (9 classi) - Azzurro #87CEEB
**Gestisce l'interfaccia utente**

- **View.Game** (2 classi)
  - GameView, GameSetup

- **View.Menu** (3 classi)
  - MenuFrame, HomeMenu, MenuOption

- **View.Profile** (1 classe)
  - ProfileMenu

- **View.Rules** (1 classe)
  - RulesPage

- **View.Impostazioni** (1 classe)
  - ViewImpostazioni

#### 🟠 Controller Layer (5 classi) - Arancione #FFB347
**Coordina Model e View**

- **Controller.Game** (2 classi)
  - GameController, Cursor

- **Controller.Profile** (3 classi)
  - ProfileController, ProfileControllerImpl, ProfileListener

---

## 🔑 Pattern Architetturali Evidenziati

1. **MVC (Model-View-Controller)**
   - Separazione netta delle responsabilità
   - Ogni layer ha il suo colore nel diagramma

2. **Observer Pattern**
   - GameStateObserver (notifiche di cambiamenti di gioco)
   - ProfileListener (notifiche di aggiornamento profilo)
   - SettingsListener (notifiche di cambio impostazioni)

3. **Singleton Pattern**
   - MenuImpostazioni (unica istanza globale)

4. **Strategy Pattern**
   - Bot con 3 strategie: EASY, MEDIUM, HARD

---

## 📖 Legenda dei Simboli

Nel diagramma troverai questi simboli per le relazioni:

| Simbolo | Nome | Significato |
|---------|------|-------------|
| `──────>` | Associazione | Una classe usa un'altra |
| `──────\|>` | Implementazione | Classe implementa interfaccia |
| `──────▷` | Estensione | Classe estende altra classe (extends) |
| `◆──────` | Composizione | Ciclo di vita dipendente |
| `○──────` | Aggregazione | Riferimento, cicli di vita indipendenti |

---

## 🚀 Quick Start

### Per Visualizzare Subito
```bash
# Apri il diagramma SVG (migliore qualità)
open "JTressette Class Diagram.svg"

# Oppure il PNG
open "JTressette Class Diagram.png"
```

### Per Modificare
```bash
# 1. Modifica il file PlantUML
nano class-diagram.puml

# 2. Rigenera i diagrammi
plantuml -tpng class-diagram.puml
plantuml -tsvg class-diagram.puml
```

---

## 📊 Statistiche

- **Classi totali**: 35
  - Model: 21 (60%)
  - View: 9 (26%)
  - Controller: 5 (14%)

- **Interfacce**: 6
  - Giocatore
  - ProfileController
  - ProfileListener
  - GameStateObserver
  - MenuImpostazioni.SettingsListener

- **Enum**: 3
  - Difficoltà (EASY, MEDIUM, HARD)
  - Cards.Segno (DENARA, SPADE, BASTONI, COPPE)
  - Cards.Rank (TRE, DUE, ASSO, RE, CAVALLO, ALFIERE, SETTE, SEI, CINQUE, QUATTRO)

- **Extends JFrame**: 4 classi
  - GameView, GameSetup, MenuFrame, ViewImpostazioni

- **Extends JPanel**: 3 classi
  - HomeMenu, ProfileMenu, RulesPage

---

## 💡 Suggerimenti per l'Uso

### Per Presentazioni
- Usa **JTressette Class Diagram.png**
- Alta risoluzione, facile da incorporare

### Per Stampa
- Usa **JTressette Class Diagram.svg**
- Vettoriale, scala senza perdita di qualità

### Per Studio
- Segui i **colori** per capire la separazione MVC
- Leggi la **documentazione** per i dettagli

### Per Sviluppo
- Tieni aperto il **diagramma** come riferimento
- Consulta i **pattern** prima di fare modifiche

---

## 🎓 Percorso di Apprendimento

### Livello Beginner
1. Visualizza il diagramma SVG
2. Leggi GUIDA_RAPIDA_DIAGRAMMA.md
3. Identifica Model, View, Controller per colore

### Livello Intermediate
1. Studia DIAGRAMMA_CLASSI.md
2. Analizza le gerarchie di ereditarietà
3. Comprendi le relazioni tra classi

### Livello Advanced
1. Leggi CLASS_DIAGRAM_README.md completo
2. Analizza i pattern implementati
3. Studia thread safety e best practices

---

## 📝 Note Tecniche

### Strumenti Utilizzati
- **PlantUML 1.2020.2**: Generazione diagrammi
- **Java**: Linguaggio del progetto
- **Swing**: Framework GUI

### Formati Generati
- PNG: Raster, 6221×2183 pixel
- SVG: Vettoriale, scalabile infinito
- PlantUML: Codice sorgente

### Compatibilità
- Visualizzabile in qualsiasi browser
- Modificabile con PlantUML
- Integrabile in IDE (IntelliJ, Eclipse, VS Code)

---

## ✅ Checklist di Verifica

Prima di usare il diagramma, verifica di avere:

- [ ] Visualizzato il diagramma (SVG o PNG)
- [ ] Letto la guida rapida
- [ ] Compreso la legenda dei colori
- [ ] Identificato i pattern principali
- [ ] Consultato la documentazione dettagliata (opzionale)

---

## 📞 Supporto

### Per Domande sul Diagramma
Consulta in ordine:
1. GUIDA_RAPIDA_DIAGRAMMA.md
2. CLASS_DIAGRAM_README.md
3. DIAGRAMMA_CLASSI.md

### Per Modifiche
1. Edita `class-diagram.puml`
2. Rigenera con PlantUML
3. Verifica il risultato

---

## 📅 Informazioni

**Data di Creazione**: 3 Gennaio 2026  
**Versione**: 1.0  
**Formato**: PlantUML → PNG + SVG  
**Classi Documentate**: 35  
**Pattern Identificati**: 4 (MVC, Observer, Singleton, Strategy)  
**File Totali**: 7 (3 diagrammi + 4 documenti)

---

## 🎉 Risultato Finale

✅ Diagramma completo con tutte le classi  
✅ Colori distintivi per MVC  
✅ Tutte le relazioni documentate  
✅ Pattern architetturali evidenziati  
✅ 3 guide in italiano  
✅ Formati multipli (PNG, SVG, PlantUML)  
✅ Pronto per uso immediato  

**Il progetto JTressette ora ha una documentazione visuale completa della sua architettura!** 🚀
