package jmri.jmrit.operations.locations;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Locations GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class LocationEditFrameTest extends OperationsTestCase {

    final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

    @Test
    public void testAddTracks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a spur track
        JemmyUtil.enterClickAndLeave(f.addSpurButton);

        // add an interchange track
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        JemmyUtil.enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        JemmyUtil.enterClickAndLeave(f.stagingRadioButton);
        JemmyUtil.enterClickAndLeave(f.addStagingButton);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.yardRadioButton);
        JemmyUtil.enterClickAndLeave(f.addYardButton);
        
        // confirm that all four add track windows exist
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) != null;
        }, "lef not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNotNull(tef);

        JUnitUtil.dispose(f);
        // confirm add windows disposed
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testDeleteButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        // add a spur track
        JemmyUtil.enterClickAndLeave(f.addSpurButton);

        // add an interchange track
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        JemmyUtil.enterClickAndLeave(f.addInterchangeButton);

        // add a staging track
        JemmyUtil.enterClickAndLeave(f.stagingRadioButton);
        JemmyUtil.enterClickAndLeave(f.addStagingButton);

        // add a yard track
        JemmyUtil.enterClickAndLeave(f.yardRadioButton);
        JemmyUtil.enterClickAndLeave(f.addYardButton);
        
        // confirm that all four add track windows exist
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) != null;
        }, "lef not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNotNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNotNull(tef);

        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("AddSpur")) == null;
        }, "lef null");
        
        // confirm add windows disposed
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddSpur"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddInterchange"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddStaging"));
        Assert.assertNull(tef);
        tef = JmriJFrame.getFrame(Bundle.getMessage("AddYard"));
        Assert.assertNull(tef);
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddDeleteSaveButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.loadFiveLocations();

        LocationEditFrame f = new LocationEditFrame(null);
        f.setTitle("Test Add Location Frame");

        f.locationNameTextField.setText("New Test Location");
        JemmyUtil.enterClickAndLeave(f.addLocationButton);

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        Location newLoc = lManager.getLocationByName("New Test Location");

        Assert.assertNotNull(newLoc);

        f.locationNameTextField.setText("Newer Test Location");
        JemmyUtil.enterClickAndLeave(f.saveLocationButton);

        Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());

        // test delete button
        JemmyUtil.enterClickAndLeaveThreadSafe(f.deleteLocationButton);
        Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
        // confirm delete dialog window should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("deletelocation?"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // location now deleted
        Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEditStaging() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createFourStagingLocations();

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 4 locations", 4, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("North End Staging");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));
        
        // confirm edit staging track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditStaging")) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditStaging"));
        Assert.assertNotNull(tef);
        
        Track t = loc.getTracksList().get(0);
        Assert.assertNotNull(t);
        t.setLength(350); // change track length to create property change

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditStaging"));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testEditSpur() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));
        
        // confirm edit spur track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditSpur")) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditSpur"));
        Assert.assertNotNull(tef);
        
        Track t = loc.getTrackByName("Test Location Spur 1", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditSpur"));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testEditInterchange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        
        JemmyUtil.enterClickAndLeave(f.interchangeRadioButton);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));
        
        // confirm edit interchange track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditInterchange")) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditInterchange"));
        Assert.assertNotNull(tef);
        
        Track t = loc.getTrackByName("Test Location Interchange 1", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditInterchange"));
        Assert.assertNull(tef);
    }
    
    @Test
    public void testEdityard() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.createOneNormalLocation("Test Location");

        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Assert.assertEquals("should be 1 locations", 1, lManager.getLocationsByNameList().size());
        Location loc = lManager.getLocationByName("Test Location");
        Assert.assertNotNull(loc);

        LocationEditFrame f = new LocationEditFrame(loc);
        Assert.assertNotNull(f);
        
        JemmyUtil.enterClickAndLeave(f.yardRadioButton);
        
        JFrameOperator jfo = new JFrameOperator(f);
        JTableOperator tbl = new JTableOperator(jfo);
        tbl.clickOnCell(1, tbl.findColumn(Bundle.getMessage("ButtonEdit")));
        
        // confirm edit interchange track window exists
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame(Bundle.getMessage("EditYard")) != null;
        }, "esf not null");
        JmriJFrame tef = JmriJFrame.getFrame(Bundle.getMessage("EditYard"));
        Assert.assertNotNull(tef);
        
        Track t = loc.getTrackByName("Test Location Yard 1", null);
        Assert.assertNotNull(t);
        t.setLength(222); // change track length to create property change

        JUnitUtil.dispose(f);
        tef = JmriJFrame.getFrame(Bundle.getMessage("EditYard"));
        Assert.assertNull(tef);
    }
}
