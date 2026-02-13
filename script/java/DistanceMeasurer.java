/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  VASSAL.build.module.documentation.HelpFile
 *  VASSAL.command.ChangePiece
 *  VASSAL.command.Command
 *  VASSAL.counters.Decorator
 *  VASSAL.counters.EditablePiece
 *  VASSAL.counters.GamePiece
 *  VASSAL.counters.KeyCommand
 *  VASSAL.counters.PieceEditor
 *  VASSAL.counters.SimplePieceEditor
 *  VASSAL.tools.SequenceEncoder
 *  VASSAL.tools.SequenceEncoder$Decoder
 */
package bushido;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangePiece;
import VASSAL.command.Command;
import VASSAL.counters.Decorator;
import VASSAL.counters.EditablePiece;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.counters.SimplePieceEditor;
import VASSAL.tools.SequenceEncoder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.KeyStroke;

public class DistanceMeasurer
extends Decorator
implements EditablePiece {
    public static final String ID = "DistanceMeasurer;";
    private boolean measuring = false;
    private int measurerSlot = -1;
    private static final Color[] COLORS = new Color[]{new Color(255, 0, 0, 180), new Color(0, 100, 255, 180), new Color(0, 200, 0, 180)};
    private static final int HIGHLIGHT_RADIUS = 50;
    private static final int PIXELS_PER_INCH = 80;
    private KeyCommand[] commands;
    private static final String START_COMMAND = "Start Distance Measurement";
    private static final String STOP_COMMAND = "Stop Distance Measurement";

    public DistanceMeasurer() {
        this(ID, null);
    }

    public DistanceMeasurer(String string, GamePiece gamePiece) {
        this.setInner(gamePiece);
        this.mySetType(string);
    }

    public void mySetState(String string) {
        SequenceEncoder.Decoder decoder = new SequenceEncoder.Decoder(string, ';');
        this.measuring = decoder.nextBoolean(false);
        this.measurerSlot = decoder.nextInt(-1);
    }

    public String myGetState() {
        SequenceEncoder sequenceEncoder = new SequenceEncoder(';');
        sequenceEncoder.append(this.measuring);
        sequenceEncoder.append(this.measurerSlot);
        return sequenceEncoder.getValue();
    }

    public String myGetType() {
        return ID;
    }

    public void mySetType(String string) {
    }

    protected KeyCommand[] myGetKeyCommands() {
        if (this.commands == null) {
            this.commands = new KeyCommand[2];
            this.commands[0] = new KeyCommand(START_COMMAND, KeyStroke.getKeyStroke(77, 128), Decorator.getOutermost((GamePiece)this));
            this.commands[1] = new KeyCommand(STOP_COMMAND, KeyStroke.getKeyStroke(77, 192), Decorator.getOutermost((GamePiece)this));
        }
        if (this.measuring) {
            return new KeyCommand[]{this.commands[1]};
        }
        return new KeyCommand[]{this.commands[0]};
    }

    public Command myKeyEvent(KeyStroke keyStroke) {
        ChangePiece changePiece = null;
        if (this.commands != null && this.commands[0] != null && this.commands[0].matches(keyStroke)) {
            if (!this.measuring) {
                this.measurerSlot = this.findAvailableSlot();
                if (this.measurerSlot >= 0) {
                    this.measuring = true;
                    changePiece = new ChangePiece(Decorator.getOutermost((GamePiece)this).getId(), null, this.getState());
                }
            }
        } else if (this.commands != null && this.commands[1] != null && this.commands[1].matches(keyStroke) && this.measuring) {
            this.measuring = false;
            this.measurerSlot = -1;
            changePiece = new ChangePiece(Decorator.getOutermost((GamePiece)this).getId(), null, this.getState());
        }
        return changePiece;
    }

    private int findAvailableSlot() {
        GamePiece[] gamePieceArray;
        boolean[] blArray = new boolean[3];
        if (this.getMap() == null) {
            return 0;
        }
        for (GamePiece gamePiece : gamePieceArray = this.getMap().getAllPieces()) {
            DistanceMeasurer distanceMeasurer = (DistanceMeasurer)Decorator.getDecorator((GamePiece)gamePiece, DistanceMeasurer.class);
            if (distanceMeasurer == null || !distanceMeasurer.measuring || distanceMeasurer == this || distanceMeasurer.measurerSlot < 0 || distanceMeasurer.measurerSlot >= 3) continue;
            blArray[distanceMeasurer.measurerSlot] = true;
        }
        for (int i = 0; i < 3; ++i) {
            if (blArray[i]) continue;
            return i;
        }
        return -1;
    }

    public void draw(Graphics graphics, int n, int n2, Component component, double d) {
        this.piece.draw(graphics, n, n2, component, d);
        Graphics2D graphics2D = (Graphics2D)graphics;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (this.measuring && this.measurerSlot >= 0 && this.measurerSlot < 3) {
            Color color = COLORS[this.measurerSlot];
            graphics2D.setColor(color);
            graphics2D.setStroke(new BasicStroke(3.0f));
            int n3 = (int)(50.0 * d);
            graphics2D.drawOval(n - n3, n2 - n3, n3 * 2, n3 * 2);
        }
        this.drawDistanceLabels(graphics2D, n, n2, component, d);
    }

    private void drawDistanceLabels(Graphics2D graphics2D, int n, int n2, Component component, double d) {
        if (this.getMap() == null) {
            return;
        }
        GamePiece[] gamePieceArray = this.getMap().getAllPieces();
        int n3 = n2 + (int)(30.0 * d);
        for (GamePiece gamePiece : gamePieceArray) {
            DistanceMeasurer distanceMeasurer = (DistanceMeasurer)Decorator.getDecorator((GamePiece)gamePiece, DistanceMeasurer.class);
            if (distanceMeasurer == null || !distanceMeasurer.measuring || distanceMeasurer == this) continue;
            Point point = this.getPosition();
            Point point2 = gamePiece.getPosition();
            if (point == null || point2 == null) continue;
            double d2 = point.distance(point2);
            double d3 = d2 / 80.0;
            Color color = COLORS[distanceMeasurer.measurerSlot];
            graphics2D.setColor(color);
            graphics2D.setFont(new Font("Arial", 1, (int)(12.0 * d)));
            String string = String.format("%.1f\"", d3);
            graphics2D.drawString(string, n + (int)(5.0 * d), n3);
            n3 += (int)(15.0 * d);
        }
    }

    public Rectangle boundingBox() {
        Rectangle rectangle = this.piece.boundingBox();
        if (this.measuring) {
            rectangle = new Rectangle(rectangle);
            rectangle.grow(60, 60);
        }
        return rectangle;
    }

    public Shape getShape() {
        return this.piece.getShape();
    }

    public String getName() {
        return this.piece.getName();
    }

    public String getDescription() {
        return "Distance Measurer";
    }

    public HelpFile getHelpFile() {
        return null;
    }

    public PieceEditor getEditor() {
        return new SimplePieceEditor((GamePiece)this);
    }
}
