package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.event.*;
import java.awt.geom.*;
import java.text.MessageFormat;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.jmrit.signalling.SignallingGuiTools;
import jmri.util.MathUtil;

/**
 * A LevelXing is two track segment on a layout that cross at an angle.
 * <p>
 * A LevelXing has four connection points, designated A, B, C, and D. At the
 * crossing, A-C and B-D are straight segments. A train proceeds through the
 * crossing on either of these segments.
 * <br>
 * <pre>
 *    A   D
 *    \\ //
 *      X
 *    // \\
 *    B   C
 * </pre>
 * <br>
 * Each straight segment carries Block information. A-C and B-D may be in the
 * same or different Layout Blocks.
 * <p>
 * For drawing purposes, each LevelXing carries a center point and displacements
 * for A and B. The displacements for C = - the displacement for A, and the
 * displacement for D = - the displacement for B. The center point and these
 * displacements may be adjusted by the user when in edit mode.
 * <p>
 * When LevelXings are first created, there are no connections. Block
 * information and connections are added when available.
 * <p>
 * Signal Head names are saved here to keep track of where signals are.
 * LevelXing only serves as a storage place for signal head names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LevelXing extends LayoutTrack {

    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlockAC = null;
    private NamedBeanHandle<LayoutBlock> namedLayoutBlockBD = null;

    protected NamedBeanHandle<SignalHead> signalAHeadNamed = null; // signal at A track junction
    protected NamedBeanHandle<SignalHead> signalBHeadNamed = null; // signal at B track junction
    protected NamedBeanHandle<SignalHead> signalCHeadNamed = null; // signal at C track junction
    protected NamedBeanHandle<SignalHead> signalDHeadNamed = null; // signal at D track junction

    protected NamedBeanHandle<SignalMast> signalAMastNamed = null; // signal at A track junction
    protected NamedBeanHandle<SignalMast> signalBMastNamed = null; // signal at B track junction
    protected NamedBeanHandle<SignalMast> signalCMastNamed = null; // signal at C track junction
    protected NamedBeanHandle<SignalMast> signalDMastNamed = null; // signal at D track junction

    private NamedBeanHandle<Sensor> sensorANamed = null; // sensor at A track junction
    private NamedBeanHandle<Sensor> sensorBNamed = null; // sensor at B track junction
    private NamedBeanHandle<Sensor> sensorCNamed = null; // sensor at C track junction
    private NamedBeanHandle<Sensor> sensorDNamed = null; // sensor at D track junction

    private LayoutTrack connectA = null;
    private LayoutTrack connectB = null;
    private LayoutTrack connectC = null;
    private LayoutTrack connectD = null;

    private Point2D dispA = new Point2D.Double(-20.0, 0.0);
    private Point2D dispB = new Point2D.Double(-14.0, 14.0);

    // public static final int POINTA = 0x01;
    // public static final int POINTB = 0x10;
    // public static final int POINTC = 0x20;
    // public static final int POINTD = 0x30;
    public enum Geometry {
        POINTA, POINTB, POINTC, POINTD
    }

    // temporary reference to the Editor that will eventually be part of View
    private final jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LevelXingEditor editor;

    /**
     * Constructor method.
     *
     * @param id           ID string.
     * @param c            the point location.
     * @param layoutEditor the main layout editor.
     */
    public LevelXing(String id, Point2D c, LayoutEditor layoutEditor) {
        super(id, c, layoutEditor);
        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LevelXingEditor(layoutEditor);
    }

    // this should only be used for debugging
    @Override
    public String toString() {
        return "LevelXing " + getName();
    }

    /*
    * Accessor methods
     */
    @Nonnull
    public String getBlockNameAC() {
        String result = null;
        if (namedLayoutBlockAC != null) {
            result = namedLayoutBlockAC.getName();
        }
        return ((result == null) ? "" : result);
    }

    @Nonnull
    public String getBlockNameBD() {
        String result = getBlockNameAC();
        if (namedLayoutBlockBD != null) {
            result = namedLayoutBlockBD.getName();
        }
        return result;
    }

    public SignalHead getSignalHead(Geometry loc) {
        NamedBeanHandle<SignalHead> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = signalAHeadNamed;
                break;
            case POINTB:
                namedBean = signalBHeadNamed;
                break;
            case POINTC:
                namedBean = signalCHeadNamed;
                break;
            case POINTD:
                namedBean = signalDHeadNamed;
                break;
            default:
                log.warn("{}.getSignalHead({})", getName(), loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    public SignalMast getSignalMast(Geometry loc) {
        NamedBeanHandle<SignalMast> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = signalAMastNamed;
                break;
            case POINTB:
                namedBean = signalBMastNamed;
                break;
            case POINTC:
                namedBean = signalCMastNamed;
                break;
            case POINTD:
                namedBean = signalDMastNamed;
                break;
            default:
                log.warn("{}.getSignalMast({})", getName(), loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    public Sensor getSensor(Geometry loc) {
        NamedBeanHandle<Sensor> namedBean = null;
        switch (loc) {
            case POINTA:
                namedBean = sensorANamed;
                break;
            case POINTB:
                namedBean = sensorBNamed;
                break;
            case POINTC:
                namedBean = sensorCNamed;
                break;
            case POINTD:
                namedBean = sensorDNamed;
                break;
            default:
                log.warn("{}.getSensor({})", getName(), loc);
                break;
        }
        if (namedBean != null) {
            return namedBean.getBean();
        }
        return null;
    }

    @Nonnull
    public String getSignalAName() {
        if (signalAHeadNamed != null) {
            return signalAHeadNamed.getName();
        }
        return "";
    }

    public void setSignalAName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalAHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalAHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalAHeadNamed = null;
        }
    }

    @Nonnull
    public String getSignalBName() {
        if (signalBHeadNamed != null) {
            return signalBHeadNamed.getName();
        }
        return "";
    }

    public void setSignalBName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalBHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalBHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalBHeadNamed = null;
        }
    }

    @Nonnull
    public String getSignalCName() {
        if (signalCHeadNamed != null) {
            return signalCHeadNamed.getName();
        }
        return "";
    }

    public void setSignalCName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalCHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalCHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalCHeadNamed = null;
        }
    }

    @Nonnull
    public String getSignalDName() {
        if (signalDHeadNamed != null) {
            return signalDHeadNamed.getName();
        }
        return "";
    }

    public void setSignalDName(String signalHead) {
        if (signalHead == null || signalHead.isEmpty()) {
            signalDHeadNamed = null;
            return;
        }

        SignalHead head = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead);
        if (head != null) {
            signalDHeadNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalHead, head);
        } else {
            signalDHeadNamed = null;
        }
    }

    public void removeBeanReference(jmri.NamedBean nb) {
        if (nb == null) {
            return;
        }
        if (nb instanceof SignalMast) {
            if (nb.equals(getSignalAMast())) {
                setSignalAMast(null);
                return;
            }
            if (nb.equals(getSignalBMast())) {
                setSignalBMast(null);
                return;
            }
            if (nb.equals(getSignalCMast())) {
                setSignalCMast(null);
                return;
            }
            if (nb.equals(getSignalDMast())) {
                setSignalDMast(null);
                return;
            }
        }
        if (nb instanceof Sensor) {
            if (nb.equals(getSensorA())) {
                setSensorAName(null);
                return;
            }
            if (nb.equals(getSensorB())) {
                setSensorBName(null);
                return;
            }
            if (nb.equals(getSensorC())) {
                setSensorCName(null);
                return;
            }
            if (nb.equals(getSensorD())) {
                setSensorDName(null);
                return;
            }
        }
        if (nb instanceof SignalHead) {
            if (nb.equals(getSignalHead(Geometry.POINTA))) {
                setSignalAName(null);
                return;
            }
            if (nb.equals(getSignalHead(Geometry.POINTB))) {
                setSignalBName(null);
                return;
            }
            if (nb.equals(getSignalHead(Geometry.POINTC))) {
                setSignalCName(null);
                return;
            }
            if (nb.equals(getSignalHead(Geometry.POINTD))) {
                setSignalDName(null);
            }
        }
    }

    public String getSignalAMastName() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalAMast() {
        if (signalAMastNamed != null) {
            return signalAMastNamed.getBean();
        }
        return null;
    }

    public void setSignalAMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalAMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalAMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalAMastNamed = null;
        }
    }

    public String getSignalBMastName() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalBMast() {
        if (signalBMastNamed != null) {
            return signalBMastNamed.getBean();
        }
        return null;
    }

    public void setSignalBMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalBMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalBMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalBMastNamed = null;
        }
    }

    public String getSignalCMastName() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalCMast() {
        if (signalCMastNamed != null) {
            return signalCMastNamed.getBean();
        }
        return null;
    }

    public void setSignalCMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalCMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalCMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalCMastNamed = null;
        }
    }

    public String getSignalDMastName() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getName();
        }
        return "";
    }

    public SignalMast getSignalDMast() {
        if (signalDMastNamed != null) {
            return signalDMastNamed.getBean();
        }
        return null;
    }

    public void setSignalDMast(String signalMast) {
        if (signalMast == null || signalMast.isEmpty()) {
            signalDMastNamed = null;
            return;
        }

        try {
            SignalMast mast = InstanceManager.getDefault(jmri.SignalMastManager.class).provideSignalMast(signalMast);
            signalDMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } catch (IllegalArgumentException ex) {
            signalDMastNamed = null;
        }
    }

    public String getSensorAName() {
        if (sensorANamed != null) {
            return sensorANamed.getName();
        }
        return "";
    }

    public Sensor getSensorA() {
        if (sensorANamed != null) {
            return sensorANamed.getBean();
        }
        return null;
    }

    public void setSensorAName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorANamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorANamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorANamed = null;
        }
    }

    public String getSensorBName() {
        if (sensorBNamed != null) {
            return sensorBNamed.getName();
        }
        return "";
    }

    public Sensor getSensorB() {
        if (sensorBNamed != null) {
            return sensorBNamed.getBean();
        }
        return null;
    }

    public void setSensorBName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorBNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorBNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorBNamed = null;
        }
    }

    public String getSensorCName() {
        if (sensorCNamed != null) {
            return sensorCNamed.getName();
        }
        return "";
    }

    public Sensor getSensorC() {
        if (sensorCNamed != null) {
            return sensorCNamed.getBean();
        }
        return null;
    }

    public void setSensorCName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorCNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorCNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorCNamed = null;
        }
    }

    public String getSensorDName() {
        if (sensorDNamed != null) {
            return sensorDNamed.getName();
        }
        return "";
    }

    public Sensor getSensorD() {
        if (sensorDNamed != null) {
            return sensorDNamed.getBean();
        }
        return null;
    }

    public void setSensorDName(String sensorName) {
        if (sensorName == null || sensorName.isEmpty()) {
            sensorDNamed = null;
            return;
        }

        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
            sensorDNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } catch (IllegalArgumentException ex) {
            sensorDNamed = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        switch (connectionType) {
            case LEVEL_XING_A:
                return connectA;
            case LEVEL_XING_B:
                return connectB;
            case LEVEL_XING_C:
                return connectC;
            case LEVEL_XING_D:
                return connectD;
            default:
                break;
        }
        String errstring = MessageFormat.format("{0}.getConnection({1}); invalid connection type", getName(), connectionType); //I18IN
        log.error("will throw {}", errstring);
        throw new jmri.JmriException(errstring);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, LayoutTrack o, HitPointType type) throws jmri.JmriException {
        if ((type != HitPointType.TRACK) && (type != HitPointType.NONE)) {
            String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); invalid type",
                    getName(), connectionType, (o == null) ? "null" : o.getName(), type);
            log.error("will throw {}", errString);
            throw new jmri.JmriException(errString);
        }
        switch (connectionType) {
            case LEVEL_XING_A:
                connectA = o;
                break;
            case LEVEL_XING_B:
                connectB = o;
                break;
            case LEVEL_XING_C:
                connectC = o;
                break;
            case LEVEL_XING_D:
                connectD = o;
                break;
            default:
                String errString = MessageFormat.format("{0}.setConnection({1}, {2}, {3}); invalid connection type",
                        getName(), connectionType, (o == null) ? "null" : o.getName(), type);
                log.error("will throw {}", errString);
                throw new jmri.JmriException(errString);
        }
    }

    public LayoutTrack getConnectA() {
        return connectA;
    }

    public LayoutTrack getConnectB() {
        return connectB;
    }

    public LayoutTrack getConnectC() {
        return connectC;
    }

    public LayoutTrack getConnectD() {
        return connectD;
    }

    public void setConnectA(LayoutTrack o, HitPointType type) {
        connectA = o;
        if ((connectA != null) && (type != HitPointType.TRACK)) {
            log.error("{}.setConnectA(({}, {}); invalid type",
                    getName(), o.getName(), type);
        }
    }

    public void setConnectB(LayoutTrack o, HitPointType type) {
        connectB = o;
        if ((connectB != null) && (type != HitPointType.TRACK)) {
            log.error("{}.setConnectB(({}, {}); invalid type",
                    getName(), o.getName(), type);
        }
    }

    public void setConnectC(LayoutTrack o, HitPointType type) {
        connectC = o;
        if ((connectC != null) && (type != HitPointType.TRACK)) {
            log.error("{}.setConnectC(({}, {}); invalid type",
                    getName(), o.getName(), type);
        }
    }

    public void setConnectD(LayoutTrack o, HitPointType type) {
        connectD = o;
        if ((connectD != null) && (type != HitPointType.TRACK)) {
            log.error("{}.setConnectD(({}, {}); invalid type",
                    getName(), o.getName(), type);
        }
    }

    public LayoutBlock getLayoutBlockAC() {
        return (namedLayoutBlockAC != null) ? namedLayoutBlockAC.getBean() : null;
    }

    public LayoutBlock getLayoutBlockBD() {
        return (namedLayoutBlockBD != null) ? namedLayoutBlockBD.getBean() : getLayoutBlockAC();
    }

    public Point2D getCoordsA() {
        return MathUtil.add(getCoordsCenter(), dispA);
    }

    public Point2D getCoordsB() {
        return MathUtil.add(getCoordsCenter(), dispB);
    }

    public Point2D getCoordsC() {
        return MathUtil.subtract(getCoordsCenter(), dispA);
    }

    public Point2D getCoordsD() {
        return MathUtil.subtract(getCoordsCenter(), dispB);
    }

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
        switch (connectionType) {
            case LEVEL_XING_CENTER:
                break;
            case LEVEL_XING_A:
                result = getCoordsA();
                break;
            case LEVEL_XING_B:
                result = getCoordsB();
                break;
            case LEVEL_XING_C:
                result = getCoordsC();
                break;
            case LEVEL_XING_D:
                result = getCoordsD();
                break;
            default:
                log.error("{}.getCoordsForConnectionType({}); Invalid connection type ",
                        getName(), connectionType); //I18IN
        }
        return result;
    }

    /**
     * @return the bounds of this crossing
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pointA = getCoordsA();
        result = new Rectangle2D.Double(pointA.getX(), pointA.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        result.add(getCoordsD());
        return result;
    }

    /**
     * Add Layout Blocks.
     *
     * @param newLayoutBlock the layout block to add.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockAC(LayoutBlock newLayoutBlock) {
        LayoutBlock blockAC = getLayoutBlockAC();
        LayoutBlock blockBD = getLayoutBlockBD();
        if (blockAC != newLayoutBlock) {
            // block 1 has changed, if old block exists, decrement use
            if ((blockAC != null) && (blockAC != blockBD)) {
                blockAC.decrementUse();
            }
            blockAC = newLayoutBlock;
            if (newLayoutBlock != null) {
                namedLayoutBlockAC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
            } else {
                namedLayoutBlockAC = null;
            }

            // decrement use if block was previously counted
            if ((blockAC != null) && (blockAC == blockBD)) {
                blockAC.decrementUse();
            }
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockBD(LayoutBlock newLayoutBlock) {
        LayoutBlock blockAC = getLayoutBlockAC();
        LayoutBlock blockBD = getLayoutBlockBD();
        if (blockBD != newLayoutBlock) {
            // block 1 has changed, if old block exists, decrement use
            if ((blockBD != null) && (blockBD != blockAC)) {
                blockBD.decrementUse();
            }
            blockBD = newLayoutBlock;
            if (newLayoutBlock != null) {
                namedLayoutBlockBD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
            } else {
                namedLayoutBlockBD = null;
            }
            // decrement use if block was previously counted
            if ((blockBD != null) && (blockBD == blockAC)) {
                blockBD.decrementUse();
            }
        }

    }

    public void updateBlockInfo() {
        LayoutBlock blockAC = getLayoutBlockAC();
        LayoutBlock blockBD = getLayoutBlockBD();
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (blockAC != null) {
            blockAC.updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != blockAC)) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != blockAC) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        if (blockBD != null) {
            blockBD.updatePaths();
        }
        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != blockBD)) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != blockBD) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    void removeSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() && InstanceManager.getDefault(jmri.SignalMastLogicManager.class).isSignalMastUsed(signalMast)) {
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    /**
     * Test if mainline track or not.
     *
     * @return true if either connecting track segment is mainline; Defaults to
     *         not mainline if connecting track segments are missing
     */
    public boolean isMainlineAC() {
        if (((connectA != null) && (((TrackSegment) connectA).isMainline()))
                || ((connectC != null) && (((TrackSegment) connectC).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isMainlineBD() {
        if (((connectB != null) && (((TrackSegment) connectB).isMainline()))
                || ((connectD != null) && (((TrackSegment) connectD).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isMainline() {
        return (isMainlineAC() || isMainlineBD());
    }

    /*
    * Modify coordinates methods.
     */
    public void setCoordsA(Point2D p) {
        dispA = MathUtil.subtract(p, getCoordsCenter());
    }

    public void setCoordsB(Point2D p) {
        dispB = MathUtil.subtract(p, getCoordsCenter());
    }

    public void setCoordsC(Point2D p) {
        dispA = MathUtil.subtract(getCoordsCenter(), p);
    }

    public void setCoordsD(Point2D p) {
        dispB = MathUtil.subtract(getCoordsCenter(), p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void scaleCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.granulize(MathUtil.multiply(getCoordsCenter(), factor), 1.0));
        dispA = MathUtil.granulize(MathUtil.multiply(dispA, factor), 1.0);
        dispB = MathUtil.granulize(MathUtil.multiply(dispB, factor), 1.0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void translateCoords(double xFactor, double yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        super.setCoordsCenter(MathUtil.add(getCoordsCenter(), factor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rotateCoords(double angleDEG) {
        // rotate coordinates
        double rotRAD = Math.toRadians(angleDEG);
        double sineRot = Math.sin(rotRAD);
        double cosineRot = Math.cos(rotRAD);

        // rotate displacements around origin {0, 0}
        Point2D center_temp = getCoordsCenter();
        super.setCoordsCenter(MathUtil.zeroPoint2D);
        dispA = rotatePoint(dispA, sineRot, cosineRot);
        dispB = rotatePoint(dispB, sineRot, cosineRot);
        super.setCoordsCenter(center_temp);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HitPointType findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection
        //note: optimization here: instead of creating rectangles for all the
        // points to check below, we create a rectangle for the test point
        // and test if the points below are in that rectangle instead.
        Rectangle2D r = trackControlCircleRectAt(hitPoint);
        Point2D p, minPoint = MathUtil.zeroPoint2D;

        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double distance, minDistance = POSITIVE_INFINITY;

        //check the center point
        if (!requireUnconnected) {
            p = getCoordsCenter();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_CENTER;
            }
        }

        //check the A connection point
        if (!requireUnconnected || (getConnectA() == null)) {
            p = getCoordsA();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_A;
            }
        }

        //check the B connection point
        if (!requireUnconnected || (getConnectB() == null)) {
            p = getCoordsB();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_B;
            }
        }

        //check the C connection point
        if (!requireUnconnected || (getConnectC() == null)) {
            p = getCoordsC();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_C;
            }
        }

        //check the D connection point
        if (!requireUnconnected || (getConnectD() == null)) {
            p = getCoordsD();
            distance = MathUtil.distance(p, hitPoint);
            if (distance < minDistance) {
                minDistance = distance;
                minPoint = p;
                result = HitPointType.LEVEL_XING_D;
            }
        }
        if ((useRectangles && !r.contains(minPoint))
                || (!useRectangles && (minDistance > circleRadius))) {
            result = HitPointType.NONE;
        }
        return result;
    }   // findHitPointType

    // initialization instance variables (used when loading a LayoutEditor)
    public String connectAName = "";
    public String connectBName = "";
    public String connectCName = "";
    public String connectDName = "";

    public String tLayoutBlockNameAC = "";
    public String tLayoutBlockNameBD = "";

    /**
     * Initialization method The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @Override
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);

        LayoutBlock lb;
        if (!tLayoutBlockNameAC.isEmpty()) {
            lb = p.provideLayoutBlock(tLayoutBlockNameAC);
            String userName = lb.getUserName();
            if (userName != null) {
                namedLayoutBlockAC = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                if (namedLayoutBlockBD != namedLayoutBlockAC) {
                    lb.incrementUse();
                }
            } else {
                log.error("LevelXing.setObjects(); bad blockname AC ''{}''", tLayoutBlockNameAC);
                namedLayoutBlockAC = null;
            }
            tLayoutBlockNameAC = null; //release this memory
        }

        if (!tLayoutBlockNameBD.isEmpty()) {
            lb = p.provideLayoutBlock(tLayoutBlockNameBD);
            String userName = lb.getUserName();
            if (userName != null) {
                namedLayoutBlockBD = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(userName, lb);
                if (namedLayoutBlockBD != namedLayoutBlockAC) {
                    lb.incrementUse();
                }
            } else {
                log.error("{}.setObjects(); bad blockname BD ''{}''", this, tLayoutBlockNameBD);
                namedLayoutBlockBD = null;
            }
            tLayoutBlockNameBD = null; //release this memory
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        ArrayList<String> beanReferences = getBeanReferences("All");  // NOI18N
        if (!beanReferences.isEmpty()) {
            displayRemoveWarningDialog(beanReferences, "LevelCrossing");  // NOI18N
        }
        return beanReferences.isEmpty();
    }

    /**
     * Build a list of sensors, signal heads, and signal masts attached to a
     * level crossing point.
     *
     * @param pointName Specify the point (A-D) or all (All) points.
     * @return a list of bean reference names.
     */
    public ArrayList<String> getBeanReferences(String pointName) {
        ArrayList<String> references = new ArrayList<>();
        if (pointName.equals("A") || pointName.equals("All")) {  // NOI18N
            if (!getSignalAMastName().isEmpty()) {
                references.add(getSignalAMastName());
            }
            if (!getSensorAName().isEmpty()) {
                references.add(getSensorAName());
            }
            if (!getSignalAName().isEmpty()) {
                references.add(getSignalAName());
            }
        }
        if (pointName.equals("B") || pointName.equals("All")) {  // NOI18N
            if (!getSignalBMastName().isEmpty()) {
                references.add(getSignalBMastName());
            }
            if (!getSensorBName().isEmpty()) {
                references.add(getSensorBName());
            }
            if (!getSignalBName().isEmpty()) {
                references.add(getSignalBName());
            }
        }
        if (pointName.equals("C") || pointName.equals("All")) {  // NOI18N
            if (!getSignalCMastName().isEmpty()) {
                references.add(getSignalCMastName());
            }
            if (!getSensorCName().isEmpty()) {
                references.add(getSensorCName());
            }
            if (!getSignalCName().isEmpty()) {
                references.add(getSignalCName());
            }
        }
        if (pointName.equals("D") || pointName.equals("All")) {  // NOI18N
            if (!getSignalDMastName().isEmpty()) {
                references.add(getSignalDMastName());
            }
            if (!getSensorDName().isEmpty()) {
                references.add(getSensorDName());
            }
            if (!getSignalDName().isEmpty()) {
                references.add(getSignalDName());
            }
        }
        return references;
    }

    JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@CheckForNull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {
            JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LevelCrossing")) + getName());
            jmi.setEnabled(false);

            boolean blockACAssigned = false;
            boolean blockBDAssigned = false;
            if (getLayoutBlockAC() == null) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", "AC"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "AC")) + getLayoutBlockAC().getDisplayName());
                blockACAssigned = true;
            }
            jmi.setEnabled(false);

            if (getLayoutBlockBD() == null) {
                jmi = popup.add(Bundle.getMessage("NoBlockX", "BD"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "BD")) + getLayoutBlockBD().getDisplayName());
                blockBDAssigned = true;
            }
            jmi.setEnabled(false);

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + connectA.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectA.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + connectB.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectB.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + connectC.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectC.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + connectD.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectD.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                popup.add(connectionsMenu);
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(isHidden());
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> setHidden(hiddenCheckBoxMenuItem.isSelected()));

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.editLayoutTrack(LevelXing.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLevelXing(LevelXing.this)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if (blockACAssigned && blockBDAssigned) {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // bring up signals at level crossing tool dialog
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().
                                setSignalsAtLevelXingFromMenu(LevelXing.this,
                                        letbp.signalIconEditor,
                                        letbp.signalFrame);
                    }
                };
                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (layoutEditor.getLETools().
                        addLevelXingSignalHeadInfoToMenu(LevelXing.this, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }
            }

            final String[] boundaryBetween = getBlockBoundaries();
            boolean blockBoundaries = false;
            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockACAssigned && !blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (!blockACAssigned && blockBDAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                } else if (blockACAssigned && blockBDAssigned) {
                    JMenu viewRouting = new JMenu(Bundle.getMessage("ViewBlockRouting"));
                    viewRouting.add(new AbstractAction(getLayoutBlockAC().getDisplayName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(getLayoutBlockAC().getDisplayName(), getLayoutBlockAC());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    viewRouting.add(new AbstractAction(getLayoutBlockBD().getDisplayName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction(getLayoutBlockBD().getDisplayName(), getLayoutBlockBD());
                            routeTableAction.actionPerformed(e);
                        }
                    });

                    popup.add(viewRouting);
                }
            }

            for (int i = 0; i < 4; i++) {
                if (boundaryBetween[i] != null) {
                    blockBoundaries = true;
                }
            }
            if (blockBoundaries) {
                popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().
                                setSignalMastsAtLevelXingFromMenu(
                                        LevelXing.this, boundaryBetween,
                                        letbp.signalFrame);
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorToolBarPanel letbp = getLayoutEditorToolBarPanel();
                        layoutEditor.getLETools().setSensorsAtLevelXingFromMenu(
                                LevelXing.this, boundaryBetween,
                                letbp.sensorIconEditor,
                                letbp.sensorFrame);
                    }
                });
            }

            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    }   // showPopup

    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        String blockNameAC = getBlockNameAC();
        String blockNameBD = getBlockNameBD();

        LayoutBlock blockAC = getLayoutBlockAC();
        LayoutBlock blockBD = getLayoutBlockAC();

        if (!blockNameAC.isEmpty() && (blockAC != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != blockAC)) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != blockAC)) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + blockAC.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
        }
        if (!blockNameBD.isEmpty() && (blockBD != null)) {
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != blockBD)) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != blockBD)) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + blockBD.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove().
     */
    public void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Remove this object from display and persistance.
     */
    public void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * Get if active. "active" means that the object is still displayed, and
     * should be stored.
     *
     * @return true if still displayed, else false.
     */
    public boolean isActive() {
        return active;
    }

    ArrayList<SignalMast> sml = new ArrayList<>();

    public void addSignalMastLogic(SignalMast sm) {
        if (sml.contains(sm)) {
            return;
        }
        if (sml.isEmpty()) {
            sml.add(sm);
            return;
        }
        SignalMastLogic sl = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
        for (SignalMast signalMast : sml) {
            SignalMastLogic s = InstanceManager.getDefault(SignalMastLogicManager.class).getSignalMastLogic(signalMast);
            if (s != null) {
                s.setConflictingLogic(sm, this);
            }
            sl.setConflictingLogic(signalMast, this);
        }
        sml.add(sm);
    }

    public void removeSignalMastLogic(SignalMast sm) {
        if (!sml.contains(sm)) {
            return;
        }
        sml.remove(sm);
        if (sml.isEmpty()) {
            return;
        }
        for (int i = 0; i < sml.size(); i++) {
            SignalMastLogic s = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).getSignalMastLogic(sm);
            if (s != null) {
                s.removeConflictingLogic(sm, this);
            }
        }
    }

    ArrayList<JMenuItem> editAdditionalMenu = new ArrayList<>(0);
    ArrayList<JMenuItem> viewAdditionalMenu = new ArrayList<>(0);

    public void addEditPopUpMenu(JMenuItem menu) {
        if (!editAdditionalMenu.contains(menu)) {
            editAdditionalMenu.add(menu);
        }
    }

    public void addViewPopUpMenu(JMenuItem menu) {
        if (!viewAdditionalMenu.contains(menu)) {
            viewAdditionalMenu.add(menu);
        }
    }

    public void setAdditionalEditPopUpMenu(JPopupMenu popup) {
        if (editAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : editAdditionalMenu) {
            popup.add(mi);
        }
    }

    public void setAdditionalViewPopUpMenu(JPopupMenu popup) {
        if (viewAdditionalMenu.isEmpty()) {
            return;
        }
        popup.addSeparator();
        for (JMenuItem mi : viewAdditionalMenu) {
            popup.add(mi);
        }
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /*
    * {@inheritDoc}
     */
    @Override
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        // nothing to see here... move along...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        List<HitPointType> result = new ArrayList<>();

        //check the A connection point
        if (getConnectA() == null) {
            result.add(HitPointType.LEVEL_XING_A);
        }

        //check the B connection point
        if (getConnectB() == null) {
            result.add(HitPointType.LEVEL_XING_B);
        }

        //check the C connection point
        if (getConnectC() == null) {
            result.add(HitPointType.LEVEL_XING_C);
        }

        //check the D connection point
        if (getConnectD() == null) {
            result.add(HitPointType.LEVEL_XING_D);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        return ((getLayoutBlockAC() != null) && (getLayoutBlockBD() != null));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
        * For each (non-null) blocks of this track do:
        * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
        * #2) If this track is already in the TrackNameSet for this block
        *     then return (done!)
        * #3) else add a new set (with this block/track) to
        *     blockNamesToTrackNameSetMap and check all the connections in this
        *     block (by calling the 2nd method below)
        * <p>
        *     Basically, we're maintaining contiguous track sets for each block found
        *     (in blockNamesToTrackNameSetMap)
         */

        // We're only using a map here because it's convient to
        // use it to pair up blocks and connections
        Map<LayoutTrack, String> blocksAndTracksMap = new HashMap<>();
        if ((getLayoutBlockAC() != null) && (connectA != null)) {
            blocksAndTracksMap.put(connectA, getLayoutBlockAC().getDisplayName());
        }
        if ((getLayoutBlockAC() != null) && (connectC != null)) {
            blocksAndTracksMap.put(connectC, getLayoutBlockAC().getDisplayName());
        }
        if ((getLayoutBlockBD() != null) && (connectB != null)) {
            blocksAndTracksMap.put(connectB, getLayoutBlockBD().getDisplayName());
        }
        if ((getLayoutBlockBD() != null) && (connectD != null)) {
            blocksAndTracksMap.put(connectD, getLayoutBlockBD().getDisplayName());
        }

        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;
        for (Map.Entry<LayoutTrack, String> entry : blocksAndTracksMap.entrySet()) {
            LayoutTrack theConnect = entry.getKey();
            String theBlockName = entry.getValue();

            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(theBlockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.debug("*New block ('{}') trackNameSets", theBlockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(theBlockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track ''{}'' to trackNameSet for block ''{}''", getName(), theBlockName);
            }
            theConnect.collectContiguousTracksNamesInBlockNamed(theBlockName, TrackNameSet);
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // check all the matching blocks in this track and...
            //  #1) add us to TrackNameSet and...
            //  #2) flood them
            //check the AC blockName
            if (getBlockNameAC().equals(blockName)) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track ''{}'for block ''{}''", getName(), blockName);
                }
                // it's time to play... flood your neighbours!
                if (connectA != null) {
                    connectA.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connectC != null) {
                    connectC.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
            //check the BD blockName
            if (getBlockNameBD().equals(blockName)) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track ''{}''for block ''{}''", getName(), blockName);
                }
                // it's time to play... flood your neighbours!
                if (connectB != null) {
                    connectB.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connectD != null) {
                    connectD.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlockAC(layoutBlock);
        setLayoutBlockBD(layoutBlock);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXing.class);
}
