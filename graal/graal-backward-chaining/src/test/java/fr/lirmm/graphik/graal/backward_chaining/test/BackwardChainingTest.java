package fr.lirmm.graphik.graal.backward_chaining.test;

/**
 * 
 */

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import fr.lirmm.graphik.graal.backward_chaining.BackwardChainer;
import fr.lirmm.graphik.graal.backward_chaining.PureRewriter;
import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.DefaultConjunctiveQuery;
import fr.lirmm.graphik.graal.core.Predicate;
import fr.lirmm.graphik.graal.core.Rule;
import fr.lirmm.graphik.graal.core.Term;
import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.factory.AtomSetFactory;
import fr.lirmm.graphik.graal.core.factory.RuleFactory;
import fr.lirmm.graphik.graal.core.ruleset.LinkedListRuleSet;
import fr.lirmm.graphik.graal.core.ruleset.RuleSet;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 * 
 */
public class BackwardChainingTest {

	private static Predicate p = new Predicate("p", 2);
	private static Predicate q = new Predicate("q", 2);

	private static final Term x = new Term("X", Term.Type.VARIABLE);
	private static final Term y = new Term("Y", Term.Type.VARIABLE);
	private static final Term z = new Term("Z", Term.Type.VARIABLE);
	// private static final Term u = new Term("U", Term.Type.VARIABLE);
	// private static final Term v = new Term("V", Term.Type.VARIABLE);
	// private static final Term w = new Term("w", Term.Type.VARIABLE);
	//
	// private static final Term a = new Term("a", Term.Type.CONSTANT);
	// private static final Term b = new Term("b", Term.Type.CONSTANT);
	//
	private static Atom p_xy, q_yx, q_yz;

	static {
		Term[] terms = new Term[2];
		terms[0] = x;
		terms[1] = y;
		p_xy = new DefaultAtom(p, Arrays.asList(terms));

		terms = new Term[2];
		terms[0] = y;
		terms[1] = x;
		q_yx = new DefaultAtom(q, Arrays.asList(terms));

		terms = new Term[2];
		terms[0] = y;
		terms[1] = z;
		q_yz = new DefaultAtom(q, Arrays.asList(terms));
	}

	/**
	 * folding on answer variables
	 */
	@Test
	public void forbiddenFoldingTest() {
		RuleSet rules = new LinkedListRuleSet();

		Rule rule = RuleFactory.getInstance().createRule();
		rule.getBody().add(p_xy);
		rule.getHead().add(q_yx);
		rules.add(rule);

		rule = RuleFactory.getInstance().createRule();
		rule.getBody().add(q_yx);
		rule.getHead().add(p_xy);
		rules.add(rule);

		AtomSet atomset = AtomSetFactory.getInstance().createAtomSet();
		atomset.add(p_xy);
		atomset.add(q_yz);
		ConjunctiveQuery query = new DefaultConjunctiveQuery(atomset);

		BackwardChainer bc = new PureRewriter(query, rules);
		int i = 0;
		while (bc.hasNext()) {
			++i;
			System.out.println(bc.next());
		}
		Assert.assertEquals(4, i);
	}
}