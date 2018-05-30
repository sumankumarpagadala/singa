package de.bioforscher.singa.simulation.model.newsections;

import de.bioforscher.singa.chemistry.descriptive.entities.ChemicalEntity;
import de.bioforscher.singa.features.parameters.Environment;
import de.bioforscher.singa.features.quantities.MolarConcentration;

import javax.measure.Quantity;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author cl
 */
public class ConcentrationPool {

    /**
     * The associated concentrations
     */
    private Map<ChemicalEntity, Quantity<MolarConcentration>> concentrations;

    public ConcentrationPool() {
        concentrations = new HashMap<>();
    }

    public Set<ChemicalEntity> getReferencedEntities() {
        return concentrations.keySet();
    }

    public Quantity<MolarConcentration> get(ChemicalEntity entity) {
        return concentrations.getOrDefault(entity, Environment.emptyConcentration());
    }

    public void set(ChemicalEntity entity, Quantity<MolarConcentration> concentration) {
        concentrations.put(entity, concentration);
    }
    
}
