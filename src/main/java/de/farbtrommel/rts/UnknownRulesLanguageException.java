package de.farbtrommel.rts;

/**
 * Exception class in case of use of unknown rule language.
 */
public class UnknownRulesLanguageException extends Exception {

    public UnknownRulesLanguageException(String msg) {
        super(msg);
    }

}
