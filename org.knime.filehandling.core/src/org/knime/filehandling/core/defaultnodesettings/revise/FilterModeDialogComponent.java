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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentButtonGroup;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ButtonGroupEnumInterface;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public final class FilterModeDialogComponent extends DialogComponent {

    /** String used as button label */
    private static final String CONFIGURE_BUTTON_LABEL = "Filter options";

    /** String used as label for the include sub folders check box */
    private static final String INCLUDE_SUBFOLDERS_LABEL = "Include subfolders";

    private final DialogComponentButtonGroup m_filterModeButtonGroup;

    /** Button to open the dialog that contains options for file filtering */
    private final JButton m_configureFilterButton;

    private final JCheckBox m_includeSubfoldersCheckBox;

    /** Panel containing options for file filtering */
    private final FilterDialogPanel m_filterDialogPanel;

    private final JPanel m_filterModePanel;

    private final JPanel m_filterPanel;

    /**
     * @param model
     */
    public FilterModeDialogComponent(final FilterModeSettingsModel model) {
        super(model);
        final SettingsModelString filterModeModel =
            new SettingsModelString("a", ((FilterModeSettingsModel)getModel()).getFilterOption().name()); // TODO
        m_filterModeButtonGroup = new DialogComponentButtonGroup(filterModeModel, null, false, FilterOption.values());
        m_configureFilterButton = new JButton(CONFIGURE_BUTTON_LABEL);
        m_includeSubfoldersCheckBox = new JCheckBox(INCLUDE_SUBFOLDERS_LABEL);
        m_configureFilterButton.addActionListener(e -> showFileFilterConfigurationDialog());
        m_filterDialogPanel = new FilterDialogPanel();

        m_filterModePanel = createFilterModePanel();
        m_filterPanel = createFilterPanel();

        filterModeModel
            .addChangeListener(l -> model.setFilterOption(FilterOption.valueOf(filterModeModel.getStringValue())));
        m_includeSubfoldersCheckBox
            .addChangeListener(l -> model.setIncludeSubfolders(m_includeSubfoldersCheckBox.isSelected()));

        initComponent();
        // TODO settings stuff
        // TODO implement filter
    }

    private void initComponent() {
        getComponentPanel().setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1;
        getComponentPanel().add(m_filterModePanel, gbc);
        gbc.weighty = 1;
        gbc.gridy++;
        getComponentPanel().add(m_filterPanel, gbc);
    }

    private JPanel createFilterModePanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 0, 1, GridBagConstraints.NORTHWEST,
            GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(m_filterModeButtonGroup.getComponentPanel(), gbc);
        return panel;
    }

    private JPanel createFilterPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(m_configureFilterButton, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(m_includeSubfoldersCheckBox, gbc);
        return panel;
    }

    /**
     * @return the selectionModePanel
     */
    public JPanel getSelectionModePanel() {
        return m_filterModePanel;
    }

    /**
     * @return the filterConfigPanel
     */
    public JPanel getFilterConfigPanel() {
        return m_filterPanel;
    }

    /** Method called if file filter configuration button is clicked */
    private void showFileFilterConfigurationDialog() {

        final Container c = getComponentPanel().getParent();
        Frame parentFrame = null;
        Container parent = getComponentPanel();
        while (parent != null) {
            if (parent instanceof Frame) {
                parentFrame = (Frame)parent;
                break;
            }
            parent = parent.getParent();
        }

        final FilterDialog filterDialog = new FilterDialog(parentFrame, m_filterDialogPanel);
        filterDialog.setLocationRelativeTo(c); // TODO
        filterDialog.setVisible(true);

        if (filterDialog.getResultStatus() == JOptionPane.OK_OPTION) {
            // updates the settings model
            ((FilterModeSettingsModel)getModel())
                .setFilterConfigSettings(m_filterDialogPanel.getFilterConfigSettings());
        } else {
            // overwrites the values in the file filter panel components with the save ones
            m_filterDialogPanel
                .setFilterConfigSettings(((FilterModeSettingsModel)getModel()).getFilterConfigSettings());
        }
    }

    public enum FilterOption implements ButtonGroupEnumInterface {
            FILE("File"), FOLDER("Folder"), FILES_IN_FOLDERS("Files in folders"), FOLDERS("Folders"),
            FILES_AND_FOLDERS("Files and folders");

        private final String m_label;

        private FilterOption(final String label) {
            m_label = label;
        }

        @Override
        public String getText() {
            return m_label;
        }

        @Override
        public String getActionCommand() {
            return name();
        }

        @Override
        public String getToolTip() {
            return null;
        }

        @Override
        public boolean isDefault() {
            return this == FILE;
        }
    }

    @Override
    protected void updateComponent() {
        final FilterModeSettingsModel model = (FilterModeSettingsModel)getModel();
        final SettingsModelString filterModeModel = (SettingsModelString)m_filterModeButtonGroup.getModel();
        filterModeModel.setStringValue(model.getFilterOption().name());
        m_includeSubfoldersCheckBox.setSelected(model.isIncludeSubfolders());
        m_filterDialogPanel.setFilterConfigSettings(model.getFilterConfigSettings());
        m_filterDialogPanel.visibleComponents(FilterOption.valueOf(filterModeModel.getStringValue()));
        setEnabledComponents(model.isEnabled());
    }
    //
    //    private void updateModel() {
    //        final FilterModeSettingsModel model = (FilterModeSettingsModel)getModel();
    //        final SettingsModelString filterModeModel = (SettingsModelString)m_filterModeButtonGroup.getModel();
    //        model.setFilterOption(FilterOption.valueOf(filterModeModel.getStringValue()));
    //        model.setFilterConfigSettings(m_filterDialogPanel.getFilterConfigSettings());
    //        model.notifyListeners();
    //    }

    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        // TODO
        // nothing to do
    }

    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // TODO
        // nothing to do
    }

    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_filterModePanel.setEnabled(enabled); // TODO does panel disable its components?
        m_filterModeButtonGroup.getModel().setEnabled(enabled);
        m_configureFilterButton.setEnabled(enabled);
        m_includeSubfoldersCheckBox.setEnabled(enabled);
    }

    @Override
    public void setToolTipText(final String text) {
        // TODO Auto-generated method stub

    }
}
