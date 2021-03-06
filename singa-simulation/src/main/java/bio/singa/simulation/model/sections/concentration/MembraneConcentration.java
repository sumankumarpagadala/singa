package bio.singa.simulation.model.sections.concentration;

import bio.singa.chemistry.entities.ChemicalEntity;
import bio.singa.features.model.Evidence;
import bio.singa.features.quantities.MolarConcentration;
import bio.singa.simulation.model.agents.pointlike.Vesicle;
import bio.singa.simulation.model.graphs.AutomatonNode;
import bio.singa.simulation.model.sections.CellRegion;
import bio.singa.simulation.model.sections.CellSubsection;
import bio.singa.simulation.model.simulation.Simulation;
import bio.singa.simulation.model.simulation.Updatable;

import javax.measure.Quantity;
import javax.measure.quantity.Area;
import java.util.Objects;

/**
 * @author cl
 */
public class MembraneConcentration implements InitialConcentration {

    private CellRegion region;

    private ChemicalEntity entity;

    private Quantity<Area> area;

    private double numberOfMolecules;

    private Evidence evidence;

    public MembraneConcentration(CellRegion region, ChemicalEntity entity, Quantity<Area> area, double numberOfMolecules, Evidence evidence) {
        this.region = region;
        this.entity = entity;
        this.area = area;
        this.numberOfMolecules = numberOfMolecules;
        this.evidence = evidence;
    }

    public CellRegion getRegion() {
        return region;
    }

    public void setRegion(CellRegion region) {
        this.region = region;
    }

    public ChemicalEntity getEntity() {
        return entity;
    }

    public void setEntity(ChemicalEntity entity) {
        this.entity = entity;
    }

    public Quantity<Area> getArea() {
        return area;
    }

    public void setArea(Quantity<Area> area) {
        this.area = area;
    }

    public double getNumberOfMolecules() {
        return numberOfMolecules;
    }

    public void setNumberOfMolecules(double numberOfMolecules) {
        this.numberOfMolecules = numberOfMolecules;
    }

    public Evidence getEvidence() {
        return evidence;
    }

    public void setEvidence(Evidence evidence) {
        this.evidence = evidence;
    }

    @Override
    public void initialize(Simulation simulation) {
        for (Updatable updatable : simulation.getUpdatables()) {
            // skip wrong regions
            if (!updatable.getCellRegion().equals(region)) {
                continue;
            }
            CellSubsection membraneSubsection = updatable.getConcentrationContainer().getMembraneSubsection();
            // skip non membrane regions
            if (membraneSubsection == null) {
                continue;
            }

            // get representative area
            Quantity<Area> updatableArea;
            if (updatable instanceof Vesicle) {
                updatableArea = ((Vesicle) updatable).getArea();
            } else {
                updatableArea = ((AutomatonNode) updatable).getMembraneArea();
            }
            // correlate
            Quantity<?> adjustedMolecules = updatableArea.to(area.getUnit()).multiply(numberOfMolecules).divide(area);
            Quantity < MolarConcentration > concentration = MolarConcentration.moleculesToConcentration(adjustedMolecules.getValue().doubleValue());
            updatable.getConcentrationContainer().initialize(membraneSubsection, entity, concentration);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MembraneConcentration that = (MembraneConcentration) o;
        return Objects.equals(region, that.region) &&
                Objects.equals(entity, that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(region, entity);
    }
}
