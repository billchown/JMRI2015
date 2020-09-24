package jmri.jmrit.display.switchboardEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.switchboardEditor.BeanSwitch;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.switchboard.BeanSwitch objects.
 *
 * @author Egbert Broerse Copyright: (c) 2017, 2020
 */
public class BeanSwitchXml extends AbstractXmlAdapter {

    public BeanSwitchXml() {
    }

    /**
     * Default implementation for storing the contents of a BeanSwitch.
     * Used to display Switchboard switches in JMRI web server.
     *
     * @param o Object to store, of type BeanSwitch
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        BeanSwitch bs = (BeanSwitch) o;
        Element element = new Element("beanswitch");
        // include attributes
        element.setAttribute("label", bs.getNameString());
        if (bs.getNamedBean() != null) {
            element.setAttribute("connected", "true");
        } else {
            element.setAttribute("connected", "false");
        }
        // get state textual info (only used for shape 'button')
        // includes beanswitch label to operate like SensorIcon
        // never null
        Element textElement = new Element("activeText");
        textElement.setAttribute("text", bs.getActiveText());
        element.addContent(textElement);
        textElement = new Element("inactiveText");
        textElement.setAttribute("text", bs.getInactiveText());
        element.addContent(textElement);
        textElement = new Element("unknownText");
        textElement.setAttribute("text", bs.getUnknownText());
        element.addContent(textElement);
        textElement = new Element("inconsistentText");
        textElement.setAttribute("text", bs.getInconsistentText());
        element.addContent(textElement);
        String txt = bs.getToolTip();
        if (txt != null) {
            Element elem = new Element("tooltip").addContent(txt);
            element.addContent(elem);
        }
        //element.setAttribute("shape", Integer.toString(bs.getShape())); // 2 = drawing, 0 = button = default
        // is same for all switches, get from SwitchboardEditor editor
        element.setAttribute("class", BeanSwitchXml.class.getName());
        return element;
    }

    /**
     * Load, starting with the BeanSwitch element, then all the value-icon
     * pairs. Not currently used because BeanSwitches are auto-generated from SwitchBoard settings.
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        //BeanSwitch bs = (BeanSwitch) o;
        log.debug("load xml called");
    }

    private static final Logger log = LoggerFactory.getLogger(BeanSwitchXml.class);

}
