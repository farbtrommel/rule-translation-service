package de.farbtrommel.rts.service;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.UnknownRulesLanguageException;
import de.farbtrommel.rts.translation.RuleTranslationException;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Set;

/**
 * Interface for the business rules translation service.
 */
public interface RuleTranslationService {
    /**
     * Translates the input in the output language.
     *
     * @param input The input (rules, queries or facts)
     * @param in    The language for the input
     * @param out   The language for the output
     * @return The result of the translation
     * @throws UnknownRulesLanguageException In case of unsupported rules language.
     */
    void translate(Object input, String in, String out, OutputStream outputStream)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException;

    void translate(Object input, RuleLanguage in, RuleLanguage out, OutputStream outputStream, HashMap<String, String> options)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException;

    void translate(Object input, String in, String out, OutputStream outputStream, HashMap<String, String> options)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException;

    /**
     * Returns list with the business rules languages that are currently
     * supported as input languages for the translation framework.
     *
     * @return Set with language objects
     */
    Set<RuleLanguage> getSupportedLanguagesToRuleML();

    /**
     * Returns list with the business rules languages that are currently
     * supported as output languages for the translation framework.
     *
     * @return Set with language objects
     */
    Set<RuleLanguage> getSupportedLanguagesFromRuleML();

    /**
     * Checks if the translation can translate the rulebase from the
     * input to output language.
     *
     * @return True if the translation is supported, false otherwise.
     */
    boolean supportsTranslation(RuleLanguage in, RuleLanguage out);

    /**
     * Tries to guess the rules language from the input source.
     *
     * @param input The rule input that should be analyzed.
     * @return The rules language of the input.
     * @throws UnknownRulesLanguageException In case of unsupported rules language.
     */
    RuleLanguage guessLanguage(String input)
            throws UnknownRulesLanguageException;
}
