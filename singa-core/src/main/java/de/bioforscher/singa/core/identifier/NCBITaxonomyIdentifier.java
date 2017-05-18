package de.bioforscher.singa.core.identifier;

import de.bioforscher.singa.core.identifier.model.AbstractIdentifier;
import de.bioforscher.singa.core.identifier.model.Identifier;

import java.util.regex.Pattern;

/**
 * @author cl
 */
public class NCBITaxonomyIdentifier extends AbstractIdentifier {

    public static final Pattern PATTERN = Pattern.compile("[\\d]+");

    public NCBITaxonomyIdentifier(String identifier) throws IllegalArgumentException {
        super(identifier, PATTERN);
    }

    public static boolean check(Identifier identifier) {
        return PATTERN.matcher(identifier.toString()).matches();
    }

    public static Pattern getPattern() {
        return PATTERN;
    }

}
