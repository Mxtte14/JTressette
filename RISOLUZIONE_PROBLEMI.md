# Risoluzione Problemi di Compilazione ed Esecuzione

Questo documento spiega come risolvere i problemi comuni di compilazione ed esecuzione di JTressette.

## Errori di Compilazione

### Errore: "package Controller.Profile does not exist"

**Causa:** Stai provando a compilare dalla directory sbagliata o con i percorsi errati.

**Soluzione:**

1. Assicurati di essere nella directory principale del repository (dove si trovano i file `build.sh` e `build.bat`)
2. Usa gli script di build forniti:
   - Su Linux/macOS: `./build.sh`
   - Su Windows: `build.bat`

**Spiegazione:** Gli errori di "package does not exist" si verificano quando il compilatore Java non riesce a trovare i file sorgente nelle directory corrette. Il codice sorgente si trova in `JTressette/src/` e deve essere compilato da lì con l'opzione `-sourcepath` corretta.

### Struttura del Progetto

```
JTressette/                    (repository root)
├── build.sh                   (script di compilazione Linux/macOS)
├── build.bat                  (script di compilazione Windows)
├── run.sh                     (script di esecuzione Linux/macOS)
├── run.bat                    (script di esecuzione Windows)
├── README.md
└── JTressette/                (directory del progetto)
    ├── .gitignore
    ├── src/                   (codice sorgente)
    │   ├── Controller/
    │   ├── Model/
    │   ├── View/
    │   ├── main/
    │   │   └── JTressette.java
    │   └── res/               (risorse: immagini, audio)
    └── bin/                   (file compilati - creato dopo build)
```

## Errori di Esecuzione

### Errore: "ClassNotFoundException: main.JTressette"

**Causa:** Le risorse non sono state copiate nella directory bin o il classpath non è impostato correttamente.

**Soluzione:**

1. Ricompila il progetto usando gli script di build:
   ```bash
   ./build.sh    # Linux/macOS
   build.bat     # Windows
   ```

2. Esegui l'applicazione usando gli script di esecuzione:
   ```bash
   ./run.sh      # Linux/macOS
   run.bat       # Windows
   ```

**Spiegazione:** L'applicazione necessita che le risorse (immagini delle carte, file audio) siano disponibili nel classpath. Gli script di build copiano automaticamente queste risorse dalla directory `src/res/` a `bin/res/`.

### Errore: "bin directory not found"

**Causa:** Il progetto non è stato compilato.

**Soluzione:** Esegui prima lo script di compilazione:
```bash
./build.sh    # Linux/macOS
build.bat     # Windows
```

## Compilazione Manuale (Avanzata)

Se hai bisogno di compilare manualmente senza usare gli script:

### Linux/macOS
```bash
cd JTressette
mkdir -p bin
javac -d bin -sourcepath src src/main/JTressette.java
cp -r src/res bin/
java -cp bin main.JTressette
```

### Windows
```cmd
cd JTressette
mkdir bin
javac -d bin -sourcepath src src\main\JTressette.java
xcopy /E /I /Y src\res bin\res
java -cp bin main.JTressette
```

### Spiegazione dei Comandi

- `javac -d bin`: Specifica la directory di output per i file `.class` compilati
- `-sourcepath src`: Indica al compilatore dove trovare i file sorgente Java
- `src/main/JTressette.java`: File principale da compilare (compilerà automaticamente tutte le dipendenze)
- `cp -r src/res bin/` o `xcopy`: Copia le risorse nella directory bin
- `java -cp bin main.JTressette`: Esegue l'applicazione con il classpath impostato su bin

## Requisiti di Sistema

- **Java JDK 17 o superiore**: Verifica la versione con `java -version` e `javac -version`
- **Sistema Operativo**: Windows, Linux, o macOS
- **Display Grafico**: L'applicazione è un'applicazione GUI e richiede un ambiente desktop

## Verifica dell'Installazione Java

Per verificare che Java sia installato correttamente:

```bash
java -version
javac -version
```

Entrambi i comandi dovrebbero mostrare la versione 17 o superiore.

Se Java non è installato, scaricalo da:
- [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- [OpenJDK](https://openjdk.org/)

## Supporto

Se riscontri ancora problemi:

1. Verifica di essere nella directory corretta
2. Assicurati che Java JDK sia installato e nel PATH
3. Prova a eliminare la directory `bin` e ricompilare:
   ```bash
   rm -rf JTressette/bin  # Linux/macOS
   rd /s /q JTressette\bin  # Windows
   ```
   Poi ricompila con gli script di build.
