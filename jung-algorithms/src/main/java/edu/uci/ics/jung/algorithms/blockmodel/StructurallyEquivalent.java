/*
 * Copyright (c) 2004, The JUNG Authors
 *
 * All rights reserved.
 * Created on Jan 28, 2004
 *
 * This software is open-source under the BSD license; see either
 * "license.txt" or
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
package edu.uci.ics.jung.algorithms.blockmodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.Graph;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Identifies sets of structurally equivalent vertices in a graph. Vertices <i> i</i> and <i>j</i>
 * are structurally equivalent iff the set of <i>i</i>'s neighbors is identical to the set of
 * <i>j</i>'s neighbors, with the exception of <i>i</i> and <i>j</i> themselves. This algorithm
 * finds all sets of equivalent vertices in O(V^2) time.
 *
 * <p>You can extend this class to have a different definition of equivalence (by overriding <code>
 * isStructurallyEquivalent</code>), and may give it hints for accelerating the process by
 * overriding <code>canPossiblyCompare</code>. (For example, in a bipartite graph, <code>
 * canPossiblyCompare</code> may return <code>false</code> for vertices in different partitions.
 * This function should be fast.)
 *
 * @author Danyel Fisher
 */
public class StructurallyEquivalent<V> implements Function<Graph<V>, VertexPartition<V>> {
  public VertexPartition<V> apply(Graph<V> g) {
    ImmutableSet<ImmutableList<V>> vertexPairs = getEquivalentPairs(g);

    Set<Set<V>> rv = new HashSet<Set<V>>();
    Map<V, Set<V>> intermediate = new HashMap<V, Set<V>>();
    for (ImmutableList<V> pair : vertexPairs) {
      Set<V> res = intermediate.get(pair.get(0));
      if (res == null) {
        res = intermediate.get(pair.get(1));
      }
      if (res == null) { // we haven't seen this one before
        res = new HashSet<V>();
      }
      res.add(pair.get(0));
      res.add(pair.get(1));
      intermediate.put(pair.get(0), res);
      intermediate.put(pair.get(1), res);
    }
    rv.addAll(intermediate.values());

    // pick up the vertices which don't appear in intermediate; they are
    // singletons (equivalence classes of size 1)
    Collection<V> singletons = new ArrayList<V>(g.nodes());
    singletons.removeAll(intermediate.keySet());
    for (V v : singletons) {
      Set<V> vSet = Collections.singleton(v);
      intermediate.put(v, vSet);
      rv.add(vSet);
    }

    return new VertexPartition<V>(g, intermediate, rv);
  }

  /**
   * For each vertex pair v, v1 in G, checks whether v and v1 are fully equivalent: meaning that
   * they connect to the exact same vertices. (Is this regular equivalence, or whathaveyou?)
   *
   * @param g the graph whose equivalent pairs are to be generated
   * @return an immutable set of pairs of vertices, where all pairs are represented as immutable
   *     lists, and the vertices in the inner pairs are equivalent.
   */
  protected ImmutableSet<ImmutableList<V>> getEquivalentPairs(Graph<V> g) {

    ImmutableSet.Builder<ImmutableList<V>> rv = ImmutableSet.builder();
    Set<V> alreadyEquivalent = new HashSet<V>();

    List<V> l = new ArrayList<V>(g.nodes());

    for (V v1 : l) {
      if (alreadyEquivalent.contains(v1)) {
        continue;
      }

      for (Iterator<V> iterator = l.listIterator(l.indexOf(v1) + 1); iterator.hasNext(); ) {
        V v2 = iterator.next();

        if (alreadyEquivalent.contains(v2)) {
          continue;
        }

        if (!canBeEquivalent(v1, v2)) {
          continue;
        }

        if (isStructurallyEquivalent(g, v1, v2)) {
          ImmutableList<V> pair = ImmutableList.of(v1, v2);
          alreadyEquivalent.add(v2);
          rv.add(pair);
        }
      }
    }

    return rv.build();
  }

  /**
   * @param g the graph in which the structural equivalence comparison is to take place
   * @param v1 the vertex to check for structural equivalence to v2
   * @param v2 the vertex to check for structural equivalence to v1
   * @return {@code true} if {@code v1}'s predecessors/successors are equal to {@code v2}'s
   *     predecessors/successors
   */
  protected boolean isStructurallyEquivalent(Graph<V> g, V v1, V v2) {

    if (g.degree(v1) != g.degree(v2)) {
      return false;
    }

    Set<V> n1 = new HashSet<V>(g.predecessors(v1));
    n1.remove(v2);
    n1.remove(v1);
    Set<V> n2 = new HashSet<V>(g.predecessors(v2));
    n2.remove(v1);
    n2.remove(v2);

    Set<V> o1 = new HashSet<V>(g.successors(v1));
    Set<V> o2 = new HashSet<V>(g.successors(v2));
    o1.remove(v1);
    o1.remove(v2);
    o2.remove(v1);
    o2.remove(v2);

    // this neglects self-loops and directed edges from 1 to other
    boolean b = (n1.equals(n2) && o1.equals(o2));
    if (!b) {
      return b;
    }

    // if there's a directed edge v1->v2 then there's a directed edge v2->v1
    b &= (g.successors(v1).contains(v2) == g.successors(v2).contains(v1));

    // self-loop check
    b &= (g.successors(v1).contains(v1) == g.successors(v2).contains(v2));

    return b;
  }

  /**
   * This is a space for optimizations. For example, for a bipartite graph, vertices from different
   * partitions cannot possibly be equivalent.
   *
   * @param v1 the first vertex to compare
   * @param v2 the second vertex to compare
   * @return {@code true} if the vertices can be equivalent
   */
  protected boolean canBeEquivalent(V v1, V v2) {
    return true;
  }
}
