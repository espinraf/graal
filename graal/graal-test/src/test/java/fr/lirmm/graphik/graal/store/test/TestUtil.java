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
/**
 * 
 */
package fr.lirmm.graphik.graal.store.test;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;

import fr.lirmm.graphik.graal.api.core.AtomSet;
import fr.lirmm.graphik.graal.api.core.AtomSetException;
import fr.lirmm.graphik.graal.api.homomorphism.Homomorphism;
import fr.lirmm.graphik.graal.api.store.TripleStore;
import fr.lirmm.graphik.graal.core.atomset.LinkedListAtomSet;
import fr.lirmm.graphik.graal.core.atomset.graph.DefaultInMemoryGraphAtomSet;
import fr.lirmm.graphik.graal.homomorphism.BacktrackHomomorphism;
import fr.lirmm.graphik.graal.homomorphism.RecursiveBacktrackHomomorphism;
import fr.lirmm.graphik.graal.homomorphism.StaticHomomorphism;
import fr.lirmm.graphik.graal.homomorphism.backjumping.GraphBaseBackJumping;
import fr.lirmm.graphik.graal.homomorphism.bbc.BCC;
import fr.lirmm.graphik.graal.homomorphism.bootstrapper.StarBootstrapper;
import fr.lirmm.graphik.graal.homomorphism.forward_checking.NFC0;
import fr.lirmm.graphik.graal.homomorphism.forward_checking.NFC2;
import fr.lirmm.graphik.graal.homomorphism.forward_checking.NFC2WithLimit;
import fr.lirmm.graphik.graal.homomorphism.forward_checking.SimpleFC;
import fr.lirmm.graphik.graal.store.gdb.BlueprintsGraphDBStore;
import fr.lirmm.graphik.graal.store.gdb.Neo4jStore;
import fr.lirmm.graphik.graal.store.rdbms.adhoc.AdHocRdbmsStore;
import fr.lirmm.graphik.graal.store.rdbms.driver.HSQLDBDriver;
import fr.lirmm.graphik.graal.store.rdbms.natural.NaturalRDBMSStore;
import fr.lirmm.graphik.graal.store.triplestore.JenaStore;
import fr.lirmm.graphik.graal.store.triplestore.SailStore;

/**
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 *
 */
public final class TestUtil {

	private TestUtil() {
	}

	public static final String PLAIN_TABLE_RDBMS_TEST = "plainTable";
	public static final String DEFAULT_RDBMS_TEST = "default";

	public static final String JENA_TEST;
	public static final String NEO4J_TEST;
	static {
		File jena;
		File neo4j;
		try {
			jena = File.createTempFile("jena-test", "db");
			neo4j = File.createTempFile("neo4j-test", "db");
		} catch (IOException e) {
			throw new Error(e);
		}
		rm(neo4j);
		rm(jena);
		JENA_TEST = jena.getAbsolutePath();
		NEO4J_TEST = neo4j.getAbsolutePath();

	}

	public static AdHocRdbmsStore defaultRDBMSStore = null;
	public static NaturalRDBMSStore plainTableRDBMSStore = null;
	public static BlueprintsGraphDBStore graphStore = null;
	public static JenaStore jenaStore = null;
	public static Neo4jStore neo4jStore = null;
	public static SailStore sailStore = null;

	public static Homomorphism[] getHomomorphisms() {

		BCC bcc0 = new BCC();
		BCC bcc1 = new BCC(new GraphBaseBackJumping(), false);
		BCC bcc2 = new BCC(new GraphBaseBackJumping(), false);

		return new Homomorphism[] { StaticHomomorphism.instance(), RecursiveBacktrackHomomorphism.instance(),
		                            new BacktrackHomomorphism(),
		                            new BacktrackHomomorphism(bcc0.getBCCScheduler(), bcc0.getBCCBackJumping()),
		                            new BacktrackHomomorphism(bcc1.getBCCScheduler(), bcc1.getBCCBackJumping()),
		                            new BacktrackHomomorphism(new NFC0()), new BacktrackHomomorphism(new NFC2()),
		                            new BacktrackHomomorphism(new SimpleFC()),
		                            new BacktrackHomomorphism(new NFC2WithLimit(8)),
		                            new BacktrackHomomorphism(bcc2.getBCCScheduler(), StarBootstrapper.instance(),
		                                                      new NFC2(), bcc2.getBCCBackJumping()) };

	}

	public static AtomSet[] getAtomSet() {

		if (graphStore != null) {
			graphStore.close();
		}

		if (neo4jStore != null) {
			neo4jStore.close();
		}

		try {
			if (defaultRDBMSStore != null) {
				defaultRDBMSStore.close();
			}

			if (plainTableRDBMSStore != null) {
				plainTableRDBMSStore.close();
			}

			defaultRDBMSStore = new AdHocRdbmsStore(new HSQLDBDriver(DEFAULT_RDBMS_TEST, null));
			plainTableRDBMSStore = new NaturalRDBMSStore(new HSQLDBDriver(PLAIN_TABLE_RDBMS_TEST, null));

			defaultRDBMSStore.clear();
			plainTableRDBMSStore.clear();

			graphStore = new BlueprintsGraphDBStore(new TinkerGraph());
			rm(NEO4J_TEST);
			neo4jStore = new Neo4jStore(NEO4J_TEST);

			return new AtomSet[] { new DefaultInMemoryGraphAtomSet(), new LinkedListAtomSet(), defaultRDBMSStore,
			                       plainTableRDBMSStore, graphStore, neo4jStore };
		} catch (SQLException e) {
			throw new Error(e);
		} catch (AtomSetException e) {
			throw new Error(e);
		}
	}

	public static TripleStore[] getTripleStores() {
		if (jenaStore != null) {
			jenaStore.clear();
			jenaStore.close();
		}

		if (sailStore != null) {
			sailStore.close();
		}

		rm(JENA_TEST);
		jenaStore = new JenaStore(JENA_TEST);

		try {
			sailStore = new SailStore();
		} catch (AtomSetException e) {
			Assert.assertTrue("Error while creating SailStore", false);
		}

		return new TripleStore[] { jenaStore, sailStore };
	}

	private static void rm(String path) {
		rm(new File(path));
	}

	private static void rm(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					throw new IOError(new Error("I can't delete the file " + file.getAbsolutePath(), e));
				}
			} else {
				if (!file.delete()) {
					throw new IOError(new Error("I can't delete the file " + file.getAbsolutePath()));
				}
			}
		}
	}

}
