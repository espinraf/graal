/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2015)
 *
 * Contributors :
 *
 * Clément SIPIETER <clement.sipieter@inria.fr>
 * Mélanie KÖNIG
 * Swan ROCHER
 * Jean-François BAGET
 * Michel LECLÈRE
 * Marie-Laure MUGNIER <mugnier@lirmm.fr>
 *
 *
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 */
 package fr.lirmm.graphik.graal.backward_chaining.pure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import fr.lirmm.graphik.graal.api.core.Atom;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.core.factory.SubstitutionFactory;
import fr.lirmm.graphik.util.Partition;

class TermPartition extends Partition<Term> {

	/**
	 * return the subset of sep containing terms that are in the same class than
	 * existential variable
	 * 
	 * @param rule
	 * @return
	 */
	public LinkedList<Term> getStickyVariable(LinkedList<Term> sep, Rule rule) {
		// TODO faire mieux niveau optimisation
		LinkedList<Term> res = new LinkedList<Term>();
		for (Collection<Term> c : partition) {
			for (Term t : c)
				if (rule.getExistentials().contains(t))
					for (Term x : c)
						if (sep.contains(x))
							res.add(x);

		}
		return res;
	}

	/**
	 * Compute the substitution associated with the current partition this
	 * method computes a substitution by choosing one representative term by
	 * class. Choosing first constant then answer variable (in context) return
	 * null if the partition contain two constants in the same class
	 */
	public Substitution getAssociatedSubstitution(ConjunctiveQuery context) {
		Substitution substitution = SubstitutionFactory.instance()
				.createSubstitution();
		// we will choose a representant for all the equivalence set of the
		// partition
		for (Collection<Term> set : partition) {

			Iterator<Term> i = set.iterator();
			Term representant = i.next();
			while (i.hasNext()) {
				Term t = i.next();
				// t and the current representant are different
				if (!representant.equals(t)) {
					if (t.isConstant()) {
						// representant is a different constant
						if (representant.isConstant()) {
							return null;
						} else {
							representant = t;
						}
					}// the current representant is a variable
					else if (!representant.isConstant()) {
						// t is a variable from the answer
						if (context != null
								&& !context.getAnswerVariables().contains(
										representant)
								&& context.getAtomSet().getTerms()
										.contains(t)) {
							representant = t;
						}
					}
				} else { 
					i.remove();
				}
			}
			// all the terms in the equivalence set have as image the
			// representant of the equivalence set
			for (Term t : set)
				if (!t.equals(representant))
					substitution.put(t, representant);

		}
		return substitution;
	}

	/**
	 * Return false if a class of the receiving partition two constants, or
	 * contains two existential variable of R, or contains a constant and an
	 * existential variable of R, or contains an existential variable of R and a
	 * frontier variable of R
	 */
	public boolean isAdmissible(Rule rule) {
		Term cst = null;
		Term exist = null;
		Term fr = null;
		for (Collection<Term> cl : partition) {
			cst = null;
			exist = null;
			fr = null;
			Iterator<Term> i = cl.iterator();
			while (i.hasNext()) {
				Term t = i.next();
				if (t.isConstant()) {
					if (cst != null || exist != null)
						if (!t.equals(cst))
							return false;
						else
							i.remove();
					cst = t;
				}
				if (rule.getExistentials().contains(t)) {
					if (exist != null || cst != null || fr != null)
						if (!t.equals(exist))
							return false;
						else
							i.remove();
					exist = t;
				}
				if (rule.getFrontier().contains(t)) {
					if (exist != null)
						return false;
					fr = t;
				}
			}
		}
		return true;
	}

	/**
	 * Return the partition that unifies the terms of the two atoms return null
	 * if the two atoms have not the same predicate arity
	 */
	public static TermPartition getPartitionByPosition(Atom toUnif, Atom atom) {
		if (toUnif.getPredicate().getArity() != atom.getPredicate().getArity())
			return null;
		else {
			TermPartition p = new TermPartition();
			for (int i = 0; i < atom.getPredicate().getArity(); i++) {
				p.add(toUnif.getTerm(i), atom.getTerm(i));
			}
			return p;
		}
	}

	public TermPartition join(TermPartition p) {
		TermPartition res = new TermPartition();
		for (ArrayList<Term> cl : partition) {
			res.partition.add(new ArrayList<Term>(cl));
		}
		for (ArrayList<Term> cl : p.partition)
			res.addClass(cl);
		return res;
	}
}