package util;

import game.Cards;

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
 * Utility class for loading and caching card images.
 * Supports both IntelliJ and Eclipse resource loading.
 */
public class CardImageLoader {

    private static final Logger LOGGER = Logger.getLogger(CardImageLoader.class.getName());
    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static BufferedImage cardBackImage;

    // Card dimensions for scaling
    public static final int CARD_WIDTH = 70;
    public static final int CARD_HEIGHT = 100;
    public static final int SMALL_CARD_WIDTH = 50;
    public static final int SMALL_CARD_HEIGHT = 75;

    /**
     * Get the image for a specific card.
     * @param card The card to get the image for
     * @return Scaled BufferedImage of the card, or null if not found
     */
    public static BufferedImage getCardImage(Cards card) {
        String imageName = getCardImageName(card);
        return getCardImage(imageName);
    }

    /**
     * Get a scaled card image for display.
     * @param card The card to get the image for
     * @param width Target width
     * @param height Target height
     * @return Scaled Image
     */
    public static Image getScaledCardImage(Cards card, int width, int height) {
        BufferedImage original = getCardImage(card);
        if (original == null) {
            return createPlaceholderImage(card, width, height);
        }
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Get the back of card image (Dorso).
     */
    public static BufferedImage getCardBackImage() {
        if (cardBackImage == null) {
            cardBackImage = loadImage("Dorso.png");
        }
        return cardBackImage;
    }

    /**
     * Get a scaled back of card image.
     */
    public static Image getScaledCardBackImage(int width, int height) {
        BufferedImage original = getCardBackImage();
        if (original == null) {
            return createBackPlaceholderImage(width, height);
        }
        return original.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    /**
     * Builds the image filename for a card based on its suit and rank.
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
     * Get cached image or load it.
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
     * Load an image from resources.
     * Tries multiple paths for compatibility with IntelliJ and Eclipse.
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
     * Create a placeholder image when the actual image cannot be loaded.
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
     * Create a placeholder for card back.
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

    private static String getSuitSymbol(Cards.Segno segno) {
        return switch (segno) {
            case DENARA -> "♦";
            case SPADE -> "♠";
            case BASTONI -> "♣";
            case COPPE -> "♥";
        };
    }

    /**
     * Preload all card images into cache.
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