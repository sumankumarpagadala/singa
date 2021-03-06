package bio.singa.features.quantities;

import bio.singa.features.model.AbstractFeature;
import bio.singa.features.model.Evidence;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.ProductUnit;

import javax.measure.Quantity;
import javax.measure.Unit;

import static bio.singa.features.units.UnitProvider.ANGSTROEM;
import static tec.uom.se.unit.Units.*;

/**
 * The molar volume, symbol Vm, is the volume occupied by one mole of a substance (chemical element or chemical
 * compound) at a given temperature and pressure. It is equal to the molar mass (M) divided by the mass density (ρ). It
 * has the SI unit cubic metres per mole (m3/mol)
 *
 * @author cl
 */
public class MolarVolume extends AbstractFeature<Quantity<MolarVolume>> implements Quantity<MolarVolume> {

    public static final Unit<MolarVolume> CUBIC_METRE_PER_MOLE = new ProductUnit<>(CUBIC_METRE.divide(MOLE));
    public static final Unit<MolarVolume> LITRE_PER_MOLE = new ProductUnit<>(LITRE.divide(MOLE));
    public static final Unit<MolarVolume> CUBIC_ANGSTROEM_PER_MOLE = new ProductUnit<>(ANGSTROEM.multiply(ANGSTROEM).multiply(ANGSTROEM).divide(MOLE));
    public static final String SYMBOL = "V_m";

    public MolarVolume(Quantity<MolarVolume> quantity, Evidence evidence) {
        super(quantity, evidence);
    }

    public MolarVolume(double quantity, Evidence evidence) {
        super(Quantities.getQuantity(quantity, CUBIC_METRE_PER_MOLE), evidence);
    }

    @Override
    public Quantity<MolarVolume> add(Quantity<MolarVolume> augend) {
        return getFeatureContent().add(augend);
    }

    @Override
    public Quantity<MolarVolume> subtract(Quantity<MolarVolume> subtrahend) {
        return getFeatureContent().subtract(subtrahend);
    }

    @Override
    public Quantity<?> divide(Quantity<?> divisor) {
        return getFeatureContent().divide(divisor);
    }

    @Override
    public Quantity<MolarVolume> divide(Number divisor) {
        return getFeatureContent().divide(divisor);
    }

    @Override
    public Quantity<?> multiply(Quantity<?> multiplier) {
        return getFeatureContent().multiply(multiplier);
    }

    @Override
    public Quantity<MolarVolume> multiply(Number multiplier) {
        return getFeatureContent().multiply(multiplier);
    }

    @Override
    public Quantity<?> inverse() {
        return getFeatureContent().inverse();
    }

    @Override
    public Quantity<MolarVolume> to(Unit<MolarVolume> unit) {
        return getFeatureContent().to(unit);
    }

    @Override
    public <T extends Quantity<T>> Quantity<T> asType(Class<T> type) throws ClassCastException {
        return getFeatureContent().asType(type);
    }

    @Override
    public Number getValue() {
        return getFeatureContent().getValue();
    }

    @Override
    public Unit<MolarVolume> getUnit() {
        return getFeatureContent().getUnit();
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

}
