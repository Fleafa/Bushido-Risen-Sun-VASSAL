/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  VASSAL.build.AbstractConfigurable
 *  VASSAL.build.Buildable
 *  VASSAL.build.GameModule
 *  VASSAL.build.module.Map
 *  VASSAL.build.module.documentation.HelpFile
 *  VASSAL.command.AddPiece
 *  VASSAL.command.Command
 *  VASSAL.counters.BasicPiece
 *  VASSAL.counters.Delete
 *  VASSAL.counters.GamePiece
 */
package bushido;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.AddPiece;
import VASSAL.command.Command;
import VASSAL.counters.BasicPiece;
import VASSAL.counters.Delete;
import VASSAL.counters.GamePiece;
import java.awt.Component;
import java.awt.Point;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PlacementTool
extends AbstractConfigurable {
    private static final int PIXELS_PER_INCH = 80;
    private static final int MAP_OFFSET_X = 139;
    private static final int MAP_OFFSET_Y = 480;
    private static final int BATTLEFIELD_WIDTH = 1920;
    private static final int BATTLEFIELD_HEIGHT = 1920;

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

    public void addTo(Buildable buildable) {
        GameModule gameModule = GameModule.getGameModule();
        if (gameModule != null) {
            JButton jButton = new JButton("Place Piece");
            jButton.setToolTipText("Place a 30mm marker at specific coordinates");
            jButton.addActionListener(actionEvent -> this.showPlacementDialog());
            gameModule.getToolBar().add(jButton);
        }
    }

    private void showPlacementDialog() {
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, 1));
        JPanel jPanel2 = new JPanel();
        jPanel2.add(new JLabel("Horizontal:"));
        JTextField jTextField = new JTextField(5);
        jPanel2.add(new JLabel("Left "));
        jPanel2.add(jTextField);
        jPanel2.add(new JLabel("\" OR Right "));
        JTextField jTextField2 = new JTextField(5);
        jPanel2.add(jTextField2);
        jPanel2.add(new JLabel("\""));
        jPanel.add(jPanel2);
        JPanel jPanel3 = new JPanel();
        jPanel3.add(new JLabel("Vertical:  "));
        JTextField jTextField3 = new JTextField(5);
        jPanel3.add(new JLabel("Top "));
        jPanel3.add(jTextField3);
        jPanel3.add(new JLabel("\" OR Bottom "));
        JTextField jTextField4 = new JTextField(5);
        jPanel3.add(jTextField4);
        jPanel3.add(new JLabel("\""));
        jPanel.add(jPanel3);
        int n = JOptionPane.showConfirmDialog((Component)GameModule.getGameModule().getPlayerWindow(), jPanel, "Place Piece at Coordinates", 2, -1);
        if (n == 0) {
            this.placePiece(jTextField.getText(), jTextField2.getText(), jTextField3.getText(), jTextField4.getText());
        }
    }

    private void placePiece(String string, String string2, String string3, String string4) {
        try {
            Double d = this.parseDouble(string);
            Double d2 = this.parseDouble(string2);
            Double d3 = this.parseDouble(string3);
            Double d4 = this.parseDouble(string4);
            if (d == null && d2 == null || d != null && d2 != null) {
                this.showError("Please specify either Left OR Right distance (not both)");
                return;
            }
            if (d3 == null && d4 == null || d3 != null && d4 != null) {
                this.showError("Please specify either Top OR Bottom distance (not both)");
                return;
            }
            int n = d != null ? 139 + (int)(d * 80.0) : 2059 - (int)(d2 * 80.0);
            int n2 = d3 != null ? 480 + (int)(d3 * 80.0) : 2400 - (int)(d4 * 80.0);
            this.createPieceAt(n, n2);
        }
        catch (NumberFormatException numberFormatException) {
            this.showError("Invalid number format. Please enter numbers only.");
        }
    }

    private Double parseDouble(String string) {
        if (string == null || string.trim().isEmpty()) {
            return null;
        }
        return Double.parseDouble(string.trim());
    }

    private void showError(String string) {
        JOptionPane.showMessageDialog((Component)GameModule.getGameModule().getPlayerWindow(), string, "Invalid Input", 0);
    }

    private void createPieceAt(int n, int n2) {
        Object object2;
        Map map = null;
        for (Object object2 : GameModule.getGameModule().getComponentsOf(Map.class)) {
            if (!"Main Table".equals(object2.getMapName())) continue;
            map = object2;
            break;
        }
        if (map == null) {
            this.showError("Could not find Main Table map");
            return;
        }
        BasicPiece basicPiece = new BasicPiece("piece;;;Base-30mm.svg;Placement Marker");
        basicPiece = new Delete("delete;Delete;68,130;", (GamePiece)basicPiece);
        object2 = new Point(n, n2);
        GameModule.getGameModule().getGameState().addPiece((GamePiece)basicPiece);
        AddPiece addPiece = new AddPiece((GamePiece)basicPiece);
        addPiece = addPiece.append(map.placeOrMerge((GamePiece)basicPiece, (Point)object2));
        GameModule.getGameModule().sendAndLog((Command)addPiece);
        map.repaint();
    }

    public void removeFrom(Buildable buildable) {
    }

    public HelpFile getHelpFile() {
        return null;
    }

    public Class<?>[] getAllowableConfigureComponents() {
        return new Class[0];
    }

    public static String getConfigureTypeName() {
        return "Placement Tool";
    }
}
