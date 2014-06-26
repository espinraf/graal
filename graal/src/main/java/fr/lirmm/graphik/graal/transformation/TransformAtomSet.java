package fr.lirmm.graphik.graal.transformation;

import fr.lirmm.graphik.graal.core.Atom;
import fr.lirmm.graphik.graal.core.atomset.AtomSet;
import fr.lirmm.graphik.graal.core.atomset.AtomSetException;
import fr.lirmm.graphik.util.stream.ObjectReader;

/**
 * 
 * @author Clément Sipieter (INRIA) <clement@6pi.fr>
 * 
 */
public class TransformAtomSet extends ReadOnlyTransformStore implements
        AtomSet {

    // /////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // /////////////////////////////////////////////////////////////////////////

    public TransformAtomSet(AtomSet store,
            AAtomTransformator transformator) {
        super(store, transformator);
    }

    // /////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // /////////////////////////////////////////////////////////////////////////

    @Override
    public boolean add(Atom atom) {
        try {
            this.getStore().add(
                    this.getAtomTransformator().transform(atom).iterator());
        } catch (AtomSetException e) {
            // TODO treat this exception
            e.printStackTrace();
            throw new Error("Untreated exception");
        }
        return true;
    }

    @Override
    public boolean remove(Atom atom) {
        try {
            this.getStore().remove(
                    this.getAtomTransformator().transform(atom).iterator());
        } catch (AtomSetException e) {
            // TODO treat this exception
            e.printStackTrace();
            throw new Error("Untreated exception");
        }
        return true;
    }

    @Override
    public void add(Iterable<Atom> atoms) {
        try {
            this.getStore().add(this.getAtomTransformator().transform(atoms));
        } catch (AtomSetException e) {
            // TODO treat this exception
            e.printStackTrace();
            throw new Error("Untreated exception");

        }
    }

    @Override
    public void remove(ObjectReader<Atom> stream) {
        try {
            this.getStore().remove(
                    this.getAtomTransformator().transform(stream));
        } catch (AtomSetException e) {
            // TODO treat this exception
            e.printStackTrace();
            throw new Error("Untreated exception");

        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS
    // /////////////////////////////////////////////////////////////////////////

    protected AtomSet getStore() {
        return (AtomSet) super.getStore();
    }
}