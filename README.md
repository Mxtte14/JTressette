# JTressette
A game developed in Java that simulates the classic Italian card game Tressette. The game supports single-player mode against 1, 2 or 3 AI opponents  .

## Descrizione progetto
Il progetto JTressette è stato sviluppato come parte di un corso universitario di Programmazione ad Oggetti. L'obiettivo principale del progetto è stato quello di creare un gioco di carte che permetta agli utenti di giocare a Tressette offline.

## Caratteristiche principali
- **Modalità di gioco**: Single-player contro 1, 2 o 3
- **Interfaccia utente**: Grafica semplice e intuitiva per facilitare l'interazione con il gioco.
- **Regole del gioco**: Implementazione fedele delle regole tradizionali del Tressette.
- **Intelligenza artificiale**: Avversari controllati dal computer con strategie di gioco di base implementata a livello facile, medio o difficile
- **Salvataggio e caricamento**: Possibilità di salvare e caricare le partite in corso.
- **Statistiche di gioco**: Monitoraggio delle statistiche di gioco per ogni giocatore.

## Scelte progettuali
Il progetto è stato sviluppato utilizzando il modello MVC (Model-View-Controller) per separare la logica di gioco dalla presentazione grafica e dalla gestione degli input dell'utente. Questo approccio facilita la manutenzione e l'espansione del codice.
Inoltre è stato implementato il pattern Singleton per la gestione delle risorse condivise, come l'audio e le impostazioni di gioco.
Infine è stato utilizzato il modello Observer-Observable per aggiornare l'interfaccia utente in risposta ai cambiamenti dello stato del gioco.

## Requisiti
- Java Development Kit (JDK) 17 o superiore
- Sistema operativo: Windows, Linux, o macOS

## Usare con Eclipse IDE

Se usi Eclipse, consulta la **[GUIDA_ECLIPSE.md](GUIDA_ECLIPSE.md)** per istruzioni dettagliate su come importare ed eseguire il progetto in Eclipse.

## Come compilare ed eseguire (da Linea di Comando)

### Su Linux/macOS

1. **Compilare il progetto:**
   ```bash
   ./build.sh
   ```

2. **Eseguire l'applicazione:**
   ```bash
   ./run.sh
   ```

### Su Windows

1. **Compilare il progetto:**
   ```cmd
   build.bat
   ```

2. **Eseguire l'applicazione:**
   ```cmd
   run.bat
   ```

### Compilazione manuale

Se preferisci compilare manualmente, assicurati di essere nella directory principale del repository e usa i seguenti comandi:

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

**Nota importante:** 
- Assicurati di eseguire i comandi dalla directory corretta. Il codice sorgente si trova nella sottodirectory `JTressette/src/`.
- Le risorse (immagini, audio) devono essere copiate dalla directory `src/res/` a `bin/res/` dopo la compilazione.

## Architettura del progetto
Il progetto è strutturato in diverse classi principali:
- `Audio`: Gestione dell'audio di gioco.
- `Controller`: Logica di controllo del gioco.
- `Game`: Classe principale che gestisce lo stato del gioco.
- `Impostazioni`: Gestione interfaccia delle impostazioni di gioco con possibilità di cambiare audio e grafica.
- `Main`: Punto di ingresso dell'applicazione.
- `Menu`: Gestione del menu principale del gioco.
- `Profile`: Gestione del profilo giocatore.
- `Rules`: Interfaccia con spiegazione del gioco.
- `Ui`: Gestione dell'interfaccia utente grafica.
- `Utils`: Classi di utilità per il gioco.