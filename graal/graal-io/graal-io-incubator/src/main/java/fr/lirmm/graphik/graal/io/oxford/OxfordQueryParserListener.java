/**
 * 
 */
package fr.lirmm.graphik.graal.io.oxford;

import java.util.LinkedList;

import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.Predicate;
import fr.lirmm.graphik.graal.core.Term;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
class OxfordQueryParserListener {
	
	private enum State {
		HEAD, BODY
	}
	
	private DefaultConjunctiveQuery cquery = null;
	private State state;
	private LinkedList<Term> awsweredVariables = new LinkedList<Term>();
	private LinkedListAtomSet body = new LinkedListAtomSet();

	private LinkedList<Term> termsOfCurrentAtom = null;
	private String predicateLabelOfCurrentAtom = null;

	public DefaultConjunctiveQuery getQuery() {
		return this.cquery;
	}
	
	public void startQuery() {
		this.state = State.HEAD;
	}
	

	public void endOfQuery() {
		this.cquery = new DefaultConjunctiveQuery(this.body, this.awsweredVariables);
	}
	

	public void startBody() {
		this.state = State.BODY;
	}
	

	public void startAtom() {
		this.termsOfCurrentAtom = new LinkedList<Term>();
	}
	

	public void endOfAtom() {
		Predicate predicate = new Predicate(this.predicateLabelOfCurrentAtom, this.termsOfCurrentAtom.size());
		Atom atom = new DefaultAtom(predicate, this.termsOfCurrentAtom);
		this.body.add(atom);
	}
	

	public void predicate(String label) {
		this.predicateLabelOfCurrentAtom = label;
	}


	public void constant(String label) {
		Term term = new Term(label, Term.Type.CONSTANT);
		switch(state) {
		case HEAD:
			this.awsweredVariables.add(term);
			break;
		case BODY:
			this.termsOfCurrentAtom.add(term);
		}
	}


	public void variable(String label) {
		Term term = new Term(label, Term.Type.VARIABLE);
		switch(state) {
		case HEAD:
			this.awsweredVariables.add(term);
			break;
		
		case BODY:
			this.termsOfCurrentAtom.add(term);
			break;
		}
	}



}