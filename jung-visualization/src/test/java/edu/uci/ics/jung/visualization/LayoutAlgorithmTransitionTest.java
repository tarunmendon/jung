package edu.uci.ics.jung.visualization;

import static edu.uci.ics.jung.visualization.layout.AWT.POINT_MODEL;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import edu.uci.ics.jung.layout.algorithms.LayoutAlgorithm;
import edu.uci.ics.jung.layout.algorithms.StaticLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.layout.model.LoadingCacheLayoutModel;
import java.awt.geom.Point2D;
import org.junit.Test;

public class LayoutAlgorithmTransitionTest {

  @Test
  public void testTransition() throws Exception {
    MutableGraph<String> graph = GraphBuilder.undirected().build();
    graph.addNode("A");
    LayoutModel<String, Point2D> model =
        LoadingCacheLayoutModel.<String, Point2D>builder()
            .setGraph(graph)
            .setPointModel(POINT_MODEL)
            .setSize(100, 100)
            .build();

    model.set("A", 0, 0);
    LayoutAlgorithm newLayoutAlgorithm = new StaticLayoutAlgorithm();
  }
}
