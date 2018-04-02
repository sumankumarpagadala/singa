package de.bioforscher.singa.simulation.model.concentrations;

import de.bioforscher.singa.chemistry.descriptive.entities.ChemicalEntity;
import de.bioforscher.singa.features.quantities.MolarConcentration;
import de.bioforscher.singa.simulation.model.compartments.CellSection;
import de.bioforscher.singa.simulation.model.compartments.EnclosedCompartment;

import javax.measure.Quantity;
import java.util.Map;
import java.util.Set;

/**
 * Concentration containers are used to manage the concentration inside of a node. Modules may require the node to
 * restrict the availability of a concentration to certain compartments. On the border between two compartments the
 * availability of a chemical entity can be different depending on the compartment of the neighbouring node.
 *
 * @author cl
 */
public interface ConcentrationContainer {

    /**
     * Returns the representative concentration of a chemical entity. If the concentration is available differently for
     * multiple compartments the average concentration is returned.
     *
     * @param chemicalEntity The ChemicalEntity.
     * @return The concentration.
     */
    Quantity<MolarConcentration> getConcentration(ChemicalEntity chemicalEntity);

    /**
     * Returns all concentrations for a particular {@link CellSection}.
     *
     * @param cellSection The CellSection
     * @return All concentrations in this CellSection.
     */
    Map<ChemicalEntity, Quantity<MolarConcentration>> getAllConcentrationsForSection(CellSection cellSection);

    /**
     * Returns the concentration of the specified {@link ChemicalEntity} available in the {@link CellSection}.
     * This returns null if the concentration of the chemical entity is not available for the cell section.
     *
     * @param cellSection The CellSection
     * @param chemicalEntity The ChemicalEntity.
     * @return The concentration of the ChemicalEntity in the CellSection.
     */
    Quantity<MolarConcentration> getAvailableConcentration(CellSection cellSection, ChemicalEntity chemicalEntity);

    /**
     * Sets the representative concentration of a chemical entity. This resets the available concentrations for all
     * registered compartments.
     *
     * @param chemicalEntity The ChemicalEntity.
     * @param concentration The concentration.
     */
    void setConcentration(ChemicalEntity chemicalEntity, Quantity<MolarConcentration> concentration);

    /**
     * Sets the available concentration based on the given compartment.
     *
     * @param cellSection The identifier of the compartment.
     * @param chemicalEntity The ChemicalEntity.
     * @param concentration The concentration.
     */
    void setAvailableConcentration(CellSection cellSection, ChemicalEntity chemicalEntity, Quantity<MolarConcentration> concentration);

    /**
     * Returns all {@link ChemicalEntity ChemicalEntities} that are referenced in this container.
     *
     * @return All {@link ChemicalEntity ChemicalEntities} that are referenced in this container.
     */
    Set<ChemicalEntity> getAllReferencedEntities();

    /**
     * Returns all {@link EnclosedCompartment Compartments} that are referenced in this container.
     *
     * @return All {@link EnclosedCompartment Compartments} that are referenced in this container.
     */
    Set<CellSection> getAllReferencedSections();

    /**
     * Returns the representative concentrations of all referenced chemical entities.
     *
     * @return The representative concentrations of all referenced chemical entities.
     */
    Map<ChemicalEntity, Quantity<MolarConcentration>> getAllConcentrations();

    /**
     * Returns a copy of this concentration container.
     *
     * @return A copy of this concentration container.
     */
    ConcentrationContainer getCopy();

}