package bio.singa.features.identifiers.model;

import bio.singa.features.model.Feature;
import bio.singa.features.model.Evidence;

import java.util.regex.Pattern;

/**
 * The most basic, abstract implementation of {@link Identifier}. Validates the identifier during instantiation, also
 * overrides equals, hashcode and toString methods.
 *
 * @author cl
 */
public abstract class AbstractIdentifier<IdentifierType> implements Identifier<IdentifierType>, Feature<IdentifierType> {

    /**
     * The identifier in string form.
     */
    private final String identifier;

    private final Evidence evidence;

    /**
     * Creates a new identifier by validating it with the given pattern.
     *
     * @param identifier The new identifier.
     * @param pattern A pattern to validate with.
     * @throws IllegalArgumentException If the identifier not valid.
     */
    public AbstractIdentifier(String identifier, Pattern pattern) throws IllegalArgumentException {
        this(identifier, pattern, Evidence.MANUALLY_ANNOTATED);
    }

    public AbstractIdentifier(String identifier, Pattern pattern, Evidence evidence) throws IllegalArgumentException {
        if (pattern.matcher(identifier).matches()) {
            this.identifier = identifier;
            this.evidence = evidence;
        } else {
            throw new IllegalArgumentException("The identifer \"" + identifier + "\" is no valid " +
                    getClass().getSimpleName() + ".");
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }


    @Override
    public Evidence getEvidence() {
        return evidence;
    }

    @Override
    public String getSymbol() {
        return "I:"+getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractIdentifier that = (AbstractIdentifier) o;
        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

}
