package de.farbtrommel.rts.translation;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.omg.dmn.TDefinitions;
import org.ruleml.deliberation.*;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Function need by multiply translators
 */
public class Util {

    static {
        JAXBContext tmpJCDMN = null;
        JAXBContext tmpJCRuleML = null;
        try {
            tmpJCDMN = JAXBContext.newInstance(Util.DMN_PACKAGE_NAME);
            tmpJCRuleML = JAXBContext.newInstance(Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        DMN_JAXB_CONTEXT = tmpJCDMN;
        RULEML_JAXB_CONTEXT = tmpJCRuleML;
    }

    //public  final static FeelToRuleML FEEL_TO_RULE_ML = new FeelToRuleML();
    public final static String DMN_PACKAGE_NAME = "org.omg.dmn";
    public final static String DMN_NAMESPACE = "http://www.omg.org/spec/DMN/20151101/dmn.xsd";
    public final static JAXBContext DMN_JAXB_CONTEXT;

    public final static String RULEML_PRODUCTION_RULES_PACKAGE_NAME = "org.ruleml.deliberation";
    public final static JAXBContext RULEML_JAXB_CONTEXT;


    public final static ObjectFactory RULEML_FACTORY = new ObjectFactory();
    public final static org.omg.dmn.ObjectFactory DMN_FACTORY = new org.omg.dmn.ObjectFactory();

    /**
     * Helps to create unique names or ids.
     */
    public static final AtomicInteger sCounter = new AtomicInteger(0);

    public static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            int chr = str.charAt(i);
            if ((chr < 48) || (chr > 57)) {
                return false;
            }
        }
        return true;
    }

    public static String readResourceFileAsString(String filePath)
            throws java.io.IOException {

        InputStream inputStream = Util.class.getClassLoader().getResourceAsStream(filePath);
        final char[] buffer = new char[1024];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(inputStream, "UTF-8")) {
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return out.toString();
    }

    public static String serializeQName(QName qName) {
        if (qName == null) {
            return "";
        }
        return qName.getNamespaceURI() + "#" + qName.getLocalPart();
    }

    public static void objToOutputStream(Object object, String packageName, OutputStream outputStream)
            throws JAXBException, RuleTranslationException {
        Marshaller m;
        if (Util.DMN_PACKAGE_NAME.equals(packageName)) {
            m = DMN_JAXB_CONTEXT.createMarshaller();
        } else if (Util.RULEML_PRODUCTION_RULES_PACKAGE_NAME.equals(packageName)) {
            m = RULEML_JAXB_CONTEXT.createMarshaller();
        } else {
            throw new RuleTranslationException("Package Name " + packageName + " is unknown.");
        }
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(object, outputStream);
    }

    public static String sfeelStringToJavaString(String value) {
        value = value.replaceAll("^\"", "");
        value = value.replaceAll("\"$", "");
        return value;
    }

    public static JAXBElement<?> readInputStreamToObj(InputStream io, String packageName) throws JAXBException {
        JAXBContext jContext = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jContext.createUnmarshaller();
        return (JAXBElement<?>) unmarshaller.unmarshal(io);
    }

    public static JAXBElement<?> readResourceXmlFileToObj(String path, String packageName) throws JAXBException {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);
        JAXBContext jContext = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jContext.createUnmarshaller();
        return (JAXBElement<?>) unmarshaller.unmarshal(inputStream);
    }

    public static JAXBElement<?> readXmlFileToObj(String path, String packageName) throws JAXBException {
        File file = new File(path);
        JAXBContext jContext = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = jContext.createUnmarshaller();
        return (JAXBElement<?>) unmarshaller.unmarshal(file);
    }

    public static JAXBElement<?> loadObject(InputStream inputStream, String packageName) throws RuleTranslationException {
        try {
            return Util.readInputStreamToObj(inputStream, packageName);
        } catch (JAXBException e) {
            throw new RuleTranslationException("Unmarshalling XML File unsuccessfully", e);
        }
    }

    public static JAXBElement<?> loadObject(String source, String packageName) throws RuleTranslationException {
        try {
            InputStream is = new ByteArrayInputStream(((String) source).getBytes());
            return Util.readInputStreamToObj(is, packageName);
        } catch (JAXBException e) {
            throw new RuleTranslationException("Given string isn't a valid DMN XML input", e);
        }
        //throw  new RuleTranslationException("No unmarshalling method found for provided input");
    }

    public static JAXBElement<?> loadObject(File file, String packageName) throws RuleTranslationException {
        if (file.isFile()) {
            try {
                return Util.readXmlFileToObj((file).getAbsolutePath(), packageName);
            } catch (JAXBException e) {
                throw new RuleTranslationException("Given path isn't valid." +
                        " Probably provided file is a valid DMN XML file.", e);
            }
        }
        throw new RuleTranslationException("Provided file isn't valid.");
    }

    public static JAXBElement<?> loadObject(Object object, String packageName) throws RuleTranslationException {
        try {
            if (object instanceof File) {
                return Util.readXmlFileToObj(((File) object).getAbsolutePath(), packageName);
            } else if (object instanceof TDefinitions) {
                return Util.DMN_FACTORY.createDefinitions((TDefinitions) object);
            } else if (object instanceof RuleMLType) {
                return Util.RULEML_FACTORY.createRuleML((RuleMLType) object);
            } else if (object instanceof JAXBElement<?>) {
                return (JAXBElement<?>) object;
            }
            throw new RuleTranslationException("No unmarshalling method found for provided input");
        } catch (JAXBException e) {
            throw new RuleTranslationException("Unmarshalling XML File unsuccessfully", e);
        }
    }

    public static CharStream antlr(Object o) throws RuleTranslationException {
        String convertType = "Unknown input type.";
        try {
            if (o instanceof InputStream) {
                convertType = "InputStream";
                CharStream input = new ANTLRInputStream((InputStream) o);
                return input;
            } else if (o instanceof File) {
                convertType = "File";
                Reader reader = new FileReader((File) o);
                CharStream input = new ANTLRInputStream(reader);
                return input;
            } else if (o instanceof String) {
                convertType = "String";
                CharStream input = new ANTLRInputStream((String) o);
                return input;
            }
            throw new RuleTranslationException("Error: " + convertType + ".");
        } catch (IOException e) {
            throw new RuleTranslationException("Error: " + convertType + ".", e);
        }
    }

    public static JAXBElement<?> create(String type, String value) throws RuleTranslationException {
        if (value == null) {
            value = "";
        }
        if (type.toLowerCase().equals("rel")) {
            RelType relType = Util.RULEML_FACTORY.createRelType();
            relType.getContent().add(value);
            return Util.RULEML_FACTORY.createRel(relType);
        } else if (type.toLowerCase().equals("var")) {
            VarType varType = Util.RULEML_FACTORY.createVarType();
            varType.getContent().add(value);
            return Util.RULEML_FACTORY.createVar(varType);
        } else if (type.toLowerCase().equals("ind")) {
            IndType indType = Util.RULEML_FACTORY.createIndType();
            indType.getContent().add(value);
            return Util.RULEML_FACTORY.createInd(indType);
        }
        throw new RuleTranslationException("Create Element: Type " + type + " unknown");
    }
}
