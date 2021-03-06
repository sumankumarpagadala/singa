package bio.singa.chemistry.entities;

import bio.singa.features.identifiers.SimpleStringIdentifier;
import bio.singa.features.model.Feature;

import java.util.*;

/**
 * @author cl
 */
public class Receptor extends Protein {

    private static final Set<Class<? extends Feature>> availableFeatures = new HashSet<>();

    static {
        Receptor.availableFeatures.addAll(ChemicalEntity.availableFeatures);
    }

    private Map<ChemicalEntity, ComplexedChemicalEntity> boundReceptorStates;

    /**
     * Creates a new Receptor with the given identifier.
     *
     * @param identifier The identifier.
     */
    protected Receptor(SimpleStringIdentifier identifier) {
        super(identifier);
        boundReceptorStates = new HashMap<>();
    }

    public Receptor(Protein protein) {
        this(protein.getIdentifier());
        name = protein.name;
        annotations = protein.annotations;
        features = protein.features;
    }

    private void generateReceptorState(ChemicalEntity chemicalEntity) {
        boundReceptorStates.put(chemicalEntity,
                new ComplexedChemicalEntity.Builder(getIdentifier().getIdentifier() + "-" + chemicalEntity.getIdentifier().getIdentifier())
                        .name("complex of " + getName() + " and " + chemicalEntity.getName())
                        .addAssociatedPart(this)
                        .addAssociatedPart(chemicalEntity)
                        .build()
        );
    }

    public ChemicalEntity getReceptorStateFor(ChemicalEntity ligand) {
        return boundReceptorStates.get(ligand);
    }

    public Collection<ComplexedChemicalEntity> getBoundReceptorStates() {
        return boundReceptorStates.values();
    }

    public Set<ChemicalEntity> getLigands() {
        return boundReceptorStates.keySet();
    }

    @Override
    public Set<Class<? extends Feature>> getAvailableFeatures() {
        return availableFeatures;
    }

    public static class Builder extends ChemicalEntity.Builder<Receptor, Receptor.Builder> {

        public Builder(SimpleStringIdentifier identifier) {
            super(identifier);
        }

        public Builder(String identifier) {
            this(new SimpleStringIdentifier(identifier));
        }

        @Override
        protected Receptor createObject(SimpleStringIdentifier primaryIdentifer) {
            return new Receptor(primaryIdentifer);
        }

        @Override
        protected Receptor.Builder getBuilder() {
            return this;
        }

    }


}
