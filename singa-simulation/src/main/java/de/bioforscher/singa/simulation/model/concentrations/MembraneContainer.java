package de.bioforscher.singa.simulation.model.concentrations;

import de.bioforscher.singa.chemistry.descriptive.entities.ChemicalEntity;
import de.bioforscher.singa.features.quantities.MolarConcentration;
import de.bioforscher.singa.features.units.UnitProvider;
import de.bioforscher.singa.simulation.model.compartments.CellSection;
import de.bioforscher.singa.simulation.model.compartments.Membrane;
import tec.units.ri.quantity.Quantities;

import javax.measure.Quantity;
import java.util.*;

/**
 * In this representation of a membrane is divided into four parts. An outer aqueous phase, an inner aqueous phase, the
 * layer of the membrane oriented to the outer phase, and the layer of the membrane oriented to the inner phase.
 *
 * This class is modelled after the representation in:
 * Dickson 2016 - Structure-kinetic relationships of passive membrane permeation from multiscale modeling.
 *
 * @author cl
 */
public class MembraneContainer implements ConcentrationContainer {

    private CellSection outerPhaseSection;
    private CellSection innerPhaseSection;
    private Membrane membrane;

    private Map<ChemicalEntity<?>, Quantity<MolarConcentration>> outerPhase;
    private Map<ChemicalEntity<?>, Quantity<MolarConcentration>> outerLayer;
    private Map<ChemicalEntity<?>, Quantity<MolarConcentration>> innerLayer;
    private Map<ChemicalEntity<?>, Quantity<MolarConcentration>> innerPhase;

    public MembraneContainer(CellSection outerPhaseSection, CellSection innerPhaseSection, Membrane membrane) {
        this.outerPhaseSection = outerPhaseSection;
        this.innerPhaseSection = innerPhaseSection;
        this.membrane = membrane;

        outerPhase = new HashMap<>();
        outerLayer = new HashMap<>();
        innerLayer = new HashMap<>();
        innerPhase = new HashMap<>();
    }

    public MembraneContainer(MembraneContainer container) {
        this(container.outerPhaseSection, container.innerPhaseSection, container.membrane);
    }

    @Override
    public Quantity<MolarConcentration> getConcentration(ChemicalEntity chemicalEntity) {
        Quantity<MolarConcentration> concentrationSum = getAvailableConcentration(outerPhaseSection, chemicalEntity);
        concentrationSum = concentrationSum.add(getAvailableConcentration(innerPhaseSection, chemicalEntity));
        concentrationSum = concentrationSum.add(getAvailableConcentration(membrane.getInnerLayer(), chemicalEntity));
        concentrationSum = concentrationSum.add(getAvailableConcentration(membrane.getOuterLayer(), chemicalEntity));
        return concentrationSum.divide(4.0);
    }

    public Quantity<MolarConcentration> getOuterPhaseConcentration(ChemicalEntity chemicalEntity) {
        if (outerPhase.containsKey(chemicalEntity)) {
            return outerPhase.get(chemicalEntity);
        }
        return Quantities.getQuantity(0.0, UnitProvider.MOLE_PER_LITRE);
    }

    public Quantity<MolarConcentration> getInnerPhaseConcentration(ChemicalEntity chemicalEntity) {
        if (innerPhase.containsKey(chemicalEntity)) {
            return innerPhase.get(chemicalEntity);
        }
        return Quantities.getQuantity(0.0, UnitProvider.MOLE_PER_LITRE);
    }

    public Quantity<MolarConcentration> getOuterMembraneLayerConcentration(ChemicalEntity chemicalEntity) {
        if (outerLayer.containsKey(chemicalEntity)) {
            return outerLayer.get(chemicalEntity);
        }
        return Quantities.getQuantity(0.0, UnitProvider.MOLE_PER_LITRE);
    }

    public Quantity<MolarConcentration> getInnerMembraneLayerConcentration(ChemicalEntity chemicalEntity) {
        if (innerLayer.containsKey(chemicalEntity)) {
            return innerLayer.get(chemicalEntity);
        }
        return Quantities.getQuantity(0.0, UnitProvider.MOLE_PER_LITRE);
    }

    @Override
    public Map<ChemicalEntity<?>, Quantity<MolarConcentration>> getAllConcentrationsForSection(CellSection cellSection) {
        if (cellSection.equals(outerPhaseSection)) {
            return outerPhase;
        } else if (cellSection.equals(innerPhaseSection)) {
            return innerPhase;
        } else if (cellSection.equals(membrane)) {
            Map<ChemicalEntity<?>, Quantity<MolarConcentration>> concentrations = new HashMap<>();
            for (Map.Entry<ChemicalEntity<?>, Quantity<MolarConcentration>> entry : innerLayer.entrySet()) {
                concentrations.put(entry.getKey(), entry.getValue().add(outerLayer.get(entry.getKey())).divide(2.0));
            }
            return concentrations;
        } else if (cellSection.equals(membrane.getInnerLayer())) {
            return innerLayer;
        } else if (cellSection.equals(membrane.getOuterLayer())) {
            return outerLayer;
        }
        return Collections.emptyMap();
    }

    @Override
    public Quantity<MolarConcentration> getAvailableConcentration(CellSection cellSection, ChemicalEntity chemicalEntity) {
        if (cellSection.equals(outerPhaseSection)) {
            if (outerPhase.containsKey(chemicalEntity)) {
                return outerPhase.get(chemicalEntity);
            }
        } else if (cellSection.equals(innerPhaseSection)) {
            if (innerPhase.containsKey(chemicalEntity)) {
                return innerPhase.get(chemicalEntity);
            }
        } else if (cellSection.equals(membrane)) {
            Quantity<MolarConcentration> concentrationSum;
            if (innerLayer.containsKey(chemicalEntity)) {
                concentrationSum = (innerLayer.get(chemicalEntity));
                concentrationSum = concentrationSum.add(outerLayer.get(chemicalEntity));
                return concentrationSum.divide(2.0);
            }
        } else if (cellSection.equals(membrane.getInnerLayer())) {
            if (innerLayer.containsKey(chemicalEntity)) {
                return innerLayer.get(chemicalEntity);
            }
        } else if (cellSection.equals(membrane.getOuterLayer())) {
            if (outerLayer.containsKey(chemicalEntity)) {
                return outerLayer.get(chemicalEntity);
            }
        }
        return Quantities.getQuantity(0.0, UnitProvider.MOLE_PER_LITRE);
    }

    @Override
    public void setConcentration(ChemicalEntity chemicalEntity, Quantity<MolarConcentration> concentration) {
        outerPhase.put(chemicalEntity, concentration);
        outerLayer.put(chemicalEntity, concentration);
        innerLayer.put(chemicalEntity, concentration);
        innerPhase.put(chemicalEntity, concentration);
    }

    @Override
    public void setAvailableConcentration(CellSection cellSection, ChemicalEntity chemicalEntity, Quantity<MolarConcentration> concentration) {
        if (cellSection.equals(outerPhaseSection)) {
            outerPhase.put(chemicalEntity, concentration);
        } else if (cellSection.equals(innerPhaseSection)) {
            innerPhase.put(chemicalEntity, concentration);
        } else if (cellSection.equals(membrane)) {
            innerLayer.put(chemicalEntity, concentration);
            outerLayer.put(chemicalEntity, concentration);
        } else if (cellSection.equals(membrane.getInnerLayer())) {
            innerLayer.put(chemicalEntity, concentration);
        } else if (cellSection.equals(membrane.getOuterLayer())) {
            outerLayer.put(chemicalEntity, concentration);
        }
    }

    @Override
    public Set<ChemicalEntity<?>> getAllReferencedEntities() {
        Set<ChemicalEntity<?>> entities = new HashSet<>();
        entities.addAll(innerLayer.keySet());
        entities.addAll(innerPhase.keySet());
        entities.addAll(outerPhase.keySet());
        entities.addAll(outerLayer.keySet());
        return entities;
    }

    @Override
    public Set<CellSection> getAllReferencedSections() {
        Set<CellSection> sections = new HashSet<>();
        sections.add(innerPhaseSection);
        sections.add(outerPhaseSection);
        sections.add(membrane.getInnerLayer());
        sections.add(membrane.getOuterLayer());
        return sections;
    }

    public Membrane getMembrane() {
        return membrane;
    }

    public CellSection getOuterPhaseSection() {
        return outerPhaseSection;
    }

    public CellSection getOuterLayerSection() {
        return membrane.getOuterLayer();
    }

    public CellSection getInnerLayerSection() {
        return membrane.getInnerLayer();
    }

    public CellSection getInnerPhaseSection() {
        return innerPhaseSection;
    }

    @Override
    public Map<ChemicalEntity<?>, Quantity<MolarConcentration>> getAllConcentrations() {
        Map<ChemicalEntity<?>, Quantity<MolarConcentration>> result = new HashMap<>();
        for (ChemicalEntity<?> chemicalEntity : getAllReferencedEntities()) {
            result.put(chemicalEntity, getConcentration(chemicalEntity));
        }
        return result;
    }

    @Override
    public MembraneContainer getCopy() {
        return new MembraneContainer(this);
    }


}
