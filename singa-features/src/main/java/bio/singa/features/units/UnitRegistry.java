package bio.singa.features.units;

import bio.singa.features.model.ScalableFeature;
import bio.singa.features.quantities.MolarConcentration;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.quantity.QuantityDimension;
import tec.uom.se.unit.Units;

import javax.measure.Dimension;
import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.*;
import java.util.HashMap;
import java.util.Map;

import static bio.singa.features.units.UnitProvider.MOLE_PER_LITRE;
import static tec.uom.se.AbstractUnit.ONE;
import static tec.uom.se.unit.MetricPrefix.MICRO;
import static tec.uom.se.unit.MetricPrefix.NANO;
import static tec.uom.se.unit.Units.*;

/**
 * @author cl
 */
public class UnitRegistry {

    // TODO maybe encapsulate units in objects to make them fixable (mol/l instead of mol/µl)

    private static UnitRegistry getInstance() {
        if (instance == null) {
            reinitialize();
        }
        return instance;
    }

    /**
     * Standard node distance [L] (100 nm)
     */
    public static final Quantity<Length> DEFAULT_SPACE = Quantities.getQuantity(1.0, MICRO(METRE));

    /**
     * Standard time step size [T] (1 us)
     */
    public static final Quantity<Time> DEFAULT_TIME = Quantities.getQuantity(1.0, MICRO(SECOND));

    /**
     * Standard molar concentration unit [N]
     */
    public static final Quantity<AmountOfSubstance> DEFAULT_AMOUNT_OF_SUBSTANCE = Quantities.getQuantity(1.0, NANO(MOLE));

    public static final Unit<Temperature> DEFAULT_TEMPERATURE_UNIT = KELVIN;
    public static final Unit<Mass> DEFAULT_MASS_UNIT = GRAM;

    private Quantity<Length> space;
    private Quantity<Time> time;

    private Map<Dimension, Unit> defaultUnits;

    private static UnitRegistry instance = getInstance();

    private UnitRegistry() {
        space = DEFAULT_SPACE;
        time = DEFAULT_TIME;

        defaultUnits = new HashMap<>();
        defaultUnits.put(QuantityDimension.LENGTH, space.getUnit());
        defaultUnits.put(QuantityDimension.TIME, time.getUnit());
        defaultUnits.put(QuantityDimension.AMOUNT_OF_SUBSTANCE, DEFAULT_AMOUNT_OF_SUBSTANCE.getUnit());
        defaultUnits.put(QuantityDimension.MASS, DEFAULT_MASS_UNIT);
        defaultUnits.put(QuantityDimension.TEMPERATURE, DEFAULT_TEMPERATURE_UNIT);
    }

    public static void reinitialize() {
        synchronized (UnitRegistry.class) {
            instance = new UnitRegistry();
        }
    }

    public static void setSpace(Quantity<Length> space) {
        setSpaceScale(space.getValue().doubleValue());
        setSpaceUnit(space.getUnit());
    }

    public static Quantity<Length> getSpace() {
        return getInstance().space;
    }

    public static double getSpaceScale() {
        return getInstance().space.getValue().doubleValue();
    }

    public static void setSpaceScale(double scale) {
        getInstance().space = Quantities.getQuantity(scale, getInstance().space.getUnit());
    }

    public static Unit<Length> getSpaceUnit() {
        return getInstance().space.getUnit();
    }

    public static void setSpaceUnit(Unit<Length> unit) {
        // only rescale if unit was updated
        getInstance().space = Quantities.getQuantity(getInstance().space.getValue().doubleValue(), unit);
        getInstance().defaultUnits.put(QuantityDimension.LENGTH, unit);
        rescaleRegisteredUnits();
    }

    public static void resetSpace() {
        setSpaceScale(DEFAULT_SPACE.getValue().doubleValue());
        setSpaceUnit(DEFAULT_SPACE.getUnit());
    }

    public static void setTime(Quantity<Time> time) {
        setTimeScale(time.getValue().doubleValue());
        setTimeUnit(time.getUnit());
    }

    public static Quantity<Time> getTime() {
        return getInstance().time;
    }

    public static double getTimeScale() {
        return getInstance().time.getValue().doubleValue();
    }

    public static void setTimeScale(double scale) {
        getInstance().time = Quantities.getQuantity(scale, getInstance().time.getUnit());
    }

    public static Unit<Time> getTimeUnit() {
        return getInstance().time.getUnit();
    }

    public static void setTimeUnit(Unit<Time> unit) {
        getInstance().time = Quantities.getQuantity(getInstance().time.getValue().doubleValue(), unit);
        getInstance().defaultUnits.put(QuantityDimension.TIME, unit);
        rescaleRegisteredUnits();
    }

    public static void resetTime() {
        setTimeScale(DEFAULT_TIME.getValue().doubleValue());
        setTimeUnit(DEFAULT_TIME.getUnit());
    }

    public static void setUnit(Unit<?> unit) {
        getInstance().defaultUnits.put(unit.getDimension(), unit);
    }

    public static Unit<MolarConcentration> getConcentrationUnit() {
        return getDefaultUnit(MOLE_PER_LITRE).asType(MolarConcentration.class);
    }

    public static Unit<Area> getAreaUnit() {
        return getDefaultUnit(SQUARE_METRE).asType(Area.class);
    }

    public static Quantity<Area> getArea() {
        return getSpace().multiply(getSpace()).asType(Area.class);
    }

    public static Unit<Volume> getVolumeUnit() {
        return getDefaultUnit(CUBIC_METRE).asType(Volume.class);
    }

    public static Quantity<Volume> getVolume() {
        return getSpace().multiply(getSpace()).multiply(getSpace()).asType(Volume.class);
    }

    public static Quantity<MolarConcentration> concentration(double value) {
        return Quantities.getQuantity(value, getConcentrationUnit());
    }

    public static Quantity<MolarConcentration> concentration(double value, Unit<MolarConcentration> unit) {
        return Quantities.getQuantity(value, unit).to(getConcentrationUnit());
    }

    public static <QuantityType extends Quantity<QuantityType>> Quantity<QuantityType> scale(Quantity<QuantityType> quantity) {
        Quantity<QuantityType> convert = convert(quantity);
        double value = convert.getValue().doubleValue();
        int spaceExponent = ScalableFeature.getSpaceExponent(convert.getUnit());
        int timeExponent = ScalableFeature.getTimeExponent(convert.getUnit());
        if (spaceExponent != 0 || timeExponent != 0) {
            if (spaceExponent > 0 && getSpaceScale() != 1.0) {
                value = value / Math.pow(getSpaceScale(), spaceExponent);
            } else {
                value = value * Math.pow(getSpaceScale(), Math.abs(spaceExponent));
            }

            if (timeExponent > 0 && getSpaceScale() != 1.0) {
                value = value / Math.pow(getTimeScale(), timeExponent);
            } else {
                value = value * Math.pow(getTimeScale(), Math.abs(timeExponent));
            }
            return Quantities.getQuantity(value, convert.getUnit());
        }
        return convert;
    }

    public static <QuantityType extends Quantity<QuantityType>> Quantity<QuantityType> scaleTime(Quantity<QuantityType> quantity) {
        Quantity<QuantityType> convert = convert(quantity);
        double value = convert.getValue().doubleValue();
        int timeExponent = ScalableFeature.getTimeExponent(convert.getUnit());
        if (timeExponent != 0) {
            if (timeExponent > 0 && getSpaceScale() != 1.0) {
                value = value / Math.pow(getTimeScale(), timeExponent);
            } else {
                value = value * Math.pow(getTimeScale(), Math.abs(timeExponent));
            }
            return Quantities.getQuantity(value, convert.getUnit());
        }
        return convert;
    }

    public static <QuantityType extends Quantity<QuantityType>> Quantity<QuantityType> convert(Quantity<QuantityType> quantity) {
        Dimension dimension = quantity.getUnit().getDimension();
        if (!getInstance().defaultUnits.containsKey(dimension)) {
            // not base unit and not registered
            addUnitForDimension(dimension);
        }
        return quantity.to(getInstance().defaultUnits.get(dimension));
    }

    private static void rescaleRegisteredUnits() {
        for (Dimension next : getInstance().defaultUnits.keySet()) {
            if (next.getBaseDimensions() != null) {
                addUnitForDimension(next);
            }
        }
    }

    private static Unit<?> getDefaultUnit(Unit<?> unit) {
        Dimension dimension = unit.getDimension();
        if (!getInstance().defaultUnits.containsKey(dimension)) {
            // not base unit and not registered
            addUnitForDimension(dimension);
        }
        return getInstance().defaultUnits.get(dimension);
    }

    private static void addUnitForDimension(Dimension dimension) {
        Unit unit = ONE;
        for (Map.Entry<? extends Dimension, Integer> entry : dimension.getBaseDimensions().entrySet()) {
            unit = unit.multiply(getPreferredUnit(entry.getKey()).pow(entry.getValue()));
        }
        getInstance().defaultUnits.put(dimension, unit);
    }

    private static Unit getPreferredUnit(Quantity<?> quantity) {
        if (getInstance().defaultUnits.containsKey(quantity.getUnit().getDimension())) {
            // if time or space use system scale
            return getInstance().defaultUnits.get(quantity.getUnit().getDimension());
        } else {
            // else use si units
            return Units.getInstance().getUnits(quantity.getUnit().getDimension()).iterator().next();
        }
    }

    private static Unit getPreferredUnit(Dimension dimension) {
        if (getInstance().defaultUnits.containsKey(dimension)) {
            // if time or space use system scale
            return getInstance().defaultUnits.get(dimension);
        } else {
            // else use si units
            return Units.getInstance().getUnits(dimension).iterator().next();
        }
    }

    public static Map<Dimension, Unit> getDefaultUnits() {
        return getInstance().defaultUnits;
    }

    public static boolean isTimeUnit(Unit<?> unit) {
        return unit.isCompatible(SECOND);
    }

    public static boolean isInverseTimeUnit(Unit<?> unit) {
        return unit.isCompatible(ONE.divide(SECOND));
    }

    public static boolean isLengthUnit(Unit<?> unit) {
        return unit.isCompatible(METRE);
    }

    public static boolean isSubstanceUnit(Unit<?> unit) {
        return unit.isCompatible(MOLE);
    }

    public static boolean isConcentrationUnit(Unit<?> unit) {
        return unit.isCompatible(MOLE_PER_LITRE);
    }

}
