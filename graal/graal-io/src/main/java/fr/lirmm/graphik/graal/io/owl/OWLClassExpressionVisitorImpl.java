/**
 * 
 */
package fr.lirmm.graphik.graal.io.owl;

import java.util.Collections;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.Predicate;
import fr.lirmm.graphik.graal.core.SymbolGenerator;
import fr.lirmm.graphik.graal.core.Term;
import fr.lirmm.graphik.graal.io.owl.logic.Literal;
import fr.lirmm.graphik.graal.io.owl.logic.LogicalFormula;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 * 
 */
public class OWLClassExpressionVisitorImpl implements
		OWLClassExpressionVisitorEx<LogicalFormula> {

	private static final Logger logger = LoggerFactory
			.getLogger(OWLClassExpressionVisitorImpl.class);
	
	private Term glueVariable;
	private SymbolGenerator varGen;
	private DefaultPrefixManager prefixManager;

	public OWLClassExpressionVisitorImpl(DefaultPrefixManager prefixManager, SymbolGenerator varGen, Term glueVariable) {
		this.prefixManager = prefixManager;
		this.glueVariable = glueVariable;
		this.varGen = varGen;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public LogicalFormula visit(OWLClass arg) {
		Predicate p = this.createPredicate(arg);
		Atom a = this.createAtom(p, glueVariable);
		return this.createLogicalFormula(a);
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// OBJECT CLASS EXPRESSION
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public LogicalFormula visit(OWLObjectIntersectionOf arg) {
		LogicalFormula f = this.createLogicalFormula();
		for (OWLClassExpression c : arg.getOperands()) {
			try {
				f.and(c.accept(this));
			} catch (Exception e) {
			}
		}

		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectUnionOf arg) {
		LogicalFormula f = this.createLogicalFormula();

		for (OWLClassExpression c : arg.getOperands()) {
			try {
				f.or(c.accept(this));
			} catch (Exception e) {
			}
		}

		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectComplementOf arg) {
		LogicalFormula f = arg.getOperand().accept(this);
		f.not();

		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectSomeValuesFrom arg) {
		Term newGlueVariable = varGen.getFreeVar();
		
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, newGlueVariable));
		
		f.and(arg.getFiller().accept(
				new OWLClassExpressionVisitorImpl(this.prefixManager, varGen, newGlueVariable)));
		
		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectAllValuesFrom arg) {
		Term var = this.varGen.getFreeVar();
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, var));
		f.not();
		f.or(arg.getFiller().accept(new OWLClassExpressionVisitorImpl(this.prefixManager, varGen, var)));
		
		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectHasValue arg) {
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, createTerm(arg.getFiller())));
		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectMinCardinality arg) {
		if(arg.getCardinality() == 1) {
			Term newGlueVariable = varGen.getFreeVar();
			
			LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
					this.prefixManager, glueVariable, newGlueVariable));
			
			f.and(arg.getFiller().accept(
					new OWLClassExpressionVisitorImpl(this.prefixManager, varGen, newGlueVariable)));
			return f;
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("OWLObjectMinCardinality with cardinality other than 1 is not supported. This axioms was skipped : "
						+ arg);
			}
			return new LogicalFormula();
		}
	}

	@Override
	public LogicalFormula visit(OWLObjectExactCardinality arg) {
		if (logger.isWarnEnabled()) {
			logger.warn("OWLObjectExactCardinality is not supported. This axioms was skipped : "
					+ arg);
		}
		return new LogicalFormula();
	}

	@Override
	public LogicalFormula visit(OWLObjectMaxCardinality arg) {
		if (logger.isWarnEnabled()) {
			logger.warn("OWLObjectMaxCardinality is not supported. This axioms was skipped : "
					+ arg);
		}
		return new LogicalFormula();
	}

	@Override
	public LogicalFormula visit(OWLObjectHasSelf arg) {
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, glueVariable));
		return f;
	}

	@Override
	public LogicalFormula visit(OWLObjectOneOf arg) {
		LogicalFormula atomset = this.createLogicalFormula();
		for (OWLIndividual i : arg.getIndividuals()) {
			// atomset.add(atom)
		}
		return atomset;
	}

	// /////////////////////////////////////////////////////////////////////////
	// DATA CLASS EXPRESSION
	// /////////////////////////////////////////////////////////////////////////
	
	@Override
	public LogicalFormula visit(OWLDataSomeValuesFrom arg) {
		Term newGlueVariable = varGen.getFreeVar();
		
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, newGlueVariable));
		
		f.and(arg.getFiller().accept(
				new OWLDataRangeVisitorImpl()));
		return f;
	}

	@Override
	public LogicalFormula visit(OWLDataAllValuesFrom arg0) {
		// TODO implement this method
		throw new Error("This method isn't implemented");
	}

	@Override
	public LogicalFormula visit(OWLDataHasValue arg) {
		LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
				this.prefixManager, glueVariable, new Term(arg.getFiller().toString(), Term.Type.LITERAL)));
		return f;
	}

	@Override
	public LogicalFormula visit(OWLDataMinCardinality arg) {
		if(arg.getCardinality() == 1) {
			Term newGlueVariable = varGen.getFreeVar();
			
			LogicalFormula f = arg.getProperty().accept(new OWLPropertyExpressionVisitorImpl(
					this.prefixManager, glueVariable, newGlueVariable));
			
			f.and(arg.getFiller().accept(
					new OWLDataRangeVisitorImpl()));
			return f;
		} else {
			if (logger.isWarnEnabled()) {
				logger.warn("OWLObjectMinCardinality with cardinality other than 1 is not supported. This axioms was skipped : "
						+ arg);
			}
			return new LogicalFormula();
		}
	}

	@Override
	public LogicalFormula visit(OWLDataExactCardinality arg0) {
		// TODO implement this method
		throw new Error("This method isn't implemented");
	}

	@Override
	public LogicalFormula visit(OWLDataMaxCardinality arg0) {
		// TODO implement this method
		throw new Error("This method isn't implemented");
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// 
	// /////////////////////////////////////////////////////////////////////////
	
	private LogicalFormula createLogicalFormula() {
		return new LogicalFormula();
	}
	
	private LogicalFormula createLogicalFormula(Atom a) {
		return new LogicalFormula(new Literal(a, true));
	}

	/**
	 * 
	 * @param owlClass
	 * @return
	 */
	private Predicate createPredicate(OWLClassExpression owlClass) {
		Predicate predicate = null;
		if (!owlClass.isAnonymous()) {
			predicate = new Predicate(
					this.prefixManager.getShortForm(owlClass.asOWLClass().getIRI()), 1);
		} else {
			System.out.println("###" + owlClass);
			// this.tmpManageOWLClass(owlClass);
		}
		return predicate;
	}

	private Atom createAtom(Predicate p, Term... terms) {
		return new DefaultAtom(p, terms);
	}
	
	/**
	 * @param value
	 * @return
	 */
	private Term createTerm(OWLIndividual value) {
		return new Term(value.toString(), Term.Type.CONSTANT);
	}

}
