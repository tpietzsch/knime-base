/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Apr 14, 2020 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.revise;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.util.ButtonGroupEnumInterface;
import org.knime.filehandling.core.filefilter.FileFilterDialog;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public final class FilterOptionPanel extends JPanel {

    /** String used as button label */
    private static final String CONFIGURE_BUTTON_LABEL = "Filter options";

    /** String used as label for the include sub folders check box */
    private static final String INCLUDE_SUBFOLDERS_LABEL = "Include subfolders";

    private final CopyOnWriteArrayList<ChangeListener> m_listeners;

    private final DialogComponentButtonGroup m_buttonGroup;

    private final JPanel m_selectionPanel;

    /** Button to open the dialog that contains options for file filtering */
    private final JButton m_configureFilter;

    /** Panel containing options for file filtering */
    private final FileAndFolderFilterConfigPanel m_fileFilterConfigurationPanel;

    private FileFilterDialog m_fileFilterDialog;

    private JPanel m_fileFilterOptionPanel;

    private final JCheckBox m_includeSubfolders;

    private final FilterOptionSettings m_settings = new FilterOptionSettings();

    public FilterOptionPanel() {
        super(new GridBagLayout());
        m_listeners = new CopyOnWriteArrayList<ChangeListener>();
        final SettingsModelString filterOptionModel = new SettingsModelString("a", m_settings.getFilterOption().name()); // TODO
        m_buttonGroup = new DialogComponentButtonGroup(filterOptionModel, null, false, FilterOption.values());
        m_selectionPanel = createSelectionPanel();
        m_configureFilter = new JButton(CONFIGURE_BUTTON_LABEL);
        m_includeSubfolders = new JCheckBox(INCLUDE_SUBFOLDERS_LABEL);
        m_configureFilter.addActionListener(e -> showFileFilterConfigurationDialog());
        m_fileFilterConfigurationPanel = new FileAndFolderFilterConfigPanel();

        initFilterOptionsPanel();
        m_fileFilterConfigurationPanel.visibleComponents(FilterOption.valueOf(filterOptionModel.getStringValue()));
        filterOptionModel.addChangeListener(l -> {
            m_fileFilterConfigurationPanel.visibleComponents(FilterOption.valueOf(filterOptionModel.getStringValue()));
            notifyChangeListeners();
        });
        // TODO settings stuff
        // TODO implement filter
    }

    private JPanel createSelectionPanel() {
        final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0);
        gbc.weightx = 1;
        add(m_buttonGroup.getComponentPanel(), gbc);
        return null;
    }

    private final void initFilterOptionsPanel() {
        m_fileFilterOptionPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        m_fileFilterOptionPanel.add(m_configureFilter, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        m_fileFilterOptionPanel.add(m_includeSubfolders, gbc);
    }

    /**
     * @return the fileFilterOptionPanel
     */
    public JPanel getFileFilterOptionPanel() {
        return m_fileFilterOptionPanel;
    }

    /** Method called if file filter configuration button is clicked */
    private void showFileFilterConfigurationDialog() {

        final Container c = this.getParent();
        Frame parentFrame = null;
        Container parent = this;
        while (parent != null) {
            if (parent instanceof Frame) {
                parentFrame = (Frame)parent;
                break;
            }
            parent = parent.getParent();
        }

        m_fileFilterDialog = new FileFilterDialog(parentFrame, m_fileFilterConfigurationPanel);
        m_fileFilterDialog.setLocationRelativeTo(c); // TODO
        m_fileFilterDialog.setVisible(true);

        if (m_fileFilterDialog.getResultStatus() == JOptionPane.OK_OPTION) {
            // updates the settings model
            m_settings.setFilterConfigSettings(m_fileFilterConfigurationPanel.getFilterConfigSettings());
            notifyChangeListeners();

        } else {
            // overwrites the values in the file filter panel components with the save ones
            m_fileFilterConfigurationPanel.setFilterConfigSettings(m_settings.getFilterConfigSettings());
        }
    }

    /**
     * Adds a listener (to the end of the listener list) which is notified, whenever a new values is set in the model or
     * the enable status changes. Does nothing if the listener is already registered.
     *
     * @param l listener to add.
     */
    public void addChangeListener(final ChangeListener l) {
        if (!m_listeners.contains(l)) {
            m_listeners.add(l);
        }
    }

    /**
     * Remove a specific listener.
     *
     * @param l listener to remove.
     */
    public void removeChangeListener(final ChangeListener l) {
        m_listeners.remove(l);
    }

    /**
     * Notifies all registered listeners about a new model content. Call this, whenever the value in the model changes!
     */
    private void notifyChangeListeners() {
        for (final ChangeListener l : m_listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    enum FilterOption implements ButtonGroupEnumInterface {
            FILE("File"), FOLDER("Folder"), FILES_IN_FOLDERS("Files in folders"), FOLDERS("Folders"),
            FILES_AND_FOLDERS("Files and folders");

        private final String m_label;

        private FilterOption(final String label) {
            m_label = label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText() {
            return m_label;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getActionCommand() {
            return name();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getToolTip() {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isDefault() {
            return this == FILE;
        }
    }
}
