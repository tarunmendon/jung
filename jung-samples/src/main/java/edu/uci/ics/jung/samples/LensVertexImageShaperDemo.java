/*
 * Copyright (c) 2003, The JUNG Authors
 * All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 *
 */
package edu.uci.ics.jung.samples;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.Network;
import com.google.common.graph.NetworkBuilder;
import edu.uci.ics.jung.layout.algorithms.FRLayoutAlgorithm;
import edu.uci.ics.jung.layout.model.LayoutModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.LayeredIcon;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.*;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.transform.LayoutLensSupport;
import edu.uci.ics.jung.visualization.transform.Lens;
import edu.uci.ics.jung.visualization.transform.LensSupport;
import edu.uci.ics.jung.visualization.transform.shape.MagnifyImageLensSupport;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.swing.*;

/**
 * Demonstrates the use of images to represent graph vertices. The images are added to the
 * DefaultGraphLabelRenderer and can either be offset from the vertex, or centered on the vertex.
 * Additionally, the relative positioning of the label and image is controlled by subclassing the
 * DefaultGraphLabelRenderer and setting the appropriate properties on its JLabel superclass
 * FancyGraphLabelRenderer
 *
 * <p>The images used in this demo (courtesy of slashdot.org) are rectangular but with a transparent
 * background. When vertices are represented by these images, it looks better if the actual shape of
 * the opaque part of the image is computed so that the edge arrowheads follow the visual shape of
 * the image. This demo uses the FourPassImageShaper class to compute the Shape from an image with
 * transparent background.
 *
 * @author Tom Nelson
 */
public class LensVertexImageShaperDemo extends JApplet {

  /** */
  private static final long serialVersionUID = 5432239991020505763L;

  /** the graph */
  Network<Number, Number> graph;

  /** the visual component and renderer for the graph */
  VisualizationViewer<Number, Number> vv;

  /** some icon names to use */
  String[] iconNames = {
    "apple",
    "os",
    "x",
    "linux",
    "inputdevices",
    "wireless",
    "graphics3",
    "gamespcgames",
    "humor",
    "music",
    "privacy"
  };

  LensSupport viewSupport;
  LensSupport modelSupport;
  LensSupport magnifyLayoutSupport;
  LensSupport magnifyViewSupport;
  /** create an instance of a simple graph with controls to demo the zoom features. */
  public LensVertexImageShaperDemo() {

    // create a simple graph for the demo
    graph = createGraph();

    // Maps for the labels and icons
    Map<Number, String> map = new HashMap<>();
    Map<Number, Icon> iconMap = new HashMap<>();
    for (Number node : graph.nodes()) {
      int i = node.intValue();
      map.put(node, iconNames[i % iconNames.length]);

      String name = "/images/topic" + iconNames[i] + ".gif";
      try {
        Icon icon =
            new LayeredIcon(
                new ImageIcon(LensVertexImageShaperDemo.class.getResource(name)).getImage());
        iconMap.put(node, icon);
      } catch (Exception ex) {
        System.err.println("You need slashdoticons.jar in your classpath to see the image " + name);
      }
    }

    FRLayoutAlgorithm<Number, Point2D> layoutAlgorithm = new FRLayoutAlgorithm<>();
    layoutAlgorithm.setMaxIterations(100);
    vv = new VisualizationViewer<>(graph, layoutAlgorithm, new Dimension(600, 600));

    Function<Number, Paint> vpf =
        new PickableVertexPaintTransformer<>(vv.getPickedVertexState(), Color.white, Color.yellow);
    vv.getRenderContext().setVertexFillPaintTransformer(vpf);
    vv.getRenderContext()
        .setEdgeDrawPaintTransformer(
            new PickableEdgePaintTransformer<>(vv.getPickedEdgeState(), Color.black, Color.cyan));

    vv.setBackground(Color.white);

    final Function<Number, String> vertexStringerImpl = new VertexStringerImpl<Number>(map);
    vv.getRenderContext().setVertexLabelTransformer(vertexStringerImpl);
    vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.cyan));
    vv.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.cyan));

    // features on and off. For a real application, use VertexIconAndShapeFunction instead.
    final VertexIconShapeTransformer<Number> vertexImageShapeFunction =
        new VertexIconShapeTransformer<Number>(new EllipseVertexShapeTransformer<Number>());

    final Function<Number, Icon> vertexIconFunction = iconMap::get;

    vertexImageShapeFunction.setIconMap(iconMap);

    vv.getRenderContext().setVertexShapeTransformer(vertexImageShapeFunction);
    vv.getRenderContext().setVertexIconTransformer(vertexIconFunction);

    // Get the pickedState and add a listener that will decorate the
    // Vertex images with a checkmark icon when they are picked
    PickedState<Number> ps = vv.getPickedVertexState();
    ps.addItemListener(new PickWithIconListener(vertexIconFunction));

    vv.addPostRenderPaintable(
        new VisualizationViewer.Paintable() {
          int x;
          int y;
          Font font;
          FontMetrics metrics;
          int swidth;
          int sheight;
          String str = "Thank You, slashdot.org, for the images!";

          public void paint(Graphics g) {
            Dimension d = vv.getSize();
            if (font == null) {
              font = new Font(g.getFont().getName(), Font.BOLD, 20);
              metrics = g.getFontMetrics(font);
              swidth = metrics.stringWidth(str);
              sheight = metrics.getMaxAscent() + metrics.getMaxDescent();
              x = (d.width - swidth) / 2;
              y = (int) (d.height - sheight * 1.5);
            }
            g.setFont(font);
            Color oldColor = g.getColor();
            g.setColor(Color.lightGray);
            g.drawString(str, x, y);
            g.setColor(oldColor);
          }

          public boolean useTransform() {
            return false;
          }
        });

    // add a listener for ToolTips
    vv.setVertexToolTipTransformer(new ToStringLabeller());

    Container content = getContentPane();
    final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
    content.add(panel);

    final DefaultModalGraphMouse<Number, Number> graphMouse = new DefaultModalGraphMouse<>();
    vv.setGraphMouse(graphMouse);

    final ScalingControl scaler = new CrossoverScalingControl();

    JButton plus = new JButton("+");
    plus.addActionListener(e -> scaler.scale(vv, 1.1f, vv.getCenter()));

    JButton minus = new JButton("-");
    minus.addActionListener(e -> scaler.scale(vv, 1 / 1.1f, vv.getCenter()));

    JComboBox<Mode> modeBox = graphMouse.getModeComboBox();
    JPanel modePanel = new JPanel();
    modePanel.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
    modePanel.add(modeBox);

    JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
    scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));
    JPanel controls = new JPanel();
    scaleGrid.add(plus);
    scaleGrid.add(minus);
    controls.add(scaleGrid);

    controls.add(modePanel);
    content.add(controls, BorderLayout.SOUTH);

    LayoutModel<Number, Point2D> layoutModel = vv.getModel().getLayoutModel();
    Dimension d = new Dimension(layoutModel.getWidth(), layoutModel.getHeight());
    Lens lens = new Lens(d);
    lens.setMagnification(2.0f);
    this.viewSupport = new MagnifyImageLensSupport<>(vv, lens);

    this.modelSupport = new LayoutLensSupport<>(vv, lens);

    graphMouse.addItemListener(modelSupport.getGraphMouse().getModeListener());
    graphMouse.addItemListener(viewSupport.getGraphMouse().getModeListener());

    ButtonGroup radio = new ButtonGroup();
    JRadioButton none = new JRadioButton("None");
    none.addItemListener(
        e -> {
          if (viewSupport != null) {
            viewSupport.deactivate();
          }
          if (modelSupport != null) {
            modelSupport.deactivate();
          }
        });
    none.setSelected(true);

    JRadioButton hyperView = new JRadioButton("View");
    hyperView.addItemListener(e -> viewSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    JRadioButton hyperModel = new JRadioButton("Layout");
    hyperModel.addItemListener(
        e -> modelSupport.activate(e.getStateChange() == ItemEvent.SELECTED));

    radio.add(none);
    radio.add(hyperView);
    radio.add(hyperModel);

    JMenuBar menubar = new JMenuBar();
    JMenu modeMenu = graphMouse.getModeMenu();
    menubar.add(modeMenu);

    JPanel lensPanel = new JPanel(new GridLayout(2, 0));
    lensPanel.setBorder(BorderFactory.createTitledBorder("Lens"));
    lensPanel.add(none);
    lensPanel.add(hyperView);
    lensPanel.add(hyperModel);
    controls.add(lensPanel);
  }

  /**
   * A simple implementation of VertexStringer that gets Vertex labels from a Map
   *
   * @author Tom Nelson
   */
  class VertexStringerImpl<V> implements Function<V, String> {

    Map<V, String> map = new HashMap<V, String>();

    boolean enabled = true;

    public VertexStringerImpl(Map<V, String> map) {
      this.map = map;
    }

    /** */
    public String apply(V v) {
      if (isEnabled()) {
        return map.get(v);
      } else {
        return "";
      }
    }

    /** @return Returns the enabled. */
    public boolean isEnabled() {
      return enabled;
    }

    /** @param enabled The enabled to set. */
    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  Network<Number, Number> createGraph() {
    MutableNetwork<Number, Number> graph = NetworkBuilder.directed().build();
    graph.addEdge(0, 1, Math.random());
    graph.addEdge(3, 0, Math.random());
    graph.addEdge(0, 4, Math.random());
    graph.addEdge(4, 5, Math.random());
    graph.addEdge(5, 3, Math.random());
    graph.addEdge(2, 1, Math.random());
    graph.addEdge(4, 1, Math.random());
    graph.addEdge(8, 2, Math.random());
    graph.addEdge(3, 8, Math.random());
    graph.addEdge(6, 7, Math.random());
    graph.addEdge(7, 5, Math.random());
    graph.addEdge(0, 9, Math.random());
    graph.addEdge(9, 8, Math.random());
    graph.addEdge(7, 6, Math.random());
    graph.addEdge(6, 5, Math.random());
    graph.addEdge(4, 2, Math.random());
    graph.addEdge(5, 4, Math.random());
    graph.addEdge(4, 10, Math.random());
    graph.addEdge(10, 4, Math.random());

    return graph;
  }

  public static class PickWithIconListener implements ItemListener {
    Function<Number, Icon> imager;
    Icon checked;

    public PickWithIconListener(Function<Number, Icon> imager) {
      this.imager = imager;
      checked = new Checkmark(Color.red);
    }

    public void itemStateChanged(ItemEvent e) {
      Icon icon = imager.apply((Number) e.getItem());
      if (icon != null && icon instanceof LayeredIcon) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          ((LayeredIcon) icon).add(checked);
        } else {
          ((LayeredIcon) icon).remove(checked);
        }
      }
    }
  }

  public static void main(String[] args) {
    JFrame frame = new JFrame();
    Container content = frame.getContentPane();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    content.add(new LensVertexImageShaperDemo());
    frame.pack();
    frame.setVisible(true);
  }
}
