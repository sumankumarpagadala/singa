package de.bioforscher.chemistry.descriptive.molecules;

import de.bioforscher.mathematics.graphs.model.AbstractEdge;

/**
 * @author cl
 */
public class MoleculeBond extends AbstractEdge<MoleculeAtom> {

    private MoleculeBondType type;

    public MoleculeBond(int identifier) {
        super(identifier);
    }

    public MoleculeBond(int identifier, char smilesSymbol) {
        super(identifier);
        this.type = MoleculeBondType.getBondForSMILESSymbol(smilesSymbol);
    }

    public MoleculeBond(int identifier, MoleculeAtom source, MoleculeAtom target, MoleculeBondType type) {
        super(identifier, source, target);
        this.type = type;
    }

    public MoleculeBondType getType() {
        return this.type;
    }

    public void setType(MoleculeBondType type) {
        this.type = type;
    }



}
