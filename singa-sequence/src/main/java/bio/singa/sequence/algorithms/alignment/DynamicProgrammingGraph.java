package bio.singa.sequence.algorithms.alignment;

import bio.singa.core.utility.Pair;
import bio.singa.mathematics.graphs.model.DirectedWeightedGraph;
import bio.singa.mathematics.vectors.Vector2D;
import bio.singa.sequence.model.ProteinSequence;
import bio.singa.structure.model.families.AminoAcidFamily;

/**
 * @author cl
 */
public class DynamicProgrammingGraph extends DirectedWeightedGraph<DynamicProgrammingNode, DynamicProgrammingEdge> {

    private ProteinSequence firstSequence;
    private ProteinSequence secondSequence;

    private DynamicProgrammingNode[][] nodes;

    public DynamicProgrammingGraph(ProteinSequence firstSequence, ProteinSequence secondSequence) {
        this.firstSequence = firstSequence;
        this.secondSequence = secondSequence;
        nodes = new DynamicProgrammingNode[firstSequence.getLength()+1][secondSequence.getLength()+1];
    }

    public Integer addNode(DynamicProgrammingNode node, int firstIndex, int secondIndex) {
        node.setPosition(new Vector2D(10 + firstIndex * 25.0 , 10 + secondIndex * 25.0));
        nodes[firstIndex][secondIndex] = node;
        return super.addNode(node);
    }

    public DynamicProgrammingNode getNode(int firstIndex, int secondIndex) {
        return nodes[firstIndex][secondIndex];
    }

    public Pair<AminoAcidFamily> getParing(int firstIndex, int secondIndex) {
        return new Pair<>(firstSequence.getLetter(firstIndex), secondSequence.getLetter(secondIndex));
    }

    public ProteinSequence getFirstSequence() {
        return firstSequence;
    }

    public int getFirstSequenceLength() {
        return firstSequence.getLength();
    }

    public ProteinSequence getSecondSequence() {
        return secondSequence;
    }

    public int getSecondSequenceLength() {
        return secondSequence.getLength();
    }

    @Override
    public int addEdgeBetween(int identifier, DynamicProgrammingNode source, DynamicProgrammingNode target) {
        return addEdgeBetween(new DynamicProgrammingEdge(identifier), source, target);
    }

    @Override
    public int addEdgeBetween(DynamicProgrammingNode source, DynamicProgrammingNode target) {
        return addEdgeBetween(nextNodeIdentifier(), source, target);
    }

    public int addEdgeBetween(int identifier, double weight, DynamicProgrammingNode source, DynamicProgrammingNode target) {
        return addEdgeBetween(new DynamicProgrammingEdge(identifier, weight), source, target);
    }

}
