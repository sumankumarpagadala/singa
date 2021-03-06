package bio.singa.mathematics;

import bio.singa.mathematics.graphs.model.Edge;
import bio.singa.mathematics.graphs.model.Graph;
import bio.singa.mathematics.graphs.model.Node;
import bio.singa.mathematics.vectors.Vector;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author cl
 */
public class GraphAssertion {

    public static <NodeType extends Node<NodeType, VectorType, IdentifierType>,
            EdgeType extends Edge<NodeType>, VectorType extends Vector, IdentifierType,
            GraphType extends Graph<NodeType, EdgeType, IdentifierType>> void assertGraphContainsNodes(GraphType graph, IdentifierType... expectedIdentifiers) {

        Collection<NodeType> nodes = graph.getNodes();
        for (IdentifierType identifier : expectedIdentifiers) {
            boolean contained = false;
            for (NodeType node : nodes) {
                if (node.getIdentifier().equals(identifier)) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                fail("The graph was expected to contain a node with the identifier <" + identifier + ">, but no node with this identifier could be found.");
            }
        }

    }

}
