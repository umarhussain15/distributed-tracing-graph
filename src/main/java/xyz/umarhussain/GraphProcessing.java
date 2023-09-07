package xyz.umarhussain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GraphProcessing {

  private final HashMap<String, HashMap<String, Integer>> latencyGraph;

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Please provide file path of input graph!");
      System.out.println("Arguments example: <input file path> [-v]");
      System.out.println("                   <input file path>: Path to read input graph from");
      System.out.println("                   -v: Make program a bit more verbose");
      System.exit(1);
    }
    boolean verbose = args.length == 2;
    if (verbose) {
      System.out.println("Reading from file: " + args[0]);
    }
    String s;
    try {
      s = Files.readString(Path.of(args[0]));
    } catch (IOException e) {
      System.out.println("Cannot read file");
      throw new RuntimeException(e);
    }
    GraphProcessing graphProcessing = new GraphProcessing(s);

    if (verbose) {
      System.out.println("Input graph:");
      graphProcessing.latencyGraph.forEach(
          (node, neighbours) ->
              neighbours.forEach((n, latency) -> System.out.printf("%s%s%d%n", node, n, latency)));
      System.out.println("Result of tests:");
    }

    for (List<String> averageLatency :
        List.of(
            List.of("A", "B", "C"),
            List.of("A", "D"),
            List.of("A", "D", "C"),
            List.of("A", "E", "B", "C", "D"),
            List.of("A", "E", "D"))) {
      graphProcessing
          .averageLatency(averageLatency)
          .ifPresentOrElse(System.out::println, () -> System.out.println("NO SUCH TRACE"));
    }

    System.out.println(graphProcessing.countTraces("C", "C", 3, true));
    System.out.println(graphProcessing.countTraces("A", "C", 4, false));
    graphProcessing
        .shortestTraceLength("A", "C")
        .ifPresentOrElse(System.out::println, () -> System.out.println("NO SUCH TRACE"));
    graphProcessing
        .shortestTraceLength("B", "B")
        .ifPresentOrElse(System.out::println, () -> System.out.println("NO SUCH TRACE"));
    System.out.println(graphProcessing.countTracesUnderLatency("C", "C", 30));
  }

  public GraphProcessing(String graph) {
    String[] items = graph.split(",");

    latencyGraph = new HashMap<>();
    for (String latency : items) {
      if (latency.length() != 3) {
        throw new RuntimeException(String.format("Invalid graph latency: %s", latency));
      }
      String start = String.valueOf(latency.charAt(0));
      String stop = String.valueOf(latency.charAt(1));
      Integer time = Integer.valueOf(String.valueOf(latency.charAt(2)));
      if (!latencyGraph.containsKey(start)) {
        latencyGraph.put(start, new HashMap<>());
      }
      HashMap<String, Integer> peers = latencyGraph.get(start);
      if (peers.containsKey(stop)) {
        throw new RuntimeException(
            String.format("duplicate entry with start: '%s' & stop: '%s", start, stop));
      }
      peers.put(stop, time);
    }
  }

  /**
   * For the given list of connected nodes, traverse the graph and sums up the latency and return
   * the result. Return empty if path is not available in graph
   *
   * @param nodes list of nodes to traverse in the order they appear
   * @return An Optional with the result if input path is present, otherwise empty result.
   */
  public Optional<Integer> averageLatency(List<String> nodes) {
    int latency = 0;
    String remove = nodes.get(0);
    HashMap<String, Integer> next = latencyGraph.get(remove);
    for (String node : nodes.subList(1, nodes.size())) {
      if (next == null || !next.containsKey(node)) {
        return Optional.empty();
      }
      latency += next.get(node);
      next = latencyGraph.get(node);
    }
    return Optional.of(latency);
  }

  /**
   * Counts the number of traces between two nodes, with the given hop count as limit. Additional
   * parameter is used to enable counting of traces which are shorter than limit
   *
   * @param start starting node in the graph
   * @param find target node in the graph
   * @param hops Limit for the number of hops to stop traversing
   * @param countShort Boolean indicating if we should count traces which are less than hop limit
   * @return count of the traces found in the graph for the input
   */
  public int countTraces(String start, String find, int hops, boolean countShort) {
    AtomicInteger count = new AtomicInteger();
    if (hops == 0) return 0;
    HashMap<String, Integer> neighbors = latencyGraph.get(start);
    if (neighbors == null) return 0;
    for (String neighbor : neighbors.keySet()) {
      // if input ask for paths where hop count < hops is allowed. OR
      // if we have to match exact hops from the input. We have found the value
      if ((countShort && neighbor.equals(find))
          || (!countShort && hops == 1 && neighbor.equals(find))) {
        count.addAndGet(1);
      } else count.addAndGet(countTraces(neighbor, find, hops - 1, countShort));
    }
    return count.get();
  }

  /**
   * Finds shortest trace between two given nodes. If start and end nodes are not same then it
   * applies Dijkstra directly. Otherwise, it starts by first getting all the neighbours of the
   * start node and then for each node it runs Dijkstra algorithm to find the distance from neighbor
   * to target node. In the end it adds the result from neighbor traversal, and it's distance from
   * starting node. If it is smaller than others it is chosen to be the shortest trace
   *
   * @param start starting node in graph
   * @param end ending node in graph
   * @return An Optional with result if shortest trace is found, otherwise empty result
   */
  public Optional<Integer> shortestTraceLength(String start, String end) {
    int latency = Integer.MAX_VALUE;
    // if start and end node are same, dijkstra will not work, so we have to apply dijkstra to
    // neighbours
    if (start.equals(end)) {
      HashMap<String, Integer> neighbors = latencyGraph.get(start);
      for (String neighbor : neighbors.keySet()) {
        Integer nl = dijkstraSearch(neighbor, end);
        nl += neighbors.get(neighbor);
        if (nl < latency) {
          latency = nl;
        }
      }
    } else {
      latency = dijkstraSearch(start, end);
    }

    return (latency < 0 || latency == Integer.MAX_VALUE) ? Optional.empty() : Optional.of(latency);
  }

  /**
   * Finds the number of trace between start and end which have latency value less than given input.
   * Recursively goes in the depth and on the way count traces which are under the limit and have
   * ending value equal to end input
   *
   * @param start start node of the trace
   * @param end end node of the trace
   * @param latency value of each trace latency should smaller than this
   * @return count of traces found for the given input
   */
  public int countTracesUnderLatency(String start, String end, int latency) {
    AtomicInteger count = new AtomicInteger();
    if (latency <= 0) return 0;
    HashMap<String, Integer> neighbours = latencyGraph.get(start);
    for (String s : neighbours.keySet()) {
      int remaining = latency - neighbours.get(s);
      if (remaining > 0) {
        // if we encounter the end node on the way that will also count as a trace
        if (s.equals(end)) {
          count.addAndGet(1);
        }
        // further check in the graph if it leads to a new trace
        count.addAndGet(countTracesUnderLatency(s, end, remaining));
      }
    }
    return count.get();
  }

  /**
   * Dijkstra algorithm implementation, with additional check to stop early if we have found the end
   * node
   *
   * @param start starting node in graph
   * @param end ending node in graph which indicates the algorithm to stop and report latency value
   * @return latency value from start to end if path is found. Otherwise {@link Integer#MAX_VALUE}
   *     is returned.
   */
  public Integer dijkstraSearch(String start, String end) {

    HashMap<String, Integer> distances = new HashMap<>();
    HashMap<String, Boolean> visited = new HashMap<>();
    for (String node : latencyGraph.keySet()) {
      distances.put(node, Integer.MAX_VALUE);
      visited.put(node, false);
    }
    distances.put(start, 0);

    String current = start;
    Set<String> visitedParents = new HashSet<>();
    boolean endNotFound = true;
    while (endNotFound) {
      visited.put(current, true);
      HashMap<String, Integer> peers = latencyGraph.get(current);
      for (String peer : peers.keySet()) {
        if (visited.get(peer)) {
          continue;
        }
        visitedParents.add(peer);
        int newDistance = distances.get(current) + peers.get(peer);
        if (newDistance < distances.get(peer)) {
          distances.put(peer, newDistance);
        }
        // we will end early if we have found the end node
        if (peer.equals(end)) {
          endNotFound = false;
          break;
        }
      }
      visitedParents.remove(current);
      if (visitedParents.isEmpty()) break;
      int minimumDistance = Integer.MAX_VALUE;
      for (String visitedParent : visitedParents) {
        if (distances.get(visitedParent) < minimumDistance) {
          minimumDistance = distances.get(visitedParent);
          current = visitedParent;
        }
      }
    }
    return distances.get(end);
  }
}
