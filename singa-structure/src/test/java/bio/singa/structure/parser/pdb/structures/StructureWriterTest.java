package bio.singa.structure.parser.pdb.structures;

import bio.singa.structure.model.interfaces.Structure;
import bio.singa.structure.model.oak.OakStructure;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

/**
 * @author fk
 */
public class StructureWriterTest {

    @Test
    public void writeMMTFStructure() throws IOException {

        StructureParser.LocalPDB localPDB = new StructureParser.LocalPDB("/tmp/pdb", SourceLocation.OFFLINE_MMTF);
        Path path = localPDB.getPathForPdbIdentifier("1acj");

        OakStructure structure = (OakStructure) StructureParser.pdb()
                .pdbIdentifier("1acj")
                .parse();
        StructureWriter.writeMMTFStructure(structure, path);

        Structure reparsedStructure = StructureParser.local()
                .localPDB(localPDB, "1acj")
                .parse();

        assertEquals(structure.getAllAtoms().size(), reparsedStructure.getAllAtoms().size());
        assertEquals(structure.getAllLigands().size(),reparsedStructure.getAllLigands().size());
    }
}