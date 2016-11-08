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
package fr.lirmm.graphik.graal.homomorphism.bbc;

import java.util.Map;

import fr.lirmm.graphik.graal.api.core.Variable;
import fr.lirmm.graphik.graal.homomorphism.Var;
import fr.lirmm.graphik.graal.homomorphism.backjumping.BackJumping;
import fr.lirmm.graphik.util.AbstractProfilable;

class BCCBackJumping extends AbstractProfilable implements BackJumping {

	/**
	 * 
	 */
    private final BCC BCC;
	private BackJumping bc;

	BCCBackJumping(BCC bcc, BackJumping bc) {
		BCC = bcc;
		this.bc = bc;
	}

	@Override
	public void init(Var[] vars, Map<Variable, Var> map) {
		this.bc.init(vars, map);
	}

	@Override
	public int previousLevel(Var var, Var[] vars) {
		if (BCC.varData[var.level].isEntry && !vars[BCC.varData[var.level].previousLevelFailure].success) {
			if (BCC.varData[BCC.varData[var.level].previousLevelFailure].forbidden != null) {
				BCC.varData[BCC.varData[var.level].previousLevelFailure].forbidden.add(vars[BCC.varData[var.level].previousLevelFailure].image);
			}
			this.getProfiler().incr("#BCCBackjumps", 1);
			return BCC.varData[var.level].previousLevelFailure;
		} else {
			return this.bc.previousLevel(var, vars);
		}
	}

}