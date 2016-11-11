package de.bioforscher.chemistry.physical.atoms;

import de.bioforscher.chemistry.descriptive.elements.Element;
import de.bioforscher.chemistry.physical.model.StructuralEntity;
import de.bioforscher.mathematics.vectors.Vector3D;

import static de.bioforscher.chemistry.descriptive.elements.ElementProvider.DEUTERIUM;
import static de.bioforscher.chemistry.descriptive.elements.ElementProvider.HYDROGEN;

/**
 * Created by Christoph on 09/11/2016.
 */
public interface Atom extends StructuralEntity<Atom> {

    Element getElement();

    void setElement(Element element);

    // getIdentifier from StructuralEntity

    void setIdentifier(int identifier);

    // getPosition from StructuralEntity

    void setPosition(Vector3D position);

    AtomName getAtomName();

    void setAtomName(AtomName atomName);

    String getAtomNameString();

    void setAtomNameString(String atomNameString);

    default boolean isHydrogen() {
        return this.getElement().equals(HYDROGEN) || this.getElement().equals(DEUTERIUM);
    }

}