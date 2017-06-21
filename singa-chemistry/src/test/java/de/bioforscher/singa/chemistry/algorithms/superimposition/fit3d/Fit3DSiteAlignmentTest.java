package de.bioforscher.singa.chemistry.algorithms.superimposition.fit3d;

import de.bioforscher.singa.chemistry.parser.pdb.structures.StructureParser;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotifs;
import de.bioforscher.singa.chemistry.physical.families.MatcherFamily;
import de.bioforscher.singa.chemistry.physical.families.substitution.matrices.SubstitutionMatrix;
import de.bioforscher.singa.chemistry.physical.model.StructuralEntityFilter;
import de.bioforscher.singa.chemistry.physical.model.Structure;
import de.bioforscher.singa.core.utility.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class Fit3DSiteAlignmentTest {

    private StructuralMotif bindingSite1;
    private StructuralMotif bindingSite2;

    @Before
    public void setUp() throws IOException {
        Structure bindingSiteStructure1 = StructureParser.local()
                .fileLocation(TestUtils.getResourceAsFilepath("truncated_1asz_A_renum.pdb"))
                .everything()
                .parse();
        this.bindingSite1 = StructuralMotif.fromLeaves(1, bindingSiteStructure1.getAllLeaves());
        Structure bindingSiteStructure2 = StructureParser.local()
                .fileLocation(TestUtils.getResourceAsFilepath("truncated_3m4p_A_renum.pdb"))
                .everything()
                .parse();
        this.bindingSite2 = StructuralMotif.fromLeaves(1, bindingSiteStructure2.getAllLeaves());
    }

    @Test
    public void shouldCreateBindingSiteAlignment() throws IOException {
        Fit3D fit3d = Fit3DBuilder.create()
                .site(this.bindingSite1)
                .vs(this.bindingSite2)
                .cutoffScore(0.35)
                .substitutionMatrix(SubstitutionMatrix.MC_LACHLAN)
                .exhaustive()
                .atomFilter(StructuralEntityFilter.AtomFilter.isBackbone())
                .run();
        assertEquals(0.29745276335597537, fit3d.getMatches().firstKey(), 1E-6);
        assertEquals(7.459982645433789, fit3d.getXieScore().getScore(), 1E-6);
        assertEquals(0.05689220664553862, fit3d.getXieScore().getNormalizedScore(), 1E-6);
        assertEquals(0.019884604882031143, fit3d.getXieScore().getSignificance(), 1E-6);
    }

    @Test
    public void shouldCreateGutteridgeBindingSiteAlignment() {
        // exchanges have only be added for one of the sites because they are transitive
        StructuralMotifs.assignComplexExchanges(this.bindingSite1, MatcherFamily.GUTTERIDGE);
        Fit3D fit3d = Fit3DBuilder.create()
                .site(this.bindingSite1)
                .vs(this.bindingSite2)
                .restrictToSpecifiedExchanges()
                .atomFilter(StructuralEntityFilter.AtomFilter.isBackbone())
                .run();
        assertEquals(0.5517396481341506, fit3d.getMatches().firstKey(), 1E-6);
        assertEquals(53.99934784373183, fit3d.getXieScore().getScore(), 1E-6);
        assertEquals(0.37573008273142394, fit3d.getXieScore().getNormalizedScore(), 1E-6);
        assertEquals(0.0, fit3d.getXieScore().getSignificance(), 1E-6);

    }
}