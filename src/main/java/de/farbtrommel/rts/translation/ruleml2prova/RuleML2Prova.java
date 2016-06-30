package de.farbtrommel.rts.translation.ruleml2prova;

import de.farbtrommel.rts.translation.RuleTranslationException;
import org.ruleml.deliberation.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;

/**
 * Translate a JAXB Structure to Prova.
 */
public class RuleML2Prova {

    private static final String EOL = "\n";

    public static String translate(JAXBElement<?> source) throws RuleTranslationException {
        StringBuffer dest = new StringBuffer();
        traverse(source, dest, "", true);
        return dest.toString();
    }

    private static void traverse(JAXBElement<?> source, StringBuffer dest, String annotation, boolean isFact) throws RuleTranslationException {
        if (source.getDeclaredType().equals(RuleMLType.class)) {
            RuleMLType item = (RuleMLType) source.getValue();
            dest.append("% RuleML " + EOL);
            traverse(item.getActOrAssertOrRetract(), dest, annotation, isFact);
        } else if (source.getDeclaredType().equals(AssertType.class)) {
            AssertType item = (AssertType) source.getValue();
            traverse(item.getAttributeFormulaScope(), dest, annotation, isFact);
        } else if (source.getDeclaredType().equals(FormulaAssertType.class)) {
            FormulaAssertType item = (FormulaAssertType) source.getValue();
            traverse(item.getRulebase(), dest, annotation, isFact);
        } else if (source.getDeclaredType().equals(AndInnerType.class) ||
                source.getDeclaredType().equals(AtomType.class) ||
                source.getDeclaredType().equals(EqualType.class) ||
                source.getDeclaredType().equals(IndType.class) ||
                source.getDeclaredType().equals(VarType.class) ||
                source.getDeclaredType().equals(Object.class) ||
                source.getDeclaredType().equals(ImpliesType.class) ||
                source.getDeclaredType().equals(RulebaseType.class)
                ) {
            traverse(source.getValue(), dest, annotation, isFact);
        }
    }

    private static void traverse(Object source, StringBuffer dest, String annotation, boolean isFact) throws RuleTranslationException {
        if (source == null) {
            dest.append("");
            return;
        } else if (source.getClass() == ArrayList.class) {
            for (Object item : (ArrayList) source) {
                traverse(item, dest, annotation, isFact);
            }
        } else if (source.getClass() == AtomType.class) {
            if (isFact && !annotation.isEmpty()) {
                dest.append(annotation.trim());
                dest.append(EOL);
            }
            atom((AtomType) source, dest, annotation);
            if (isFact) {
                dest.append("." + EOL);
            }
        } else if (source.getClass() == ImpliesType.class) {
            //dest.append("% Implies " + EOL);
            implies((ImpliesType) source, dest, annotation);
        } else if (source.getClass() == VarType.class) {
            dest.append(((VarType) source).getContent().get(0));
        } else if (source.getClass() == IndType.class) {
            dest.append("\"");
            if (((IndType) source).getContent().size() > 0) {
                dest.append(((IndType) source).getContent().get(0));
            }
            dest.append("\"");
        } else if (source.getClass() == RelType.class) {
            dest.append(((RelType) source).getContent().get(0));
        } else if (source.getClass() == EqualType.class) {
            equal(((EqualType) source), dest, annotation);
        } else if (source.getClass() == AndInnerType.class) {
            and(((AndInnerType) source), dest, annotation);
        } else if (source.getClass() == AssertType.class) {
            dest.append("% Assert" + EOL);
            traverse(((AssertType) source).getFormulaOrRulebaseOrImplies(), dest, annotation, isFact);
        } else if (source.getClass() == RulebaseType.class) {
            RulebaseType rulebaseType = (RulebaseType) source;
            if (rulebaseType.getOid() != null && rulebaseType.getOid()
                    .getInd().getContent().size() > 0) {
                annotation = "@rulebase(\"" + rulebaseType.getOid().getInd()
                        .getContent().get(0) + "\") ";
            }
            dest.append("% Rulebase" + EOL);
            traverse(((RulebaseType) source).getFormulaOrImpliesOrRule(), dest, annotation, isFact);
        } else if (source.getClass() == FormulaAssertType.class) {
            //dest.append("% Formula " + EOL);
            traverse(((FormulaAssertType) source).getRulebase(), dest, annotation, isFact);
        } else if (source.getClass() == String.class) {
            if (((String) source).startsWith("\"")) {
                dest.append(((Object) source).toString());
            } else {
                dest.append("\"");
                dest.append(source.toString());
                dest.append("\"");
            }
        } else if (source.getClass() == Double.class ||
                source.getClass() == Integer.class
                ) {
            dest.append(((Object) source).toString());
        }
    }

    private static void equal(EqualType source, StringBuffer dest, String annotation) throws RuleTranslationException {
        if (source.getContent().size() == 2) {
            traverse(source.getContent().get(0), dest, annotation, false);
            dest.append(" = ");
            traverse(source.getContent().get(1), dest, annotation, false);
        } else {
            throw new RuleTranslationException("Equal should have two children but there are " +
                    source.getContent().size() + " children.");
        }
    }

    private static void and(AndInnerType source, StringBuffer dest, String annotation) throws RuleTranslationException {
        for (Object item : source.getFormulaOrOperatorOrAnd()) {
            traverse(item, dest, annotation, false);
            dest.append(", ");
        }
        dest.delete(dest.length() - 2, dest.length());
    }

    private static void atom(AtomType source, StringBuffer dest, String annotation) throws RuleTranslationException {
        atom(source, dest, annotation, false);
    }

    private static void atom(AtomType source, StringBuffer dest, String annotation, boolean isAnnotation) throws RuleTranslationException {
        if (source.getKeyref() != null && !source.getKeyref().isEmpty()) {
            property(dest, "keyref", source.getKeyref());
        }
        if (source.getIri() != null && !source.getIri().isEmpty()) {
            property(dest, "iri", source.getIri());
        }
        for (JAXBElement<?> item : source.getContent()) {
            if (item.getDeclaredType().equals(RelType.class)) {
                RelType rel = ((RelType) item.getValue());
                if (rel.getKeyref() != null && !rel.getKeyref().isEmpty()) {
                    //property(dest, "keyref", rel.getKeyref());
                }
                if (rel.getIri() != null && !rel.getIri().isEmpty()) {
                    //property(dest, "iri", rel.getIri());
                }
                if (isAnnotation) {
                    dest.append("@");
                }
                traverse(rel, dest, annotation, false);
                dest.append("(");
            } else {
                traverse(item.getValue(), dest, annotation, false);
                dest.append(", ");
            }
        }
        dest.delete(dest.length() - 2, dest.length());
        dest.append(")");
    }

    private static void property(StringBuffer str, String key, String value) {
        if (key.equals("iri")) {
            if (value.startsWith("http://www.w3.org/2003/11/swrlb#")) {
                value = value.replace("http://www.w3.org/2003/11/swrlb#", "swrlb:");
            } else if (value.startsWith("http://www.omg.org/spec/DMN/20151101/dmn.xsd#")) {
                value = value.replace("http://www.omg.org/spec/DMN/20151101/dmn.xsd#", "dmn:");
            }
        }
        str.append("@attr(\"" + key + "\", \"" + value + "\") ");
    }

    /**
     * implies has only two possible orders: and atom or meta and atom.
     *
     * @param source
     * @param dest
     * @param annotation
     * @throws RuleTranslationException
     */
    private static void implies(ImpliesType source, StringBuffer dest, String annotation)
            throws RuleTranslationException {
        dest.append(annotation);
        if (source.getContent().get(0).getDeclaredType().equals(AndInnerType.class) &&
                source.getContent().get(1).getDeclaredType().equals(AtomType.class)) {
            traverse(source.getContent().get(1), dest, annotation, false);
            dest.append(" :- ");
            traverse(source.getContent().get(0), dest, annotation, false);
            dest.append("." + EOL);
        } else if (source.getContent().get(0).getDeclaredType().equals(MetaType.class) &&
                source.getContent().get(1).getDeclaredType().equals(AndInnerType.class) &&
                source.getContent().get(2).getDeclaredType().equals(AtomType.class)) {
            atom(((MetaType) source.getContent().get(0).getValue()).getAtom(), dest, annotation, true);
            dest.append(EOL);
            traverse(source.getContent().get(2), dest, annotation, false);
            dest.append(" :- ");
            traverse(source.getContent().get(1), dest, annotation, false);
            dest.append("." + EOL);
        }
    }
}
