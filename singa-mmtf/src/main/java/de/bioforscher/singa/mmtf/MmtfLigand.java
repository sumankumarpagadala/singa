package de.bioforscher.singa.mmtf;

import de.bioforscher.singa.chemistry.physical.families.LigandFamily;
import de.bioforscher.singa.chemistry.physical.interfaces.Ligand;
import de.bioforscher.singa.chemistry.physical.model.LeafIdentifier;
import org.rcsb.mmtf.api.StructureDataInterface;

/**
 * The implementation of {@link Ligand}s for mmtf structures.
 *
 * @author cl
 */
public class MmtfLigand extends MmtfLeafSubstructure<LigandFamily> implements Ligand {

    /**
     * Creates a new {@link MmtfLigand}.
     *
     * @param data The original data.
     * @param family The leaf family.
     * @param leafIdentifier The leaf identifier.
     * @param internalGroupIndex The index of this leaf in the data array.
     * @param atomStartIndex The index of the first atom that belong to this leaf.
     * @param atomEndIndex The index of the last atom that belong to this leaf.
     */
    MmtfLigand(StructureDataInterface data, LigandFamily family, LeafIdentifier leafIdentifier, int internalGroupIndex, int atomStartIndex, int atomEndIndex) {
        super(data, family, leafIdentifier, internalGroupIndex, atomStartIndex, atomEndIndex);
    }

    /**
     * A copy constructor that passes all attributes of the given {@link MmtfLeafSubstructure} to a new instance.
     *
     * @param mmtfLeafSubstructure The {@link MmtfLeafSubstructure} to copy.
     */
    private MmtfLigand(MmtfLeafSubstructure mmtfLeafSubstructure) {
        super(mmtfLeafSubstructure);
    }

    @Override
    public String flatToString() {
        return "Ligand ("+getThreeLetterCode()+") "+getIdentifier();
    }

    @Override
    public String toString() {
        return flatToString();
    }

    @Override
    public Ligand getCopy() {
        return new MmtfLigand(this);
    }
}
