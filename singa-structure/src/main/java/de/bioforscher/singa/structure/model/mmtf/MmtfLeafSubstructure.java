package de.bioforscher.singa.structure.model.mmtf;

import de.bioforscher.singa.structure.model.families.StructuralFamily;
import de.bioforscher.singa.structure.model.identifiers.LeafIdentifier;
import de.bioforscher.singa.structure.model.interfaces.Atom;
import de.bioforscher.singa.structure.model.interfaces.LeafSubstructure;
import org.rcsb.mmtf.api.StructureDataInterface;

import java.util.*;

/**
 * The implementation of {@link LeafSubstructure} for mmtf structures. Remembers the internal group index, the leaf
 * identifier and the indices of the first and the last atom belonging to this group.
 *
 * @author cl
 */
public abstract class MmtfLeafSubstructure<FamilyType extends StructuralFamily> implements LeafSubstructure<FamilyType> {

    /**
     * The original bytes kept to copy.
     */
    private byte[] bytes;

    /**
     * The original mmtf data.
     */
    private StructureDataInterface data;

    /**
     * The index of this leaf in the group data arrays.
     */
    private int internalGroupIndex;

    /**
     * The generated leaf identifier.
     */
    private LeafIdentifier leafIdentifier;

    /**
     * The atoms that have already been requested.
     */
    private Map<Integer, MmtfAtom> cachedAtoms;

    /**
     * The index of the first atom that belong to this leaf.
     */
    private int atomStartIndex;

    /**
     * The index of the last atom that belong to this leaf.
     */
    private int atomEndIndex;

    /**
     * The set of atoms anot available
     */
    private Set<Integer> removedAtoms;

    /**
     * The structural family of this entity
     */
    private FamilyType family;

    /**
     * The families to which the {@link LeafSubstructure} can be exchanged.
     */
    private Set<FamilyType> exchangeableFamilies;

    /**
     * Creates a new {@link MmtfLeafSubstructure}.
     *
     * @param data The original data.
     * @param family The leaf family.
     * @param leafIdentifier The leaf identifier.
     * @param internalGroupIndex The index of this leaf in the data array.
     * @param atomStartIndex The index of the first atom that belong to this leaf.
     * @param atomEndIndex The index of the last atom that belong to this leaf.
     */
    MmtfLeafSubstructure(StructureDataInterface data, byte[] bytes, FamilyType family, LeafIdentifier leafIdentifier, int internalGroupIndex, int atomStartIndex, int atomEndIndex) {
        this.data = data;
        this.bytes = bytes;
        this.family = family;
        this.leafIdentifier = leafIdentifier;
        this.internalGroupIndex = internalGroupIndex;
        this.atomStartIndex = atomStartIndex;
        this.atomEndIndex = atomEndIndex;
        this.exchangeableFamilies = new HashSet<>();
        this.cachedAtoms = new HashMap<>();
        this.removedAtoms = new HashSet<>();
    }

    /**
     * A copy constructor that passes all attributes of the given {@link MmtfLeafSubstructure} to a new instance.
     *
     * @param mmtfLeafSubstructure The {@link MmtfLeafSubstructure} to copy.
     */
    protected MmtfLeafSubstructure(MmtfLeafSubstructure<?> mmtfLeafSubstructure) {
        this.bytes = mmtfLeafSubstructure.bytes;
        this.data = MmtfStructure.bytesToStructureData(bytes);
        this.leafIdentifier = mmtfLeafSubstructure.leafIdentifier;
        this.atomStartIndex = mmtfLeafSubstructure.atomStartIndex;
        this.atomEndIndex = mmtfLeafSubstructure.atomEndIndex;
        this.removedAtoms = mmtfLeafSubstructure.removedAtoms;
    }

    @Override
    public LeafIdentifier getIdentifier() {
        return leafIdentifier;
    }

    @Override
    public String getThreeLetterCode() {
        return family.getThreeLetterCode();
    }

    @Override
    public List<Atom> getAllAtoms() {
        // terminate records are fucking the numbering up
        List<Atom> results = new ArrayList<>();
        for (int internalAtomIndex = atomStartIndex; internalAtomIndex <= atomEndIndex; internalAtomIndex++) {
            // skip removed atoms
            if (removedAtoms.contains(internalAtomIndex)) {
                continue;
            }
            // cache atoms
            if (cachedAtoms.containsKey(internalAtomIndex)) {
                results.add(cachedAtoms.get(internalAtomIndex));
            } else {
                MmtfAtom mmtfAtom = new MmtfAtom(data, bytes, internalGroupIndex, internalAtomIndex - atomStartIndex, internalAtomIndex);
                cachedAtoms.put(internalAtomIndex, mmtfAtom);
                results.add(mmtfAtom);
            }
        }
        return results;
    }

    @Override
    public Optional<Atom> getAtom(Integer internalAtomIndex) {
        if (internalAtomIndex < atomStartIndex || internalAtomIndex > atomEndIndex || removedAtoms.contains(internalAtomIndex)) {
            return Optional.empty();
        }
        if (cachedAtoms.containsKey(internalAtomIndex)) {
            return Optional.of(cachedAtoms.get(internalAtomIndex));
        } else {
            MmtfAtom mmtfAtom = new MmtfAtom(data, bytes, internalGroupIndex, internalAtomIndex - atomStartIndex, internalAtomIndex);
            cachedAtoms.put(internalAtomIndex, mmtfAtom);
            return Optional.of(mmtfAtom);
        }
    }

    @Override
    public void removeAtom(Integer atomIdentifier) {
        this.removedAtoms.add(atomIdentifier);
    }

    @Override
    public boolean containsAtomWithName(String atomName) {
        for (Atom atom : getAllAtoms()) {
            if (atom.getAtomName().equals(atomName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<Atom> getAtomByName(String atomName) {
        for (Atom atom : getAllAtoms()) {
            if (atom.getAtomName().equals(atomName)) {
                return Optional.of(atom);
            }
        }
        return Optional.empty();
    }

    @Override
    public FamilyType getFamily() {
        return this.family;
    }

    @Override
    public Set<FamilyType> getExchangeableFamilies() {
        return this.exchangeableFamilies;
    }

}
