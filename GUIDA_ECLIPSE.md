# Guida per Eclipse IDE

## Come Usare JTressette con Eclipse

Se stai usando Eclipse IDE, il progetto è già configurato con i file `.project` e `.classpath`. Segui questi passaggi per importare ed eseguire correttamente il progetto.

## Importare il Progetto in Eclipse

### Metodo 1: Importa Progetto Esistente (Raccomandato)

1. Apri Eclipse
2. Vai su **File** → **Import...**
3. Seleziona **General** → **Existing Projects into Workspace**
4. Clicca **Next**
5. In "Select root directory", clicca **Browse** e seleziona la directory principale del repository (quella che contiene `.project`)
6. Assicurati che "JTressette" sia selezionato nella lista dei progetti
7. **IMPORTANTE**: Deseleziona "Copy projects into workspace" (lascia il progetto nella sua posizione originale)
8. Clicca **Finish**

### Metodo 2: Aprire dal File System

1. Apri Eclipse
2. Vai su **File** → **Open Projects from File System...**
3. Clicca **Directory** e seleziona la directory principale del repository
4. Clicca **Finish**

## Configurazione del Progetto

Dopo aver importato il progetto, verifica la configurazione:

### 1. Verifica il Source Path

1. Tasto destro sul progetto → **Properties**
2. Vai su **Java Build Path** → scheda **Source**
3. Dovresti vedere: `JTressette/src`
4. Se non è presente, clicca **Add Folder** e seleziona `JTressette/src`

### 2. Verifica l'Output Folder

1. Nella stessa finestra **Java Build Path** → scheda **Source**
2. In basso, "Default output folder" dovrebbe essere: `JTressette/out/production/JTressette` o `JTressette/bin`
3. Le risorse (immagini, audio) in `JTressette/src/res` saranno automaticamente copiate nella directory di output da Eclipse

### 3. Verifica la Versione di Java

1. Tasto destro sul progetto → **Properties**
2. Vai su **Java Build Path** → scheda **Libraries**
3. Verifica che ci sia una JRE/JDK versione 17 o superiore
4. Se non è presente o è una versione vecchia:
   - Clicca **Add Library** → **JRE System Library**
   - Seleziona una JRE 17 o superiore

## Eseguire il Progetto

### Configurare la Run Configuration

1. Tasto destro sul file `JTressette/src/main/JTressette.java`
2. Seleziona **Run As** → **Java Application**
3. Eclipse creerà automaticamente una configurazione di esecuzione

Oppure:

1. Vai su **Run** → **Run Configurations...**
2. Tasto destro su **Java Application** → **New Configuration**
3. Configura:
   - **Name**: JTressette
   - **Project**: JTressette
   - **Main class**: `main.JTressette` (clicca **Search** per trovarlo)
4. Vai alla scheda **Classpath**
5. Verifica che "User Entries" contenga:
   - JTressette (default classpath)
   - JTressette/src (per le risorse)
6. Clicca **Apply** e poi **Run**

## Risoluzione Problemi Comuni in Eclipse

### Errore: "package Controller.Profile does not exist"

**Causa**: Il source path non è configurato correttamente.

**Soluzione**:
1. Tasto destro sul progetto → **Properties**
2. **Java Build Path** → **Source**
3. Rimuovi eventuali source path errati
4. Clicca **Add Folder** → seleziona `JTressette/src`
5. Clicca **Apply and Close**
6. Tasto destro sul progetto → **Refresh** (o premi F5)

### Errore: "Package explorer mostra errori rossi"

**Soluzione**:
1. Tasto destro sul progetto → **Clean**
2. Seleziona "Clean projects selected below"
3. Seleziona JTressette
4. Clicca **Clean**
5. Eclipse ricompilerà automaticamente il progetto

### Errore: "Cannot find resources (immagini, audio)"

**Causa**: Le risorse non sono nella directory di output.

**Soluzione**:
Eclipse copia automaticamente i file non-.java dal source path all'output folder. Verifica:

1. Tasto destro sul progetto → **Properties**
2. **Java Compiler** → **Building**
3. Assicurati che "Filtered resources" NON includa `*.png`, `*.wav`, `*.jpg`
4. Se le risorse non vengono copiate:
   - Vai su **Project** → **Clean...**
   - Pulisci il progetto e ricompila

### Errore: "ClassNotFoundException: main.JTressette"

**Causa**: Il main class non è configurato correttamente nella run configuration.

**Soluzione**:
1. **Run** → **Run Configurations...**
2. Seleziona la tua configurazione JTressette
3. Nella scheda **Main**, verifica:
   - **Project**: JTressette
   - **Main class**: `main.JTressette`
4. Se il campo è vuoto, clicca **Search** e seleziona `JTressette - main`

## Struttura del Progetto in Eclipse

Quando apri il progetto in Eclipse, dovresti vedere questa struttura nel Package Explorer:

```
JTressette
├── src
│   ├── Controller
│   │   ├── Game
│   │   └── Profile
│   ├── Model
│   │   ├── Audio
│   │   ├── Game
│   │   ├── Impostazioni
│   │   ├── Profile
│   │   └── Util
│   ├── View
│   │   ├── Game
│   │   ├── Impostazioni
│   │   ├── Menu
│   │   ├── Profile
│   │   └── Rules
│   ├── main
│   │   └── JTressette.java
│   └── res
│       ├── Cards
│       ├── audio
│       └── default_images
├── JRE System Library
└── Referenced Libraries
```

## Build Automatico

Eclipse compila automaticamente il progetto quando salvi i file. Se vuoi disabilitare questa funzione:

1. Vai su **Project** menu
2. Deseleziona **Build Automatically**
3. Per compilare manualmente, vai su **Project** → **Build Project**

## Debug in Eclipse

Per eseguire il debug:

1. Imposta dei breakpoint cliccando sulla barra sinistra accanto al numero di riga
2. Tasto destro su `JTressette.java` → **Debug As** → **Java Application**
3. Eclipse passerà alla prospettiva Debug

## Nota Importante

I file `.project` e `.classpath` nella directory root del repository sono già configurati correttamente. Se Eclipse non riconosce automaticamente la struttura, segui i passaggi di importazione sopra indicati.

## Alternative: Run Configuration manuale

Se i passaggi automatici non funzionano, puoi creare manualmente la run configuration:

1. Assicurati che il progetto sia compilato senza errori
2. Crea una nuova **Run Configuration**:
   - Main tab: `main.JTressette`
   - Arguments tab: (lascia vuoto)
   - Classpath tab: Usa il classpath di default del progetto
3. **Run**

## Supporto

Se continui ad avere problemi con Eclipse:
1. Verifica che Eclipse sia aggiornato (versione 2020-06 o successiva consigliata)
2. Assicurati di avere installato Java JDK 17 o superiore
3. Prova a chiudere e riaprire Eclipse
4. Come ultima risorsa, elimina il progetto da Eclipse (senza cancellare i file) e re-importalo

Per problemi non specifici di Eclipse, consulta:
- **RISOLUZIONE_PROBLEMI.md**: Problemi generali di compilazione
- **SOLUZIONE_ERRORI.md**: Spiegazione dettagliata degli errori comuni
