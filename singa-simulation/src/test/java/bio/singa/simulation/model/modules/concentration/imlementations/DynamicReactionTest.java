package bio.singa.simulation.model.modules.concentration.imlementations;

import bio.singa.chemistry.entities.ChemicalEntity;
import bio.singa.chemistry.entities.SmallMolecule;
import bio.singa.chemistry.features.reactions.RateConstant;
import bio.singa.features.units.UnitRegistry;
import bio.singa.simulation.model.graphs.AutomatonGraph;
import bio.singa.simulation.model.graphs.AutomatonGraphs;
import bio.singa.simulation.model.graphs.AutomatonNode;
import bio.singa.simulation.model.modules.concentration.reactants.Reactant;
import bio.singa.simulation.model.modules.concentration.reactants.ReactantRole;
import bio.singa.simulation.model.sections.CellRegion;
import bio.singa.simulation.model.sections.concentration.ConcentrationInitializer;
import bio.singa.simulation.model.sections.concentration.SectionConcentration;
import bio.singa.simulation.model.simulation.Simulation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tec.uom.se.quantity.Quantities;

import static bio.singa.features.units.UnitProvider.NANO_MOLE_PER_LITRE;
import static bio.singa.simulation.model.sections.CellSubsection.SECTION_A;
import static bio.singa.simulation.model.sections.CellTopology.INNER;
import static tec.uom.se.unit.Units.SECOND;

/**
 * @author cl
 */
public class DynamicReactionTest {

    @BeforeAll
    static void initialize() {
        UnitRegistry.reinitialize();
    }

    @AfterEach
    void cleanUp() {
        UnitRegistry.reinitialize();
    }

    @Test
    @DisplayName("example reaction - arbitrary law")
    public void shouldPerformDynamicReaction() {

        // the substrate
        ChemicalEntity substrate = new SmallMolecule.Builder("substrate")
                .name("substrate")
                .build();

        // the product
        ChemicalEntity product = new SmallMolecule.Builder("product")
                .name("product")
                .build();

        // the catalytic reactant
        ChemicalEntity catalyt = new SmallMolecule.Builder("catalyt")
                .name("catalyt")
                .build();

        // create simulation
        Simulation simulation = new Simulation();

        RateConstant rateConstant = RateConstant.create(0.1)
                .forward()
                .firstOrder()
                .timeUnit(SECOND)
                .build();

        // create and add module
        DynamicReaction.inSimulation(simulation)
                .kineticLaw("substrate*sin(catalyt)*k/product")
                .referenceParameter(new Reactant(substrate, ReactantRole.SUBSTRATE, INNER))
                .referenceParameter(new Reactant(product, ReactantRole.PRODUCT, INNER))
                .referenceParameter(new Reactant(catalyt, ReactantRole.CATALYTIC, INNER))
                .referenceParameter("k", rateConstant)
                .build();

        // TODO clean up reaction interface / abstract class (reaction based interface defining default methods?)

        // setup graph
        final AutomatonGraph automatonGraph = AutomatonGraphs.singularGraph();
        simulation.setGraph(automatonGraph);
        // set concentrations
        AutomatonNode node = automatonGraph.getNode(0, 0);
        node.setCellRegion(CellRegion.CYTOSOL_A);

        ConcentrationInitializer ci = new ConcentrationInitializer();
        ci.addInitialConcentration(new SectionConcentration(SECTION_A, substrate, Quantities.getQuantity(200, NANO_MOLE_PER_LITRE)));
        ci.addInitialConcentration(new SectionConcentration(SECTION_A, product, Quantities.getQuantity(100, NANO_MOLE_PER_LITRE)));
        ci.addInitialConcentration(new SectionConcentration(SECTION_A, catalyt, Quantities.getQuantity(30, NANO_MOLE_PER_LITRE)));
        simulation.setConcentrationInitializer(ci);

        for (int i = 0; i < 10; i++) {
            simulation.nextEpoch();
        }
    }

}