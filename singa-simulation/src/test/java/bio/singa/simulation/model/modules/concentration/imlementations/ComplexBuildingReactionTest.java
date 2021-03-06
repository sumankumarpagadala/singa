package bio.singa.simulation.model.modules.concentration.imlementations;

import bio.singa.chemistry.entities.*;
import bio.singa.chemistry.features.reactions.RateConstant;
import bio.singa.features.identifiers.ChEBIIdentifier;
import bio.singa.features.identifiers.UniProtIdentifier;
import bio.singa.features.model.Evidence;
import bio.singa.features.parameters.Environment;
import bio.singa.features.quantities.MolarConcentration;
import bio.singa.features.units.UnitRegistry;
import bio.singa.mathematics.geometry.faces.Rectangle;
import bio.singa.mathematics.vectors.Vector2D;
import bio.singa.simulation.model.agents.pointlike.Vesicle;
import bio.singa.simulation.model.agents.pointlike.VesicleLayer;
import bio.singa.simulation.model.graphs.AutomatonGraph;
import bio.singa.simulation.model.graphs.AutomatonGraphs;
import bio.singa.simulation.model.graphs.AutomatonNode;
import bio.singa.simulation.model.sections.CellRegion;
import bio.singa.simulation.model.sections.CellSubsection;
import bio.singa.simulation.model.sections.ConcentrationContainer;
import bio.singa.simulation.model.simulation.Simulation;
import bio.singa.structure.features.molarmass.MolarMass;
import org.junit.jupiter.api.*;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Time;

import static bio.singa.features.units.UnitProvider.MOLE_PER_LITRE;
import static bio.singa.simulation.model.sections.CellSubsection.SECTION_A;
import static bio.singa.simulation.model.sections.CellTopology.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tec.uom.se.unit.MetricPrefix.*;
import static tec.uom.se.unit.Units.*;

/**
 * @author cl
 */
class ComplexBuildingReactionTest {

    @BeforeAll
    static void initialize() {
        UnitRegistry.reinitialize();
    }

    @AfterEach
    void cleanUp() {
        UnitRegistry.reinitialize();
    }

    @Test
    @DisplayName("complex building reaction - minimal setup")
    void minimalSetUpTest() {
        // the rate constants
        RateConstant forwardRate = RateConstant.create(1).forward().secondOrder().concentrationUnit(MOLE_PER_LITRE).timeUnit(SECOND).build();
        RateConstant backwardRate = RateConstant.create(1).backward().firstOrder().timeUnit(SECOND).build();

        // the ligand
        ChemicalEntity bindee = new SmallMolecule.Builder("bindee")
                .name("bindee")
                .build();

        // the receptor
        Protein binder = new Protein.Builder("binder")
                .name("binder")
                .build();

        // create simulation
        Simulation simulation = new Simulation();

        // create and add module
        ComplexBuildingReaction binding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("binding")
                .of(bindee, forwardRate)
                .in(OUTER)
                .by(binder, backwardRate)
                .to(MEMBRANE)
                .build();
        ComplexedChemicalEntity complex = binding.getComplex();

        // setup graph
        final AutomatonGraph automatonGraph = AutomatonGraphs.singularGraph();
        simulation.setGraph(automatonGraph);
        // set concentrations
        AutomatonNode membraneNode = automatonGraph.getNode(0, 0);
        membraneNode.setCellRegion(CellRegion.MEMBRANE);
        membraneNode.getConcentrationContainer().set(OUTER, bindee, 1.0);
        membraneNode.getConcentrationContainer().set(MEMBRANE, binder, 1.0);
        membraneNode.getConcentrationContainer().set(MEMBRANE, complex, 1.0);

        // forward and backward reactions should cancel each other out
        Quantity<MolarConcentration> empty = Environment.emptyConcentration();
        Quantity<MolarConcentration> one =  UnitRegistry.concentration(1.0, MOLE_PER_LITRE);
        for (int i = 0; i < 10; i++) {
            ConcentrationContainer container = membraneNode.getConcentrationContainer();

            assertEquals(empty, container.get(INNER, bindee));
            assertEquals(empty, container.get(INNER, binder));
            assertEquals(empty, container.get(INNER, complex));

            assertEquals(empty, container.get(MEMBRANE, bindee));
            assertEquals(one, container.get(MEMBRANE, binder));
            assertEquals(one, container.get(MEMBRANE, complex));

            assertEquals(one, container.get(OUTER, bindee));
            assertEquals(empty, container.get(OUTER, binder));
            assertEquals(empty, container.get(OUTER, complex));

            simulation.nextEpoch();
        }
    }

    @Test
    @DisplayName("complex building reaction - monovalent receptor binding")
    void testPrazosinExample() {
        UnitRegistry.setSpace(Quantities.getQuantity(1.0, MILLI(METRE)));

        // see Receptors (Lauffenburger) p. 30
        // prazosin, CHEBI:8364
        ChemicalEntity ligand = new SmallMolecule.Builder("ligand")
                .name("prazosin")
                .additionalIdentifier(new ChEBIIdentifier("CHEBI:8364"))
                .build();

        // the corresponding rate constants
        RateConstant forwardsRate = RateConstant.create(2.4e8).forward().secondOrder().concentrationUnit(MOLE_PER_LITRE).timeUnit(MINUTE).build();
        RateConstant backwardsRate = RateConstant.create(0.018).backward().firstOrder().timeUnit(MINUTE).build();
        // alpha-1 adrenergic receptor, P35348
        Receptor receptor = new Receptor.Builder("receptor")
                .name("alpha-1 adrenergic receptor")
                .additionalIdentifier(new UniProtIdentifier("P35348"))
                .build();

        // create simulation
        Simulation simulation = new Simulation();

        // setup graph
        final AutomatonGraph automatonGraph = AutomatonGraphs.singularGraph();
        simulation.setGraph(automatonGraph);
        // concentrations
        AutomatonNode membraneNode = automatonGraph.getNode(0, 0);
        membraneNode.setCellRegion(CellRegion.MEMBRANE);
        membraneNode.getConcentrationContainer().set(SECTION_A, ligand, UnitRegistry.concentration(0.1, MOLE_PER_LITRE));
        membraneNode.getConcentrationContainer().set(CellSubsection.MEMBRANE, receptor, UnitRegistry.concentration(0.1, MOLE_PER_LITRE));

        // create and add module
        ComplexBuildingReaction reaction = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("binding reaction")
                .of(ligand, forwardsRate)
                .in(INNER)
                .by(receptor, backwardsRate)
                .to(MEMBRANE)
                .build();
        ComplexedChemicalEntity complex = reaction.getComplex();

        // checkpoints
        Quantity<Time> currentTime;
        Quantity<Time> firstCheckpoint = Quantities.getQuantity(0.05, MILLI(SECOND));
        boolean firstCheckpointPassed = false;
        Quantity<Time> secondCheckpoint = Quantities.getQuantity(2.0, MILLI(SECOND));
        // run simulation
        while ((currentTime = simulation.getElapsedTime().to(MILLI(SECOND))).getValue().doubleValue() < secondCheckpoint.getValue().doubleValue()) {
            simulation.nextEpoch();
            if (!firstCheckpointPassed && currentTime.getValue().doubleValue() > firstCheckpoint.getValue().doubleValue()) {
                assertEquals(0.00476, membraneNode.getConcentrationContainer().get(CellSubsection.MEMBRANE, receptor).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
                assertEquals(0.00476, membraneNode.getConcentrationContainer().get(INNER, ligand).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
                assertEquals(0.09523, membraneNode.getConcentrationContainer().get(CellSubsection.MEMBRANE, complex).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
                firstCheckpointPassed = true;
            }
        }

        // check final values
        assertEquals(0.0001, membraneNode.getConcentrationContainer().get(CellSubsection.MEMBRANE, receptor).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
        assertEquals(0.0001, membraneNode.getConcentrationContainer().get(INNER, ligand).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
        assertEquals(0.0998, membraneNode.getConcentrationContainer().get(CellSubsection.MEMBRANE, complex).to(MOLE_PER_LITRE).getValue().doubleValue(), 1e-3);
    }

    @Test
    @DisplayName("complex building reaction - simple section changing binding")
    void testMembraneAbsorption() {
        // the rate constants
        RateConstant forwardRate = RateConstant.create(1.0e6)
                .forward().secondOrder()
                .concentrationUnit(MOLE_PER_LITRE)
                .timeUnit(MINUTE)
                .build();

        RateConstant backwardRate = RateConstant.create(0.01)
                .backward().firstOrder()
                .timeUnit(MINUTE)
                .build();

        // the ligand
        ChemicalEntity bindee = new SmallMolecule.Builder("bindee")
                .name("bindee")
                .assignFeature(new MolarMass(10, Evidence.MANUALLY_ANNOTATED))
                .build();

        // the receptor
        Protein binder = new Protein.Builder("binder")
                .name("binder")
                .assignFeature(new MolarMass(100, Evidence.MANUALLY_ANNOTATED))
                .build();

        // create simulation
        Simulation simulation = new Simulation();

        // create and add module
        ComplexBuildingReaction binding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("binding")
                .of(bindee, forwardRate)
                .in(OUTER)
                .by(binder, backwardRate)
                .to(MEMBRANE)
                .build();

        // setup graph
        final AutomatonGraph automatonGraph = AutomatonGraphs.singularGraph();
        simulation.setGraph(automatonGraph);
        // concentrations
        AutomatonNode membraneNode = automatonGraph.getNode(0, 0);
        membraneNode.setCellRegion(CellRegion.MEMBRANE);
        membraneNode.getConcentrationContainer().set(OUTER, bindee, 1.0);
        membraneNode.getConcentrationContainer().set(MEMBRANE, binder, 0.1);
        membraneNode.getConcentrationContainer().set(MEMBRANE, binding.getComplex(), 0.0);

        Quantity<MolarConcentration> previousConcentration = null;
        for (int i = 0; i < 10; i++) {
            simulation.nextEpoch();
            Quantity<MolarConcentration> currentConcentration = membraneNode.getConcentrationContainer().get(CellSubsection.MEMBRANE, binding.getComplex());
            if (previousConcentration != null) {
                assertTrue(currentConcentration.getValue().doubleValue() > previousConcentration.getValue().doubleValue());
            }
            previousConcentration = currentConcentration;
        }
    }

    @Test
    @DisplayName("complex building reaction - section changing binding with concurrent inside and outside reactions")
    void shouldReactInsideAndOutside() {
        // the rate constants
        RateConstant innerForwardsRateConstant = RateConstant.create(1.0e6).forward().secondOrder().concentrationUnit(MOLE_PER_LITRE).timeUnit(MINUTE).build();
        RateConstant innerBackwardsRateConstant = RateConstant.create(0.01).backward().firstOrder().timeUnit(MINUTE).build();

        // the rate constants
        RateConstant outerForwardsRateConstant = RateConstant.create(1.0e6).forward().secondOrder().concentrationUnit(MOLE_PER_LITRE).timeUnit(MINUTE).build();
        RateConstant outerBackwardsRateConstant = RateConstant.create(0.01).backward().firstOrder().timeUnit(MINUTE).build();

        // the inner ligand
        ChemicalEntity innerBindee = new SmallMolecule.Builder("inner bindee")
                .name("inner bindee")
                .assignFeature(new MolarMass(10, Evidence.MANUALLY_ANNOTATED))
                .build();

        // the outer ligand
        ChemicalEntity outerBindee = new SmallMolecule.Builder("outer bindee")
                .name("outer bindee")
                .assignFeature(new MolarMass(10, Evidence.MANUALLY_ANNOTATED))
                .build();

        // the receptor
        Protein binder = new Protein.Builder("binder")
                .name("binder")
                .assignFeature(new MolarMass(100, Evidence.MANUALLY_ANNOTATED))
                .build();

        // create simulation
        Simulation simulation = new Simulation();

        // create and add inner module
        ComplexBuildingReaction innerBinding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("Inner Binding")
                .of(innerBindee, innerForwardsRateConstant)
                .in(INNER)
                .by(binder, innerBackwardsRateConstant)
                .to(MEMBRANE)
                .build();

        // create and add outer module
        ComplexBuildingReaction outerBinding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("Outer Binding")
                .of(outerBindee, outerForwardsRateConstant)
                .in(OUTER)
                .by(binder, outerBackwardsRateConstant)
                .to(MEMBRANE)
                .build();

        // setup graph
        final AutomatonGraph automatonGraph = AutomatonGraphs.singularGraph();
        simulation.setGraph(automatonGraph);
        // concentrations
        AutomatonNode membraneNode = automatonGraph.getNode(0, 0);
        membraneNode.setCellRegion(CellRegion.MEMBRANE);

        membraneNode.getConcentrationContainer().set(INNER, innerBindee, 0.1);
        membraneNode.getConcentrationContainer().set(OUTER, outerBindee, 0.1);
        membraneNode.getConcentrationContainer().set(MEMBRANE, binder, 0.1);
        membraneNode.getConcentrationContainer().set(MEMBRANE, innerBinding.getComplex(), 0.0);
        membraneNode.getConcentrationContainer().set(MEMBRANE, outerBinding.getComplex(), 0.0);

        Quantity<MolarConcentration> previousInnerConcentration = null;
        Quantity<MolarConcentration> previousOuterConcentration = null;
        for (int i = 0; i < 10; i++) {
            simulation.nextEpoch();
            // inner assertions
            Quantity<MolarConcentration> currentInnerConcentration = membraneNode.getConcentrationContainer().get(MEMBRANE, innerBinding.getComplex());
            if (previousInnerConcentration != null) {
                assertTrue(currentInnerConcentration.getValue().doubleValue() > previousInnerConcentration.getValue().doubleValue());
            }
            previousInnerConcentration = currentInnerConcentration;
            // outer assertions
            Quantity<MolarConcentration> currentOuterConcentration = membraneNode.getConcentrationContainer().get(MEMBRANE, outerBinding.getComplex());
            if (previousOuterConcentration != null) {
                assertTrue(currentOuterConcentration.getValue().doubleValue() > previousOuterConcentration.getValue().doubleValue());
            }
            previousOuterConcentration = currentOuterConcentration;
        }
    }

    @Test
    @DisplayName("complex building reaction - section changing binding with fully contained vesicle")
    void testComplexBuildingWithVesicle() {
        double simulationExtend = 150;
        int nodesHorizontal = 3;
        int nodesVertical = 3;

        Rectangle rectangle = new Rectangle(simulationExtend, simulationExtend);
        Simulation simulation = new Simulation();
        simulation.setSimulationRegion(rectangle);

        // setup scaling
        ComparableQuantity<Length> systemExtend = Quantities.getQuantity(1, MICRO(METRE));
        Environment.setSystemExtend(systemExtend);
        Environment.setSimulationExtend(simulationExtend);
        Environment.setNodeSpacingToDiameter(systemExtend, nodesHorizontal);

        // setup graph and assign regions
        AutomatonGraph graph = AutomatonGraphs.createRectangularAutomatonGraph(nodesHorizontal, nodesVertical);
        simulation.setGraph(graph);

        // setup spatial representations
        simulation.initializeGraph();
        simulation.initializeSpatialRepresentations();

        // the rate constants
        RateConstant forwardRate = RateConstant.create(1.0e6)
                .forward().secondOrder()
                .concentrationUnit(MOLE_PER_LITRE)
                .timeUnit(MINUTE)
                .build();

        RateConstant backwardRate = RateConstant.create(0.01)
                .backward().firstOrder()
                .timeUnit(MINUTE)
                .build();

        // the ligand
        ChemicalEntity bindee = new SmallMolecule.Builder("bindee")
                .name("bindee")
                .assignFeature(new MolarMass(10, Evidence.MANUALLY_ANNOTATED))
                .build();

        // the receptor
        Protein binder = new Protein.Builder("binder")
                .name("binder")
                .assignFeature(new MolarMass(100, Evidence.MANUALLY_ANNOTATED))
                .build();

        // create and add module
        ComplexBuildingReaction binding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("binding")
                .of(bindee, forwardRate)
                .in(INNER)
                .by(binder, backwardRate)
                .to(MEMBRANE)
                .build();

        // initialize vesicle layer
        VesicleLayer vesicleLayer = new VesicleLayer(simulation);
        simulation.setVesicleLayer(vesicleLayer);

        ComparableQuantity<Length> radius = Quantities.getQuantity(20, NANO(METRE));

        // vesicle contained
        Vesicle vesicle = new Vesicle("Vesicle", new Vector2D(25.0,25.0), radius);
        vesicle.getConcentrationContainer().set(MEMBRANE, binder, 0.1);
        vesicle.getConcentrationContainer().set(MEMBRANE, binding.getComplex(), 0.0);
        vesicleLayer.addVesicle(vesicle);

        // concentrations
        AutomatonNode node = graph.getNode(0, 0);
        node.getConcentrationContainer().set(INNER, bindee, 1.0);

        Quantity<MolarConcentration> previousNodeConcentration = null;
        Quantity<MolarConcentration> previousVesicleConcentration = null;
        for (int i = 0; i < 10; i++) {
            simulation.nextEpoch();
            // node assertion
            Quantity<MolarConcentration> currentNodeConcentration = node.getConcentrationContainer().get(INNER, bindee);
            if (previousNodeConcentration != null) {
                assertTrue(currentNodeConcentration.getValue().doubleValue() < previousNodeConcentration.getValue().doubleValue());
            }
            previousNodeConcentration = currentNodeConcentration;
            // vesicle assertion
            Quantity<MolarConcentration> currentVesicleConcentration = vesicle.getConcentrationContainer().get(MEMBRANE, binding.getComplex());
            if (previousVesicleConcentration != null) {
                assertTrue(currentVesicleConcentration.getValue().doubleValue() > previousVesicleConcentration.getValue().doubleValue());
            }
            previousVesicleConcentration = currentVesicleConcentration;
        }
    }


    @Test
    @DisplayName("complex building reaction - section changing binding with partially contained vesicle")
    void testComplexBuildingWithPartialVesicle() {
        double simulationExtend = 150;
        int nodesHorizontal = 3;
        int nodesVertical = 3;

        Rectangle rectangle = new Rectangle(simulationExtend, simulationExtend);
        Simulation simulation = new Simulation();
        simulation.setSimulationRegion(rectangle);

        // setup scaling
        ComparableQuantity<Length> systemExtend = Quantities.getQuantity(1, MICRO(METRE));
        Environment.setSystemExtend(systemExtend);
        Environment.setSimulationExtend(simulationExtend);
        Environment.setNodeSpacingToDiameter(systemExtend, nodesHorizontal);

        // setup graph and assign regions
        AutomatonGraph graph = AutomatonGraphs.createRectangularAutomatonGraph(nodesHorizontal, nodesVertical);
        simulation.setGraph(graph);

        // setup spatial representations
        simulation.initializeGraph();
        simulation.initializeSpatialRepresentations();

        // the rate constants
        RateConstant forwardRate = RateConstant.create(1.0e6)
                .forward().secondOrder()
                .concentrationUnit(MOLE_PER_LITRE)
                .timeUnit(MINUTE)
                .build();

        RateConstant backwardRate = RateConstant.create(0.01)
                .backward().firstOrder()
                .timeUnit(MINUTE)
                .build();

        // the ligand
        ChemicalEntity bindee = new SmallMolecule.Builder("bindee")
                .name("bindee")
                .assignFeature(new MolarMass(10, Evidence.MANUALLY_ANNOTATED))
                .build();

        // the receptor
        Protein binder = new Protein.Builder("binder")
                .name("binder")
                .assignFeature(new MolarMass(100, Evidence.MANUALLY_ANNOTATED))
                .build();

        // create and add module
        ComplexBuildingReaction binding = ComplexBuildingReaction.inSimulation(simulation)
                .identifier("binding")
                .of(bindee, forwardRate)
                .in(INNER)
                .by(binder, backwardRate)
                .to(MEMBRANE)
                .build();

        // initialize vesicle layer
        VesicleLayer vesicleLayer = new VesicleLayer(simulation);
        simulation.setVesicleLayer(vesicleLayer);

        ComparableQuantity<Length> radius = Quantities.getQuantity(20, NANO(METRE));

        // vesicle contained
        Vesicle vesicle = new Vesicle("Vesicle", new Vector2D(25.0,50.0), radius);
        vesicle.getConcentrationContainer().set(MEMBRANE, binder, 0.1);
        vesicle.getConcentrationContainer().set(MEMBRANE, binding.getComplex(), 0.0);
        vesicleLayer.addVesicle(vesicle);

        // concentrations
        AutomatonNode first = graph.getNode(0, 0);
        first.getConcentrationContainer().set(INNER, bindee, 1.0);
        // concentrations
        AutomatonNode second = graph.getNode(0, 1);
        second.getConcentrationContainer().set(INNER, bindee, 0.5);

        Quantity<MolarConcentration> previousFirstConcentration = null;
        Quantity<MolarConcentration> previousSecondConcentration = null;
        Quantity<MolarConcentration> previousVesicleConcentration = null;
        for (int i = 0; i < 10; i++) {
            simulation.nextEpoch();
            // first node assertions
            Quantity<MolarConcentration> currentFirstConcentration = first.getConcentrationContainer().get(INNER, bindee);
            if (previousFirstConcentration != null) {
                assertTrue(currentFirstConcentration.getValue().doubleValue() < previousFirstConcentration.getValue().doubleValue());
            }
            previousFirstConcentration = currentFirstConcentration;
            // first node assertions
            Quantity<MolarConcentration> currentSecondConcentration = second.getConcentrationContainer().get(INNER, bindee);
            if (previousSecondConcentration != null) {
                assertTrue(currentSecondConcentration.getValue().doubleValue() < previousSecondConcentration.getValue().doubleValue());
            }
            previousSecondConcentration = currentSecondConcentration;
            // outer assertions
            Quantity<MolarConcentration> currentVesicleConcentration = vesicle.getConcentrationContainer().get(MEMBRANE, binding.getComplex());
            if (previousVesicleConcentration != null) {
                assertTrue(currentVesicleConcentration.getValue().doubleValue() > previousVesicleConcentration.getValue().doubleValue());
            }
            previousVesicleConcentration = currentVesicleConcentration;
        }
    }

}
