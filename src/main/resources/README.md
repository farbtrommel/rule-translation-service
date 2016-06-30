# Test Rules

Rules files in the folder `rules` only for units test purpose. See more at `src/test/java/de/farbtrommel/rts/translation`.

# JAXB

XSD files to Java class for simple marshalling and unmarshalling xml files.

### DMN

Make sure that all DMN files starts with a `definitions` tag. 
The tag have to contain the default namespace `xmlns="http://www.omg.org/spec/DMN/20130901"`, 
otherwise the unmarshalling function throws an error.

### RuleML Reaction

Make sure that all RuleML files starts with a `RuleML` tag. 
The tag have to contain the default namespace `xmlns="http://ruleml.org/spec"`, 
otherwise the unmarshalling function throws an error.

### Build

To generate java code:
```bash
$> mvn jaxb2:gernate
```

### Unmarshalling XML file

Simple example to load a XML file by using JAXB.

```java
try {
    InputStream inputStream = ClassLoader.getSystemResourceAsStream("rules/ruleml/own.xml");
    JAXBContext jContext = JAXBContext.newInstance("org.ruleml.delibartion");
    Unmarshaller unmarshaller = jContext.createUnmarshaller();
    JAXBElement<RuleMLType> unmarshal = (JAXBElement<RuleMLType>) unmarshaller
            .unmarshal(inputStream);
    System.out.println(unmarshal);            
} catch (JAXBException e) {
    e.printStackTrace();
}
```

The XML file should contain following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<RuleML
    xmlns="http://ruleml.org/spec"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.ruleml.org/1.0/xsd http://www.ruleml.org/1.0/xsd/datalog.xsd">
  <Assert mapClosure="universal">
    <Implies>
      <if>
        <!-- explicit 'And' -->
        <And>
          <Atom>
            <op><Rel>buy</Rel></op>
            <Var>person</Var>
            <Var>merchant</Var>
            <Var>object</Var>
          </Atom>
          <Atom>
            <op><Rel>keep</Rel></op>
            <Var>person</Var>
            <Var>object</Var>
          </Atom>
        </And>
      </if>
      <then>
        <Atom>
          <op><Rel>own</Rel></op>
          <Var>person</Var>
          <Var>object</Var>
        </Atom>
      </then>
    </Implies>

    <Implies>
      <if>
        <Atom>
          <op><Rel>sell</Rel></op>
          <Var>merchant</Var>
          <Var>person</Var>
          <Var>object</Var>
        </Atom>
      </if>
      <then>
        <Atom>
          <op><Rel>buy</Rel></op>
          <Var>person</Var>
          <Var>merchant</Var>
          <Var>object</Var>
        </Atom>
      </then>
    </Implies>

    <Atom>
      <op><Rel>sell</Rel></op>
      <Ind>John</Ind>
      <Ind>Mary</Ind>
      <Ind>XMLBible</Ind>
    </Atom>

    <Atom>
      <op><Rel>keep</Rel></op>
      <Ind>Mary</Ind>
      <Ind>XMLBible</Ind>
    </Atom>
  </Assert>
 
</RuleML>
```

