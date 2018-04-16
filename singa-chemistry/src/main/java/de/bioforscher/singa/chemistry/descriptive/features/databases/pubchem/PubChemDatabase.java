package de.bioforscher.singa.chemistry.descriptive.features.databases.pubchem;

import de.bioforscher.singa.features.identifiers.ChEBIIdentifier;
import de.bioforscher.singa.features.identifiers.InChIKey;
import de.bioforscher.singa.features.identifiers.PubChemIdentifier;
import de.bioforscher.singa.features.model.FeatureOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cl
 */
public class PubChemDatabase {

    public static final FeatureOrigin origin = new FeatureOrigin(FeatureOrigin.OriginType.DATABASE,
            "PubChem Database",
            "Kim, Sunghwan, et al. \"PubChem substance and compound databases.\" Nucleic acids research " +
                    "44.D1 (2016): D1202-D1213.");
    private static final Logger logger = LoggerFactory.getLogger(PubChemDatabase.class);

    /**
     * The instance.
     */
    private static final PubChemDatabase instance = new PubChemDatabase();

    public static PubChemDatabase getInstance() {
        return instance;
    }

    public static InChIKey fetchInchiKey(PubChemIdentifier pubChemIdentifier) {
        return PubChemParserService.parse(pubChemIdentifier).getFeature(InChIKey.class);
    }

    public static ChEBIIdentifier fetchChEBIIdentifier(PubChemIdentifier pubChemIdentifier) {
        return PubChemParserService.parse(pubChemIdentifier).getFeature(ChEBIIdentifier.class);
    }

}
