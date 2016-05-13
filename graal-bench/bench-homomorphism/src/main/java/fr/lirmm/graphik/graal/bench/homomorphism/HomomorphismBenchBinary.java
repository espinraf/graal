/*
 * Copyright (C) Inria Sophia Antipolis - Méditerranée / LIRMM
 * (Université de Montpellier & CNRS) (2014 - 2016)
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
package fr.lirmm.graphik.graal.bench.homomorphism;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.lang3.tuple.ImmutablePair;

import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.ConjunctiveQuery;
import fr.lirmm.graphik.graal.api.core.InMemoryAtomSet;
import fr.lirmm.graphik.graal.api.core.Predicate;
import fr.lirmm.graphik.graal.api.core.Query;
import fr.lirmm.graphik.graal.api.core.Rule;
import fr.lirmm.graphik.graal.api.core.Substitution;
import fr.lirmm.graphik.graal.api.core.Term;
import fr.lirmm.graphik.graal.bench.core.AbstractGraalBench;
import fr.lirmm.graphik.graal.core.DefaultAtom;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphAtomSet;
import fr.lirmm.graphik.graal.core.term.DefaultTermFactory;
import fr.lirmm.graphik.graal.homomorphism.BacktrackHomomorphism;
import fr.lirmm.graphik.graal.io.dlp.DlgpParser;
import fr.lirmm.graphik.util.DefaultProfiler;
import fr.lirmm.graphik.util.stream.AbstractIterator;
import fr.lirmm.graphik.util.stream.CloseableIterator;
import fr.lirmm.graphik.util.stream.CloseableIteratorAdapter;
import fr.lirmm.graphik.util.stream.converter.Converter;
import fr.lirmm.graphik.util.stream.converter.ConverterCloseableIterator;
import fr.lirmm.graphik.util.stream.filter.Filter;
import fr.lirmm.graphik.util.stream.filter.FilterCloseableIterator;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class HomomorphismBenchBinary extends AbstractGraalBench {



	// /////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	// /////////////////////////////////////////////////////////////////////////

	private Random                       rand;

	private static final Predicate[] PREDICATES             = { new Predicate("p", 2), new Predicate("p2", 2),
	        new Predicate("p3", 2), new Predicate("q", 2), new Predicate("q2", 2), new Predicate("q3", 2),
	        new Predicate("r", 2), new Predicate("r2", 2), new Predicate("r3", 2), new Predicate("s", 2),
	        new Predicate("s2", 2), new Predicate("s3", 2) };

	private int                      domainSize             = 32;
	private float                    domainIncreaseFactor   = 1f;

	private int                      minInstanceSize        = 50;
	private int                      maxInstanceSize        = 102400;
	private float                    instanceIncreaseFactor = 2f;


	/**
	 * A CompilationBench with a seed equals to 0.
	 */
	protected HomomorphismBenchBinary() {
		this(0);
	}

	/**
	 * 
	 * @param seed
	 *            the seed for random generation of data.
	 */
	public HomomorphismBenchBinary(int seed) {
		this.rand = new Random(0);
	}

	// /////////////////////////////////////////////////////////////////////////
	// GETTERS/SETTERS
	// /////////////////////////////////////////////////////////////////////////


	/**
	 * Set the domain size, if the value of domainSize is < 0 then the domain
	 * size will be increase with the instance size.
	 * 
	 * @param domainSize
	 */
	public void setDomainSize(int domainSize) {
		this.domainSize = domainSize;
	}

	/**
	 * @return the domainSize
	 */
	public int getDomainSize() {
		return domainSize;
	}

	/**
	 * @return the domainIncreaseFactor
	 */
	public float getDomainIncreaseFactor() {
		return domainIncreaseFactor;
	}

	/**
	 * @param domainIncreaseFactor
	 *            the domainIncreaseFactor to set
	 */
	public void setDomainIncreaseFactor(float domainIncreaseFactor) {
		this.domainIncreaseFactor = domainIncreaseFactor;
	}

	/**
	 * @return the minInstanceSize
	 */
	public int getMinInstanceSize() {
		return minInstanceSize;
	}

	/**
	 * @param minInstanceSize
	 *            the minInstanceSize to set
	 */
	public void setMinInstanceSize(int minInstanceSize) {
		this.minInstanceSize = minInstanceSize;
	}

	/**
	 * @return the maxInstanceSize
	 */
	public int getMaxInstanceSize() {
		return maxInstanceSize;
	}

	/**
	 * @param maxInstanceSize
	 *            the maxInstanceSize to set
	 */
	public void setMaxInstanceSize(int maxInstanceSize) {
		this.maxInstanceSize = maxInstanceSize;
	}

	/**
	 * @return the instanceIncreaseFactor
	 */
	public float getInstanceIncreaseFactor() {
		return instanceIncreaseFactor;
	}

	/**
	 * @param instanceIncreaseFactor
	 *            the instanceIncreaseFactor to set
	 */
	public void setInstanceIncreaseFactor(float instanceIncreaseFactor) {
		this.instanceIncreaseFactor = instanceIncreaseFactor;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////

	@Override
	public CloseableIterator<Map.Entry<String, Query>> getQueries() {
		DlgpParser dlgpParser = new DlgpParser(
		                                       "[ATOMIC] ?(X1,X2) :- p(X1,X2). "
		                                               + "[FC0] ?(X,Y,Z) :- p(X,Y), q(X,Z), r(Y,Z)."
		                                               + "[FC1] ?(X,Y,Z) :- p0(X,Y), q(X,Z), r(Y,Z)."
		                                               + "[FC2] ?(X,Y,Z) :- p(X,Y), q0(X,Z), r(Y,Z)."
		                                               + "[STAR3-SIMPLE] ?(X1,X2,X3,X4) :- p(X1,X2), q(X1,X3), r(X1,X4)."
		                                               + "[2-STAR3-SIMPLE] ?(X1,X2,X3,X4) :- p(X1,X2), q(X1,X3), r(X1,X4), p2(X4,X2), p2(X4,X5),p2(X4,X6)."
		                                               + "[DOUBLE-3CIRCUIT-SIMPLE] ?(X1,X2,X3) :- p(X1,X2), q(X2,X3), r(X3, X1), p(X3,X2), q(X2,X1), r(X1,X3). "
		                                               + "[DOUBLE-3CIRCUIT] ?(X1,X2,X3,X4) :- p(X1,X2), q(X2,X3), r(X3, X1), p(X3,X2), q(X2,X1), r(X1,X3), s(X3,X4). "
		                                               + "[DOUBLE-4CIRCUIT-SIMPLE] ?(X5,X6,X7,X8) :- p(X5,X6), q(X6,X7), r(X7,X8), s(X8,X5), p(X8,X7), q(X7,X6), r(X6,X5), s(X5,X8). "
		                                               + "[DOUBLE-4CIRCUIT] ?(X5,X6,X7,X8) :- p(X5,X6), q(X6,X7), r(X7,X8), s(X8,X5), p(X8,X7), q(X7,X6), r(X6,X5), s(X5,X8), p2(X8,X9), q2(X9,X10), r2(X10,X8), s2(X10,X11). "
		                                               + "[DOUBLE-4CIRCUIT-BCC] ?(B,G,D,E) :- p(B,G), q(G,D), r(D,E), s(E,B), p(E,D), q(D,G), r(G,B), s(B,E), p2(E,A), q2(A,F), r2(F,E), s2(F,C). ");
		return new ConverterCloseableIterator<Query, Map.Entry<String, Query>>(
				  new CloseableIteratorAdapter<Query>(
						  	new FilterCloseableIterator<Object, Query>(dlgpParser, new Filter<Object>() {
								@Override
								public boolean filter(Object o) {
									return o instanceof Query;
								}
		                                                                                                                                                      })),
				  new Converter<Query, Map.Entry<String, Query>>() {
		          
			      @Override
			      public Entry<String, Query> convert(
			          Query object) {
			          return new ImmutablePair<String, Query>(
				                                                                                                               ((ConjunctiveQuery) object).getLabel(),
			                                                  object);
			      }
		          
				  });
		
	}

	@Override
	public CloseableIterator<Rule> getOntology() {
		return new CloseableIteratorAdapter<Rule>(Collections.<Rule> emptyIterator());
	}

	@Override
	public CloseableIterator<Map.Entry<String, AtomSet>> getInstances() {

		return new CloseableIteratorAdapter<Map.Entry<String, AtomSet>>(
		                                                                new AbstractIterator<Map.Entry<String, AtomSet>>() {

			                                                                InMemoryAtomSet s       = new DefaultInMemoryGraphAtomSet();
			                                                                int             nbAtoms = minInstanceSize;
			                                                                int             domain  = domainSize;
			                                                                int             realNbAtoms = 0;

			                                                                {
				                                                                addNAtoms(s, nbAtoms);
			                                                                }

			                                                                @Override
			                                                                public boolean hasNext() {
				                                                                return (nbAtoms * instanceIncreaseFactor) <= maxInstanceSize;
			                                                                }

			                                                                @Override
			                                                                public Entry<String, AtomSet> next() {
				                                                                addNAtoms(s, nbAtoms);
				                                                                nbAtoms *= instanceIncreaseFactor;
				                                                                domain *= domainIncreaseFactor;
				                                                                return new ImmutablePair<String, AtomSet>(
				                                                                                                          Integer.toString(realNbAtoms),
				                                                                                                          s);

			                                                                }

			                                                                private void addNAtoms(InMemoryAtomSet to,
			                                                                    int n) {
				                                                                for (int i = 0; i < n; ++i) {
					                                                                int p = rand.nextInt(PREDICATES.length);
					                                                                List<Term> terms = new LinkedList<Term>();
					                                                                for (int j = 0; j < PREDICATES[p].getArity(); ++j) {
						                                                                terms.add(DefaultTermFactory.instance()
						                                                                                            .createConstant(
						                                                                                                rand.nextInt(domain)));
					                                                                }
					                                                                if (to.add(new DefaultAtom(
					                                                                                       PREDICATES[p],
					                                                                                           terms))) {
						                                                                ++realNbAtoms;
					                                                                }
				                                                                }
			                                                                }

		                                                                });
	}

	@Override
	public void run() {

		Query q = this.getQuery();
		AtomSet atomset = this.getAtomSet();
		Object o = this.getExtra();

		DefaultProfiler profiler = new DefaultProfiler();
		BacktrackHomomorphism h = (BacktrackHomomorphism) o;
		h.setProfiler(profiler);

		try {

			profiler.start("totalTime");
			CloseableIterator<Substitution> it = h.execute((ConjunctiveQuery) q, atomset);
			int i = 0;
			while (it.hasNext()) {
				it.next();
				++i;
			}
			it.close();
			profiler.stop("totalTime");

			profiler.put("nbResults", i);

		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setResults(profiler.entrySet().iterator());
	}

	/**
	 * @param nbAtomsMax
	 */
	public void setNbAtomsMax(int nbAtomsMax) {
		this.maxInstanceSize = nbAtomsMax;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	// /////////////////////////////////////////////////////////////////////////

}