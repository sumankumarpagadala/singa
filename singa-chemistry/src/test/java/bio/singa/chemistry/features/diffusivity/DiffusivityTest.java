package bio.singa.chemistry.features.diffusivity;

import bio.singa.chemistry.entities.SmallMolecule;
import bio.singa.features.identifiers.ChEBIIdentifier;
import bio.singa.features.model.Evidence;
import bio.singa.structure.features.molarmass.MolarMass;
import org.junit.jupiter.api.Test;
import tec.uom.se.quantity.Quantities;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author cl
 */
class DiffusivityTest {

    @Test
    void shouldResolveRequiredFeature() {
        SmallMolecule testSpecies = new SmallMolecule.Builder("dnp")
                .additionalIdentifier(new ChEBIIdentifier("CHEBI:29802"))
                .build();
        Diffusivity diffusivity = testSpecies.getFeature(Diffusivity.class);
        // this also needs to resolve the molar mass feature
        MolarMass molarMass = testSpecies.getFeature(MolarMass.class);
        assertEquals(108.0104, molarMass.getValue().doubleValue());
        System.out.println(diffusivity);
        assertEquals(7.889770977995664E-6, diffusivity.getValue().doubleValue());
    }

    @Test
    void shouldUseWilkeToCalculateDiffusifity() {
        SmallMolecule testSpecies = new SmallMolecule.Builder("light entity")
                .assignFeature(new MolarMass(100, Evidence.MANUALLY_ANNOTATED))
                .build();
        // get feature
        Diffusivity feature = testSpecies.getFeature(Diffusivity.class);
        // assert attributes and values
        assertEquals("Wilke Correlation", feature.getEvidence().getName());
        assertEquals(8.217150338823197E-6, feature.getValue().doubleValue());
        assertEquals(Diffusivity.SQUARE_CENTIMETRE_PER_SECOND, feature.getUnit());
    }

    @Test
    void shouldUseYoungToCalculateDiffusifity() {
        SmallMolecule testSpecies = new SmallMolecule.Builder("heavy entity")
                .assignFeature(new MolarMass(10000, Evidence.MANUALLY_ANNOTATED))
                .build();
        // get feature
        Diffusivity feature = testSpecies.getFeature(Diffusivity.class);
        // assert attributes and values
        assertEquals("Young Correlation", feature.getEvidence().getName());
        assertEquals(1.134227930559286E-6, feature.getValue().doubleValue());
        assertEquals(Diffusivity.SQUARE_CENTIMETRE_PER_SECOND, feature.getUnit());
    }

}