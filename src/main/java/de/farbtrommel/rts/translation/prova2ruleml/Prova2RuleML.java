package de.farbtrommel.rts.translation.prova2ruleml;

import de.farbtrommel.rts.translation.RuleTranslationException;
import de.farbtrommel.rts.translation.Util;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.ruleml.deliberation.*;
import ws.prova.parser.ProvaParser;

import javax.xml.bind.JAXBElement;

/**
 * Traversing ANTLR ParserTree and map to RuleML Object.
 */
public class Prova2RuleML {
    public static void runTranslation(ParseTree tree, RulebaseDispatcher dispatcher, QueryType query)
            throws RuleTranslationException {
        breadthFirstSearch(tree, dispatcher, query);
    }

    private static void breadthFirstSearch(ParseTree tree, RulebaseDispatcher dispatcher, QueryType query)
            throws RuleTranslationException {
        //System.out.println(tree.getClass() + " "  + tree.getText());
        if (tree.getClass() == ProvaParser.ClauseContext.class) {
            clause(tree, dispatcher, query);
        } else if (tree.getClass() == ProvaParser.QueryContext.class) {
            query(tree, dispatcher, query);
        } else if (tree.getClass() == ProvaParser.StatementContext.class) {
            for (int i = 0; i < tree.getChildCount(); i++) {
                if (tree.getChild(i).getClass() == ProvaParser.StatContext.class) {
                    breadthFirstSearch(tree.getChild(i).getChild(0), dispatcher, query);
                }
            }
        } else if (tree.getClass() == ProvaParser.RulebaseContext.class) {
            for (int i = 0; i < tree.getChildCount(); i++) {
                breadthFirstSearch(tree.getChild(i), dispatcher, query);
            }
        }


    }

    /**
     * {@code
     * \@anontation(val1, val2)
     * clause(val3, val4) :- pred(val5, val6), ... , pred(valN).
     * }
     *
     * @param tree
     * @param dispatcher
     * @param query
     */
    private static void clause(ParseTree tree, RulebaseDispatcher dispatcher, QueryType query) throws RuleTranslationException {
        boolean isFact = true;

        AndInnerType and = Util.RULEML_FACTORY.createAndInnerType();
        AtomType atom = Util.RULEML_FACTORY.createAtomType();
        ImpliesType implies = Util.RULEML_FACTORY.createImpliesType();
        String rulebaseId = "";
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getClass() == ProvaParser.MetadataContext.class) {
                for (int s = 0; s < tree.getChild(i).getChildCount(); s++) {
                    if (tree.getChild(i).getChild(s).getClass() == ProvaParser.AnnotationContext.class) {
                        rulebaseId = annotation(tree.getChild(i).getChild(s), implies, atom, rulebaseId);
                    }
                }
            } else if (tree.getChild(i).getClass() == ProvaParser.RelationContext.class) {
                relation(tree.getChild(i), atom, null);
            } else if (tree.getChild(i).getClass() == ProvaParser.LiteralsContext.class) {
                for (int s = 0; s < tree.getChild(i).getChildCount(); s++) {
                    if (tree.getChild(i).getChild(s).getClass() == ProvaParser.LiteralContext.class) {
                        literal(tree.getChild(i).getChild(s), and);
                    }
                }
            } else if (tree.getChild(i).getText().equals(":-")) {
                isFact = false;
            }
        }

        implies.getContent().add(Util.RULEML_FACTORY.createAnd(and));
        implies.getContent().add(Util.RULEML_FACTORY.createAtom(atom));

        if (isFact) {
            dispatcher.getRulebase(rulebaseId).getFormulaOrImpliesOrRule().add(atom);
        } else {
            dispatcher.getRulebase(rulebaseId).getFormulaOrImpliesOrRule().add(implies);
        }
    }

    /**
     * {@code
     * pred(val1, val2)
     * or
     * $var * 12
     * }
     *
     * @param tree
     * @param and
     */
    private static void literal(ParseTree tree, AndInnerType and) throws RuleTranslationException {
        AtomType atom = Util.RULEML_FACTORY.createAtomType();
        and.getFormulaOrOperatorOrAnd().add(atom);

        String iri = null;
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getClass() == ProvaParser.Semantic_attachmentContext.class) {
                operation(tree.getChild(i), and);
            } else if (tree.getChild(i).getClass() == ProvaParser.MetadataContext.class) {
                for (int s = 0; s < tree.getChild(i).getChildCount(); s++) {
                    if (tree.getChild(i).getChild(s).getClass() == ProvaParser.AnnotationContext.class) {
                        String tmp = annotation(tree.getChild(i).getChild(s), null, atom, null);
                        if (iri == null) {
                            iri = tmp;
                        }
                    }
                }
            } else if (tree.getChild(i).getClass() == ProvaParser.RelationContext.class) {
                relation(tree.getChild(i), atom, iri);
            }
        }
    }

    /**
     * {@code
     * 12 * 12 - 12 / 3
     * }
     *
     * @param tree
     * @param and
     */
    private static void operation(ParseTree tree, AndInnerType and) throws RuleTranslationException {
        //TODO: the left side can be an expression like a + 12 - d
        ProvaParser.Semantic_attachmentContext op = ((ProvaParser.Semantic_attachmentContext) tree);
        String operation = op.binary_operation().EQUAL().getText();
        if (operation.equals("=")) {
            EqualType equal = Util.RULEML_FACTORY.createEqualType();
            equal.getContent().add(term(op.binary_operation().left_term()));
            equal.getContent().add(term(op.binary_operation().getChild(2)));
            if (equal.getContent().size() > 0) {
                and.getFormulaOrOperatorOrAnd().add(equal);
            }
        }
    }

    private static void relation(ParseTree tree, AtomType atom, String iri) throws RuleTranslationException {
        JAXBElement<RelType> rel = (JAXBElement<RelType>) Util.create("rel", tree.getChild(0).getText());
        if (iri != null && !iri.isEmpty()) {
            rel.getValue().setIri(iri);
        }
        atom.getContent().add(rel);
        terms(tree.getChild(2).getChild(0), atom);
    }

    private static void terms(ParseTree tree, AtomType atom) throws RuleTranslationException {
        for (int i = 0; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getClass() == TerminalNodeImpl.class) {
                continue;
            }
            atom.getContent().add(term(tree.getChild(i)));
        }
    }

    private static JAXBElement<?> term(ParseTree tree) throws RuleTranslationException {
        if (tree.getChild(0).getClass() == ProvaParser.ValueContext.class ||
                //tree.getChild(0).getClass() == ProvaParser.ConstantContext.class ||
                tree.getChild(0).getClass() == ProvaParser.VariableContext.class
                ) {
            return Util.create("var", tree.getText());
        } else if (tree.getChild(0).getClass() == ProvaParser.StringContext.class) {
            //return Util.RULEML_FACTORY.createData(tree.getText());
            return Util.create("ind", removeQuotes(tree.getText()));
        } else if (tree.getChild(0).getClass() == ProvaParser.NumberContext.class) {
            return Util.RULEML_FACTORY.createData(Double.valueOf(tree.getText()));
        } else if (tree.getChild(0).getClass() == ProvaParser.Prova_listContext.class) {
            PlexType plex = Util.RULEML_FACTORY.createPlexType();
            ParseTree list = tree.getChild(1);
            for (int i = 0; i < tree.getChild(1).getChildCount(); i++) {
                plex.getContent().add(term(tree.getChild(1).getChild(i)));
            }
            return Util.RULEML_FACTORY.createData(Double.valueOf(tree.getText()));
        } else if (tree != null && tree.getChild(0) != null) {
            return term(tree.getChild(0));
        }
        return null;
    }

    /**
     * @param tree
     * @param implies
     * @param annotation
     * @return rulebase id
     * @throws RuleTranslationException
     */
    private static String annotation(ParseTree tree, ImpliesType implies, AtomType atom, String annotation) throws RuleTranslationException {
        if (tree.getChild(1).getText().equals("rulebase")) {
            return tree.getChild(3).getText();
        }
        if (tree.getChild(1).getText().equals("attr")) {
            //TODO: dirty hack, needed to be fixed. iri to Rel not to Atom
            if (implies == null && tree.getChild(3).getText().toLowerCase().equals("\"iri\"")) {
                return removeQuotes(tree.getChild(5).getText());
            }

            setAttribute(atom, tree.getChild(3).getText(), tree.getChild(5).getText());
            return annotation;
        }
        //Metadata can only add to implies.
        // When implies == null we are in body of an implies
        if (implies == null) {
            return "";
        }
        MetaType metaType = Util.RULEML_FACTORY.createMetaType();
        AtomType atomType = Util.RULEML_FACTORY.createAtomType();
        atomType.getContent().add(Util.create("rel", tree.getChild(1).getText()));
        for (int i = 3; i < tree.getChildCount(); i++) {
            if (tree.getChild(i).getClass() == ProvaParser.ValueContext.class) {
                atomType.getContent().add(Util.create("Ind", removeQuotes(tree.getChild(i).getText())));
            }
        }
        metaType.setAtom(atomType);
        implies.getContent().add(Util.RULEML_FACTORY.createMeta(metaType));

        return annotation;
    }

    private static String removeQuotes(String value) {
        return value.replaceAll("^\"", "").replaceAll("\"$", "");
    }

    private static void setAttribute(AtomType atom, String key, String value) {
        value = removeQuotes(value);
        if (key.toLowerCase().equals("\"iri\"")) {
            if (value.startsWith("dmn:")) {
                atom.setIri(value.replace("dmn:", "http://www.omg.org/spec/DMN/20151101/dmn.xsd#"));
            } else if (value.startsWith("swrlb:")) {
                atom.setIri(value.replace("swrlb:", "http://www.w3.org/2003/11/swrlb#"));
            }
        } else if (key.toLowerCase().equals("\"keyref\"")) {
            atom.setKeyref(value);
        }
    }

    private static void query(ParseTree tree, RulebaseDispatcher dispatcher, QueryType query) {

    }
}
