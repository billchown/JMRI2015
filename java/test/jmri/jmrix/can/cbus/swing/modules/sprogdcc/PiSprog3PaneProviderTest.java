package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.node.*;
import jmri.jmrix.can.cbus.swing.modules.*;
import jmri.jmrix.can.cbus.simulator.CbusDummyNode;
import jmri.jmrix.can.cbus.simulator.moduletypes.SprogPiSprog3;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test for Pi-SPROG 3 pane provider
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class PiSprog3PaneProviderTest {
    
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        PiSprog3PaneProvider t = new PiSprog3PaneProvider();
        Assertions.assertNotNull(t, "exists");
    }
    
    @Test
    public void testPaneFound() {
        CbusDummyNode node = new SprogPiSprog3().getNewDummyNode(memo, 65534);
        CbusConfigPaneProvider t = CbusConfigPaneProvider.getProviderByNode(node);

        Assertions.assertNotNull(t);
        Assertions.assertFalse(t instanceof UnknownPaneProvider,"Not Unknown");
        Assertions.assertTrue(t instanceof PiSprog3PaneProvider,"found PiSprog3PaneProvider");

        node.dispose();
    }

    private CanSystemConnectionMemo memo;
    private CbusNodeNVTableDataModel model;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        model = new CbusNodeNVTableDataModel(memo, 3,CbusNodeTableDataModel.MAX_COLUMN);
    }

    @AfterEach
    public void tearDown() {
        model.dispose();
        model = null;
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }
    
}
