package de.bioforscher.singa.simulation.model.compartments;

import de.bioforscher.singa.chemistry.descriptive.entities.ChemicalEntity;
import de.bioforscher.singa.chemistry.descriptive.entities.Protein;
import de.bioforscher.singa.core.identifier.SimpleStringIdentifier;
import de.bioforscher.singa.features.quantities.Permeability;

import java.util.Map;

/**
 * @author cl
 */
public class MembraneProtein extends Protein {

    private EnclosedCompartment sourceEnclosedCompartment;
    private EnclosedCompartment targetEnclosedCompartment;

    private Map<ChemicalEntity, Permeability> basePermeability;

    /**
     * Creates a new Chemical Entity with the given identifier.
     *
     * @param identifier The identifier.
     */
    public MembraneProtein(SimpleStringIdentifier identifier) {
        super(identifier);
    }

    // TODO maybe use http://stackoverflow.com/questions/17164375/subclassing-a-java-builder-class

}