package de.bioforscher.simulation.application.components;

import de.bioforscher.chemistry.descriptive.ChemicalEntity;
import de.bioforscher.simulation.modules.model.Simulation;
import de.bioforscher.simulation.util.AutomatonGraphUtilities;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.Map;
import java.util.Map.Entry;

/**
 * The class represents a context menu that is able to manipulate the graph and
 * its rendering.
 *
 * @author Christoph Leberecht
 */
public class BioGraphContextMenu extends ContextMenu {

    private final SimulationCanvas owner;
    private Simulation simulation;

    private MenuItem colorByStateItem = new MenuItem();
    private Menu colorByChemicalEntityMenu;
    private ToggleGroup chemicalEntitiesGrouping;

    BioGraphContextMenu(Simulation simulation, SimulationCanvas canvas) {
        this.simulation = simulation;
        this.owner = canvas;
        configureColorByStateItem();
        configureColorByChemicalEntityMenu();
        addItemsToMenu();
    }

    private void configureColorByStateItem() {
        this.colorByStateItem.setText("Color by state");
        this.colorByStateItem.setOnAction(this::colorByState);
    }

    private void configureColorByChemicalEntityMenu() {
        this.colorByChemicalEntityMenu = new Menu("Color by entity ...");
        this.chemicalEntitiesGrouping = new ToggleGroup();
        Map<String, ChemicalEntity> chemicalEntities = AutomatonGraphUtilities
                .generateEntityMapFromSet(this.simulation.getChemicalEntities());
        // add MenuItem for every Species
        if (!chemicalEntities.isEmpty()) {
            fillSpeciesMenu(chemicalEntities);
        } else {
            RadioMenuItem itemCompound = new RadioMenuItem("No entity to highlight.");
            itemCompound.setUserData(null);
            itemCompound.setToggleGroup(this.chemicalEntitiesGrouping);
            this.colorByChemicalEntityMenu.getItems().add(itemCompound);
        }
    }

    private void fillSpeciesMenu(Map<String, ChemicalEntity> speciesMap) {
        for (Entry<String, ChemicalEntity> species : speciesMap.entrySet()) {
            RadioMenuItem speciesMenuItem = setupSpeciesMenuItem(species.getValue());
            this.colorByChemicalEntityMenu.getItems().add(speciesMenuItem);
        }
    }

    private RadioMenuItem setupSpeciesMenuItem(final ChemicalEntity species) {
        RadioMenuItem itemCompound = new RadioMenuItem(species.getName());
        itemCompound.setUserData(species);
        itemCompound.setToggleGroup(this.chemicalEntitiesGrouping);
        itemCompound.setOnAction(this::colorBySpecies);
        return itemCompound;
    }

    private void addItemsToMenu() {
        this.getItems().addAll(this.colorByStateItem, this.colorByChemicalEntityMenu);
    }

    private void colorBySpecies(ActionEvent event) {
        ChemicalEntity chemicalEntity = (ChemicalEntity)((RadioMenuItem)event.getSource()).getUserData();
        this.owner.getRenderer().getBioRenderingOptions().setColoringByEntity(true);
        this.owner.getRenderer().getBioRenderingOptions().setNodeHighlightEntity(chemicalEntity);
        this.owner.getRenderer().getBioRenderingOptions().setEdgeHighlightEntity(chemicalEntity);
        this.owner.draw();
    }

    private void colorByState(ActionEvent event) {
        this.owner.getRenderer().getBioRenderingOptions().setColoringByEntity(false);
        this.owner.draw();
    }

    public Simulation getSimulation() {
        return this.simulation;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

}
