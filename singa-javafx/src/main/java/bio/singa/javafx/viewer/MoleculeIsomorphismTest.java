package bio.singa.javafx.viewer;

import bio.singa.chemistry.features.smiles.SmilesParser;
import bio.singa.javafx.renderer.graphs.GraphDisplayApplication;
import bio.singa.javafx.renderer.molecules.MoleculeGraphRenderer;
import bio.singa.structure.algorithms.molecules.MoleculeIsomorphism;
import bio.singa.structure.algorithms.molecules.MoleculeIsomorphismFinder;
import bio.singa.structure.algorithms.molecules.MoleculeIsomorphismFinder.AtomConditions;
import bio.singa.structure.model.identifiers.LeafIdentifier;
import bio.singa.structure.model.interfaces.LeafSubstructure;
import bio.singa.structure.model.interfaces.Structure;
import bio.singa.structure.model.molecules.MoleculeAtom;
import bio.singa.structure.model.molecules.MoleculeBond;
import bio.singa.structure.model.molecules.MoleculeGraph;
import bio.singa.structure.model.molecules.MoleculeGraphs;
import bio.singa.structure.model.oak.OakLeafSubstructure;
import bio.singa.structure.parser.pdb.structures.StructureParser;
import javafx.application.Application;
import javafx.scene.paint.Color;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author fk
 */
public class MoleculeIsomorphismTest {

    public static void main(String[] args) {

//        Species alanylAMP = ChEBIParserService.parse("CHEBI:139296");
//        MoleculeGraph targetGraph = SmilesParser.parse(alanylAMP.getFeature(Smiles.class).getFeatureContent());
//
//        Species alanine = ChEBIParserService.parse("CHEBI:15570");
//        MoleculeGraph patternGraph= SmilesParser.parse(alanine.getFeature(Smiles.class).getFeatureContent());
//
//        RISubGraphFinder<MoleculeAtom, MoleculeBond, Vector2D, Integer, MoleculeGraph, Element, MoleculeBondType> finder
//                = new RISubGraphFinder<>(patternGraph, targetGraph, MoleculeAtom::getElement, MoleculeBond::getType);
//
//        GraphDisplayApplication.renderer = new MoleculeGraphRenderer();
//        GraphDisplayApplication.graph = patternGraph;
//
//        GraphDisplayApplication.renderer.setRenderAfter(graph -> {
//            for (MoleculeAtom moleculeAtom : targetGraph.getNodes()) {
//                if (finder.getFullMatches().get(0).contains(moleculeAtom)) {
//                    GraphDisplayApplication.renderer.getGraphicsContext().setStroke(Color.DARKRED);
//                    GraphDisplayApplication.renderer.strokeCircle(moleculeAtom.getPosition(), 35);
//                }
//            }
//            return null;
//        });

        Structure structure = StructureParser.pdb()
                .pdbIdentifier("1jjc")
                .parse();

        MoleculeGraph phenylAlaninePattern = SmilesParser.parse("C1=CC=C(C=C1)CC(C=O)N");

        Optional<LeafSubstructure<?>> fa5 = structure.getLeafSubstructure(new LeafIdentifier("1jjc", 1, "A", 999));
        MoleculeGraph fa5Graph = MoleculeGraphs.createMoleculeGraphFromStructure((OakLeafSubstructure<?>) fa5.get());

        MoleculeBond consideredEdge = phenylAlaninePattern.getEdge(7);
        // this is a complex bond condition where only a single bond in the pattern graph is considered regarding its type
        BiFunction<MoleculeBond, MoleculeBond, Boolean> bondCondition = (patternBond, targetBond) -> {
            if (patternBond.equals(consideredEdge)) {
                return patternBond.getType().equals(targetBond.getType());
            }
            return true;
        };

        MoleculeIsomorphism moleculeIsomorphism = MoleculeIsomorphismFinder.of(phenylAlaninePattern, fa5Graph, AtomConditions.isSameElement(), bondCondition);
        moleculeIsomorphism.reduceMatches();

        GraphDisplayApplication.renderer = new MoleculeGraphRenderer();
        GraphDisplayApplication.graph = fa5Graph;

        GraphDisplayApplication.renderer.setRenderAfter(graph -> {
            for (MoleculeAtom moleculeAtom : fa5Graph.getNodes()) {
                if (moleculeIsomorphism.getFullMatches().get(0).getNodes().contains(moleculeAtom)) {
                    GraphDisplayApplication.renderer.getGraphicsContext().setStroke(Color.DARKRED);
                    GraphDisplayApplication.renderer.strokeCircle(moleculeAtom.getPosition(), 35);
                }
            }
            return null;
        });

        Application.launch(GraphDisplayApplication.class);
    }
}
