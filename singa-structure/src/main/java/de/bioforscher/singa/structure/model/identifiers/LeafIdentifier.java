package de.bioforscher.singa.structure.model.identifiers;

import java.util.Comparator;

/**
 * The leaf identifier consists of the PDB identifer, the model identifier, the chain identifier, the serial of a leaf
 * substructure and optionally its insertion code.
 *
 * @author cl
 */
public class LeafIdentifier implements Comparable<LeafIdentifier> {

    public static final String DEFAULT_PDB_IDENTIFIER = "0000";
    public static final int DEFAULT_MODEL_IDENTIFIER = 1;
    public static final String DEFAULT_CHAIN_IDENTIFIER = "X";
    public static final char DEFAULT_INSERTION_CODE = '\u0000';

    private static final Comparator<LeafIdentifier> leafIdentiferComparator = Comparator
            .comparing(LeafIdentifier::getPdbIdentifier)
            .thenComparing(LeafIdentifier::getModelIdentifier)
            .thenComparing(LeafIdentifier::getChainIdentifier)
            .thenComparing(LeafIdentifier::getSerial)
            .thenComparing(LeafIdentifier::getInsertionCode);

    private final String pdbIdentifer;
    private final int modelIdentifer;
    private final String chainIdentifer;
    private final int serial;
    private final char insertionCode;

    public LeafIdentifier(String pdbIdentifer, int modelIdentifer, String chainIdentifer, int serial, char insertionCode) {
        this.pdbIdentifer = pdbIdentifer.toLowerCase();
        this.modelIdentifer = modelIdentifer;
        this.chainIdentifer = chainIdentifer.toUpperCase();
        this.serial = serial;
        this.insertionCode = insertionCode;
    }

    public LeafIdentifier(String pdbIdentifer, int modelIdentifer, String chainIdentifer, int serial) {
        this(pdbIdentifer, modelIdentifer, chainIdentifer, serial, DEFAULT_INSERTION_CODE);
    }

    public LeafIdentifier(String chainIdentifier, int serial, char insertionCode) {
        this(DEFAULT_PDB_IDENTIFIER, DEFAULT_MODEL_IDENTIFIER, chainIdentifier, serial, insertionCode);
    }

    public LeafIdentifier(String chainIdentifer, int serial) {
        this(DEFAULT_PDB_IDENTIFIER, DEFAULT_MODEL_IDENTIFIER, chainIdentifer, serial);
    }

    public LeafIdentifier(int serial) {
        this(DEFAULT_PDB_IDENTIFIER, DEFAULT_MODEL_IDENTIFIER, DEFAULT_CHAIN_IDENTIFIER, serial);
    }

    /**
     * Constructs a {@link LeafIdentifier} from the given simple string. Only chain-ID, residue number and optional
     * insertion code is required.
     *
     * @param identifier The identifier in string format (e.g. A-62 or A-62B).
     * @return The {@link LeafIdentifier}.
     */
    public static LeafIdentifier fromString(String identifier) {
        String[] split = identifier.split("-");
        // decide whether insertion code was specified
        String firstPart = split[0];
        String secondPart = split[1];
        if (secondPart.substring(secondPart.length() - 1).matches("[A-Z]")) {
            char insertionCode = secondPart.charAt(secondPart.length() - 1);
            return new LeafIdentifier(firstPart, Integer.valueOf(secondPart.substring(0, secondPart.length() - 1)), insertionCode);
        }
        return new LeafIdentifier(firstPart, Integer.valueOf(secondPart));
    }

    public String getPdbIdentifier() {
        return pdbIdentifer;
    }

    public int getModelIdentifier() {
        return modelIdentifer;
    }

    public String getChainIdentifier() {
        return chainIdentifer;
    }

    public int getSerial() {
        return serial;
    }

    public char getInsertionCode() {
        return insertionCode;
    }

    @Override
    public int compareTo(LeafIdentifier o) {
        return leafIdentiferComparator.compare(this, o);
    }

    @Override
    public String toString() {
        return pdbIdentifer + "-" + modelIdentifer + "-" + chainIdentifer + "-" + serial + (insertionCode != DEFAULT_INSERTION_CODE ? insertionCode : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeafIdentifier that = (LeafIdentifier) o;

        if (serial != that.serial) return false;
        if (modelIdentifer != that.modelIdentifer) return false;
        if (insertionCode != that.insertionCode) return false;
        if (pdbIdentifer != null ? !pdbIdentifer.equals(that.pdbIdentifer) : that.pdbIdentifer != null)
            return false;
        return chainIdentifer != null ? chainIdentifer.equals(that.chainIdentifer) : that.chainIdentifer == null;
    }

    @Override
    public int hashCode() {
        int result = pdbIdentifer != null ? pdbIdentifer.hashCode() : 0;
        result = 31 * result + modelIdentifer;
        result = 31 * result + (chainIdentifer != null ? chainIdentifer.hashCode() : 0);
        result = 31 * result + serial;
        result = 31 * result + (int) insertionCode;
        return result;
    }
}
