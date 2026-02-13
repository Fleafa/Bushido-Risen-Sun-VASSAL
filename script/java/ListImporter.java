/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  VASSAL.build.AbstractConfigurable
 *  VASSAL.build.Buildable
 *  VASSAL.build.GameModule
 *  VASSAL.build.module.Map
 *  VASSAL.build.module.documentation.HelpFile
 *  VASSAL.build.widget.PieceSlot
 *  VASSAL.command.AddPiece
 *  VASSAL.command.Command
 *  VASSAL.command.NullCommand
 *  VASSAL.counters.GamePiece
 *  VASSAL.counters.PieceCloner
 */
package bushido;

import VASSAL.build.AbstractConfigurable;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.documentation.HelpFile;
import VASSAL.build.widget.PieceSlot;
import VASSAL.command.AddPiece;
import VASSAL.command.Command;
import VASSAL.command.NullCommand;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceCloner;
import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ListImporter
extends AbstractConfigurable {
    private static final int PIECE_SPACING = 100;
    private static final int P1_START_X = 200;
    private static final int P1_START_Y = 2350;
    private static final int P2_START_X = 1950;
    private static final int P2_START_Y = 2350;

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
            JButton jButton = new JButton("Import P1 List");
            jButton.setToolTipText("Import Player 1's BattleScribe list");
            jButton.addActionListener(actionEvent -> this.showImportDialog(1));
            gameModule.getToolBar().add(jButton);
            JButton jButton2 = new JButton("Import P2 List");
            jButton2.setToolTipText("Import Player 2's BattleScribe list");
            jButton2.addActionListener(actionEvent -> this.showImportDialog(2));
            gameModule.getToolBar().add(jButton2);
        }
    }

    private void showImportDialog(int n) {
        JTextArea jTextArea = new JTextArea(20, 50);
        jTextArea.setLineWrap(true);
        jTextArea.setWrapStyleWord(true);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        int n2 = JOptionPane.showConfirmDialog((Component)GameModule.getGameModule().getPlayerWindow(), jScrollPane, "Import Player " + n + " List", 2, -1);
        if (n2 == 0) {
            String string = jTextArea.getText();
            this.importList(string, n);
        }
    }

    private void importList(String string, int n) {
        List<String> list = this.parseListText(string);
        if (list.isEmpty()) {
            this.showError("No units found in list. Please check the format.");
            return;
        }
        ArrayList<PieceSlot> arrayList = new ArrayList<PieceSlot>();
        for (PieceSlot pieceSlot : GameModule.getGameModule().getAllDescendantComponentsOf(PieceSlot.class)) {
            String string2 = pieceSlot.getConfigureName();
            if (string2 == null || string2.endsWith("Card") || string2.endsWith(" Card")) continue;
            arrayList.add(pieceSlot);
        }
        int n2 = n == 1 ? 200 : 1950;
        int n3 = n == 1 ? 2350 : 2350;
        int n4 = n == 1 ? 1 : -1;
        int n5 = 0;
        int n6 = 0;
        int n7 = 0;
        ArrayList<String> arrayList2 = new ArrayList<String>();
        NullCommand nullCommand = new NullCommand();
        for (String string3 : list) {
            GamePiece gamePiece = this.findMatchingPiece(string3, arrayList);
            if (gamePiece != null) {
                int n8 = n2 + n5 * n4;
                int n9 = n3 + n6;
                Command command = this.placePieceOnMap(gamePiece, n8, n9);
                if (command != null) {
                    nullCommand = nullCommand.append(command);
                    ++n7;
                }
                if ((n5 += 100) <= 500) continue;
                n5 = 0;
                n6 -= 100;
                continue;
            }
            arrayList2.add(string3);
        }
        if (n7 > 0) {
            GameModule.getGameModule().sendAndLog((Command)nullCommand);
        }
        Object object = String.format("Import complete!\n\nCreated: %d pieces\nNot found: %d pieces", n7, arrayList2.size());
        if (!arrayList2.isEmpty() && arrayList2.size() <= 10) {
            object = (String)object + "\n\nNot found:\n" + String.join((CharSequence)"\n", arrayList2);
        } else if (!arrayList2.isEmpty()) {
            object = (String)object + "\n\nFirst 10 not found:\n" + String.join((CharSequence)"\n", arrayList2.subList(0, 10));
        }
        JOptionPane.showMessageDialog((Component)GameModule.getGameModule().getPlayerWindow(), object, "Import Summary", 1);
    }

    private List<String> parseListText(String string) {
        ArrayList<String> arrayList = new ArrayList<String>();
        Pattern pattern = Pattern.compile("^([A-Za-z][^\\[\\n]+?)\\s*\\[\\d+\\s*Rice\\]", 8);
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String string2 = matcher.group(1).trim();
            if (string2.startsWith("#") || string2.startsWith("++")) continue;
            arrayList.add(string2);
        }
        return arrayList;
    }

    private GamePiece findMatchingPiece(String string, List<PieceSlot> list) {
        String string2;
        String string3 = this.normalizeForMatching(string);
        for (PieceSlot object22 : list) {
            String string4 = object22.getConfigureName();
            if (string4 == null || !this.normalizeForMatching(string4).equals(string3)) continue;
            return object22.getPiece();
        }
        for (PieceSlot pieceSlot : list) {
            String string5 = pieceSlot.getConfigureName();
            if (string5 == null || !this.normalizeForMatching(string5).equalsIgnoreCase(string3)) continue;
            return pieceSlot.getPiece();
        }
        String string4 = string3.replaceAll("[\\s_-]", "").toLowerCase();
        for (PieceSlot pieceSlot : list) {
            String string6 = pieceSlot.getConfigureName();
            if (string6 == null || !(string2 = this.normalizeForMatching(string6).replaceAll("[\\s_-]", "").toLowerCase()).equals(string4)) continue;
            return pieceSlot.getPiece();
        }
        String string7 = string3.toLowerCase();
        for (PieceSlot pieceSlot : list) {
            String string8;
            string2 = pieceSlot.getConfigureName();
            if (string2 == null || !(string8 = this.normalizeForMatching(string2).toLowerCase()).contains(string7) && !string7.contains(string8)) continue;
            return pieceSlot.getPiece();
        }
        return null;
    }

    private String normalizeForMatching(String string) {
        return string.trim().replaceAll("\\s+", " ");
    }

    private Command placePieceOnMap(GamePiece gamePiece, int n, int n2) {
        Object object;
        Map map = null;
        Object object2 = GameModule.getGameModule().getComponentsOf(Map.class).iterator();
        while (object2.hasNext()) {
            object = (Map)object2.next();
            if (!"Main Table".equals(object.getMapName())) continue;
            map = object;
            break;
        }
        if (map == null && !(object2 = GameModule.getGameModule().getComponentsOf(Map.class)).isEmpty()) {
            map = (Map)object2.get(0);
        }
        if (map == null) {
            return null;
        }
        object2 = PieceCloner.getInstance().clonePiece(gamePiece);
        object = new Point(n, n2);
        object2.setPosition((Point)object);
        Command command = map.placeOrMerge((GamePiece)object2, (Point)object);
        AddPiece addPiece = new AddPiece((GamePiece)object2);
        AddPiece addPiece2 = command == null ? addPiece : addPiece.append(command);
        return addPiece2;
    }

    private void showError(String string) {
        JOptionPane.showMessageDialog((Component)GameModule.getGameModule().getPlayerWindow(), string, "Import Error", 0);
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
        return "List Importer";
    }
}
