package de.farbtrommel.rts.service;

import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.UnknownRulesLanguageException;
import de.farbtrommel.rts.translation.*;
import org.ruleml.deliberation.RuleMLType;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Set;

/**
 * Prototype implementation of the RulesTranslationService interface.
 */
public class RuleTranslationServiceImpl implements RuleTranslationService {

    private HashMap<RuleLanguage, RuleTranslationToRuleML> translatorsToRuleML =
            new HashMap<RuleLanguage, RuleTranslationToRuleML>();
    private HashMap<RuleLanguage, RuleTranslationFromRuleML> translatorsFromRuleML =
            new HashMap<RuleLanguage, RuleTranslationFromRuleML>();

    /**
     * @param input The input (rules, queries or facts)
     * @param in    The language for the input
     * @param out   The language for the output
     * @return The translated input file
     * @throws UnknownRulesLanguageException
     */
    @Override
    public void translate(Object input, RuleLanguage in, RuleLanguage out,
                          OutputStream outputStream, HashMap<String, String> options)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException {
        //RuleML => x
        if (in.getName().toLowerCase().equals("ruleml")) {
            fromRuleML(input, in, out, outputStream, options);
            // x => RuleML
        } else if (out.getName().toLowerCase().equals("ruleml")) {
            Util.objToOutputStream(
                    toRuleML(input, in, out, outputStream, options),
                    Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME,
                    outputStream);
            // x => RuleML => y
        } else if (this.supportsTranslation(in, out)) {
            OutputStream stepOut = new ByteArrayOutputStream();
            fromRuleML(toRuleML(input, in, out, stepOut, options), in, out, outputStream, options);
        } else {
            throw new UnknownRulesLanguageException(
                    "Could not find translation for " + in + " -> " + out);
        }

    }

    @Override
    public void translate(Object input, String in, String out,
                          OutputStream outputStream)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException {
        translate(input, in, out, outputStream, null);
    }

    @Override
    public void translate(Object input, String in, String out,
                          OutputStream outputStream, HashMap<String, String> options)
            throws UnknownRulesLanguageException, RuleTranslationException, JAXBException, IOException {
        RuleLanguage inLang = guessLanguage(in);
        RuleLanguage outLang = guessLanguage(out);
        translate(input, inLang, outLang, outputStream, options);
    }

    private RuleMLType toRuleML(Object input, RuleLanguage in, RuleLanguage out, OutputStream outputStream,
                                HashMap<String, String> options) throws RuleTranslationException, IOException, JAXBException {
        RuleTranslation translator = translatorsToRuleML.get(in);
        if (input instanceof File) {
            return ((RuleTranslationToRuleML) translator).runTranslation((File) input, options);
        } else if (input instanceof InputStream) {
            return ((RuleTranslationToRuleML) translator).runTranslation((InputStream) input, options);
        } else if (input instanceof String) {
            return ((RuleTranslationToRuleML) translator).runTranslation((String) input, options);
        }
        throw new RuleTranslationException("Input type unknown.");
    }

    private void fromRuleML(Object input, RuleLanguage in, RuleLanguage out, OutputStream outputStream,
                            HashMap<String, String> options) throws RuleTranslationException, IOException {
        RuleTranslation translator = translatorsFromRuleML.get(out);
        String string = "";
        if (input instanceof File) {
            string = ((RuleTranslationFromRuleML) translator).runTranslation((File) input, options);
        } else if (input instanceof InputStream) {
            string = ((RuleTranslationFromRuleML) translator).runTranslation((InputStream) input, options);
        } else if (input instanceof RuleMLType) {
            string = ((RuleTranslationFromRuleML) translator).runTranslation((RuleMLType) input, options);
        } else if (input instanceof JAXBElement) {
            string = ((RuleTranslationFromRuleML) translator).runTranslation((JAXBElement) input, options);
        } else if (input instanceof String) {
            string = ((RuleTranslationFromRuleML) translator).runTranslation((String) input, options);
        }
        outputStream.write(string.getBytes(Charset.forName("UTF-8")));
    }

    /**
     * Return all supported input languages.
     *
     * @return Set of all Rule
     */
    @Override
    public Set<RuleLanguage> getSupportedLanguagesToRuleML() {
        return translatorsToRuleML.keySet();
    }

    /**
     * Return all supported output languages.
     *
     * @return Return all possible outputs.
     */
    @Override
    public Set<RuleLanguage> getSupportedLanguagesFromRuleML() {
        return translatorsFromRuleML.keySet();
    }

    @Override
    public boolean supportsTranslation(RuleLanguage in, RuleLanguage out) {
        //RuleML => out
        if (in.getName().toLowerCase().equals("ruleml")) {
            RuleTranslation ruleTranslatorFromRuleML = translatorsFromRuleML.get(in);
            return ruleTranslatorFromRuleML.getOutputLanguage().equals(out);
        }
        //in => RuleML
        else if (out.getName().toLowerCase().equals("ruleml")) {
            RuleTranslation ruleTranslatorToRuleML = translatorsToRuleML.get(out);
            return ruleTranslatorToRuleML.getInputLanguage().equals(in);
        }
        //in => RuleML => out
        else {
            RuleTranslation ruleTranslatorToRuleML = translatorsToRuleML.get(in);
            RuleTranslation ruleTranslatorFromRuleML = translatorsFromRuleML.get(out);
            return ruleTranslatorToRuleML.getInputLanguage().equals(in) &&
                    ruleTranslatorFromRuleML.getOutputLanguage().equals(out);
        }
    }


    @Override
    public RuleLanguage guessLanguage(String input)
            throws UnknownRulesLanguageException {
        input = input.toLowerCase();
        if (input.contains("ruleml")) {
            return new RuleLanguage("RuleML", "1.0", "application/xml");
        }
        Set<RuleLanguage> availableLanguage = this.getSupportedLanguagesToRuleML();
        String supported = "";
        for (RuleLanguage item : availableLanguage) {
            if (item.getName().toLowerCase().contains(input)) {
                return item;
            }
            supported += item.getName() + ", ";
        }

        Set<RuleLanguage> list = this.getSupportedLanguagesFromRuleML();
        for (RuleLanguage element : list) {
            if (element.getName().toLowerCase().contains(input)) {
                return element;
            }
            supported += element.getName() + ", ";
        }

        if (supported.length() > 3) {
            supported = supported.substring(0, supported.length() - 2);
        }

        throw new UnknownRulesLanguageException("Rule Language " + input + " is unknown. Supported are " + supported + ".");
    }

    /**
     * Method to add a translation to this service. It will be called from the
     * configurator to initialize the translation service.
     *
     * @param t Translator to be added.
     * @throws UnknownRulesLanguageException Thrown in case of whrong version of the ruleml mediator
     *                                       language.
     */
    public void addTranslatorToRuleML(RuleTranslationToRuleML t)
            throws UnknownRulesLanguageException {
        translatorsToRuleML.put(t.getInputLanguage(), t);
    }

    /**
     * Method to add a translation to this service. It will be called from the
     * configurator to initialize the translation service.
     *
     * @param t Translator to be added.
     * @throws UnknownRulesLanguageException Thrown in case of whrong version of the ruleml mediator
     *                                       language.
     */
    public void addTranslatorFromRuleML(RuleTranslationFromRuleML t)
            throws UnknownRulesLanguageException {
        translatorsFromRuleML.put(t.getOutputLanguage(), t);
    }
}
