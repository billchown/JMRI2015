package jmri.jmrit.logixng.implementation.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.AbstractSwingConfigurator;

/**
 * Abstract class for SwingConfiguratorInterface
 */
public abstract class AbstractMaleSocketSwing extends AbstractSwingConfigurator {

    private JPanel panel;
    private JPanel tablePanel;
    private JTable table;
    
    /** {@inheritDoc} */
    @Override
    public BaseManager<? extends NamedBean> getManager() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public final JPanel getConfigPanel(@Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(null, buttonPanel);
        return panel;
    }
    
    /** {@inheritDoc} */
    @Override
    public final JPanel getConfigPanel(@Nonnull Base object, @Nonnull JPanel buttonPanel) throws IllegalArgumentException {
        createPanel(object, buttonPanel);
        return panel;
    }
    
    protected final void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && (! (object instanceof MaleSocket))) {
            throw new IllegalArgumentException("object is not a MaleSocket: " + object.getClass().getName());
        }
        MaleSocket maleSocket = (MaleSocket)object;
        int row = 0;
        panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.anchor = java.awt.GridBagConstraints.EAST;
        JPanel subPanel = createSubPanel(object, buttonPanel);
        if (subPanel != null) {
            c.gridy = row++;
            panel.add(subPanel, c);
        }
        createTablePanel(maleSocket);
        c.gridy = row;
//        c.gridy = row++;
        panel.add(tablePanel, c);
    }
    
    private void createTablePanel(MaleSocket maleSocket) {
        tablePanel = new JPanel();
        table = new JTable();
        tablePanel.add(table);
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /**
     * The sub class may override this method to add more detail to the panel.
     * @param object the object for which to return a configuration panel
     * @param buttonPanel panel with the buttons
     * @return a panel that configures this object
     */
    protected JPanel createSubPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        return null;
    }
    
    /**
     * If the sub class overrides createSubPanel(), it may use this method to
     * validate the sub panel.
     * <P>
     * The parameter errorMessage is used to give the error message in case of
     * an error. If there are errors, the error messages is added to the list
     * errorMessage.
     * 
     * @param errorMessages the error messages in case of an error
     * @return true if data in the form is valid, false otherwise
     */
    public boolean validateSubPanel(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public final void updateObject(@Nonnull Base object) {
        // Nothing to update
    }
    
    /** {@inheritDoc} */
    @Override
    public String getExampleSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public final void dispose() {
    }
    
    /**
     * Dispose the sub panel and remove all the listeners that this class may
     * have registered.
     */
    public void disposeSubPanel() {
    }
    
    
    
    
    
}
