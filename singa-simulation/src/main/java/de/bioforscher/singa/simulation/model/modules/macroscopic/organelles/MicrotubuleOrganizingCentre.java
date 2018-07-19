package de.bioforscher.singa.simulation.model.modules.macroscopic.organelles;

import de.bioforscher.singa.mathematics.geometry.faces.Circle;
import de.bioforscher.singa.mathematics.vectors.Vector2D;
import de.bioforscher.singa.simulation.model.modules.macroscopic.filaments.FilamentLayer;
import de.bioforscher.singa.simulation.model.modules.macroscopic.membranes.MacroscopicMembraneLayer;
import de.bioforscher.singa.simulation.model.simulation.Simulation;

/**
 * @author cl
 */
public class MicrotubuleOrganizingCentre {

    private Simulation simulation;
    private MacroscopicMembraneLayer membraneLayer;

    private Circle circleRepresentation;
    private int initialFilaments;


    public MicrotubuleOrganizingCentre(Simulation simulation, MacroscopicMembraneLayer membraneLayer, Circle circleRepresentation, int initialFilaments) {
        this.simulation = simulation;
        this.circleRepresentation = circleRepresentation;
        this.initialFilaments = initialFilaments;
        this.membraneLayer = membraneLayer;
    }

    public FilamentLayer initializeFilaments() {
        FilamentLayer filamentLayer = new FilamentLayer(simulation, membraneLayer);
        // initialize filaments
        int currentFilaments = 0;
        Vector2D centre = circleRepresentation.getMidpoint();
        while (currentFilaments != initialFilaments) {
            // random point on circle circumference
            double angle = Math.random() * Math.PI * 2;
            double x = Math.cos(angle) * circleRepresentation.getRadius();
            double y = Math.sin(angle) * circleRepresentation.getRadius();
            // set starting position and direction
            Vector2D initialPosition = centre.add(new Vector2D(x, y));
            filamentLayer.addFilament(initialPosition, centre.subtract(initialPosition));
            // increment filaments
            currentFilaments++;
        }
        // grow filaments
        while (filamentLayer.hasGrowingFilaments()) {
            filamentLayer.nextEpoch();
        }
        return filamentLayer;
    }

}
