# Soluzione agli Errori di Compilazione

## Problema Originale

Gli errori di compilazione che hai riscontrato:

```
src\main\JTressette.java:3: error: package Controller.Profile does not exist
import Controller.Profile.ProfileController;
```

E l'errore di runtime:

```
Errore: impossibile trovare o caricare la classe principale main.JTressette
Causato da: java.lang.ClassNotFoundException: main.JTressette
```

## Causa

Entrambi gli errori erano causati da:

1. **Directory errata**: Il comando di compilazione veniva eseguito dalla directory sbagliata
2. **Risorse mancanti**: Le risorse (immagini, audio) non venivano copiate nella directory di output

### Struttura del Progetto

Il progetto ha questa struttura:

```
JTressette/                    ← directory repository root
├── build.sh                   ← NUOVO: script di build
├── build.bat                  ← NUOVO: script di build Windows
├── run.sh                     ← NUOVO: script di esecuzione
├── run.bat                    ← NUOVO: script di esecuzione Windows
└── JTressette/                ← directory del progetto vero e proprio
    ├── src/
    │   ├── Controller/
    │   ├── Model/
    │   ├── View/
    │   ├── main/JTressette.java
    │   └── res/               ← risorse (immagini, audio)
    └── bin/                   ← creato durante la compilazione
```

Il problema era che stavi provando a compilare dalla directory root con:
```
javac -d bin src/main/JTressette.java
```

Ma il codice sorgente si trova in `JTressette/src/`, non in `src/`!

## Soluzione

Ho creato script automatici che risolvono tutti i problemi:

### 1. Script di Compilazione (`build.sh` / `build.bat`)

**Linux/macOS:**
```bash
./build.sh
```

**Windows:**
```cmd
build.bat
```

Questi script:
- Si spostano nella directory corretta (`JTressette/`)
- Compilano tutti i file Java con il comando corretto
- Copiano le risorse (immagini, audio) nella directory `bin/`

### 2. Script di Esecuzione (`run.sh` / `run.bat`)

**Linux/macOS:**
```bash
./run.sh
```

**Windows:**
```cmd
run.bat
```

Questi script:
- Verificano che il progetto sia stato compilato
- Eseguono l'applicazione con il classpath corretto

## Come Usare

### Metodo Semplice (Raccomandato)

1. Apri il terminale/prompt dei comandi
2. Vai alla directory principale del repository (dove si trovano `build.sh` e `build.bat`)
3. Compila:
   ```bash
   ./build.sh      # Linux/macOS
   build.bat       # Windows
   ```
4. Esegui:
   ```bash
   ./run.sh        # Linux/macOS
   run.bat         # Windows
   ```

### Metodo Manuale (Avanzato)

Se preferisci compilare manualmente:

**Linux/macOS:**
```bash
cd JTressette
mkdir -p bin
javac -d bin -sourcepath src src/main/JTressette.java
cp -r src/res bin/
java -cp bin main.JTressette
```

**Windows:**
```cmd
cd JTressette
mkdir bin
javac -d bin -sourcepath src src\main\JTressette.java
xcopy /E /I /Y src\res bin\res
java -cp bin main.JTressette
```

## Spiegazione Tecnica

### Perché gli errori "package does not exist"?

Quando compili con:
```
javac -d bin src/main/JTressette.java
```

Il compilatore cerca i package relativi alla posizione del file sorgente. Se `src/main/JTressette.java` importa `Controller.Profile.ProfileController`, il compilatore cerca il file in:
```
src/Controller/Profile/ProfileController.java
```

Ma nel tuo caso, la struttura è:
```
JTressette/src/Controller/Profile/ProfileController.java
```

Quindi devi:
1. Essere nella directory `JTressette/` quando compili
2. Usare `-sourcepath src` per dire al compilatore dove trovare i sorgenti

### Perché ClassNotFoundException?

L'applicazione carica risorse (immagini delle carte, file audio) usando:
```java
getClass().getResource("/res/audio/backgroundMenu.wav")
```

Questo cerca nel classpath. Se compili solo i `.java` senza copiare le risorse, il programma non trova i file necessari e può fallire.

La soluzione è copiare `src/res/` in `bin/res/` dopo la compilazione.

## Documenti Aggiuntivi

- **README.md**: Istruzioni complete di build e esecuzione
- **RISOLUZIONE_PROBLEMI.md**: Guida dettagliata alla risoluzione di problemi comuni

## Risultato

Ora puoi compilare ed eseguire il progetto senza errori! 🎉
