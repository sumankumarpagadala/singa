package bio.singa.simulation.model.modules.displacement.implementations;

import bio.singa.chemistry.entities.ChemicalEntity;
import bio.singa.chemistry.features.reactions.RateConstant;
import bio.singa.features.parameters.Environment;
import bio.singa.features.quantities.MolarConcentration;
import bio.singa.mathematics.vectors.Vector2D;
import bio.singa.simulation.features.DefautFeatureSources;
import bio.singa.simulation.features.endocytosis.ActinBoostVelocity;
import bio.singa.simulation.model.modules.displacement.DisplacementBasedModule;
import bio.singa.simulation.model.modules.displacement.DisplacementDelta;
import bio.singa.simulation.model.modules.displacement.Vesicle;
import bio.singa.simulation.model.sections.CellTopology;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;

import static bio.singa.simulation.model.modules.displacement.Vesicle.AttachmentState.ACTIN_DEPOLYMERIZATION;
import static bio.singa.simulation.model.modules.displacement.Vesicle.AttachmentState.UNATTACHED;
import static bio.singa.simulation.model.modules.displacement.Vesicle.TargetDirection.MINUS;
import static tec.uom.se.unit.Units.SECOND;

/**
 * @author cl
 */
public class EndocytosisActinBoost extends DisplacementBasedModule {

    /**
     * Average vesicle with a radius of 50 nm was coated by 60 clathrins. The depolymerization finished after about
     * 11 seconds.
     *
     * 9.963234242562985E-23 is the concentration of 60 clathrin molecules scaled to 1 mol/um^3
     */
    public static final RateConstant DEFAULT_CLATHRIN_DEPOLYMERIZATION_RATE = RateConstant.create(9.963234242562985E-23/11.0)
            .forward()
            .zeroOrder()
            .concentrationUnit(Environment.getConcentrationUnit())
            .timeUnit(SECOND)
            .origin(DefautFeatureSources.EHRLICH2004)
            .build();

    private Quantity<Speed> scaledVelocity;
    private ChemicalEntity decayingEntity;

    public EndocytosisActinBoost() {
        // delta function
        addDeltaFunction(this::calculateDisplacement, vesicle -> vesicle.getAttachmentState() == ACTIN_DEPOLYMERIZATION);
        // feature
        getRequiredFeatures().add(ActinBoostVelocity.class);
    }

    public void setDecayingEntity(ChemicalEntity decayingEntity) {
        this.decayingEntity = decayingEntity;
    }

    @Override
    public void calculateUpdates() {
        scaledVelocity = getScaledFeature(ActinBoostVelocity.class).multiply(2.0).divide(60.0);
        super.calculateUpdates();
    }

    public DisplacementDelta calculateDisplacement(Vesicle vesicle) {
        // calculate speed based on clathrins available
        double numberOfClathrins = MolarConcentration.concentrationToMolecules(vesicle.getConcentrationContainer().get(CellTopology.MEMBRANE, decayingEntity),
                Environment.getSubsectionVolume()).getValue().doubleValue();
        if (numberOfClathrins < 1) {
            vesicle.setAttachmentState(UNATTACHED);
            // TODO alter for vesicles moving from centre to outside
            vesicle.setTargetDirection(MINUS);
        }
        Quantity<Speed> systemSpeed = scaledVelocity.multiply(numberOfClathrins);
        Quantity<Length> distance = Quantities.getQuantity(systemSpeed.getValue().doubleValue(), Environment.getNodeDistanceUnit());
        // determine direction
        Vector2D centre = simulation.getSimulationRegion().getCentre();
        Vector2D direction = centre.subtract(vesicle.getCurrentPosition()).normalize();
        // determine delta
        Vector2D delta = direction.multiply(Environment.convertSystemToSimulationScale(distance));
        return new DisplacementDelta(this, delta);
    }

    @Override
    public String toString() {
        return "Actin boost after endocytosis";
    }

}