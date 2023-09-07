package xyz.umarhussain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class GraphProcessingTest {

    GraphProcessing graphProcessing;

    @BeforeEach
    public void setUp() throws IOException {
        String graph = Files.readString(Path.of("src/test/resources/input.txt"));
        graphProcessing = new GraphProcessing(graph);
    }

    @Test
    void testInvalidGraphInput() {
        String input = "AB3,BC1,CE2,BBD3";
        Assertions.assertThrows(RuntimeException.class, () -> new GraphProcessing(input));
    }

    @Test
    void testDuplicateEdgeInput() {
        String input = "AB3,BC1,CE2,CE2";
        Assertions.assertThrows(RuntimeException.class, () -> new GraphProcessing(input));
    }

    @Test
    void testAverageLatencyWithNoNExistentPathReturnEmpty() {
        Optional<Integer> result= graphProcessing.averageLatency(List.of("C","A","D","E"));
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testDijkstraWithNoNExistentPathReturnInfinity() {
        Integer latency = graphProcessing.dijkstraSearch("C", "A");
        Assertions.assertEquals(Integer.MAX_VALUE, latency);
    }


    @Test
    void testTracesWithNoNExistentPathReturnZero() {
        Integer traces = graphProcessing.countTraces("C", "A",10,true);
        Assertions.assertEquals(0, traces);
    }

    @Test
    void testTracesCountWithLatencyForNoNExistentPathReturnZero() {
        Integer traces = graphProcessing.countTracesUnderLatency("C", "A",10);
        Assertions.assertEquals(0, traces);
    }
    @Test
    void testTracesCountWithZeroLatencyReturnZero() {
        Integer traces = graphProcessing.countTracesUnderLatency("A", "E",0);
        Assertions.assertEquals(0, traces);
    }

    @Test
    void testShortestTraceWithNoNExistentPathReturnEmpty() {
        Optional<Integer> result= graphProcessing.shortestTraceLength("C", "A");
        Assertions.assertTrue(result.isEmpty());
    }

}
