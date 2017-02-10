package de.bioforscher.simulation.application.components.chemicalEntities;

import de.bioforscher.chemistry.descriptive.ChemicalEntity;
import de.bioforscher.simulation.application.IconProvider;
import de.bioforscher.simulation.application.components.plots.ConcentrationPlot;
import de.bioforscher.simulation.application.renderer.SpeciesColorManager;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by Christoph on 26.07.2016.
 */
public class ColoredEntityCell extends ListCell<ChemicalEntity<?>> {

    private ConcentrationPlot plot;

    private GridPane grid = new GridPane();
    private Label name = new Label();
    private Label legendIndicator = new Label();

    private ContextMenu contextMenu = new ContextMenu();
    private MenuItem hideItem = new MenuItem();
    private CustomMenuItem colorItem = new CustomMenuItem();
    private ColorPicker colorPicker = new ColorPicker();

    public ColoredEntityCell(ConcentrationPlot plot) {
        this.plot = plot;
        configureGrid();
        configureLegendIndicator();
        configureName();
        configureColorPicker();
        configureContextMenu();
        addControlsToGrid();
    }

    private void configureGrid() {
        this.grid.setHgap(10);
        this.grid.setVgap(4);
        this.grid.setPadding(new Insets(0, 10, 0, 10));
    }

    private void configureName() {
        this.name.setFont(Font.font(null, FontWeight.BOLD, 12));
    }

    private void configureLegendIndicator() {
        this.legendIndicator.setFont(IconProvider.FONT_AWESOME);
        this.legendIndicator.setText(IconProvider.FontAwesome.ICON_DOT_CIRCLE);
    }

    private void configureColorPicker() {
        this.colorPicker.setStyle("-fx-background-color: white;");
    }

    private void configureContextMenu() {
        this.hideItem.setText("Hide");
        this.hideItem.setOnAction(this::toggleVisibility);
        this.colorItem.setContent(this.colorPicker);
        this.colorItem.setHideOnClick(false);
        this.colorItem.setOnAction(this::setColor);
        this.contextMenu.getItems().addAll(this.hideItem, this.colorItem);
    }

    private void addControlsToGrid() {
        this.grid.add(this.legendIndicator, 0, 0);
        this.grid.add(this.name, 1, 0);
    }

    @Override
    public void updateItem(ChemicalEntity entity, boolean empty) {
        super.updateItem(entity, empty);
        if (empty) {
            clearContent();
        } else {
            addContent(entity);
        }
    }

    private void clearContent() {
        setText(null);
        setGraphic(null);
        setContextMenu(null);
    }

    private void toggleVisibility(ActionEvent event) {
        ChemicalEntity entity = this.getItem();
        if (SpeciesColorManager.getInstance().getVisibility(entity)) {
            SpeciesColorManager.getInstance().setVisibility(entity, false);
            this.hideItem.setText("Show");
            this.plot.hideSeries(entity);
            this.setStyle("-fx-control-inner-background: #d0d0d0;");
        } else {
            SpeciesColorManager.getInstance().setVisibility(entity, true);
            this.hideItem.setText("Hide");
            this.plot.showSeries(entity);
            this.setStyle("-fx-control-inner-background: #f4f4f4;");
        }
    }

    private void setColor(ActionEvent event) {
        SpeciesColorManager.getInstance().setColor(this.getItem(), this.colorPicker.getValue());
        this.legendIndicator.setTextFill(this.colorPicker.getValue());
        this.plot.updateColor(this.getItem());
    }

    private void addContent(ChemicalEntity entity) {
        setText(null);
        this.legendIndicator.setTextFill(SpeciesColorManager.getInstance().getColor(entity));
        this.name.setText(entity.getName());
        this.colorPicker.setValue(SpeciesColorManager.getInstance().getColor(this.getItem()));
        setContextMenu(this.contextMenu);
        setGraphic(this.grid);
    }

}
