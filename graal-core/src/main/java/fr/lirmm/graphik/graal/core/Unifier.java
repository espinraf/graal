/**
 * 
 */
package fr.lirmm.graphik.graal.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.factory.SubstitutionFactory;
import fr.lirmm.graphik.util.LinkedSet;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class Unifier {
	
	private Unifier(){
	};
	
	public static Collection<Substitution> computePieceUnifier(Rule rule, AtomSet atomset) {
		Collection<Substitution> unifiers = new LinkedSet<Substitution>();
		for (Atom a1 : atomset) {
			unifiers.addAll(extendUnifier(rule, atomset, a1,
					new TreeMapSubstitution()));
		}
		return unifiers;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE FUNCTIONS
	// /////////////////////////////////////////////////////////////////////////

	private static Collection<Substitution> extendUnifier(Rule rule, AtomSet atomset,
			Atom pieceElement, Substitution unifier) {
		Collection<Substitution> unifierCollection = new LinkedList<Substitution>();
		Set<Term> frontierVars = rule.getFrontier();
		Set<Term> existentialVars = rule.getExistentials();
		
		for (Atom atom : rule.getHead()) {
			Substitution u = unifier(unifier, pieceElement, atom,
					frontierVars, existentialVars);
			if (u != null) {
				boolean isExtended = false;
				for (Atom newPieceElement : atomset) {
					boolean containsExist = false;
					boolean containsNonAffected = false;

					// test if exist a var substitut by an existential and a var
					// not already substitut
					for (Term t : newPieceElement) {
						if (existentialVars.contains(u.getSubstitute(t))) {
							containsExist = true;
						} else if (Term.Type.VARIABLE.equals(t.getType())
								&& u.getSubstitute(t).equals(t)) {
							containsNonAffected = true;
						}
					}

					if (containsExist && containsNonAffected) {
						isExtended = true;
						unifierCollection.addAll(extendUnifier(rule, atomset,
								newPieceElement, u));
					}
				}

				if (!isExtended) {
					unifierCollection.add(u);
				}
			}
		}
		return unifierCollection;
	}

	private static Substitution unifier(Substitution baseUnifier, Atom a1,
			Atom atomFromHead, Set<Term> frontierVars, Set<Term> existentialVars) {
		if (a1.getPredicate().equals(atomFromHead.getPredicate())) {
			boolean error = false;
			Substitution u = SubstitutionFactory.getInstance()
					.createSubstitution();
			u.put(baseUnifier);
			for (int i = 0; i < a1.getPredicate().getArity(); ++i) {
				Term t1 = a1.getTerm(i);
				Term t2 = atomFromHead.getTerm(i);
				if (!t1.equals(t2)) {
					if (Term.Type.VARIABLE.equals(t1.getType())) {
						if (!compose(u, frontierVars, existentialVars, t1, t2))
							error = true;
					} else if (Term.Type.VARIABLE.equals(t2.getType())
							&& !existentialVars.contains(t2)) {
						if (!compose(u, frontierVars, existentialVars, t2, t1))
							error = true;
					}
				}
			}

			if (!error)
				return u;
		}

		return null;
	}

	private static boolean compose(Substitution u, Set<Term> frontierVars,
			Set<Term> existentials, Term term, Term substitut) {
		term = u.getSubstitute(term);
		substitut = u.getSubstitute(substitut);

		if (Term.Type.CONSTANT.equals(term.getType())
				|| existentials.contains(term)) {
			Term tmp = term;
			term = substitut;
			substitut = tmp;
		}

		for (Term t : u.getTerms()) {
			if (term.equals(u.getSubstitute(t))) {
				if (!put(u, frontierVars, existentials, t, substitut)) {
					return false;
				}
			}
		}

		if (!put(u, frontierVars, existentials, term, substitut)) {
			return false;
		}
		return true;
	}

	private static boolean put(Substitution u, Set<Term> frontierVars,
			Set<Term> existentials, Term term, Term substitut) {
		if (!term.equals(substitut)) {
			// two (constant | existentials vars)
			if (Term.Type.CONSTANT.equals(term.getType()) || existentials.contains(term)) {
				return false;
				// fr -> existential vars
			} else if (frontierVars.contains(term) && existentials.contains(substitut)) {
				return false;
			}
		}
		return u.put(term, substitut);
	}
}
