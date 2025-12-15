package Model.Game;

/**
 * Enumerazione che rappresenta i livelli di difficoltà disponibili per i bot nel gioco.
 * Il livello di difficoltà determina la strategia di gioco utilizzata dall'intelligenza artificiale.
 */
public enum Difficoltà {
    /** Livello facile - il bot effettua scelte casuali tra le mosse legali */
    EASY,
    
    /** Livello medio - il bot applica una strategia di base cercando di vincere con carte minime */
    MEDIUM,
    
    /** Livello difficile - il bot utilizza una strategia avanzata con euristica sofisticata */
    HARD
}
