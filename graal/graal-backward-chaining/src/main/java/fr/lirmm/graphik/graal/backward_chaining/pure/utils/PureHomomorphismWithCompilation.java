/**
 * 
 */
package fr.lirmm.graphik.graal.backward_chaining.pure.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.lirmm.graphik.graal.backward_chaining.pure.rules.RulesCompilation;
import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.atomset.ReadOnlyAtomSet;
import fr.lirmm.graphik.graal.homomorphism.HomomorphismException;
import fr.lirmm.graphik.graal.homomorphism.PureHomomorphism;

/**
 * @author Clément Sipieter (INRIA) {@literal <clement@6pi.fr>}
 *
 */
public class PureHomomorphismWithCompilation extends PureHomomorphism {

	private static final Logger logger = LoggerFactory
			.getLogger(PureHomomorphismWithCompilation.class);
	
	private static PureHomomorphismWithCompilation instance;

	protected PureHomomorphismWithCompilation() {
		super();
	}

	public static synchronized PureHomomorphismWithCompilation getInstance() {
		if (instance == null)
			instance = new PureHomomorphismWithCompilation();

		return instance;
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	// /////////////////////////////////////////////////////////////////////////
	
	/**
	 * return true iff exist an homomorphism from the query to the fact else
	 * return false
	 */
	public boolean exist(ReadOnlyAtomSet source, ReadOnlyAtomSet target, RulesCompilation compilation)
			throws HomomorphismException {

		Homomorphism homomorphism = new Homomorphism();
		homomorphism.compilation = compilation;

		// check if the query is empty
		if (source == null || !source.iterator().hasNext()) {
			if (logger.isInfoEnabled()) {
				logger.info("Empty query");
			}
			return true;
		}

		// /////////////////////////////////////////////////////////////////////
		// Initialisation
		if (!initialiseHomomorphism(homomorphism, source, target))
			return false;

		return backtrack(homomorphism);
	}
	
	@Override
	protected boolean isMappable(Atom a, Atom im, PureHomomorphism.Homomorphism homomorphism) {
		if(((Homomorphism) homomorphism).compilation != null){
			return ((Homomorphism) homomorphism).compilation.isMappable(a, im);
		}
		else {
			return a.getPredicate().equals(im.getPredicate());
		}
	}
	
	// /////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASS
	// /////////////////////////////////////////////////////////////////////////

	protected static class Homomorphism extends PureHomomorphism.Homomorphism {
		RulesCompilation compilation = null;
	}
}