package bio.singa.chemistry.features.identifiers;

import bio.singa.chemistry.entities.ChemicalEntity;
import bio.singa.chemistry.entities.SmallMolecule;
import bio.singa.features.identifiers.PDBLigandIdentifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author cl
 */
class PDBLigandIdentiferProviderTest {

    private static ChemicalEntity chebiEntity;
    private static ChemicalEntity inchiEntity;

    @BeforeAll
    static void initialize() {
        // tetrahydrocannabinol
        chebiEntity = SmallMolecule.create("CHEBI:66964").build();
        // 6-(4-azanyl-4-methyl-piperidin-1-yl)-3-[2,3-bis(chloranyl)phenyl]pyrazin-2-amine
        inchiEntity = SmallMolecule.create("YGUFCDOEKKVKJK-UHFFFAOYSA-N").build();
    }

    @Test
    @DisplayName("pdb ligand identifier provider - using chebi identifier")
    void fetchWithChebi() {
        chebiEntity.setFeature(PDBLigandIdentifier.class);
        assertEquals("TCI", chebiEntity.getFeature(PDBLigandIdentifier.class).toString());
    }

    @Test
    @DisplayName("pdb ligand identifier provider - using inchi key")
    void fetchWithInCHI() {
        inchiEntity.setFeature(PDBLigandIdentifier.class);
        assertEquals("5OD", inchiEntity.getFeature(PDBLigandIdentifier.class).toString());
    }

}