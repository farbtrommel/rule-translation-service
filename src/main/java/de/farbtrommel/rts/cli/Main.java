package de.farbtrommel.rts.cli;


import de.farbtrommel.rts.RuleLanguage;
import de.farbtrommel.rts.service.RuleTranslationServiceFactory;
import de.farbtrommel.rts.service.RuleTranslationServiceImpl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Set;

/**
 * Run the jar from command line.
 * Translate service use parameter:
 *  <from> <to> <source> <destination>
 * Get current version:
 *  --version
 * Get supported languages:
 *  --list
 */
public class Main {
    /**
     * java -jar ruletranslator-x.jar <from> <to> <source> <destination>
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(Main.class.getClassLoader().getResourceAsStream(".properties"));
        properties.load(stream);
        stream.close();

        RuleTranslationServiceImpl ruleTranslatorService = (RuleTranslationServiceImpl)
                RuleTranslationServiceFactory.createRuleTranslationService();

        if (args[0].endsWith("version")) {
            System.out.println("Rule Translator Version: " + properties.getProperty("version"));
        } else if (args[0].endsWith("list")) {
            System.out.println("Supported languages to RuleML:");
            Set<RuleLanguage> supportedLanguagesToRuleML = ruleTranslatorService.getSupportedLanguagesToRuleML();
            for (RuleLanguage item : supportedLanguagesToRuleML) {
                System.out.println(" - " + item.toString());
            }

            System.out.println("Supported languages from RuleML:");
            Set<RuleLanguage> supportedLanguagesFromRuleML = ruleTranslatorService.getSupportedLanguagesFromRuleML();
            for (RuleLanguage item : supportedLanguagesFromRuleML) {
                System.out.println(" - " + item.toString());
            }

        } else if (args.length == 3 || args.length == 4) {
            RuleLanguage in = ruleTranslatorService.guessLanguage(args[0]);
            RuleLanguage out = ruleTranslatorService.guessLanguage(args[1]);
            if (args.length == 3) {
                ruleTranslatorService.translate(new File(args[2]), in, out, System.out, null);
            } else {
                FileOutputStream fileOut = new FileOutputStream(args[3]);
                ruleTranslatorService.translate(new File(args[2]), in, out, fileOut, null);
                fileOut.close();
            }
        } else {
            System.out.println("The arguments are <from> <to> <source> <destination>");
            System.out.println("e.g. dmn ruleml table.dmn table.ruleml");
            System.out.println("or use --version or --list to see all supported languages.");
        }
    }
}
