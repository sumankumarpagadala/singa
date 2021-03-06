package bio.singa.chemistry.features.reactions;

import bio.singa.features.model.Evidence;
import tec.uom.se.quantity.Quantities;

import javax.measure.Quantity;
import javax.measure.Unit;

/**
 * The turnover number is the maximal number of substrate molecules converted to product by enzyme and second.
 *
 * @author cl
 */
public class TurnoverNumber extends FirstOrderRateConstant implements ForwardsRateConstant<FirstOrderRate> {

    public static final String SYMBOL = "k_cat";

    public TurnoverNumber(Quantity<FirstOrderRate> firstOrderRateQuantity, Evidence evidence) {
        super(firstOrderRateQuantity, evidence);
    }

    public TurnoverNumber(double value, Unit<FirstOrderRate> unit, Evidence evidence) {
        super(Quantities.getQuantity(value, unit), evidence);
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }

}
