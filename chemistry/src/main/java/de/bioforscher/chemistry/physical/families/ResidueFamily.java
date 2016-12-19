package de.bioforscher.chemistry.physical.families;

import de.bioforscher.chemistry.parser.pdb.structures.PDBParserService;
import de.bioforscher.chemistry.physical.atoms.Atom;
import de.bioforscher.chemistry.physical.atoms.AtomName;
import de.bioforscher.chemistry.physical.leafes.Residue;
import de.bioforscher.chemistry.physical.model.StructuralFamily;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static de.bioforscher.chemistry.physical.atoms.AtomName.*;

/**
 * The residue type should contain the general data, that is the same across all amino acids of this type.
 */
public enum ResidueFamily implements StructuralFamily {

    ALANINE("Alanine", "A", "Ala", ALANINE_ATOM_NAMES),
    ARGININE("Arginine", "R", "Arg", ARGININE_ATOM_NAMES),
    ASPARAGINE("Asparagine", "N", "Asn", ASPARAGINE_ATOM_NAMES),
    ASPARTIC_ACID("Aspartic acid", "D", "Asp", ASPARTIC_ACID_ATOM_NAMES),
    CYSTEINE("Cysteine", "C", "Cys", CYSTEINE_ATOM_NAMES),
    GLUTAMINE("Glutamine", "Q", "Gln", GLUTAMINE_ATOM_NAMES),
    GLUTAMIC_ACID("Glutamic acid", "E", "Glu", GLUTAMIC_ACID_ATOM_NAMES),
    GLYCINE("Glycine", "G", "Gly", GLYCINE_ATOM_NAMES),
    HISTIDINE("Histidine", "H", "His", HISTIDINE_ATOM_NAMES),
    ISOLEUCINE("Isoleucine", "I", "Ile", ISOLEUCINE_ATOM_NAMES),
    LEUCINE("Leucine", "L", "Leu", LEUCINE_ATOM_NAMES),
    LYSINE("Lysine", "K", "Lys", LYSINE_ATOM_NAMES),
    METHIONINE("Methionine", "M", "Met", METHIONINE_ATOM_NAMES),
    PHENYLALANINE("Phenylalanine", "F", "Phe", PHENYLALANINE_ATOM_NAMES),
    PROLINE("Proline", "P", "Pro", PROLINE_ATOM_NAMES),
    SERINE("Serine", "S", "Ser", SERINE_ATOM_NAMES),
    THREONINE("Threonine", "T", "Thr", THREONINE_ATOM_NAMES),
    TRYPTOPHAN("Tryptophan", "W", "Trp", TRYPTOPHAN_ATOM_NAMES),
    TYROSINE("Tyrosine", "Y", "Tyr", TYROSINE_ATOM_NAMES),
    VALINE("Valine", "V", "Val", VALINE_ATOM_NAMES);

    private static final String RESIDUE_PROTOTYPES_BASE_DIR = "physical/leafes/prototypes/";
    private String name;
    private String oneLetterCode;
    private String threeLetterCode;
    private EnumSet<AtomName> allowedAtoms;

    ResidueFamily(String name, String oneLetterCode, String threeLetterCode, EnumSet<AtomName> allowedAtoms) {
        this.name = name;
        this.oneLetterCode = oneLetterCode;
        this.threeLetterCode = threeLetterCode;
        this.allowedAtoms = allowedAtoms;
    }

    public static Optional<ResidueFamily> getResidueTypeByThreeLetterCode(String threeLetterCode) {
        return Arrays.stream(values())
                .filter(type -> threeLetterCode.equalsIgnoreCase(type.getThreeLetterCode()))
                .findAny();
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String getOneLetterCode() {
        return this.oneLetterCode;
    }

    @Override
    public String getThreeLetterCode() {
        return this.threeLetterCode;
    }

    public EnumSet<AtomName> getAllowedAtoms() {
        return this.allowedAtoms;
    }

    /**
     * Returns true if the set of Atoms contains only Atom names, that can occur in the given residue type.
     *
     * @param atoms         The atoms to be checked.
     * @param residueFamily The expected type of residue.
     * @return True, if the set of Atoms contains only Atom names, that can occur in the given residue type.
     */
    public boolean containsExpectedAtoms(List<Atom> atoms, ResidueFamily residueFamily) {
        final Set<String> actualNames = atoms.stream()
                .map(Atom::getAtomNameString)
                .collect(Collectors.toSet());
        final Set<String> expectedNames = residueFamily.getAllowedAtoms().stream()
                .map(AtomName::getName)
                .collect(Collectors.toSet());
        return expectedNames.containsAll(actualNames);
    }

    /**
     * Returns a prototype of the {@link Residue} that are deposited in the project resources.
     *
     * @return A {@link Residue} prototype.
     * @throws IOException
     */
    public Residue getPrototype() throws IOException {
        return PDBParserService.parsePDBFile(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RESIDUE_PROTOTYPES_BASE_DIR +
                        this.getName().replaceAll(" ", "_").toLowerCase() + ".pdb"))
                .getAllResidues()
                .get(0);
    }
}
