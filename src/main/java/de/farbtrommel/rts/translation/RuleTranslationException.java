package de.farbtrommel.rts.translation;

/**
 * This exception got threw when any kind of error accrue.
 */
public class RuleTranslationException extends Exception {

    public RuleTranslationException() {
        super();
    }

    public RuleTranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RuleTranslationException(String message) {
        super(message);
    }

    public RuleTranslationException(Throwable cause) {
        super(cause);
    }

}
