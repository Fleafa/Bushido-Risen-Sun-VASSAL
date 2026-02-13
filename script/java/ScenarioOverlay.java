/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  VASSAL.build.AbstractConfigurable
 *  VASSAL.build.Buildable
 *  VASSAL.build.GameModule
 *  VASSAL.build.module.Map
 *  VASSAL.build.module.documentation.HelpFile
 *  VASSAL.build.module.map.Drawable
 */
package bushido;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.module.map.Drawable;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

public class ScenarioOverlay
extends AbstractConfigurable
implements Drawable {
    private static final int PPI = 80;
    private static final int BF_LEFT = 139;
    private static final int BF_TOP = 480;
    private static final int BF_RIGHT = 2059;
    private static final int BF_BOTTOM = 2400;
    private static final int BF_SIZE = 1920;
    private static final double MM30_DIAMETER_PX = 94.48818897637796;
    private static final double INCH4_DIAMETER_PX = 320.0;
    private static final double INCH8_DIAMETER_PX = 640.0;
    private static final float BASE_STROKE_WIDTH = 5.0f;
    private static final Color CIRCLE_COLOR = new Color(128, 128, 128, 200);
    private String currentScenario = "None";
    private Map mainMap = null;
    private boolean registered = false;
    private final List<double[]> currentCircles = new ArrayList<double[]>();

    public String[] getAttributeNames() {
        return new String[0];
    }

    public String[] getAttributeDescriptions() {
        return new String[0];
    }

    public Class<?>[] getAttributeTypes() {
        return new Class[0];
    }

    public void setAttribute(String string, Object object) {
    }

    public String getAttributeValueString(String string) {
        return null;
    }

    public Class<?>[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public HelpFile getHelpFile() {
        return null;
    }

    public void addTo(Buildable buildable) {
        GameModule gameModule = GameModule.getGameModule();
        if (gameModule != null) {
            JButton jButton = new JButton("Scenario");
            jButton.setToolTipText("Select a scenario to display guide circles");
            jButton.addActionListener(actionEvent -> this.showScenarioDialog());
            gameModule.getToolBar().add(jButton);
        }
    }

    public void removeFrom(Buildable buildable) {
        if (this.mainMap != null && this.registered) {
            this.mainMap.removeDrawComponent((Drawable)this);
            this.registered = false;
        }
    }

    private Map findMainMap() {
        if (this.mainMap != null) {
            return this.mainMap;
        }
        GameModule gameModule = GameModule.getGameModule();
        if (gameModule == null) {
            return null;
        }
        List list = gameModule.getComponentsOf(Map.class);
        for (Map map : list) {
            if (!"Main Table".equals(map.getMapName())) continue;
            this.mainMap = map;
            return map;
        }
        if (!list.isEmpty()) {
            this.mainMap = (Map)list.get(0);
            return this.mainMap;
        }
        return null;
    }

    private void ensureRegistered() {
        Map map;
        if (!this.registered && (map = this.findMainMap()) != null) {
            map.addDrawComponent((Drawable)this);
            this.registered = true;
        }
    }

    private void showScenarioDialog() {
        String[] stringArray = new String[]{"None", "Depletion", "Omaju", "Sacred Idols", "Shin Keii", "Ryodo", "Ryu-Seikyuu", "Botoku", "Ichi No Riten", "Muzukashi", "Ninki", "Seigyo", "Osatsu"};
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        jPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel.add(new JLabel("Select Scenario:"));
        jPanel.add(Box.createVerticalStrut(10));
        ButtonGroup buttonGroup = new ButtonGroup();
        JRadioButton[] jRadioButtonArray = new JRadioButton[stringArray.length];
        for (int i = 0; i < stringArray.length; ++i) {
            jRadioButtonArray[i] = new JRadioButton(stringArray[i]);
            jRadioButtonArray[i].setAlignmentX(0.0f);
            if (stringArray[i].equals(this.currentScenario)) {
                jRadioButtonArray[i].setSelected(true);
            }
            buttonGroup.add(jRadioButtonArray[i]);
            jPanel.add(jRadioButtonArray[i]);
        }
        if (buttonGroup.getSelection() == null) {
            jRadioButtonArray[0].setSelected(true);
        }
        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setPreferredSize(new Dimension(250, 400));
        int n = JOptionPane.showConfirmDialog(null, jScrollPane, "Scenario Selection", 2, -1);
        if (n == 0) {
            for (int i = 0; i < jRadioButtonArray.length; ++i) {
                if (!jRadioButtonArray[i].isSelected()) continue;
                this.currentScenario = stringArray[i];
                break;
            }
            this.applyScenario();
        }
    }

    private void applyScenario() {
        this.ensureRegistered();
        Map map = this.findMainMap();
        if (map == null) {
            return;
        }
        this.currentCircles.clear();
        if (!"None".equals(this.currentScenario)) {
            this.buildCircles(this.currentScenario);
        }
        map.repaint();
    }

    private static double ifl(double d) {
        return 139.0 + d * 80.0;
    }

    private static double ift(double d) {
        return 480.0 + d * 80.0;
    }

    private static double ifr(double d) {
        return 2059.0 - d * 80.0;
    }

    private static double ifb(double d) {
        return 2400.0 - d * 80.0;
    }

    private static double cx() {
        return 1099.0;
    }

    private static double cy() {
        return 1440.0;
    }

    private void addC(double d, double d2, double d3) {
        this.currentCircles.add(new double[]{d, d2, d3});
    }

    private void buildCircles(String string) {
        switch (string) {
            case "Depletion": 
            case "Shin Keii": 
            case "Muzukashi": 
            case "Ninki": {
                this.addC(ScenarioOverlay.ifl(8.0), ScenarioOverlay.ift(6.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifr(8.0), ScenarioOverlay.ift(6.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifl(6.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifr(6.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifl(8.0), ScenarioOverlay.ifb(6.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifr(8.0), ScenarioOverlay.ifb(6.0), 94.48818897637796);
                break;
            }
            case "Omaju": 
            case "Sacred Idols": 
            case "Seigyo": {
                this.addC(ScenarioOverlay.ifl(6.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifl(12.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifl(18.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                break;
            }
            case "Ryodo": {
                this.addC(ScenarioOverlay.ifl(4.0), ScenarioOverlay.ift(8.0), 320.0);
                this.addC(ScenarioOverlay.cx(), ScenarioOverlay.cy(), 320.0);
                this.addC(ScenarioOverlay.ifr(4.0), ScenarioOverlay.ifb(8.0), 320.0);
                break;
            }
            case "Ryu-Seikyuu": {
                this.addC(ScenarioOverlay.ifl(5.0), ScenarioOverlay.ift(12.0), 320.0);
                this.addC(ScenarioOverlay.ifl(12.0), ScenarioOverlay.ift(12.0), 320.0);
                this.addC(ScenarioOverlay.ifr(5.0), ScenarioOverlay.ift(12.0), 320.0);
                break;
            }
            case "Botoku": {
                this.addC(ScenarioOverlay.ifr(6.0), ScenarioOverlay.ift(6.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifl(6.0), ScenarioOverlay.ifb(6.0), 94.48818897637796);
                this.addC(ScenarioOverlay.cx(), ScenarioOverlay.cy(), 320.0);
                break;
            }
            case "Ichi No Riten": {
                this.addC(ScenarioOverlay.ifl(7.0), ScenarioOverlay.ifb(10.0), 640.0);
                this.addC(ScenarioOverlay.ifr(7.0), ScenarioOverlay.ift(10.0), 640.0);
                break;
            }
            case "Osatsu": {
                this.addC(ScenarioOverlay.ifl(6.0), ScenarioOverlay.ift(12.0), 640.0);
                this.addC(ScenarioOverlay.ifl(6.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.ifr(6.0), ScenarioOverlay.ift(12.0), 640.0);
                this.addC(ScenarioOverlay.ifr(6.0), ScenarioOverlay.ift(12.0), 94.48818897637796);
                this.addC(ScenarioOverlay.cx(), ScenarioOverlay.cy(), 94.48818897637796);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void draw(Graphics graphics, Map map) {
        if (this.currentCircles.isEmpty()) {
            return;
        }
        Graphics2D graphics2D = (Graphics2D)graphics;
        double d = graphics2D.getDeviceConfiguration().getDefaultTransform().getScaleX();
        double d2 = map.getZoom();
        double d3 = d2 * d;
        Graphics2D graphics2D2 = (Graphics2D)graphics.create();
        try {
            graphics2D2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D2.setColor(CIRCLE_COLOR);
            graphics2D2.setStroke(new BasicStroke((float)(5.0 * d)));
            for (double[] dArray : this.currentCircles) {
                double d4 = dArray[0] * d3;
                double d5 = dArray[1] * d3;
                double d6 = dArray[2] / 2.0 * d3;
                graphics2D2.draw(new Ellipse2D.Double(d4 - d6, d5 - d6, d6 * 2.0, d6 * 2.0));
            }
        }
        finally {
            graphics2D2.dispose();
        }
    }

    public boolean drawAboveCounters() {
        return true;
    }
}
