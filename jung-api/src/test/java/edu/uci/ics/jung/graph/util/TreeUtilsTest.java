package edu.uci.ics.jung.graph.util;

import edu.uci.ics.jung.graph.MutableCTreeNetwork;
import edu.uci.ics.jung.graph.TreeNetworkBuilder;
import org.junit.Assert;
import org.junit.Test;

/** Created by Tom Nelson */
public class TreeUtilsTest {

  @Test
  public void testSubTree() {

    MutableCTreeNetwork<String, Integer> tree =
        TreeNetworkBuilder.builder().expectedNodeCount(7).build();

    tree.addNode("root");

    tree.addEdge("root", "child1", 1); // two kids
    tree.addEdge("root", "child2", 2);

    tree.addEdge("child1", "grandchild11", 11);
    tree.addEdge("child1", "grandchild12", 12);

    tree.addEdge("child2", "grandchild21", 21);
    tree.addEdge("child2", "grandchild22", 22);

    MutableCTreeNetwork<String, Integer> subTree = TreeUtils.getSubTree(tree, "root");

    // better be the same
    Assert.assertEquals(tree.asGraph(), subTree.asGraph());

    MutableCTreeNetwork<String, Integer> childOneSubTree = TreeUtils.getSubTree(tree, "child1");

    MutableCTreeNetwork<String, Integer> expectedSubTree =
        TreeNetworkBuilder.builder().expectedNodeCount(3).build();
    expectedSubTree.addNode("child1");
    expectedSubTree.addEdge("child1", "grandchild11", 11);
    expectedSubTree.addEdge("child1", "grandchild12", 12);

    Assert.assertEquals(childOneSubTree.asGraph(), expectedSubTree.asGraph());
  }
}
