package de.bioforscher.singa.simulation.model.compartments;

import de.bioforscher.singa.mathematics.algorithms.graphs.ShortestPathFinder;
import de.bioforscher.singa.simulation.model.graphs.BioNode;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * An EnclosedCompartment is a {@link CellSection} that is bordered or enclosed by a {@link Membrane}.
 *
 * @author cl
 */
public class EnclosedCompartment extends CellSection {

    /**
     * The enclosing membrane.
     */
    private Membrane enclosingMembrane;

     /**
     * Creates a new EnclosedCompartment with the given identifier and name.
     *
     * @param identifier The identifier (should be unique).
     * @param name       The qualified name.
     */
    public EnclosedCompartment(String identifier, String name) {
        super(identifier, name);
    }

    /**
     * Tries to generate a {@link Membrane} around the contents of this compartment. This methods looks for neighbours
     * that are not part of this compartment and generates the membrane following this border.
     *
     * @return The generated membrane
     */
    public Membrane generateMembrane() {
        // TODO fix placement of borders along graph borders
        // TODO fix all the other problems :(

        // the nodes of the membrane
        LinkedList<BioNode> nodes = new LinkedList<>();
        // set the internal node state to cytosol
        getContent().forEach(node -> node.setState(NodeState.CYTOSOL));
        // find starting point
        BioNode first = getContent().stream()
                .filter(bioNode -> bioNode.getNeighbours().stream()
                        .anyMatch(neighbour -> neighbour.getCellSection().getIdentifier().equals(this.getIdentifier())))
                .findAny().get();
        // add first node
        nodes.add(first);
        // the iterating node
        BioNode step = first;
        // remembers if a connection around the compartment could be made
        boolean notConnected = true;
        // as lon as no connection could be found
        while (notConnected) {

            boolean foundNeighbour = false;
            // search neighbours
            for (BioNode neighbour : step.getNeighbours()) {
                if (isNewBorder(nodes, neighbour)) {
                    foundNeighbour = true;
                    nodes.add(neighbour);
                    neighbour.setState(NodeState.MEMBRANE);
                    step = neighbour;
                    break;
                }
            }

            // check if border can be closed
            if (!foundNeighbour) {
                for (BioNode neighbour : step.getNeighbours()) {
                    if (nodes.getFirst().equals(neighbour)) {
                        notConnected = false;
                        foundNeighbour = true;
                        nodes.add(neighbour);
                        neighbour.setState(NodeState.MEMBRANE);
                    }
                }
            }

            // try to traverse bridge
            if (!foundNeighbour) {
                LinkedList<BioNode> nextBest = ShortestPathFinder.trackBasedOnPredicates(step, currentNode -> this.isNewBorder(nodes, currentNode), this::isInThisCompartment);
                if (nextBest != null) {
                    for (BioNode node : nextBest) {
                        if (!nodes.contains(node)) {
                            nodes.add(node);
                            node.setState(NodeState.MEMBRANE);
                        }
                    }
                    step = nextBest.getLast();
                } else {
                    System.out.println("could not finish compartment border");
                    break;
                }

            }

        }

        this.enclosingMembrane = Membrane.forCompartment(this);
        this.enclosingMembrane.setContent(new HashSet<>(nodes));
        return this.enclosingMembrane;
    }

    private boolean isInThisCompartment(BioNode node) {
        return node.getCellSection().getIdentifier().equals(this.getIdentifier());
    }

    private boolean hasNeighbourInOtherCompartment(BioNode node) {
        for (BioNode neighbour : node.getNeighbours()) {
            if (!isInThisCompartment(neighbour)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNewBorder(LinkedList<BioNode> oldNodes, BioNode currentNode) {
        return isInThisCompartment(currentNode) &&
                !oldNodes.contains(currentNode) &&
                hasNeighbourInOtherCompartment(currentNode);
    }

    public Membrane getEnclosingMembrane() {
        return this.enclosingMembrane;
    }

    public void setEnclosingMembrane(Membrane enclosingMembrane) {
        this.enclosingMembrane = enclosingMembrane;
    }

}