package jmri.jmrit.display;

import java.awt.GraphicsEnvironment;
import javax.swing.JComponent;

import jmri.InstanceManager;
import jmri.Light;
import jmri.Memory;
import jmri.Reporter;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.rules.Timeout;
import jmri.util.junit.rules.RetryRule;
import org.junit.runner.Description;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for the SensorIcon
 *
 * @author Bob Jacobsen Copyright 2009, 2010
 */
public class IconEditorWindowTest {

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description description) {
            if (Boolean.valueOf(System.getenv("TRAVIS_PULL_REQUEST"))) {
                // use System.out.println instead of logging to avoid using
                // warning or error while still providing this output on PRs
                // in Travis CI (and blocking elsewhere)
                System.out.println("Starting test: " + description.getMethodName());
            }
        }
    };

    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(3);  // allow 3 retries

    Editor _editor = null;
    JComponent _panel;

    @Test
    public void testSensorEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        _editor.addSensorEditor();

        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Sensor");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IS1");
        iconEditor.addToTable();

        SensorIcon icon = _editor.putSensor();
        Assert.assertNotNull(icon);
        Sensor sensor = icon.getSensor();
        Assert.assertNotNull(sensor);

        int x = 50;
        int y = 20;

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        Assert.assertEquals("initial state", Sensor.UNKNOWN, sensor.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        // this will wait for WAITFOR_MAX_DELAY (15 seconds) max
        // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
        // if it's still false after max wait it throws an assert.
        JUnitUtil.waitFor(() -> {
            return sensor.getState() == Sensor.INACTIVE;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return sensor.getState() == Sensor.ACTIVE;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testRightTOEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("RightTurnout");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IT2");
        iconEditor.addToTable();

        TurnoutIcon icon = _editor.addTurnout(iconEditor);
        Assert.assertNotNull(icon);
        Turnout turnout = icon.getTurnout();
        Assert.assertNotNull(turnout);

        int x = 30;
        int y = 10;

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());
        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.THROWN;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testLeftTOEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("LeftTurnout");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IT1");
        iconEditor.addToTable();

        TurnoutIcon icon = _editor.addTurnout(iconEditor);
        Assert.assertNotNull(icon);
        Turnout turnout = icon.getTurnout();
        Assert.assertNotNull(turnout);

        int x = 30;
        int y = 10;

        jmri.util.ThreadingUtil.runOnGUI(() -> {
            icon.setLocation(x, y);
            _panel.repaint();
        });

        new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Sensor.UNKNOWN, turnout.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.CLOSED;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return turnout.getState() == Turnout.THROWN;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testLightEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Light");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IL2");
        iconEditor.addToTable();

        LightIcon icon = _editor.addLight();
        Assert.assertNotNull(icon);
        Light light = icon.getLight();
        Assert.assertNotNull(light);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        Assert.assertEquals("initial state", Light.OFF, light.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        JUnitUtil.waitFor(() -> {
            return light.getState() == Light.ON;
        }, "state after one click");

        // Click icon change state to inactive
        jfo.clickMouse(xloc, yloc, 1);
        JUnitUtil.waitFor(() -> {
            return light.getState() == Light.OFF;
        }, "state after two clicks");

        iefo.requestClose();
    }

    @Test
    public void testSignalHeadEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("SignalHead");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        SignalHead signalHead = new jmri.implementation.VirtualSignalHead("IH2");
        InstanceManager.getDefault(jmri.SignalHeadManager.class).register(signalHead);

        iconEditor.setSelection(signalHead);

        SignalHeadIcon icon = _editor.putSignalHead();
        Assert.assertNotNull(icon);
        SignalHead sh = icon.getSignalHead();
        Assert.assertEquals("SignalHead==sh", signalHead, sh);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        new java.awt.Point(x + icon.getSize().width / 2,
                y + icon.getSize().height / 2);

        int[] states = signalHead.getValidStates();
        Assert.assertEquals("initial state", states[0], signalHead.getState());

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        for (int i = 1; i < states.length; i++) {
            //Assert.assertEquals("state after " + i + " click", states[i], signalHead.getState());
            final int state = states[i];
            // this will wait for WAITFOR_MAX_DELAY (15 seconds) max
            // checking the condition every WAITFOR_DELAY_STEP (5 mSecs)
            // if it's still false after max wait it throws an assert.
            JUnitUtil.waitFor(() -> {
                return signalHead.getState() == state;
            }, "state after " + i + " click(s)");

            jfo.clickMouse(xloc, yloc, 1);
        }

        iefo.requestClose();
    }

    @Test
    public void testMemoryEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Memory");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IM2");
        iconEditor.addToTable();

        MemoryIcon memIcon = _editor.putMemory();
        Assert.assertNotNull(memIcon);
        Memory memory = memIcon.getMemory();
        Assert.assertNotNull(memory);

        int x = 20;
        int y = 10;
        memIcon.setLocation(x, y);
        _panel.repaint();

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = memIcon.getLocation().x + memIcon.getSize().width / 2;
        int yloc = memIcon.getLocation().y + memIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iconEditor._sysNameText.setText("IM1");
        iconEditor.addToTable();

        MemorySpinnerIcon memSpinIcon = _editor.addMemorySpinner();
        Assert.assertNotNull(memSpinIcon);
        memory = memSpinIcon.getMemory();
        Assert.assertNotNull(memory);

        x = 70;
        y = 10;
        memSpinIcon.setLocation(x, y);
        _panel.repaint();

        xloc = memIcon.getLocation().x + memSpinIcon.getSize().width / 2;
        yloc = memIcon.getLocation().y + memSpinIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iconEditor._sysNameText.setText("IM2");
        iconEditor.addToTable();

        MemoryInputIcon memInputIcon = _editor.addMemoryInputBox();
        Assert.assertNotNull(memInputIcon);
        memory = memInputIcon.getMemory();
        Assert.assertNotNull(memory);

        x = 150;
        y = 10;
        memInputIcon.setLocation(x, y);
        _panel.repaint();

        xloc = memIcon.getLocation().x + memInputIcon.getSize().width / 2;
        yloc = memIcon.getLocation().y + memInputIcon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iefo.requestClose();
    }

    @Test
    public void testReporterEditor() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Editor.JFrameItem iconEditorFrame = _editor.getIconFrame("Reporter");
        IconAdder iconEditor = iconEditorFrame.getEditor();
        Assert.assertNotNull(iconEditor);

        iconEditor._sysNameText.setText("IR2");
        iconEditor.addToTable();

        ReporterIcon icon = _editor.addReporter();
        Assert.assertNotNull(icon);
        Reporter reporter = icon.getReporter();
        Assert.assertNotNull(reporter);

        int x = 30;
        int y = 10;
        icon.setLocation(x, y);
        _panel.repaint();

        JFrameOperator iefo = new JFrameOperator(iconEditorFrame);
        JComponentOperator jfo = new JComponentOperator(_panel);
        int xloc = icon.getLocation().x + icon.getSize().width / 2;
        int yloc = icon.getLocation().y + icon.getSize().height / 2;
        jfo.clickMouse(xloc, yloc, 1);

        iefo.requestClose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initInternalSignalHeadManager();

        if (!GraphicsEnvironment.isHeadless()) {
            _editor = new PanelEditor("IconEditorTestPanel");
            Assert.assertNotNull(JFrameOperator.waitJFrame("IconEditorTestPanel", true, true));
            _panel = _editor.getTargetPanel();
            Assert.assertNotNull(_panel);
        }
    }

    @After
    public void tearDown() throws Exception {

        // Delete the editor by calling dispose() defined in PanelEditor
        // directly instead of closing the window through a WindowClosing()
        // event - this is the method called to delete a panel if a user
        // selects that in the Hide/Delete dialog triggered by WindowClosing().
        if (_editor != null) {
            //_editor.dispose();  // this sometimes Disposal was interrupted:
            //java.lang.InterruptedException
            //  at java.lang.Object.wait(Native Method)
            //  at java.lang.Object.wait(Object.java:502)
            //  at java.awt.EventQueue.invokeAndWait(EventQueue.java:1343)
            //  at java.awt.Window.doDispose(Window.java:1210)
            //  at java.awt.Window.dispose(Window.java:1147)
            //  at jmri.util.JmriJFrame.dispose(JmriJFrame.java:983)
            //  at jmri.jmrit.display.Editor.dispose(Editor.java:2666)
            //  at jmri.jmrit.display.IconEditorWindowTest.tearDown(IconEditorWindowTest.java:409)
            //  at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            //  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
            //  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            //  at java.lang.reflect.Method.invoke(Method.java:498)
            //  at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
            //  at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
            //  at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
            //  at org.junit.internal.runners.statements.RunAfters.evaluate(RunAfters.java:33)
            //  at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:298)
            //  at org.junit.internal.runners.statements.FailOnTimeout$CallableStatement.call(FailOnTimeout.java:292)
            //  at java.util.concurrent.FutureTask.run(FutureTask.java:266)
            //  at java.lang.Thread.run(Thread.java:748)causes the test to fail with the exception below:

            // using the EditorFrameOperator to close causes these tests to timeout because the window can't be found.

            JUnitUtil.dispose(_editor); // this seems to be more reliable, though it doesn't answer the question about saving.
        }
        _editor = null;

        JUnitUtil.resetWindows(false, false); // don't log existing windows here, should just be from this class
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
