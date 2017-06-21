package de.bioforscher.singa.chemistry.algorithms.superimposition.consensus;

import de.bioforscher.singa.chemistry.algorithms.superimposition.SubstructureSuperimposition;
import de.bioforscher.singa.chemistry.physical.branches.StructuralMotif;
import de.bioforscher.singa.mathematics.graphs.trees.BinaryTree;

/**
 * A container encapsulating a {@link StructuralMotif} with a {@link BinaryTree} that represents its associated
 * consensus tree.
 *
 * @author fk
 */
public class ConsensusContainer {

    private StructuralMotif structuralMotif;
    private double consensusDistance;
    private BinaryTree<ConsensusContainer> consensusTree;
    private SubstructureSuperimposition superimposition;
    private boolean consensus;

    public ConsensusContainer(StructuralMotif structuralMotif, boolean consensus) {
        this.structuralMotif = structuralMotif;
        this.consensus = consensus;
        this.consensusDistance = 0.0;
    }

    /**
     * Returns true if the associated {@link StructuralMotif} is an artificial consensus.
     *
     * @return True if consensus.
     */
    public boolean isConsensus() {
        return this.consensus;
    }

    public SubstructureSuperimposition getSuperimposition() {
        return this.superimposition;
    }

    public void setSuperimposition(SubstructureSuperimposition superimposition) {
        this.superimposition = superimposition;
    }

    @Override
    public String toString() {
        return this.structuralMotif.toString() +
                "_" + this.consensusDistance;
    }

    public StructuralMotif getStructuralMotif() {
        return this.structuralMotif;
    }

    public BinaryTree<ConsensusContainer> getConsensusTree() {
        return this.consensusTree;
    }

    public void setConsensusTree(BinaryTree<ConsensusContainer> consensusTree) {
        this.consensusTree = consensusTree;
    }

    public void addToConsensusDistance(double delta) {
        this.consensusDistance += delta;
    }

    public double getConsensusDistance() {
        return this.consensusDistance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConsensusContainer that = (ConsensusContainer) o;

        return this.structuralMotif != null ? this.structuralMotif.equals(that.structuralMotif) : that.structuralMotif == null;
    }

    @Override
    public int hashCode() {
        return this.structuralMotif != null ? this.structuralMotif.hashCode() : 0;
    }
}