# Rule Translation Service (RTS)

RTS is a translation library translate to and from [RuleML](http://wiki.ruleml.org/).
RuleML are the vehicle for using rules on the web and other distributed systems. 
They allow deploying, executing, publishing and communicating rules in a network.

Currently supported:

* [DMN](http://www.omg.org/spec/DMN/) S-FEEL -> RuleML
* DMN decision table -> RuleML
* RuleML -> [Prova](http://www.prova.ws/)

## Install

Download and install with maven:

```sh
$ git clone https://github.com/farbtrommel/rule-translation-service.git rts
$ cd rts
$ mvn install
```

Now you can use locally the library, add to your pom.xml file:
```xml
<dependencies>
    <dependency>
        <groupId>de.farbtrommel</groupId>
        <artifactId>rts</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```

## How to run?

### Command line

Create a jar with all dependencies.
```sh
$ cd rts
$ mvn assembly:assembly
$ cd target
```

The command line tool need for parameter. The first parameter is the input type, 
second the output type, third the source path, fourth the destination path.
```sh
$ java  -jar rts-0.0.9-jar-with-dependencies.jar dmn ruleml /path/to/source /path/to/destination 
```

### Java Library

This sample shows how to translate a DMN file "table.dmn" to RuleML file. The result will written to "table.ruleml" file.

```java
//...
public static void main(String[] args) {
    RuleTranslatorService rts = RuleTranslationServiceFactory.createRuleTranslationService();
      
    try {
        rts.translate(new File("table.dmn"), "DMN", "RuleML", new FileOutputStream("table.ruleml"));
    } catch (UnknownRulesLanguageException ex) {
        // ...
    } catch (RuleTranslatorException ex) {
        // ...
    }
}
// ...
```
