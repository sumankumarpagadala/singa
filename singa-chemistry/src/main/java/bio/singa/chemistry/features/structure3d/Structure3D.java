package bio.singa.chemistry.features.structure3d;

import bio.singa.features.model.AbstractFeature;
import bio.singa.features.model.Evidence;
import bio.singa.structure.model.interfaces.AtomContainer;

/**
 * @author cl
 */
public class Structure3D extends AbstractFeature<AtomContainer> {

public static final String SYMBOL = "Structure3D";

    public Structure3D(AtomContainer structure, Evidence evidence) {
        super(structure, evidence);
    }

    @Override
    public String getSymbol() {
        return SYMBOL;
    }
    
}
