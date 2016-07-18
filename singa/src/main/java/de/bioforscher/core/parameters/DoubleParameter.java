package de.bioforscher.core.parameters;

import java.math.BigDecimal;

public final class DoubleParameter implements Parameter<Double> {

    private final String name;
    private final BigDecimal minimalValue;
    private final BigDecimal maximalValue;

    public DoubleParameter(String name, double minimalValue, double maximalValue) {
        this.name = name;
        this.minimalValue = new BigDecimal(String.valueOf(minimalValue));
        this.maximalValue = new BigDecimal(String.valueOf(maximalValue));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Double getLowerBound() {
        return this.minimalValue.doubleValue();
    }

    @Override
    public Double getUpperBound() {
        return this.maximalValue.doubleValue();
    }

    @Override
    public String toString() {
        return "Parameter (Double) " + name + " [" + minimalValue + " ... " + maximalValue + "]";
    }

}