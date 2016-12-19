package de.bioforscher.chemistry.physical.families;

import de.bioforscher.chemistry.physical.model.StructuralFamily;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author cl
 */
public enum NucleotideFamily implements StructuralFamily {

    ADENOSINE("A","A"),
    DESOXYADENOSINE("A", "dA"),
    GUANOSINE("G","G"),
    DESOXYGUANOSINE("G", "dG"),
    THYMIDINE("T","T"),
    DESOXYTHYMIDINE("T", "dT"),
    URIDINE("U","U"),
    DESOXYURIDINE("U", "dU"),
    CYTIDINE("C","C"),
    DESOXYCYTIDINE("C", "dC"),

    MODIFIED_NUCLEOTIDE("M", "M");

    private String oneLetterCode;
    private String threeLetterCode;

    NucleotideFamily(String oneLetterCode, String threeLetterCode) {
        this.oneLetterCode = oneLetterCode;
        this.threeLetterCode = threeLetterCode;
    }

    @Override
    public String getOneLetterCode() {
        return this.oneLetterCode;
    }

    @Override
    public String getThreeLetterCode() {
        return this.threeLetterCode;
    }

    public static Optional<NucleotideFamily> getNucleotideByThreeLetterCode(String threeLetterCode) {
        return Arrays.stream(values())
                .filter(type -> threeLetterCode.trim().equalsIgnoreCase(type.getThreeLetterCode()))
                .findAny();
    }

}
