package de.bioforscher.chemistry.parser.pdb.tokens;

import de.bioforscher.chemistry.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.chemistry.parser.pdb.PDBParsingTreeNode;
import de.bioforscher.chemistry.physical.atoms.Atom;
import de.bioforscher.chemistry.physical.atoms.AtomName;
import de.bioforscher.chemistry.physical.branches.Chain;
import de.bioforscher.chemistry.physical.branches.StructuralModel;
import de.bioforscher.chemistry.physical.families.LeafFactory;
import de.bioforscher.chemistry.physical.families.LigandFamily;
import de.bioforscher.chemistry.physical.families.ResidueFamily;
import de.bioforscher.chemistry.physical.leafes.AtomContainer;
import de.bioforscher.chemistry.physical.leafes.Residue;
import de.bioforscher.chemistry.physical.model.Structure;
import de.bioforscher.chemistry.physical.model.UniqueAtomIdentifer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.bioforscher.chemistry.parser.pdb.tokens.AtomToken.*;

/**
 * @author cl
 */
public class StructureCollector {

    private static final Logger logger = LoggerFactory.getLogger(StructureCollector.class);

    private String currentPDB = "0000";
    private int currentModel = 0;

    private Map<UniqueAtomIdentifer, Atom> atoms;
    private Map<String, String> leafStructure;

    public StructureCollector() {
        this.atoms = new HashMap<>();
        this.leafStructure = new TreeMap<>();
    }

    // TODO here, the atom serial is parsed twice, once creating the identifer and once creating the atom

    public static Structure collectStructure(List<String> pdbLines, String chainId) {
        StructureCollector collector = new StructureCollector();
        logger.debug("collecting content from {} pdblines", pdbLines.size());
        for (String currentLine : pdbLines) {
            if (AtomToken.RECORD_PATTERN.matcher(currentLine).matches()) {
                UniqueAtomIdentifer identifier = collector.createUniqueIdentifier(currentLine);
                collector.atoms.put(identifier, AtomToken.assembleAtom(currentLine));
                collector.leafStructure.put(String.valueOf(identifier.getAtomSerial()), RESIDUE_NAME.extract(currentLine));
            } else if (ModelToken.RECORD_PATTERN.matcher(currentLine).matches()) {
                collector.currentModel = Integer.valueOf(ModelToken.MODEL_SERIAL.extract(currentLine));
            } else if (TitleToken.RECORD_PATTERN.matcher((currentLine)).matches()) {
                collector.currentPDB = TitleToken.ID_CODE.extract(currentLine);
            }
        }

        logger.debug("grouping lines by content");
        PDBParsingTreeNode root = new PDBParsingTreeNode(collector.currentPDB, PDBParsingTreeNode.StructureLevel.STRUCTURE);
        collector.atoms.forEach((identifer, atom) -> root.appendAtom(atom, identifer));

        Map<String, String> leafNames = root.getLeafNames(collector.leafStructure);

        Structure structure = new Structure();

        logger.debug("creating structure");
        int chainGraphId = 0;
        for (PDBParsingTreeNode modelNode : root.getNodesFromLevel(PDBParsingTreeNode.StructureLevel.MODEL)) {
            logger.debug("collecting chains for model {}", modelNode.getIdentifier());
            StructuralModel model = new StructuralModel(Integer.valueOf(modelNode.getIdentifier()));
            for (PDBParsingTreeNode chainNode : modelNode.getNodesFromLevel(PDBParsingTreeNode.StructureLevel.CHAIN)) {
                if (chainNode.getIdentifier().matches(chainId)) {
                    logger.trace("collecting leafs for chain {}", chainNode.getIdentifier());
                    Chain chain = new Chain(chainGraphId++);
                    chain.setChainIdentifier(chainNode.getIdentifier());
                    for (PDBParsingTreeNode leafNode : chainNode.getNodesFromLevel(PDBParsingTreeNode.StructureLevel.LEAF)) {
                        String leafName = leafNames.get(leafNode.getIdentifier());
                        logger.trace("creating leaf {}:{} for chain {}", leafNode.getIdentifier(), leafName, chainNode.getIdentifier());
                        Optional<ResidueFamily> residueFamily = ResidueFamily.getResidueTypeByThreeLetterCode(leafName);
                        EnumMap<AtomName, Atom> atoms = leafNode.getAtomMap();
                        if (residueFamily.isPresent()) {
                            Residue residue = LeafFactory.createResidueFromAtoms(Integer.valueOf(leafNode.getIdentifier()), residueFamily.get(), atoms);
                            residue.setIdentiferMap(leafNode.getIdentiferMap());
                            chain.addSubstructure(residue);
                        } else {
                            AtomContainer<LigandFamily> container = new AtomContainer<>(Integer.valueOf(leafNode.getIdentifier()), LigandFamily.UNKNOWN);
                            container.setName(leafName);
                            leafNode.getAtomMap().forEach((key, value) -> container.addNode(value));
                            container.setIdentiferMap(leafNode.getIdentiferMap());
                            chain.addSubstructure(container);
                        }
                    }
                    model.addSubstructure(chain);
                }
                structure.addSubstructure(model);
            }
        }

        return structure;
    }

    private UniqueAtomIdentifer createUniqueIdentifier(String atomLine) {
        int atomSerial = Integer.valueOf(ATOM_SERIAL.extract(atomLine));
        String chain = CHAIN_IDENTIFIER.extract(atomLine);
        int leaf = Integer.valueOf(RESIDUE_SERIAL.extract(atomLine));
        return new UniqueAtomIdentifer(this.currentPDB, this.currentModel, chain, leaf, atomSerial);
    }

}
