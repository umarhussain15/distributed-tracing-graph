package xyz.umarhussain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  void testEmptyGraphInput() {
    String input = "";
    Assertions.assertThrows(RuntimeException.class, () -> new GraphProcessing(input));
  }

  @Test
  void testAdditionalCommaInput() {
    String input = "AB3,BC1,CE2,";
    Assertions.assertDoesNotThrow(() -> new GraphProcessing(input));
  }

  @Test
  void testAverageLatencyWithNoNExistentPathReturnEmpty() {
    Optional<Integer> result = graphProcessing.averageLatency(List.of("C", "A", "D", "E"));
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testAverageLatency() {
    Optional<Integer> result = graphProcessing.averageLatency(List.of("A", "E", "B", "C", "D"));
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(22, result.get());
  }

  @Test
  void testDijkstraWithNoNExistentPathReturnInfinity() {
    Integer latency = graphProcessing.dijkstraSearch("C", "A");
    Assertions.assertEquals(Integer.MAX_VALUE, latency);
  }

  @Test
  void testDijkstraWithPathExisting() {
    Integer latency = graphProcessing.dijkstraSearch("A", "C");
    Assertions.assertEquals(9, latency);
  }

  @Test
  void testTracesWithNoNExistentPathReturnZero() {
    Integer traces = graphProcessing.countTraces("C", "A", 10, true);
    Assertions.assertEquals(0, traces);
  }

  @Test
  void testCountTracesWithExistingPathAndSmallerHopCounts() {
    Integer traces = graphProcessing.countTraces("C", "C", 3, true);
    Assertions.assertEquals(2, traces);
  }

  @Test
  void testCountTracesWithExistingPathAndExactHopCounts() {
    Integer traces = graphProcessing.countTraces("C", "C", 3, false);
    Assertions.assertEquals(1, traces);
  }

  @Test
  void testTracesCountWithLatencyForNoNExistentPathReturnZero() {
    Integer traces = graphProcessing.countTracesUnderLatency("C", "A", 10);
    Assertions.assertEquals(0, traces);
  }

  @Test
  void testTracesCountWithLatencyForExistingPaths() {
    Integer traces = graphProcessing.countTracesUnderLatency("A", "E", 12);
    Assertions.assertEquals(3, traces);
    Integer traces2 = graphProcessing.countTracesUnderLatency("A", "E", 11);
    Assertions.assertEquals(1, traces2);
  }

  @Test
  void testTracesCountWithZeroLatencyReturnZero() {
    Integer traces = graphProcessing.countTracesUnderLatency("A", "E", 0);
    Assertions.assertEquals(0, traces);
  }

  @Test
  void testShortestTraceWithNoNExistentPathReturnEmpty() {
    Optional<Integer> result = graphProcessing.shortestTraceLength("C", "A");
    Assertions.assertTrue(result.isEmpty());
  }

  @Test
  void testShortestTraceWithSameStartEnd() {
    Optional<Integer> result = graphProcessing.shortestTraceLength("C", "C");
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(9, result.get());

    Optional<Integer> result2 = graphProcessing.shortestTraceLength("A", "A");
    Assertions.assertTrue(result2.isEmpty());
  }

  @Test
  void testShortestTraceWithDifferentStartEnd() {
    Optional<Integer> result = graphProcessing.shortestTraceLength("A", "C");
    Assertions.assertTrue(result.isPresent());
    Assertions.assertEquals(9, result.get());

    Optional<Integer> result2 = graphProcessing.shortestTraceLength("A", "E");
    Assertions.assertTrue(result2.isPresent());
    Assertions.assertEquals(7, result2.get());
  }
}
