## Translator Service Provider

Here you find all implemented service provider for translation to and from RuleML.

## Programming Hints

### Do not forgot to registered a new translation service to the provider

If a new translator provider got added, don't forget to add a line into the file:
`resource/META-INF/services/de.farbtrommel.rts.translation.RuleTranslation`.

Please add a line like:
```
de.farbtrommel.rts.translation.dmn2ruleml.Dmn2RuleMLTranslation
de.farbtrommel.rts.translation.feel2ruleml.Feel2RuleMLTranslation
de.farbtrommel.rts.translation.ruleml2prova.RuleML2ProvaTranslation
```