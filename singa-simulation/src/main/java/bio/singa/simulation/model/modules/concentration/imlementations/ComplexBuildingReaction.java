package bio.singa.simulation.model.modules.concentration.imlementations;

import bio.singa.chemistry.entities.ChemicalEntity;
import bio.singa.chemistry.entities.ComplexedChemicalEntity;
import bio.singa.chemistry.features.reactions.BackwardsRateConstant;
import bio.singa.chemistry.features.reactions.ForwardsRateConstant;
import bio.singa.chemistry.features.reactions.RateConstant;
import bio.singa.features.exceptions.FeatureUnassignableException;
import bio.singa.features.model.Feature;
import bio.singa.features.units.UnitRegistry;
import bio.singa.simulation.model.agents.pointlike.Vesicle;
import bio.singa.simulation.model.graphs.AutomatonNode;
import bio.singa.simulation.model.modules.concentration.*;
import bio.singa.simulation.model.modules.concentration.functions.UpdatableDeltaFunction;
import bio.singa.simulation.model.modules.concentration.scope.IndependentUpdate;
import bio.singa.simulation.model.modules.concentration.specifity.UpdatableSpecific;
import bio.singa.simulation.model.sections.CellSubsection;
import bio.singa.simulation.model.sections.CellTopology;
import bio.singa.simulation.model.sections.ConcentrationContainer;
import bio.singa.simulation.model.simulation.Simulation;
import bio.singa.simulation.model.simulation.Updatable;

import javax.measure.Quantity;
import java.util.HashMap;
import java.util.Map;

/**
 * Complex building or breaking reactions are a special kind of {@link ReversibleReaction}. In this kind of reaction
 * association or dissociation of chemical entities results in a change of section. For example when a ligand is bound
 * to a receptor the resulting complex is associated to the membrane section the receptor is located in.<br> Speaking in
 * terms of this module a <b>bindee</b> in the <b>bindee topology</b> is bound to a <b>binder</b> in the
 * <b>binder topology</b>, resulting in a <b>complex</b> in the <b>binder topology</b>.<br>
 * The speed of the reaction is guided by any {@link ForwardsRateConstant} that determines the speed of the association
 * reaction and a {@link BackwardsRateConstant} the determines the speed of the dissociation of the complex.
 * Complex building reactions are {@link UpdatableSpecific} and supply {@link IndependentUpdate}s.
 * <pre>
 *  // From: Lauffenburger, Douglas A., and Jennifer J. Linderman.
 *  //       Receptors: models for binding, trafficking, and signaling. Oxford University Press, 1996.
 *  //       Table on Page 30
 *  // prazosin
 *  ChemicalEntity ligand = new SmallMolecule.Builder("ligand")
 *      .name("prazosin")
 *      .additionalIdentifier(new ChEBIIdentifier("CHEBI:8364"))
 *      .build();
 *  // alpha-1 adrenergic receptor
 *  Receptor receptor = new Receptor.Builder("receptor")
 *      .name("alpha-1 adrenergic receptor")
 *      .additionalIdentifier(new UniProtIdentifier("P35348"))
 *      .build();
 *  // the forwards rate constants
 *  RateConstant forwardsRate = RateConstant.create(2.4e8)
 *      .forward().secondOrder()
 *      .concentrationUnit(MOLE_PER_LITRE)
 *      .timeUnit(MINUTE)
 *      .build();
 *  // the backwards rate constants
 *  RateConstant backwardsRate = RateConstant.create(0.018)
 *      .backward().firstOrder()
 *      .timeUnit(MINUTE)
 *      .build();
 *  // create and add module
 *  ComplexBuildingReaction reaction = ComplexBuildingReaction.inSimulation(simulation)
 *      .identifier("binding reaction")
 *      .of(ligand, forwardsRate)
 *      .in(OUTER)
 *      .by(receptor, backwardsRate)
 *      .to(MEMBRANE)
 *      .build(); </pre>
 *
 * @author cl
 */
public class ComplexBuildingReaction extends ConcentrationBasedModule<UpdatableDeltaFunction> {

    public static BindeeSelection inSimulation(Simulation simulation) {
        return new ComplexBuildingReactionBuilder(simulation);
    }

    private RateConstant forwardsReactionRate;
    private RateConstant backwardsReactionRate;

    private ChemicalEntity binder;
    private CellTopology binderTopology;

    private ChemicalEntity bindee;
    private CellTopology bindeeTopology;

    private ComplexedChemicalEntity complex;

    public void initialize() {
        // apply
        // TODO apply condition ?
        setApplicationCondition(updatable -> true);
        // function
        // TODO apply condition this::containsReactants
        UpdatableDeltaFunction function = new UpdatableDeltaFunction(this::calculateDeltas, container -> true);
        addDeltaFunction(function);
        // feature
        getRequiredFeatures().add(ForwardsRateConstant.class);
        getRequiredFeatures().add(BackwardsRateConstant.class);
        // in no complex has been set create it
        if (complex == null) {
            complex = new ComplexedChemicalEntity.Builder(binder.getIdentifier().getIdentifier() + ":" + bindee.getIdentifier().getIdentifier())
                    .addAssociatedPart(binder)
                    .addAssociatedPart(bindee)
                    .build();
        }
        // reference entities for this module
        addReferencedEntity(bindee);
        addReferencedEntity(binder);
        addReferencedEntity(complex);
        // reference module in simulation
        addModuleToSimulation();
    }

    private boolean containsReactants(ConcentrationContainer concentrationContainer) {
        // binder and complex are zero
        if (concentrationContainer.get(binderTopology, binder).getValue().doubleValue() == 0.0
                && concentrationContainer.get(binderTopology, complex).getValue().doubleValue() == 0.0) {
            return false;
        }
        // bindee and complex are zero
        return !(concentrationContainer.get(bindeeTopology, bindee).getValue().doubleValue() == 0.0)
                || !(concentrationContainer.get(binderTopology, complex).getValue().doubleValue() == 0.0);
    }

    private Map<ConcentrationDeltaIdentifier, ConcentrationDelta> calculateDeltas(ConcentrationContainer concentrationContainer) {
        Map<ConcentrationDeltaIdentifier, ConcentrationDelta> deltas = new HashMap<>();
        Updatable currentUpdatable = supplier.getCurrentUpdatable();
        if (currentUpdatable instanceof Vesicle) {
            handlePartialDistributionInVesicles(deltas, (Vesicle) currentUpdatable);
        } else {
            double velocity = calculateVelocity(concentrationContainer);
            // bindee concentration
            CellSubsection bindeeSubsection = concentrationContainer.getSubsection(bindeeTopology);
            addDelta(deltas, new ConcentrationDeltaIdentifier(supplier.getCurrentUpdatable(), bindeeSubsection, bindee), -velocity);
            // binder concentration
            CellSubsection binderSubsection = concentrationContainer.getSubsection(binderTopology);
            addDelta(deltas, new ConcentrationDeltaIdentifier(supplier.getCurrentUpdatable(), binderSubsection, binder), -velocity);
            // complex concentration
            addDelta(deltas, new ConcentrationDeltaIdentifier(supplier.getCurrentUpdatable(), binderSubsection, complex), velocity);
        }
        return deltas;
    }

    private void handlePartialDistributionInVesicles(Map<ConcentrationDeltaIdentifier, ConcentrationDelta> deltas, Vesicle vesicle) {
        Map<AutomatonNode, Double> associatedNodes = vesicle.getAssociatedNodes();
        ConcentrationContainer vesicleContainer = vesicle.getConcentrationContainer();
        for (Map.Entry<AutomatonNode, Double> entry : associatedNodes.entrySet()) {
            AutomatonNode node = entry.getKey();
            ConcentrationContainer nodeContainer;
            if (supplier.isStrutCalculation()) {
                nodeContainer = getScope().getHalfStepConcentration(node);
            } else {
                nodeContainer = node.getConcentrationContainer();
            }
            // assuming equal distribution of entities on the membrane surface the fraction of the associated surface is
            // used to scale the velocity
            double velocity = calculateVelocity(vesicleContainer, nodeContainer) * entry.getValue();
            if (bindeeTopology.equals(CellTopology.MEMBRANE)) {
                // bindee concentration in vesicle
                CellSubsection bindeeSubsection = vesicleContainer.getMembraneSubsection();
                addDelta(deltas, new ConcentrationDeltaIdentifier(vesicle, bindeeSubsection, bindee), -velocity);
            } else {
                // bindee concentration in node
                CellSubsection bindeeSubsection = nodeContainer.getSubsection(bindeeTopology);
                addDelta(deltas, new ConcentrationDeltaIdentifier(node, bindeeSubsection, bindee), -velocity);
            }
            if (binderTopology.equals(CellTopology.MEMBRANE)) {
                // binder concentration in vesicle
                CellSubsection binderSubsection = vesicleContainer.getMembraneSubsection();
                addDelta(deltas, new ConcentrationDeltaIdentifier(vesicle, binderSubsection, binder), -velocity);
                // complex concentration in vesicle
                addDelta(deltas, new ConcentrationDeltaIdentifier(vesicle, binderSubsection, complex), velocity);
            } else {
                // binder concentration in node
                CellSubsection binderSubsection = nodeContainer.getSubsection(binderTopology);
                addDelta(deltas, new ConcentrationDeltaIdentifier(node, binderSubsection, binder), -velocity);
                // complex concentration in node
                addDelta(deltas, new ConcentrationDeltaIdentifier(node, binderSubsection, complex), velocity);
            }
        }
    }

    private double calculateVelocity(ConcentrationContainer vesicleContainer, ConcentrationContainer nodeContainer) {
        // get rates
        final double forwardsRateConstant = getScaledForwardsReactionRate().getValue().doubleValue();
        final double backwardsRateConstant = getScaledBackwardsReactionRate().getValue().doubleValue();

        // get concentrations
        double bindeeConcentration;
        if (bindeeTopology.equals(CellTopology.MEMBRANE)) {
            bindeeConcentration = vesicleContainer.get(CellTopology.MEMBRANE, bindee).getValue().doubleValue();
        } else {
            bindeeConcentration = nodeContainer.get(bindeeTopology, bindee).getValue().doubleValue();
        }
        double binderConcentration;
        double complexConcentration;
        if (binderTopology.equals(CellTopology.MEMBRANE)) {
            binderConcentration = vesicleContainer.get(CellTopology.MEMBRANE, binder).getValue().doubleValue();
            complexConcentration = vesicleContainer.get(CellTopology.MEMBRANE, complex).getValue().doubleValue();
        } else {
            binderConcentration = nodeContainer.get(binderTopology, binder).getValue().doubleValue();
            complexConcentration = nodeContainer.get(binderTopology, complex).getValue().doubleValue();
        }

        // calculate velocity
        return forwardsRateConstant * binderConcentration * bindeeConcentration - backwardsRateConstant * complexConcentration;
    }

    private double calculateVelocity(ConcentrationContainer concentrationContainer) {
        // get rates
        final double forwardsRateConstant = getScaledForwardsReactionRate().getValue().doubleValue();
        final double backwardsRateConstant = getScaledBackwardsReactionRate().getValue().doubleValue();
        // get concentrations
        final double bindeeConcentration = concentrationContainer.get(bindeeTopology, bindee).getValue().doubleValue();
        final double binderConcentration = concentrationContainer.get(binderTopology, binder).getValue().doubleValue();
        final double complexConcentration = concentrationContainer.get(binderTopology, complex).getValue().doubleValue();
        // calculate velocity
        return forwardsRateConstant * binderConcentration * bindeeConcentration - backwardsRateConstant * complexConcentration;
    }

    private void addDelta(Map<ConcentrationDeltaIdentifier, ConcentrationDelta> deltas, ConcentrationDeltaIdentifier identifier, double concentrationDelta) {
        if (deltas.containsKey(identifier)) {
            deltas.put(identifier, deltas.get(identifier).add(concentrationDelta));
        } else {
            deltas.put(identifier, new ConcentrationDelta(this, identifier.getSubsection(), identifier.getEntity(),
                    UnitRegistry.concentration(concentrationDelta)));
        }
    }

    public ComplexedChemicalEntity getComplex() {
        return complex;
    }

    public void setComplex(ComplexedChemicalEntity complex) {
        this.complex = complex;
    }

    public ChemicalEntity getBinder() {
        return binder;
    }

    public void setBinder(ChemicalEntity binder) {
        this.binder = binder;
    }

    public CellTopology getBinderTopology() {
        return binderTopology;
    }

    public void setBinderTopology(CellTopology binderTopology) {
        this.binderTopology = binderTopology;
    }

    public ChemicalEntity getBindee() {
        return bindee;
    }

    public void setBindee(ChemicalEntity bindee) {
        this.bindee = bindee;
    }

    public CellTopology getBindeeTopology() {
        return bindeeTopology;
    }

    public void setBindeeTopology(CellTopology bindeeTopology) {
        this.bindeeTopology = bindeeTopology;
    }

    public String getReactionString() {
        String substrates = binder.getIdentifier() + " + " + bindee.getIdentifier();
        String products = complex.getIdentifier().toString();
        return substrates + " \u21CB " + products;
    }

    @Override
    public void checkFeatures() {
        boolean forwardsRateFound = false;
        boolean backwardsRateFound = false;
        for (Feature<?> feature : getFeatures()) {
            // any forwards rate constant
            if (feature instanceof ForwardsRateConstant) {
                forwardsRateFound = true;
            }
            // any backwards rate constant
            if (feature instanceof BackwardsRateConstant) {
                backwardsRateFound = true;
            }
        }
        if (!forwardsRateFound || !backwardsRateFound) {
            throw new FeatureUnassignableException("Required reaction rates unavailable for reaction " + getIdentifier() + ".");
        }
    }

    private Quantity getScaledForwardsReactionRate() {
        if (forwardsReactionRate == null) {
            for (Feature<?> feature : getFeatures()) {
                // any forwards rate constant
                if (feature instanceof ForwardsRateConstant) {
                    forwardsReactionRate = (RateConstant) feature;
                    break;
                }
            }
        }
        if (supplier.isStrutCalculation()) {
            return forwardsReactionRate.getHalfScaledQuantity();
        }
        return forwardsReactionRate.getScaledQuantity();
    }

    private Quantity getScaledBackwardsReactionRate() {
        if (backwardsReactionRate == null) {
            for (Feature<?> feature : getFeatures()) {
                // any forwards rate constant
                if (feature instanceof BackwardsRateConstant) {
                    backwardsReactionRate = (RateConstant) feature;
                    break;
                }
            }
        }
        if (supplier.isStrutCalculation()) {
            return backwardsReactionRate.getHalfScaledQuantity();
        }
        return backwardsReactionRate.getScaledQuantity();
    }


    public static ModuleBuilder getBuilder(Simulation simulation) {
        return new ComplexBuildingReactionBuilder(simulation);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getIdentifier() + " (" + getReactionString() + ")";
    }

    public interface BindeeSelection {
        BindeeSelection identifier(String identifier);

        BindeeSectionSelection of(ChemicalEntity bindee);

        BindeeSectionSelection of(ChemicalEntity bindee, RateConstant forwardsRateConstant);
    }

    public interface BindeeSectionSelection {
        BinderSelection in(CellTopology bindeeTopology);
    }

    public interface BinderSelection {
        BinderSectionSelection by(ChemicalEntity binder);

        BinderSectionSelection by(ChemicalEntity binder, RateConstant forwardsRateConstant);
    }

    public interface BinderSectionSelection {
        BuilderStep to(CellTopology binderTopology);
    }

    public interface BuilderStep {
        BuilderStep formingComplex(ComplexedChemicalEntity complex);

        ComplexBuildingReaction build();
    }

    public static class ComplexBuildingReactionBuilder implements BinderSelection, BinderSectionSelection, BindeeSelection, BindeeSectionSelection, BuilderStep, ModuleBuilder {

        private ComplexBuildingReaction module;

        public ComplexBuildingReactionBuilder(Simulation simulation) {
            createModule(simulation);
        }

        @Override
        public ComplexBuildingReaction getModule() {
            return module;
        }

        @Override
        public ComplexBuildingReaction createModule(Simulation simulation) {
            module = ModuleFactory.setupModule(ComplexBuildingReaction.class,
                    ModuleFactory.Scope.SEMI_NEIGHBOURHOOD_DEPENDENT,
                    ModuleFactory.Specificity.UPDATABLE_SPECIFIC);
            module.setSimulation(simulation);
            return module;
        }

        @Override
        public BindeeSelection identifier(String identifier) {
            module.setIdentifier(identifier);
            return this;
        }

        @Override
        public BindeeSectionSelection of(ChemicalEntity bindee) {
            module.bindee = bindee;
            return this;
        }

        @Override
        public BindeeSectionSelection of(ChemicalEntity binder, RateConstant forwardsRateConstant) {
            module.setFeature(forwardsRateConstant);
            return of(binder);
        }

        @Override
        public BinderSelection in(CellTopology binderTopology) {
            module.bindeeTopology = binderTopology;
            return this;
        }

        @Override
        public BinderSectionSelection by(ChemicalEntity binder) {
            module.binder = binder;
            return this;
        }

        @Override
        public BinderSectionSelection by(ChemicalEntity binder, RateConstant backwardsRateConstant) {
            module.setFeature(backwardsRateConstant);
            return by(binder);
        }

        @Override
        public BuilderStep to(CellTopology binderTopology) {
            module.binderTopology = binderTopology;
            return this;
        }

        @Override
        public BuilderStep formingComplex(ComplexedChemicalEntity complex) {
            module.complex = complex;
            return this;
        }

        @Override
        public ComplexBuildingReaction build() {
            module.initialize();
            return module;
        }
    }

}
