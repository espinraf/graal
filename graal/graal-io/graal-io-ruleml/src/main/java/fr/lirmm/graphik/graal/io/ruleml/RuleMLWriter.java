/**
 * 
 */
package fr.lirmm.graphik.graal.io.ruleml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.core.Predicate;
import fr.lirmm.graphik.graal.core.Query;
import fr.lirmm.graphik.graal.core.Rule;
import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.term.Term;
import fr.lirmm.graphik.graal.core.term.Term.Type;
import fr.lirmm.graphik.graal.io.AbstractGraalWriter;
import fr.lirmm.graphik.util.Prefix;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public class RuleMLWriter extends AbstractGraalWriter {
	
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RuleMLWriter.class);

	private final String indentStyle;
	private transient int currentIndentSize = 0;

	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	// /////////////////////////////////////////////////////////////////////////

	public RuleMLWriter(Writer out) {
		super(out);
		indentStyle = "  ";
		init();
	}

	public RuleMLWriter() {
		this(new OutputStreamWriter(System.out));
	}
	
	public RuleMLWriter(OutputStream out) {
		this(new OutputStreamWriter(out));
	}
	
	public RuleMLWriter(File file) throws IOException {
		this(new FileWriter(file));
	}
	
	public RuleMLWriter(String path) throws IOException {
		 this(new FileWriter(path));
	}
	
	private void init() {
		try {
			this.writeln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			this.writeln("<?xml-model href=\"http://deliberation.ruleml.org/1.01/relaxng/datalogplus_min_relaxed.rnc\"?>");
			this.openBalise("RuleML xmlns=\"http://ruleml.org/spec\"");
			this.writeComment("This file was generated by Graal");
		} catch (IOException e) {
			LOGGER.error("Error on init RuleML file", e);
		}
	}

	// /////////////////////////////////////////////////////////////////////////
	// METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	@Override
	public void writeComment(String str) throws IOException {
		this.write("\n\n<!-- ");
		this.write(str);
		this.writeln(" -->");
	}

	@Override
	public void write(AtomSet atomset) throws IOException {
		this.openBalise("Assert");
		this.writeAtomSet(atomset);
		this.closeBaliseWithReturnLine("Assert");
	}
	
	@Override
	public void write(Rule rule) throws IOException {
		Set<Term> existVar = rule.getExistentials();
		Set<Term> universalVar = rule.getTerms(Type.VARIABLE);
		universalVar.removeAll(existVar);

		this.openBalise("Assert");
		this.openBalise("Forall");
		this.writeLabel(rule.getLabel());
		for (Term t : universalVar) {
			this.writeTerm(t);
		}
		this.openBalise("Implies");
		this.openBalise("if");
		this.openBalise("And");
		this.writeAtomSet(rule.getBody());
		this.closeBaliseWithReturnLine("And");
		this.closeBaliseWithReturnLine("if");
		this.openBalise("then");
		if (existVar.isEmpty()) {
			this.openBalise("And");
			this.writeAtomSet(rule.getHead());
			this.closeBaliseWithReturnLine("And");
		} else {
			this.openBalise("Exists");
			for (Term t : existVar) {
				this.writeTerm(t);
			}
			this.openBalise("And");
			this.writeAtomSet(rule.getHead());
			this.closeBaliseWithReturnLine("And");
			this.closeBaliseWithReturnLine("Exists");
		}
		this.closeBaliseWithReturnLine("then");
		this.closeBaliseWithReturnLine("Implies");
		this.closeBaliseWithReturnLine("Forall");
		this.closeBaliseWithReturnLine("Assert");
	}

	public void write(Query query) throws IOException {
		if (query instanceof ConjunctiveQuery) {
			this.write((ConjunctiveQuery)query);
		}
		else if (query instanceof Iterable) {
			for (Object q : (Iterable<?>)query) {
				if (q instanceof ConjunctiveQuery) {
					this.write((ConjunctiveQuery)q);
				}
			}
		}
	}
	
	@Override
	public void write(ConjunctiveQuery query) throws IOException {
		Set<Term> existVar = query.getAtomSet().getTerms(Term.Type.VARIABLE);
		existVar.removeAll(query.getAnswerVariables());

		this.openBalise("Query");
		if(!query.getLabel().isEmpty()) {
			this.writeLabel(query.getLabel());
		}
		this.openBalise("Exists");
		for (Term t : existVar) {
			this.writeTerm(t);
		}
		this.openBalise("And");
		this.writeAtomSet(query.getAtomSet());
		this.closeBaliseWithReturnLine("And");
		this.closeBaliseWithReturnLine("Exists");
		this.closeBaliseWithReturnLine("Query");
	}
	
	public void write(Prefix prefix) throws IOException {
		// this.writer.write("@prefix ");
		// this.writer.write(prefix.getPrefixName());
		// this.writer.write(" <");
		// this.writer.write(prefix.getPrefix());
		// this.writer.write(">\n");
	}

	// /////////////////////////////////////////////////////////////////////////
	// OVERRIDE METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public void close() throws IOException {
		this.closeBaliseWithReturnLine("RuleML");
		super.close();
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	protected void writeLabel(String label) throws IOException {
		if(!label.isEmpty()) {
			this.write("<!-- ");
			this.write(label);
			this.write(" -->");
		}
	}
	
	protected void writeAtomSet(Iterable<Atom> atomSet) throws IOException {
		for(Atom a : atomSet) {
			this.write(a);
		}
	}
	
	@Override
	protected void writeAtom(Atom atom) throws IOException {
		this.openBalise("Atom");
		this.writePredicate(atom.getPredicate());

		for (Term t : atom.getTerms()) {
			this.writeTerm(t);
		}
		this.closeBaliseWithReturnLine("Atom");
	}

	@Override
	protected void writeEquality(Term term, Term term2) throws IOException {
		this.openBalise("Equal");
		this.writeTerm(term);
		this.writeTerm(term2);
		this.closeBaliseWithReturnLine("Equal");
	}

	@Override
	protected void writeBottom() throws IOException {
		this.writeIndent();
		this.write("<Or/>");
	}

	protected void writeTerm(Term t) throws IOException {
		if(Type.VARIABLE.equals(t.getType())) {
			this.openBalise("Var");
			this.write(t.getIdentifier());
			this.closeBalise("Var");
		} else if(Type.CONSTANT.equals(t.getType())) {
			this.openBalise("Ind");
			this.write(t.getIdentifier());
			this.closeBalise("Ind");
		} else { // LITERAL
			this.openBalise("Data");
			this.write(t.getIdentifier());
			this.closeBalise("Data");
		}
	}
	
	protected void writePredicate(Predicate p) throws IOException {
		this.openBalise("Rel");
		this.write(p.getIdentifier());
		this.closeBalise("Rel");
	}
	
	////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

	private void incrIndent() {
		++this.currentIndentSize;
	}

	private void decrIndent() {
		--this.currentIndentSize;
	}

	private void writeIndent() throws IOException {
		this.write("\n");
		for (int i = 0; i < this.currentIndentSize; ++i) {
			this.write(indentStyle);
		}
	}

	private void openBalise(String baliseName) throws IOException {
		this.writeIndent();
		this.write('<');
		this.write(baliseName);
		this.write('>');
		this.incrIndent();
	}

	private void closeBalise(String baliseName) throws IOException {
		this.decrIndent();
		this.write("</");
		this.write(baliseName);
		this.write('>');
	}

	private void closeBaliseWithReturnLine(String baliseName)
			throws IOException {
		this.decrIndent();
		this.writeIndent();
		this.write("</");
		this.write(baliseName);
		this.write(">");
	}

	// /////////////////////////////////////////////////////////////////////////
	// STATIC METHODS
	////////////////////////////////////////////////////////////////////////////

	public static String writeToString(Object o) {
		StringWriter s = new StringWriter();
		RuleMLWriter w = new RuleMLWriter(s);
		try {
			w.write(o);
			w.close();
		} catch (IOException e) {
			
		}
		return s.toString();
	}
	
};

