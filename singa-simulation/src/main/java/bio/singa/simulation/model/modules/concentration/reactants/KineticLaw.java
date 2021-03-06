package bio.singa.simulation.model.modules.concentration.reactants;

import bio.singa.features.model.Evidence;
import bio.singa.features.model.ScalableFeature;
import bio.singa.features.model.ScalableQuantityFeature;
import bio.singa.features.quantities.MolarConcentration;
import bio.singa.simulation.exceptions.ModuleCalculationException;
import bio.singa.simulation.model.parameters.Parameter;
import bio.singa.simulation.model.sections.ConcentrationContainer;
import tec.uom.se.quantity.Quantities;
import uk.co.cogitolearning.cogpar.*;

import javax.measure.Quantity;
import java.util.HashMap;
import java.util.Map;

import static tec.uom.se.AbstractUnit.ONE;

/**
 * Dynamic kinetic laws allow for the definition of reaction kinetics based on equations.
 *
 * @author cl
 */
public class KineticLaw {

    /**
     * The expression that is evaluated.
     */
    private final ExpressionNode expression;

    /**
     * The original string of the expression.
     */
    private final String expressionString;

    /**
     * The features influencing the reaction.
     */
    private Map<String, ScalableQuantityFeature> featureMap;

    /**
     * The parameters remaining constant in the course of the simulation.
     */
    private Map<String, Parameter> parameterMap;

    /**
     * The reactants involved in the reaction.
     */
    private Map<String, Reactant> concentrationMap;

    public KineticLaw(String kineticLawString) {
        ExpressionParser parser = new ExpressionParser();
        expressionString = kineticLawString;
        expression = parser.parse(kineticLawString);
        featureMap = new HashMap<>();
        concentrationMap = new HashMap<>();
        parameterMap = new HashMap<>();
    }

    public void referenceReactant(String parameterIdentifier, Reactant reactant) {
        concentrationMap.put(parameterIdentifier, reactant);
    }

    public void referenceReactant(Reactant reactant) {
        concentrationMap.put(reactant.getEntity().getIdentifier().toString(), reactant);
    }

    public void referenceFeature(String parameterIdentifier, ScalableQuantityFeature feature) {
        featureMap.put(parameterIdentifier, feature);
    }

    public void referenceFeature(ScalableQuantityFeature feature) {
        featureMap.put(feature.getSymbol(), feature);
    }

    public void referenceConstant(String parameterIdentifier, double constant) {
        parameterMap.put(parameterIdentifier, new Parameter<>(parameterIdentifier, Quantities.getQuantity(constant, ONE), Evidence.MANUALLY_ANNOTATED));
        expression.accept(new SetVariable(parameterIdentifier, constant));
    }

    public void referenceConstant(String parameterIdentifier, double constant, Evidence evidence) {
        parameterMap.put(parameterIdentifier, new Parameter<>(parameterIdentifier, Quantities.getQuantity(constant, ONE), evidence));
        expression.accept(new SetVariable(parameterIdentifier, constant));
    }

    public void referenceParameter(Parameter<?> parameter) {
        parameterMap.put(parameter.getIdentifier(), parameter);
    }

    public Map<String, ScalableQuantityFeature> getFeatureMap() {
        return featureMap;
    }

    public void setFeatureMap(Map<String, ScalableQuantityFeature> featureMap) {
        this.featureMap = featureMap;
    }

    public Map<String, Reactant> getConcentrationMap() {
        return concentrationMap;
    }

    public void setConcentrationMap(Map<String, Reactant> concentrationMap) {
        this.concentrationMap = concentrationMap;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public Map<String, Parameter> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Parameter> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public void scaleScalableFeatures() {
        getFeatureMap().values().forEach(ScalableFeature::scale);
        getParameterMap().values().forEach(Parameter::scale);
    }

    /**
     * Calculates the velocity of the reaction based on the entities in the concentration container.
     *
     * @param concentrationContainer The concentration container.
     * @return The velocity.
     */
    public double calculateVelocity(ConcentrationContainer concentrationContainer, boolean isStrutCalculation) {
        // set features
        for (Map.Entry<String, ScalableQuantityFeature> entry : featureMap.entrySet()) {
            Quantity<?> featureQuantity;
            if (isStrutCalculation) {
                featureQuantity = entry.getValue().getHalfScaledQuantity();
            } else {
                featureQuantity = entry.getValue().getScaledQuantity();
            }
            SetVariable variable = new SetVariable(entry.getKey(), featureQuantity.getValue().doubleValue());
            expression.accept(variable);
        }
        // set parameters
        for (Map.Entry<String, Parameter> entry : parameterMap.entrySet()) {
            Quantity<?> parameterQuantity;
            if (isStrutCalculation) {
                parameterQuantity = entry.getValue().getHalfScaledQuantity();
            } else {
                parameterQuantity = entry.getValue().getScaledQuantity();
            }
            SetVariable variable = new SetVariable(entry.getKey(), parameterQuantity.getValue().doubleValue());
            expression.accept(variable);
        }
        // set concentrations
        for (Map.Entry<String, Reactant> entry : concentrationMap.entrySet()) {
            Reactant reactant = entry.getValue();
            Quantity<MolarConcentration> concentration;
            if (reactant.getPreferredConcentrationUnit() != null) {
                concentration = concentrationContainer.get(reactant.getPreferredTopology(), reactant.getEntity()).to(reactant.getPreferredConcentrationUnit());
            } else {
                concentration = concentrationContainer.get(reactant.getPreferredTopology(), reactant.getEntity());
            }
            SetVariable variable = new SetVariable(entry.getKey(), concentration.getValue().doubleValue());
            expression.accept(variable);
        }
        // calculate
        return evaluate();
    }


    /**
     * Evaluates the expression and returns the result. If not all parameters have been set or the expression evaluates
     * to NaN an error is logged.
     *
     * @return The result of the evaluated expression.
     */
    private double evaluate() {
        double value;
        try {
            value = expression.getValue();
        } catch (ParserException | EvaluationException e) {
            throw new ModuleCalculationException("Could not calculate expression" + expressionString + ". " + e.getMessage());
        }
        if (Double.isNaN(value)) {
            throw new ModuleCalculationException("Could not calculate expression for " + expressionString + ", value was NaN.");
        }
        return value;
    }

}
