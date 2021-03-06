package bio.singa.simulation.model.simulation;

import bio.singa.core.events.UpdateEventListener;
import bio.singa.features.model.QuantityFormatter;
import bio.singa.features.units.UnitRegistry;
import bio.singa.simulation.events.*;
import bio.singa.simulation.model.graphs.AutomatonGraph;
import bio.singa.simulation.model.graphs.AutomatonNode;
import javafx.application.Platform;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tec.uom.se.ComparableQuantity;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static tec.uom.se.unit.MetricPrefix.MICRO;
import static tec.uom.se.unit.MetricPrefix.MILLI;
import static tec.uom.se.unit.Units.SECOND;

/**
 * Changes in simulations can be observed by tagging {@link AutomatonNode}s of the {@link AutomatonGraph}. As a standard
 * implementation there is the {@link EpochUpdateWriter} that can be added to the Simulation that will write log files
 * to the specified file locations.
 *
 * @author cl
 */
public class SimulationManager extends Task<Simulation> {

    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(SimulationManager.class);

    private static final boolean DEFAULT_KEEP_PLATFORM_OPEN = false;

    private static ComparableQuantity<Time> REPORT_THRESHOLD = Quantities.getQuantity(1, SECOND);

    /**
     * The simulation.
     */
    private final Simulation simulation;

    /**
     * The condition determining when the simulation should be terminated.
     */
    private Predicate<Simulation> terminationCondition;

    /**
     * The condition determining when events should be emitted.
     */
    private Predicate<Simulation> emitCondition;

    /**
     * The emitter for node events.
     */
    private NodeEventEmitter nodeEventEmitter;

    /**
     * The emitter for graph events.
     */
    private GraphEventEmitter graphEventEmitter;

    /**
     * The time for the next update to be issued. (For FPS based emission).
     */
    private long nextTick = System.currentTimeMillis();

    private long startingTime = System.currentTimeMillis();

    private long previousTimeMillis = 0;

    private Quantity<Time> previousTimeSimulation = Quantities.getQuantity(0.0, UnitRegistry.getTimeUnit());

    /**
     * The time for the next update to be issued (in simulation time).
     */
    private Quantity<Time> scheduledEmitTime = Quantities.getQuantity(0.0, UnitRegistry.getTimeUnit());

    private Quantity<Time> terminationTime;

    private boolean keepPlatformOpen = DEFAULT_KEEP_PLATFORM_OPEN;

    private CountDownLatch terminationLatch;

    /**
     * Creates a new simulation manager for the given simulation.
     *
     * @param simulation The simulation.
     */
    public SimulationManager(Simulation simulation) {
        logger.debug("Initializing simulation manager ...");
        this.simulation = simulation;
        nodeEventEmitter = new NodeEventEmitter();
        graphEventEmitter = new GraphEventEmitter();
        // emit every event if not specified otherwise
        emitCondition = s -> true;
    }

    /**
     * Adds a new listener for node based events.
     *
     * @param listener The listener.
     */
    public void addNodeUpdateListener(UpdateEventListener<UpdatableUpdatedEvent> listener) {
        logger.info("Added {} to node update listeners.", listener.getClass().getSimpleName());
        nodeEventEmitter.addEventListener(listener);
    }

    /**
     * Returns all currently registered node event listeners.
     *
     * @return All currently registered node event listeners.
     */
    public CopyOnWriteArrayList<UpdateEventListener<UpdatableUpdatedEvent>> getNodeListeners() {
        return nodeEventEmitter.getListeners();
    }

    /**
     * Adds a new listener for graph based events.
     *
     * @param listener The listener.
     */
    public void addGraphUpdateListener(UpdateEventListener<GraphUpdatedEvent> listener) {
        logger.info("Added {} to graph update listeners.", listener.getClass().getSimpleName());
        graphEventEmitter.addEventListener(listener);
    }

    public CopyOnWriteArrayList<UpdateEventListener<GraphUpdatedEvent>> getGraphListeners() {
        return graphEventEmitter.getListeners();
    }

    /**
     * Sets a condition determining when the simulation should be terminated.
     *
     * @param terminationCondition The termination condition.
     */
    public void setTerminationCondition(Predicate<Simulation> terminationCondition) {
        this.terminationCondition = terminationCondition;
    }

    /**
     * Schedules the termination of the simulation after the given time (simulation time) has passed.
     *
     * @param time The time.
     */
    public void setSimulationTerminationToTime(Quantity<Time> time) {
        terminationTime = time.to(MICRO(SECOND));
        setTerminationCondition(s -> s.getElapsedTime().isLessThan(time));
    }

    /**
     * Schedules the termination of the simulation after the given number of epochs have passed.
     *
     * @param numberOfEpochs The number of epochs.
     */
    public void setSimulationTerminationToEpochs(long numberOfEpochs) {
        setTerminationCondition(s -> s.getEpoch() < numberOfEpochs);
    }

    /**
     * Sets a condition determining when events should be emitted.
     *
     * @param emitCondition The emission condition.
     */
    public void setUpdateEmissionCondition(Predicate<Simulation> emitCondition) {
        this.emitCondition = emitCondition;
    }

    /**
     * Sets the emission of updates for a rending engine. If more epochs are processed than can be displayed the epochs
     * in between are not emitted. If epoch calculation is slower each epoch is emitted.
     *
     * @param fps The frames (emits) per (real time) second.
     */
    public void tieUpdateEmissionToFPS(int fps) {
        int skipTicks = 1000 / fps;
        emitCondition = s -> {
            long currentMillis = System.currentTimeMillis();
            if (currentMillis > nextTick) {
                nextTick = currentMillis + skipTicks;
                return true;
            }
            return false;
        };
    }

    /**
     * Schedules the emission of events after the given time (simulation time) has passed.
     *
     * @param timePassed The (simulation) time passed.
     */
    public void setUpdateEmissionToTimePassed(Quantity<Time> timePassed) {
        emitCondition = s -> {
            ComparableQuantity<Time> currentTime = s.getElapsedTime();
            if (currentTime.isGreaterThan(scheduledEmitTime)) {
                scheduledEmitTime = currentTime.add(timePassed);
                return true;
            }
            return false;
        };
    }

    public void setTerminationLatch(CountDownLatch terminationLatch) {
        this.terminationLatch = terminationLatch;
    }

    public void emitGraphEvent(Simulation simulation) {
        graphEventEmitter.emitEvent(new GraphUpdatedEvent(simulation.getGraph(), simulation.getElapsedTime()));
    }

    public void emitNodeEvent(Simulation simulation, Updatable updatable) {
        nodeEventEmitter.emitEvent(new UpdatableUpdatedEvent(simulation.getElapsedTime(), updatable));
    }

    public boolean keepPlatformOpen() {
        return keepPlatformOpen;
    }

    public void setKeepPlatformOpen(boolean keepPlatformOpen) {
        this.keepPlatformOpen = keepPlatformOpen;
    }

    /**
     * Returns the simulation.
     *
     * @return The simulation.
     */
    public Simulation getSimulation() {
        return simulation;
    }

    @Override
    protected Simulation call() {
        while (!isCancelled() && terminationCondition.test(simulation)) {
            if (emitCondition.test(simulation)) {
                logger.debug("Emitting event after {} (epoch {}).", QuantityFormatter.formatTime(simulation.getElapsedTime()), simulation.getEpoch());
                emitGraphEvent(simulation);
                for (Updatable updatable : simulation.getObservedUpdatables()) {
                    emitNodeEvent(simulation, updatable);
                    logger.debug("Emitted next epoch event for node {}.", updatable.getStringIdentifier());
                }
                simulation.clearPreviouslyObservedDeltas();
                if (terminationTime != null) {
                    estimateRuntime();
                }
            }
            simulation.nextEpoch();
        }
        return simulation;
    }

    private void estimateRuntime() {
        // calculate time since last report
        long currentTimeMillis = System.currentTimeMillis();
        long millisSinceLastReport = currentTimeMillis - previousTimeMillis;
        ComparableQuantity<Time> timeSinceLastReport = Quantities.getQuantity(millisSinceLastReport, MILLI(SECOND));
        // if it has been 1 second since last report
        if (timeSinceLastReport.isGreaterThanOrEqualTo(REPORT_THRESHOLD)) {
            // calculate time remaining
            ComparableQuantity<Time> currentTimeSimulation = simulation.getElapsedTime().to(MICRO(SECOND));
            double fractionDone = currentTimeSimulation.getValue().doubleValue() / terminationTime.getValue().doubleValue();
            long timeRequired = System.currentTimeMillis() - startingTime;
            long estimatedMillisRemaining = (long) (timeRequired / fractionDone) - timeRequired;
            ComparableQuantity<Time> subtract = currentTimeSimulation.subtract(previousTimeSimulation);
            if (previousTimeMillis > 0) {
                ComparableQuantity<Time> estimatesTimeRemaining = Quantities.getQuantity(estimatedMillisRemaining, MILLI(SECOND));
                double speed = subtract.getValue().doubleValue() / Quantities.getQuantity(currentTimeMillis - previousTimeMillis, MILLI(SECOND)).to(SECOND).getValue().doubleValue();
                if (Double.isInfinite(speed)) {
                    logger.info("estimated time remaining: " + QuantityFormatter.formatTime(estimatesTimeRemaining) + ", current simulation speed: [very high] (Simulation Time) per s(Real Time)");
                } else {
                    logger.info("estimated time remaining: " + QuantityFormatter.formatTime(estimatesTimeRemaining) + ", current simulation speed: " + QuantityFormatter.formatTime(Quantities.getQuantity(speed, MICRO(SECOND))) + "(Simulation Time) per s(Real Time)");
                }
            }
            previousTimeMillis = currentTimeMillis;
            previousTimeSimulation = currentTimeSimulation;
        }
    }

    @Override
    protected void done() {
        try {
            logger.info("Simulation finished.");
            for (UpdateEventListener<UpdatableUpdatedEvent> nodeEventListener : getNodeListeners()) {
                if (nodeEventListener instanceof EpochUpdateWriter) {
                    ((EpochUpdateWriter) nodeEventListener).closeWriters();
                }
            }
            for (UpdateEventListener<GraphUpdatedEvent> graphEventListener : getGraphListeners()) {
                if (graphEventListener instanceof GraphImageWriter) {
                    GraphImageWriter graphImageWriter = (GraphImageWriter) graphEventListener;
                    graphImageWriter.shutDown();
                    graphImageWriter.combineToGif();
                }
            }
            if (terminationLatch != null) {
                terminationLatch.countDown();
            }
            // will exit jfx when simulation finishes
            if (!keepPlatformOpen) {
                Platform.exit();
            }
            if (!isCancelled()) {
                get();
            }
        } catch (ExecutionException e) {
            // Exception occurred, deal with it
            logger.error("Encountered an exception during simulation: " + e.getCause());
            e.printStackTrace();
        } catch (InterruptedException e) {
            // Shouldn't happen, we're invoked when computation is finished
            throw new AssertionError(e);
        }
    }


    private static String formatMillis(long millis) {
        StringBuilder builder = new StringBuilder(20);
        String sgn = "";

        if (millis < 0) {
            sgn = "-";
            millis = Math.abs(millis);
        }

        append(builder, sgn, 0, (millis / 3600000));
        millis %= 3600000;
        builder.append(" h ");
        append(builder, "", 2, (millis / 60000));
        builder.append(" min");
        return builder.toString();
    }

    /**
     * Append a right-aligned and zero-padded numeric value to a `StringBuilder`.
     */
    private static void append(StringBuilder tgt, String pfx, int dgt, long val) {
        tgt.append(pfx);
        if (dgt > 1) {
            int pad = (dgt - 1);
            for (long xa = val; xa > 9 && pad > 0; xa /= 10) {
                pad--;
            }
            for (int xa = 0; xa < pad; xa++) {
                tgt.append('0');
            }
        }
        tgt.append(val);
    }

}
