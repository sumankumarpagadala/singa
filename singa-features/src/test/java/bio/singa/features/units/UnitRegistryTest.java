package bio.singa.features.units;

import bio.singa.features.quantities.MolarConcentration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;
import tec.uom.se.unit.ProductUnit;

import javax.measure.Quantity;

import static bio.singa.features.units.UnitProvider.MICRO_MOLE_PER_LITRE;
import static bio.singa.features.units.UnitProvider.MOLE_PER_LITRE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tec.uom.se.unit.MetricPrefix.*;
import static tec.uom.se.unit.Units.*;

/**
 * @author cl
 */
class UnitRegistryTest {

    @BeforeAll
    static void initialize() {
        UnitRegistry.reinitialize();
    }

    @AfterEach
    void cleanUp() {
        UnitRegistry.reinitialize();
    }

    @Test
    void shouldConvertUnit() {
        // time
        Quantity<?> time = UnitRegistry.convert(Quantities.getQuantity(1.0, MINUTE));
        assertEquals(Quantities.getQuantity(6.0E7, UnitRegistry.DEFAULT_TIME.getUnit()), time);
        // area
        Quantity<?> area = UnitRegistry.convert(Quantities.getQuantity(1.0, CENTI(METRE).pow(2)));
        assertEquals(Quantities.getQuantity(1.0E8, UnitRegistry.DEFAULT_SPACE.getUnit().pow(2)), area);
        // concentration
        Quantity<?> concentration = UnitRegistry.convert(Quantities.getQuantity(1.0, MICRO_MOLE_PER_LITRE));
        assertEquals(Quantities.getQuantity(1.0E-12, UnitRegistry.DEFAULT_AMOUNT_OF_SUBSTANCE.getUnit().divide(UnitRegistry.DEFAULT_SPACE.getUnit().pow(3))), concentration);
        // reaction rate
        Quantity<?> reactionRate = UnitRegistry.convert(Quantities.getQuantity(1.0, MICRO_MOLE_PER_LITRE.divide(SECOND)));
        assertEquals(Quantities.getQuantity(1.0E-18, UnitRegistry.DEFAULT_AMOUNT_OF_SUBSTANCE.getUnit().divide(UnitRegistry.DEFAULT_SPACE.getUnit().pow(3).multiply(UnitRegistry.DEFAULT_TIME.getUnit()))), reactionRate);
        // weight
        Quantity<?> weight = UnitRegistry.convert(Quantities.getQuantity(1, KILOGRAM));
        assertEquals(Quantities.getQuantity(1000, UnitRegistry.DEFAULT_MASS_UNIT), weight);
        // diffusivity
        Quantity<?> diffusivity = UnitRegistry.convert(Quantities.getQuantity(1.0, METRE.divide(100).pow(2).divide(SECOND)));
        assertEquals(Quantities.getQuantity(100, UnitRegistry.DEFAULT_SPACE.getUnit().pow(2).divide(UnitRegistry.DEFAULT_TIME.getUnit())), diffusivity);
    }

    @Test
    void shouldScale() {
        // time
        Quantity<?> first = UnitRegistry.scale(Quantities.getQuantity(1.0, MINUTE));
        assertEquals(Quantities.getQuantity(6.0E7, UnitRegistry.DEFAULT_TIME.getUnit()), first);
        // double the time scale
        UnitRegistry.setTimeScale(2);
        Quantity<?> second = UnitRegistry.scale(Quantities.getQuantity(1.0, MINUTE));
        // half the result
        assertEquals(Quantities.getQuantity(1.20E8, UnitRegistry.DEFAULT_TIME.getUnit()), second);
        // double the space scale
        UnitRegistry.setSpaceScale(2);
        Quantity<?> diffusivity = UnitRegistry.scale(Quantities.getQuantity(1.0, METRE.divide(100).pow(2).divide(SECOND)));
        // double from time and quarter from space (since 1/2*2) diffusivity
        assertEquals(Quantities.getQuantity(50, UnitRegistry.DEFAULT_SPACE.getUnit().pow(2).divide(UnitRegistry.DEFAULT_TIME.getUnit())), diffusivity);
    }

    @Test
    void getTransformedMolarConcentration() {
        final ComparableQuantity<MolarConcentration> molePerLitre = Quantities.getQuantity(1, MOLE_PER_LITRE);
        final ComparableQuantity<MolarConcentration> molePerCubicMicroMetre = molePerLitre.to(new ProductUnit<>(NANO(MOLE).divide(MICRO(METRE).multiply(MICRO(METRE).multiply(MICRO(METRE))))));
        assertEquals(molePerCubicMicroMetre, UnitRegistry.scale(molePerLitre));
        UnitRegistry.setSpace(Quantities.getQuantity(2, MICRO(METRE)));
        // 2*2*2 = 8
        assertEquals(8.0E-6 , UnitRegistry.scale(molePerLitre).getValue());
    }

}