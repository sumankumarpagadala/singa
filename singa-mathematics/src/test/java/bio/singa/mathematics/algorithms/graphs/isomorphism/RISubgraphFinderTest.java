package bio.singa.mathematics.algorithms.graphs.isomorphism;

import bio.singa.core.utility.Pair;
import bio.singa.mathematics.graphs.model.DirectedEdge;
import bio.singa.mathematics.graphs.model.DirectedGraph;
import bio.singa.mathematics.graphs.model.GenericNode;
import bio.singa.mathematics.vectors.Vector2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author fk
 */
class RISubgraphFinderTest {

    private DirectedGraph<GenericNode<String>> pattern1;
    private DirectedGraph<GenericNode<String>> target1;

    private DirectedGraph<GenericNode<String>> pattern2;
    private DirectedGraph<GenericNode<String>> target2;

    @BeforeEach
    void initialize() {
        pattern1 = createFirstPatternGraph();
        target1 = createFirstTargetGraph();
        pattern2 = createSecondPatternGraph();
        target2 = createSecondTargetGraph();

    }

    private static DirectedGraph<GenericNode<String>> createFirstPatternGraph() {
        DirectedGraph<GenericNode<String>> patternGraph = new DirectedGraph<>();
        GenericNode<String> patternNode0 = new GenericNode<>(patternGraph.nextNodeIdentifier(), "0");
        patternGraph.addNode(patternNode0);
        GenericNode<String> patternNode3 = new GenericNode<>(patternGraph.nextNodeIdentifier(), "3");
        patternGraph.addNode(patternNode3);
        GenericNode<String> patternNode4 = new GenericNode<>(patternGraph.nextNodeIdentifier(), "4");
        patternGraph.addNode(patternNode4);
        GenericNode<String> patternNode6 = new GenericNode<>(patternGraph.nextNodeIdentifier(), "6");
        patternGraph.addNode(patternNode6);
        GenericNode<String> patternNode7 = new GenericNode<>(patternGraph.nextNodeIdentifier(), "7");
        patternGraph.addNode(patternNode7);

        patternGraph.addEdgeBetween(patternNode0, patternNode3);
        patternGraph.addEdgeBetween(patternNode3, patternNode0);

        patternGraph.addEdgeBetween(patternNode3, patternNode6);
        patternGraph.addEdgeBetween(patternNode6, patternNode3);

        patternGraph.addEdgeBetween(patternNode0, patternNode4);
        patternGraph.addEdgeBetween(patternNode4, patternNode0);

        patternGraph.addEdgeBetween(patternNode4, patternNode6);
        patternGraph.addEdgeBetween(patternNode6, patternNode4);

        patternGraph.addEdgeBetween(patternNode6, patternNode7);
        patternGraph.addEdgeBetween(patternNode7, patternNode6);

        return patternGraph;
    }

    private static DirectedGraph<GenericNode<String>> createFirstTargetGraph() {
        DirectedGraph<GenericNode<String>> targetGraph = new DirectedGraph<>();
        GenericNode<String> targetNode0 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "0");
        targetGraph.addNode(targetNode0);
        GenericNode<String> targetNode1 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "1");
        targetGraph.addNode(targetNode1);
        GenericNode<String> targetNode2 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "2");
        targetGraph.addNode(targetNode2);
        GenericNode<String> targetNode3 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "3");
        targetGraph.addNode(targetNode3);
        GenericNode<String> targetNode4 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "4");
        targetGraph.addNode(targetNode4);
        GenericNode<String> targetNode5 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "5");
        targetGraph.addNode(targetNode5);
        GenericNode<String> targetNode6 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "6");
        targetGraph.addNode(targetNode6);
        GenericNode<String> targetNode7 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "7");
        targetGraph.addNode(targetNode7);
        GenericNode<String> targetNode8 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "8");
        targetGraph.addNode(targetNode8);
        GenericNode<String> targetNode9 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "9");
        targetGraph.addNode(targetNode9);
        GenericNode<String> targetNode10 = new GenericNode<>(targetGraph.nextNodeIdentifier(), "10");
        targetGraph.addNode(targetNode10);


        targetGraph.addEdgeBetween(targetNode0, targetNode1);
        targetGraph.addEdgeBetween(targetNode1, targetNode0);

        targetGraph.addEdgeBetween(targetNode1, targetNode2);
        targetGraph.addEdgeBetween(targetNode2, targetNode1);

        targetGraph.addEdgeBetween(targetNode0, targetNode3);
        targetGraph.addEdgeBetween(targetNode3, targetNode0);

        targetGraph.addEdgeBetween(targetNode3, targetNode6);
        targetGraph.addEdgeBetween(targetNode6, targetNode3);

        targetGraph.addEdgeBetween(targetNode6, targetNode7);
        targetGraph.addEdgeBetween(targetNode7, targetNode6);

        targetGraph.addEdgeBetween(targetNode7, targetNode8);
        targetGraph.addEdgeBetween(targetNode8, targetNode7);

        targetGraph.addEdgeBetween(targetNode5, targetNode8);
        targetGraph.addEdgeBetween(targetNode8, targetNode5);

        targetGraph.addEdgeBetween(targetNode2, targetNode5);
        targetGraph.addEdgeBetween(targetNode5, targetNode2);

        targetGraph.addEdgeBetween(targetNode0, targetNode4);
        targetGraph.addEdgeBetween(targetNode4, targetNode0);

        targetGraph.addEdgeBetween(targetNode4, targetNode6);
        targetGraph.addEdgeBetween(targetNode6, targetNode4);

        targetGraph.addEdgeBetween(targetNode1, targetNode4);
        targetGraph.addEdgeBetween(targetNode4, targetNode1);

        targetGraph.addEdgeBetween(targetNode4, targetNode7);
        targetGraph.addEdgeBetween(targetNode7, targetNode4);

        targetGraph.addEdgeBetween(targetNode5, targetNode4);
        targetGraph.addEdgeBetween(targetNode4, targetNode5);

        targetGraph.addEdgeBetween(targetNode1, targetNode5);
        targetGraph.addEdgeBetween(targetNode5, targetNode1);

        targetGraph.addEdgeBetween(targetNode5, targetNode7);
        targetGraph.addEdgeBetween(targetNode7, targetNode5);

        targetGraph.addEdgeBetween(targetNode0, targetNode9);
        targetGraph.addEdgeBetween(targetNode9, targetNode0);

        targetGraph.addEdgeBetween(targetNode9, targetNode6);
        targetGraph.addEdgeBetween(targetNode6, targetNode9);

        targetGraph.addEdgeBetween(targetNode10, targetNode9);
        targetGraph.addEdgeBetween(targetNode9, targetNode10);
        return targetGraph;
    }

    private static DirectedGraph<GenericNode<String>> createSecondPatternGraph() {
        DirectedGraph<GenericNode<String>> pattern = new DirectedGraph<>();

        GenericNode<String> a = new GenericNode<>(pattern.nextNodeIdentifier(), "A");
        pattern.addNode(a);
        GenericNode<String> b = new GenericNode<>(pattern.nextNodeIdentifier(), "B");
        pattern.addNode(b);
        GenericNode<String> c = new GenericNode<>(pattern.nextNodeIdentifier(), "C");
        pattern.addNode(c);

        pattern.addEdgeBetween(a, b);
        pattern.addEdgeBetween(c, b);
        return pattern;
    }

    private static DirectedGraph<GenericNode<String>> createSecondTargetGraph() {
        DirectedGraph<GenericNode<String>> target = new DirectedGraph<>();

        GenericNode<String> ta = new GenericNode<>(target.nextNodeIdentifier(), "A");
        target.addNode(ta);
        GenericNode<String> tb = new GenericNode<>(target.nextNodeIdentifier(), "B");
        target.addNode(tb);
        GenericNode<String> tc = new GenericNode<>(target.nextNodeIdentifier(), "C");
        target.addNode(tc);
        GenericNode<String> td = new GenericNode<>(target.nextNodeIdentifier(), "D");
        target.addNode(td);

        target.addEdgeBetween(ta, tb);
        target.addEdgeBetween(tc, tb);
        target.addEdgeBetween(td, tc);
        target.addEdgeBetween(tb, td);
        return target;
    }

    @Test
    void shouldFindFullSubgraph() {
        RISubgraphFinder<GenericNode<String>, DirectedEdge<GenericNode<String>>, Vector2D, Integer, DirectedGraph<GenericNode<String>>> finder
                = new RISubgraphFinder<>(pattern1, target1, (a, b) -> a.getContent().equals(b.getContent()), (a, b) -> true);
        List<GenericNode<String>> solution = Stream.of(target1.getNode(7), target1.getNode(4),
                target1.getNode(0), target1.getNode(3), target1.getNode(6))
                .collect(Collectors.toList());
        List<List<GenericNode<String>>> matches = finder.getFullMatches();
        assertEquals(1, matches.size());
        assertEquals(solution, matches.get(0));
        for (Pair<GenericNode<String>> pairedMatch : finder.getFullMatchPairs().get(0)) {
            assertEquals(pairedMatch.getFirst().getContent(), pairedMatch.getSecond().getContent());
        }
    }

    @Test
    void shouldFindPartialSubgraph() {
        target1.removeNode(7);
        RISubgraphFinder<GenericNode<String>, DirectedEdge<GenericNode<String>>, Vector2D, Integer, DirectedGraph<GenericNode<String>>> finder
                = new RISubgraphFinder<>(pattern1, target1, (a, b) -> a.getContent().equals(b.getContent()), (a, b) -> true, 4);
        assertEquals(0, finder.getFullMatches().size());
        List<GenericNode<String>> solution = Stream.of(target1.getNode(4), target1.getNode(0),
                target1.getNode(3), target1.getNode(6))
                .collect(Collectors.toList());
        assertEquals(1, finder.getPartialMatches().size());
        assertEquals(solution, finder.getPartialMatches().get(4).get(0));
    }

    @Test
    @Disabled
    void shouldFindFullDirectedMatch() {
        BiFunction<GenericNode<String>, GenericNode<String>, Boolean> nodeConditionExtractor =
                (first, second) -> first.getContent().equals(second.getContent());

        // TODO the directed edge should be considered (or is it considered implicitly)
        BiFunction<DirectedEdge<GenericNode<String>>, DirectedEdge<GenericNode<String>>, Boolean> edgeConditionExtractor = (first, second) -> {
            // (first, second) -> nodeConditionExtractor.apply(first.getSource(), second.getSource()) && nodeConditionExtractor.apply(first.getTarget(), second.getTarget());
            return true;
        };

        RISubgraphFinder<GenericNode<String>, DirectedEdge<GenericNode<String>>, Vector2D, Integer, DirectedGraph<GenericNode<String>>> finder
                = new RISubgraphFinder<>(pattern2, target2, nodeConditionExtractor, edgeConditionExtractor);

        assertEquals(1, finder.getFullMatches().size());
    }

}