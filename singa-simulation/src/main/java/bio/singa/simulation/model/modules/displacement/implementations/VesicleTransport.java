package bio.singa.simulation.model.modules.displacement.implementations;

import bio.singa.core.utility.Pair;
import bio.singa.features.units.UnitRegistry;
import bio.singa.mathematics.vectors.Vector2D;
import bio.singa.simulation.features.MotorMovementVelocity;
import bio.singa.simulation.model.agents.pointlike.Vesicle;
import bio.singa.simulation.model.agents.pointlike.VesicleStateRegistry;
import bio.singa.simulation.model.modules.displacement.DisplacementBasedModule;
import bio.singa.simulation.model.modules.displacement.DisplacementDelta;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Speed;
import java.util.ListIterator;

import static bio.singa.simulation.features.MotorPullDirection.Direction.MINUS;

/**
 * @author cl
 */
public class VesicleTransport extends DisplacementBasedModule {

    public VesicleTransport() {
        // delta function
        addDeltaFunction(this::calculateDisplacement, VesicleTransport::isAttachedToFilament);
        // feature
        getRequiredFeatures().add(MotorMovementVelocity.class);
    }

    private static boolean isAttachedToFilament(Vesicle vesicle) {
        return vesicle.getVesicleState().equals(VesicleStateRegistry.MICROTUBULE_ATTACHED) ||
                vesicle.getVesicleState().equals(VesicleStateRegistry.ACTIN_ATTACHED);
    }

    @Override
    public void checkFeatures() {

    }

    public DisplacementDelta calculateDisplacement(Vesicle vesicle) {
        // move along with segmental iterator
        // x(t+dt) = x(t) + v * dt * y
        // new position = current position + velocity * time step size * unit direction
        ListIterator<Vector2D> segmentIterator = vesicle.getSegmentIterator();
        // path are sorted, such that the first element is the + end and the last element is the - end
        Vector2D guide = null;
        if (vesicle.getTargetDirection() == MINUS) {
            // to get to minus go to next
            Pair<Vector2D> surroundingSegments = scoutMinusEnd(segmentIterator);
            Vector2D currentPosition = vesicle.getCurrentPosition();
            double distanceNext = currentPosition.distanceTo(surroundingSegments.getFirst());
            if (surroundingSegments.getSecond() != null) {
                double distanceAfterNext = currentPosition.distanceTo(surroundingSegments.getSecond());
                if (distanceAfterNext < distanceNext) {
                    segmentIterator.next();
                }
                guide = surroundingSegments.getSecond().subtract(currentPosition).normalize();
            }
        } else {
            // to get to plus go to previous
            Pair<Vector2D> surroundingSegments = scoutPlusEnd(segmentIterator);
            Vector2D currentPosition = vesicle.getCurrentPosition();
            double distancePrevious = currentPosition.distanceTo(surroundingSegments.getFirst());
            if (surroundingSegments.getSecond() != null) {
                double distanceAfterPrevious = currentPosition.distanceTo(surroundingSegments.getSecond());
                if (distanceAfterPrevious < distancePrevious) {
                    segmentIterator.previous();
                }
                guide = surroundingSegments.getSecond().subtract(currentPosition).normalize();
            }
        }
        if (guide == null) {
            return new DisplacementDelta(this, new Vector2D(0.0, 0.0));
        }
        Quantity<Speed> speed = getScaledFeature(MotorMovementVelocity.class);
        Quantity<Length> distance = Quantities.getQuantity(speed.getValue().doubleValue(), UnitRegistry.getSpaceUnit());
        return new DisplacementDelta(this, guide.multiply(distance.getValue().doubleValue()));
    }

    private Pair<Vector2D> scoutMinusEnd(ListIterator<Vector2D> segmentIterator) {
        Vector2D next = null;
        if (segmentIterator.hasNext()) {
            next = segmentIterator.next();
        }
        Vector2D afterNext = null;
        if (segmentIterator.hasNext()) {
            afterNext = segmentIterator.next();
        }
        segmentIterator.previous();
        segmentIterator.previous();
        return new Pair<>(next, afterNext);
    }

    private Pair<Vector2D> scoutPlusEnd(ListIterator<Vector2D> segmentIterator) {
        Vector2D previous = null;
        if (segmentIterator.hasPrevious()) {
            previous = segmentIterator.previous();
        }
        Vector2D beforePrevious = null;
        if (segmentIterator.hasPrevious()) {
            beforePrevious = segmentIterator.previous();
        }
        segmentIterator.next();
        segmentIterator.next();
        return new Pair<>(previous, beforePrevious);
    }

    @Override
    public String toString() {
        return "Vesicle Transport";
    }

}
