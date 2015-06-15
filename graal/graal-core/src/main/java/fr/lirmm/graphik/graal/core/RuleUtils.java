/* Graal v0.7.4
 * Copyright (c) 2014-2015 Inria Sophia Antipolis - Méditerranée / LIRMM (Université de Montpellier & CNRS)
 * All rights reserved.
 * This file is part of Graal <https://graphik-team.github.io/graal/>.
 *
 * Author(s): Clément SIPIETER
 *            Mélanie KÖNIG
 *            Swan ROCHER
 *            Jean-François BAGET
 *            Michel LECLÈRE
 *            Marie-Laure MUGNIER
 */
 /**
 * 
 */
package fr.lirmm.graphik.graal.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.atomset.InMemoryAtomSet;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public final class RuleUtils {

	private RuleUtils() {
	}

	/**
	 * 
	 * @param rule
	 * @return true if and only if the body of the specified rule contains only
	 *         one atom.
	 */
	public static boolean hasAtomicBody(Rule rule) {
		Iterator<Atom> it = rule.getBody().iterator();
		boolean res = it.hasNext();
		it.next();
		return res && !it.hasNext();
	}

	/**
	 * 
	 * @param rule
	 * @return true if and only if the head of the specified rule contains only
	 *         one atom.
	 */
	public static boolean hasAtomicHead(Rule rule) {
		Iterator<Atom> it = rule.getHead().iterator();
		boolean res = it.hasNext();
		it.next();
		return res && !it.hasNext();
	}

	/**
	 * 
	 * @param rules
	 *            a set of rules
	 * @return The equivalent set of mono-piece rules.
	 */
	public static Iterator<Rule> computeMonoPiece(Iterator<Rule> rules) {
		return new MonoPieceRulesIterator(rules);
	}

	public static Collection<Rule> computeMonoPiece(Rule rule) {
		String label = rule.getLabel();
		Collection<Rule> monoPiece = new LinkedList<Rule>();

		if (label.isEmpty()) {
			for (AtomSet piece : rule.getPieces()) {
				monoPiece.add(new DefaultRule(rule.getBody(), piece));
			}
		} else {
			int i = -1;
			for (InMemoryAtomSet piece : rule.getPieces()) {
				monoPiece.add(new DefaultRule(label + "-p" + ++i, rule
						.getBody(), piece));
			}
		}

		return monoPiece;
	}

	private static class MonoPieceRulesIterator implements Iterator<Rule> {

		Iterator<Rule> it;
		Queue<Rule> currentMonoPiece = new LinkedList<Rule>();
		Rule currentRule;

		MonoPieceRulesIterator(Iterator<Rule> iterator) {
			this.it = iterator;
		}

		@Override
		public boolean hasNext() {
			return !currentMonoPiece.isEmpty() || it.hasNext();
		}

		@Override
		public Rule next() {
			if (currentMonoPiece.isEmpty()) {
				currentRule = it.next();
				currentMonoPiece
						.addAll(RuleUtils.computeMonoPiece(currentRule));
			}
			return currentMonoPiece.poll();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
}