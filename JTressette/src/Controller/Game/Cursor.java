package Controller.Game;

import View.Menu.HomeMenu;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Gestisce il cursore di selezione nel menu principale del gioco.
 * Visualizza un'immagine indicatrice accanto alle opzioni del menu e
 * tiene traccia dell'opzione attualmente selezionata.
 * 
 * <p>Il cursore si posiziona automaticamente in base all'opzione selezionata
 * e carica un'immagine personalizzata per la visualizzazione.</p>
 */
public class Cursor {

    /** Logger per la registrazione di eventi ed errori del cursore */
    private static final Logger LOGGER = Logger.getLogger(Cursor.class.getName());

    /** Coordinata x del cursore sullo schermo */
    public int x, y;
    
    /** Indice dell'opzione attualmente selezionata (0-based) */
    private int selectedIndex = 0;
    
    /** Riferimento al menu principale per accedere alle opzioni */
    private final HomeMenu mp;
    
    /** Immagine visualizzata come cursore */
    private BufferedImage image;

    /**
     * Costruttore del cursore.
     * Inizializza il cursore caricando l'immagine e posizionandolo sulla prima opzione.
     * 
     * @param mp il menu principale che contiene le opzioni selezionabili
     */
    public Cursor(HomeMenu mp) {
        this.mp = mp;
        loadImage();
        setPosition();
    }

    /**
     * Carica l'immagine del cursore dalle risorse.
     * Se l'immagine non può essere caricata, viene registrato un warning ma il cursore
     * continua a funzionare senza immagine.
     */
    private void loadImage() {
        try (java.io.InputStream is = getClass().getResourceAsStream("/res/default_images/white_ping.jpg")) {
            if (is == null) {
                LOGGER.warning("Immagine cursore non trovata: /main/resource/sfondo_1.jpg");
                return;
            }
            image = ImageIO.read(is);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore nel caricamento del cursore", e);
        }
    }

    /**
     * Imposta l'indice dell'opzione selezionata e aggiorna la posizione del cursore.
     * Il cursore viene posizionato automaticamente accanto all'opzione specificata.
     * 
     * @param index indice dell'opzione da selezionare (0-based, deve essere valido)
     */
    public void setSelectedIndex(int index) {
        if (index < 0 || index >= mp.options.length) return;
        selectedIndex = index;
        y = mp.options[index].y - 24; // centratura verticale
        x = 50; // distanza dal bordo sinistro
    }

    /**
     * Restituisce l'indice dell'opzione attualmente selezionata.
     * 
     * @return indice dell'opzione selezionata (0-based)
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Imposta la posizione iniziale del cursore sulla prima opzione del menu.
     */
    private void setPosition() {
        x = 50;
        y = mp.options[selectedIndex].y - 24;
    }
}