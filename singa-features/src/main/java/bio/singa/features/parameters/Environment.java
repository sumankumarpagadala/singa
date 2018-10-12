package bio.singa.features.parameters;

import bio.singa.features.quantities.DynamicViscosity;
import bio.singa.features.quantities.MolarConcentration;
import bio.singa.features.units.UnitRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.measure.quantity.Temperature;
import java.util.Observable;
import java.util.Observer;

import static bio.singa.features.units.UnitProvider.PASCAL_SECOND;
import static tec.uom.se.unit.MetricPrefix.MICRO;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.KELVIN;
import static tec.uom.se.unit.Units.METRE;

public class Environment extends Observable {

    private static final Logger logger = LoggerFactory.getLogger(Environment.class);

    /**
     * Standard system temperature [temperature] (293 K = 20 C)
     */
    public static final Quantity<Temperature> DEFAULT_SYSTEM_TEMPERATURE = Quantities.getQuantity(293.0, KELVIN);

    /**
     * Standard system viscosity [pressure per time] (1 mPa*s = 1cP = Viscosity of Water at 20 C)
     */
    public static final Quantity<DynamicViscosity> DEFAULT_SYSTEM_VISCOSITY = Quantities.getQuantity(1.0, MILLI(PASCAL_SECOND));

    /**
     * Standard system extend [length] (5 um)
     */
    public static final Quantity<Length> DEFAULT_SYSTEM_EXTEND = Quantities.getQuantity(1.0, MICRO(METRE));

    /**
     * Standard simulation extend [pseudo length] 500
     */
    public static final double DEFAULT_SIMULATION_EXTEND = 100;

    /**
     * The singleton instance.
     */
    private static Environment instance;

    /**
     * The global temperature of the simulation system.
     */
    private Quantity<Temperature> systemTemperature;

    /**
     * The global viscosity of the simulation system.
     */
    private Quantity<DynamicViscosity> systemViscosity;

    /**
     * An empty concentration quantity
     */
    private Quantity<MolarConcentration> emptyConcentration;

    /**
     * The extend of the actual system.
     */
    private Quantity<Length> systemExtend;

    /**
     * Multiply the scale by a simulation distance to get the system distance.
     */
    private Quantity<Length> systemScale;

    /**
     * The extend of the simulation.
     */
    private double simulationExtend;

    /**
     * Multiply the scale by a system distance to get the simulation distance.
     */
    private double simulationScale;

    private static Environment getInstance() {
        if (instance == null) {
            synchronized (Environment.class) {
                instance = new Environment();
            }
        }
        return instance;
    }

    private Environment() {
        systemExtend = DEFAULT_SYSTEM_EXTEND;
        simulationExtend = DEFAULT_SIMULATION_EXTEND;
        systemTemperature = DEFAULT_SYSTEM_TEMPERATURE;
        systemViscosity = DEFAULT_SYSTEM_VISCOSITY;
        emptyConcentration = UnitRegistry.concentration(0.0);
        setSystemAndSimulationScales();
        setChanged();
        notifyObservers();
    }

    public static void reset() {
        getInstance().systemExtend = DEFAULT_SYSTEM_EXTEND;
        getInstance().simulationExtend = DEFAULT_SIMULATION_EXTEND;
        getInstance().systemTemperature = DEFAULT_SYSTEM_TEMPERATURE;
        getInstance().systemViscosity = DEFAULT_SYSTEM_VISCOSITY;
        getInstance().emptyConcentration = UnitRegistry.concentration(0.0);
        getInstance().setSystemAndSimulationScales();
        getInstance().setChanged();
        getInstance().notifyObservers();
    }

    public static Quantity<MolarConcentration> emptyConcentration() {
        return getInstance().emptyConcentration;
    }

    public static Quantity<Temperature> getTemperature() {
        return getInstance().systemTemperature;
    }

    public static void setTemperature(Quantity<Temperature> temperature) {
        logger.debug("Setting environmental temperature to {}.", temperature);
        getInstance().systemTemperature = temperature.to(KELVIN);
    }

    public static Quantity<DynamicViscosity> getViscosity() {
        return getInstance().systemViscosity;
    }

    public static void setSystemViscosity(Quantity<DynamicViscosity> viscosity) {
        logger.debug("Setting environmental dynamic viscosity of to {}.", viscosity);
        getInstance().systemViscosity = viscosity.to(MILLI(PASCAL_SECOND));
    }

    public static void setNodeSpacingToDiameter(Quantity<Length> diameter, int spanningNodes) {
        logger.debug("Setting system diameter to {} using {} spanning nodes.", diameter, spanningNodes);
        UnitRegistry.setSpace(diameter.divide(spanningNodes));
    }

    public static Quantity<Length> getSystemExtend() {
        return getInstance().systemExtend;
    }

    public static void setSystemExtend(Quantity<Length> systemExtend) {
        getInstance().systemExtend = systemExtend;
        getInstance().setSystemAndSimulationScales();
    }

    public static double getSimulationExtend() {
        return getInstance().simulationExtend;
    }

    public static void setSimulationExtend(double simulationExtend) {
        getInstance().simulationExtend = simulationExtend;
        getInstance().setSystemAndSimulationScales();
    }

    public static Quantity<Length> getSystemScale() {
        return getInstance().systemScale;
    }

    public static double getSimulationScale() {
        return getInstance().simulationScale;
    }

    private void setSystemAndSimulationScales() {
        simulationScale = simulationExtend / systemExtend.getValue().doubleValue();
        systemScale = systemExtend.divide(simulationExtend);
    }

    public static Quantity<Length> convertSimulationToSystemScale(double simulationDistance) {
        return getInstance().systemScale.multiply(simulationDistance);
    }

    public static double convertSystemToSimulationScale(Quantity<Length> realDistance) {
        return realDistance.to(getInstance().systemExtend.getUnit()).getValue().doubleValue() * getInstance().simulationScale;
    }

    public static void attachObserver(Observer observer) {
        getInstance().addObserver(observer);
    }

    public static String report() {
        return "Environment: \n" +
                "system extend = " + getInstance().systemExtend + "\n" +
                "simulation extend = " + getInstance().simulationExtend + "\n" +
                "system temperature = " + getInstance().systemTemperature + "\n" +
                "system viscosity = " + getInstance().systemViscosity + "\n";
    }

}
