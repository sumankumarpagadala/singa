package de.bioforscher.singa.mmtf;

import de.bioforscher.singa.chemistry.physical.interfaces.Atom;
import de.bioforscher.singa.chemistry.physical.interfaces.LeafSubstructure;
import de.bioforscher.singa.chemistry.physical.interfaces.Structure;
import de.bioforscher.singa.chemistry.physical.model.LeafIdentifier;
import de.bioforscher.singa.mathematics.vectors.Vector3D;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rcsb.mmtf.api.StructureDataInterface;
import org.rcsb.mmtf.decoder.GenericDecoder;
import org.rcsb.mmtf.decoder.ReaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author cl
 */
public class MmtfLeafSubstructureTest {

    private static LeafSubstructure leaf162;
    private static LeafSubstructure leaf620A;

    @BeforeClass
    public static void prepareData() throws IOException {
        StructureDataInterface data1C0A = new GenericDecoder(ReaderUtils.getDataFromUrl("1C0A"));
        Structure structure1C0A = new MmtfStructure(data1C0A);
        leaf162 = structure1C0A.getLeafSubstructure(new LeafIdentifier("1C0A", 1, "A", 162)).get();
        leaf620A = structure1C0A.getLeafSubstructure(new LeafIdentifier("1C0A", 1, "B", 620, 'A')).get();
    }

    @Test
    public void getIdentifier() throws Exception {
        assertEquals(new LeafIdentifier("1C0A", 1, "A", 162), leaf162.getIdentifier());
        assertEquals(new LeafIdentifier("1C0A", 1, "B", 620, 'A'), leaf620A.getIdentifier());
    }

    @Test
    public void getThreeLetterCode() throws Exception {
        assertEquals("Thr", leaf162.getThreeLetterCode());
        assertEquals("H2U", leaf620A.getThreeLetterCode());
    }

    @Test
    public void getAllAtoms() throws Exception {
        final List<Atom> allAtoms = leaf162.getAllAtoms();
        assertEquals(7, allAtoms.size());
    }

    @Test
    public void getAtom() throws Exception {
        final Optional<Atom> atom = leaf620A.getAtom(437);
        if (!atom.isPresent()) {
            fail("Optional atom was empty.");
        }
        // one offset to regular pdb file
        assertEquals("O5'", atom.get().getAtomName());
        assertEquals(new Vector3D(64.28299713134766, 0.38600000739097595, 30.815000534057617), atom.get().getPosition());
    }


}