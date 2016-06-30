package de.farbtrommel.rts.translation;

import de.farbtrommel.rts.RuleLanguage;

/**
 * Generic interface for rule translations.
 */
public interface RuleTranslation {
    /**
     * Returns the input language of the translation.
     *
     * @return The input language of the translation.
     */
    RuleLanguage getInputLanguage();

    /**
     * Returns the output language of the translation.
     *
     * @return The output language of the translation.
     */
    RuleLanguage getOutputLanguage();

}
