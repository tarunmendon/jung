/*
 * Copyright (c) 2003, The JUNG Authors
 *
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.generators.random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Generates a random graph using the Erdos-Renyi binomial model (each pair of vertices is connected
 * with probability p).
 *
 * @author William Giordano, Scott White, Joshua O'Madadhain
 */
public class ErdosRenyiGenerator<N> {
  private int nodeCount;
  private double edgeConnectionProbability;
  private Random random;
  Supplier<N> nodeSupplier;

  /**
   * @param nodeSupplier factory for vertices of the appropriate type
   * @param nodeCount number of vertices graph should have
   * @param p Connection's probability between 2 vertices
   */
  public ErdosRenyiGenerator(Supplier<N> nodeSupplier, int nodeCount, double p) {
    this.nodeSupplier = checkNotNull(nodeSupplier);
    checkArgument(nodeCount > 0, "Number of vertices must be positive");
    checkArgument(p >= 0 && p <= 1, "Probability of connection must be in [0, 1]");
    this.nodeCount = nodeCount;
    edgeConnectionProbability = p;
    random = new Random();
  }

  /**
   * Returns a graph in which each pair of vertices is connected by an undirected edge with the
   * probability specified by the constructor.
   */
  public Graph<N> get() {
    MutableGraph<N> graph = GraphBuilder.undirected().expectedNodeCount(nodeCount).build();
    for (int i = 0; i < nodeCount; i++) {
      graph.addNode(nodeSupplier.get());
    }
    List<N> list = new ArrayList<N>(graph.nodes());

    for (int i = 0; i < nodeCount - 1; i++) {
      N v_i = list.get(i);
      for (int j = i + 1; j < nodeCount; j++) {
        N v_j = list.get(j);
        if (random.nextDouble() < edgeConnectionProbability) {
          graph.putEdge(v_i, v_j);
        }
      }
    }
    return graph;
  }

  /**
   * Sets the seed of the internal random number generator to {@code seed}. Enables consistent
   * behavior.
   *
   * @param seed the seed to use for the internal random number generator
   */
  public void setSeed(long seed) {
    random.setSeed(seed);
  }
}
