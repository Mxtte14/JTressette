package Model.Util;

import Model.Game.Cards;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Classe di utilità per il caricamento e la gestione delle immagini delle carte.
 * Implementa un sistema di caching per migliorare le prestazioni evitando
 * di ricaricare le stesse immagini più volte.
 * 
 * <p>Funzionalità principali:</p>
 * <ul>
 *   <li>Caricamento lazy delle immagini dalle risorse</li>
 *   <li>Cache in memoria per accesso rapido</li>
 *   <li>Supporto per immagini ridimensionate</li>
 *   <li>Generazione di immagini placeholder in caso di errore di caricamento</li>
 *   <li>Gestione del retro delle carte (Dorso)</li>
 *   <li>Compatibilità con diversi path di risorse (IntelliJ, Eclipse)</li>
 * </ul>
 */
public class CardImageLoader {

    /** Logger per la registrazione di eventi ed errori */
    private static final Logger LOGGER = Logger.getLogger(CardImageLoader.class.getName());
    
    /** Cache delle immagini caricate per evitare ricaricamenti */
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    
    /** Immagine del retro della carta (caricata una sola volta) */
    private static BufferedImage cardBackImage;

    /**
     * Carica l'immagine di una carta specifica.
     * L'immagine viene cercata prima nella cache, altrimenti viene caricata dalle risorse.
     * 
     * @param card la carta di cui caricare l'immagine
     * @return l'immagine della carta, o null se non può essere caricata
     */
    public static BufferedImage getCardImage(Cards card) {
        String imageName = getCardImageName(card);
        return getCardImage(imageName);
    }


    /**
     * Restituisce un'immagine ridimensionata di una carta.
     * Se l'immagine originale non può essere caricata, viene generata un'immagine placeholder.
     * 
     * @param card la carta di cui caricare l'immagine
     * @param width larghezza desiderata in pixel
     * @param height altezza desiderata in pixel
     * @return immagine ridimensionata della carta
     */
    public static Image getScaledCardImage(Cards card, int width, int height) {
        BufferedImage original = getCardImage(card);
        if (original == null) {
            return createPlaceholderImage(card, width, height);
        }
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Restituisce l'immagine del retro della carta (Dorso).
     * L'immagine viene caricata una sola volta e poi mantenuta in cache.
     * 
     * @return immagine del retro della carta, o null se non può essere caricata
     */
    public static BufferedImage getCardBackImage() {
        if (cardBackImage == null) {
            cardBackImage = loadImage("Dorso.png");
        }
        return cardBackImage;
    }

    /**
     * Restituisce un'immagine ridimensionata del retro della carta.
     * Se l'immagine originale non può essere caricata, viene generata un'immagine placeholder.
     * 
     * @param width larghezza desiderata in pixel
     * @param height altezza desiderata in pixel
     * @return immagine ridimensionata del retro della carta
     */
    public static Image getScaledCardBackImage(int width, int height) {
        BufferedImage original = getCardBackImage();
        if (original == null) {
            return createBackPlaceholderImage(width, height);
        }
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Costruisce il nome del file immagine per una carta in base al seme e al valore.
     * Il formato utilizzato è: "[Seme][Valore].png" (es. "BastoniAsso.png", "Denari03.png").
     * 
     * @param card la carta per cui generare il nome file
     * @return il nome del file immagine
     */
    private static String getCardImageName(Cards card) {
        String suitName = switch (card.getSegno()) {
            case BASTONI -> "Bastoni";
            case COPPE -> "Coppe";
            case DENARA -> "Denari";
            case SPADE -> "Spade";
        };

        String rankName = switch (card.getRank()) {
            case ASSO -> "Asso";
            case DUE -> "02";
            case TRE -> "03";
            case QUATTRO -> "04";
            case CINQUE -> "05";
            case SEI -> "06";
            case SETTE -> "07";
            case ALFIERE -> "08";  // Fante
            case CAVALLO -> "09";  // Cavallo
            case RE -> "10";       // Re
        };

        return suitName + rankName + ".png";
    }

    /**
     * Carica un'immagine dalla cache o dalle risorse.
     * Se l'immagine è già in cache, la restituisce direttamente.
     * Altrimenti la carica e la aggiunge alla cache per usi futuri.
     * 
     * @param imageName nome del file immagine da caricare
     * @return l'immagine caricata, o null se il caricamento fallisce
     */
    private static BufferedImage getCardImage(String imageName) {
        if (imageCache.containsKey(imageName)) {
            return imageCache.get(imageName);
        }

        BufferedImage image = loadImage(imageName);
        if (image != null) {
            imageCache.put(imageName, image);
        }
        return image;
    }

    /**
     * Carica un'immagine dalle risorse del progetto.
     * Prova diversi percorsi per garantire compatibilità con IntelliJ, Eclipse e altri IDE.
     * I percorsi provati sono:
     * <ul>
     *   <li>/res/Cards/[nome_file]</li>
     *   <li>res/Cards/[nome_file]</li>
     *   <li>/Cards/[nome_file]</li>
     * </ul>
     * 
     * @param imageName nome del file immagine da caricare (con estensione)
     * @return l'immagine caricata, o null se nessun percorso funziona
     */
    private static BufferedImage loadImage(String imageName) {
        // Try different resource paths for compatibility
        String[] paths = {
                "/res/Cards/" + imageName,
                "res/Cards/" + imageName,
                "/Cards/" + imageName
        };

        for (String path : paths) {
            try {
                InputStream is = CardImageLoader.class.getResourceAsStream(path);
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    is.close();
                    return img;
                }
            } catch (IOException e) {
                LOGGER.log(Level.FINE, "Failed to load from path: " + path, e);
            }
        }

        LOGGER.log(Level.WARNING, "Could not load card image: " + imageName);
        return null;
    }

    /**
     * Crea un'immagine placeholder quando l'immagine reale non può essere caricata.
     * L'immagine generata mostra il valore e il seme della carta con colori appropriati.
     * Viene utilizzata come fallback per garantire che il gioco funzioni anche senza
     * le immagini delle carte.
     * 
     * @param card la carta per cui creare il placeholder
     * @param width larghezza dell'immagine in pixel
     * @param height altezza dell'immagine in pixel
     * @return immagine placeholder generata
     */
    private static Image createPlaceholderImage(Cards card, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card background
        g2d.setColor(Color.WHITE);
        g2d.fillRoundRect(0, 0, width, height, 10, 10);

        // Border
        g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

        // Card content
        Color suitColor = switch (card.getSegno()) {
            case DENARA, COPPE -> Color.RED;
            case SPADE, BASTONI -> Color.BLACK;
        };
        g2d.setColor(suitColor);

        // Rank
        g2d.setFont(new Font("Serif", Font.BOLD, 18));
        String rank = getRankSymbol(card.getRank());
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(rank, (width - fm.stringWidth(rank)) / 2, 30);

        // Suit
        g2d.setFont(new Font("Serif", Font.BOLD, 28));
        String suit = getSuitSymbol(card.getSegno());
        fm = g2d.getFontMetrics();
        g2d.drawString(suit, (width - fm.stringWidth(suit)) / 2, 70);

        g2d.dispose();
        return img;
    }

    /**
     * Crea un'immagine placeholder per il retro della carta.
     * Genera un'immagine stilizzata con sfondo blu e bordi decorativi
     * quando l'immagine del dorso non può essere caricata.
     * 
     * @param width larghezza dell'immagine in pixel
     * @param height altezza dell'immagine in pixel
     * @return immagine placeholder del retro carta
     */
    private static Image createBackPlaceholderImage(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Card back
        g2d.setColor(new Color(30, 60, 120));
        g2d.fillRoundRect(0, 0, width, height, 10, 10);

        // Border
        g2d.setColor(Color.WHITE);
        g2d.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

        // Inner pattern
        g2d.setColor(new Color(200, 180, 100));
        g2d.drawRoundRect(5, 5, width - 11, height - 11, 5, 5);

        g2d.dispose();
        return img;
    }

    /**
     * Converte un valore di carta in simbolo testuale per l'immagine placeholder.
     * Utilizzato quando le immagini reali non sono disponibili.
     * 
     * @param rank il valore della carta
     * @return simbolo testuale del valore (es. "A", "2", "K")
     */
    private static String getRankSymbol(Cards.Rank rank) {
        return switch (rank) {
            case ASSO -> "A";
            case DUE -> "2";
            case TRE -> "3";
            case QUATTRO -> "4";
            case CINQUE -> "5";
            case SEI -> "6";
            case SETTE -> "7";
            case ALFIERE -> "J";
            case CAVALLO -> "Q";
            case RE -> "K";
        };
    }

    /**
     * Converte un seme di carta in simbolo Unicode per l'immagine placeholder.
     * Utilizzato quando le immagini reali non sono disponibili.
     * 
     * @param segno il seme della carta
     * @return simbolo Unicode del seme (♦, ♠, ♣, ♥)
     */
    private static String getSuitSymbol(Cards.Segno segno) {
        return switch (segno) {
            case DENARA -> "♦";
            case SPADE -> "♠";
            case BASTONI -> "♣";
            case COPPE -> "♥";
        };
    }

    /**
     * Pre-carica tutte le immagini delle carte nella cache.
     * Questo metodo può essere chiamato all'avvio dell'applicazione per
     * migliorare le prestazioni durante il gioco, evitando ritardi nel
     * primo caricamento di ciascuna immagine.
     * Carica anche l'immagine del retro della carta.
     */
    public static void preloadImages() {
        for (Cards.Segno segno : Cards.Segno.values()) {
            for (Cards.Rank rank : Cards.Rank.values()) {
                Cards card = new Cards(segno, rank);
                getCardImage(card);
            }
        }
        getCardBackImage();
    }
}