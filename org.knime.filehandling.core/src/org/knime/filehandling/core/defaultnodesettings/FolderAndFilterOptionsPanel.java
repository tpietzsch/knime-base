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
 *   Oct 18, 2019 (julian): created
 */
package org.knime.filehandling.core.defaultnodesettings;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.knime.filehandling.core.filefilter.FileFilter.FilterType;
import org.knime.filehandling.core.filefilter.FileFilterDialog;
import org.knime.filehandling.core.filefilter.FileFilterPanel;

/**
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
class FolderAndFilterOptionsPanel extends JPanel {

    private static final long serialVersionUID = -7321063815273870613L;

    /** String used as label for the include sub folders check box */
    private static final String INCLUDE_SUBFOLDERS_LABEL = "Include subfolders";

    /** String used as label for the filter files check box */
    private static final String FILTER_FILES_LABEL = "Filter files in folder";

    /** String used as button label */
    private static final String CONFIGURE_BUTTON_LABEL = "Configure";

    /** Check box to select whether to include sub folders while listing files in folders */
    private JCheckBox m_includeSubfolders;

    /** Check box to select whether to filter files or not */
    private JCheckBox m_filterFiles;

    /** Button to open the dialog that contains options for file filtering */
    private JButton m_configureFilter;

    /** Panel containing options for file filtering */
    private FileFilterPanel m_fileFilterPanel;

    /** Extra dialog containing the file filter panel */
    private FileFilterDialog m_fileFilterDialog;

    /**
     * @param suffixes
     */
    FolderAndFilterOptionsPanel(final String...suffixes) {
        m_includeSubfolders = new JCheckBox(INCLUDE_SUBFOLDERS_LABEL);
        m_filterFiles = new JCheckBox(FILTER_FILES_LABEL);
        m_configureFilter = new JButton(CONFIGURE_BUTTON_LABEL);
        m_fileFilterPanel = new FileFilterPanel(suffixes);
        initLayout();
    }

    private final void initLayout() {
        setLayout(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(m_includeSubfolders, gbc);

        gbc.gridx++;
        add(m_filterFiles, gbc);

        gbc.gridx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 0, 5, 0);
        add(m_configureFilter, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        add(Box.createHorizontalGlue(), gbc);
    }

    private final void addIncludeSubfolderActionListener(final ActionListener listener) {
        m_includeSubfolders.addActionListener(listener);
    }

    private final void addFilterFilesActionListener(final ActionListener listener) {
        m_filterFiles.addActionListener(listener);
    }

    final void addActionListener(final ActionListener listener) {
        addIncludeSubfolderActionListener(listener);
        addFilterFilesActionListener(listener);
    }

    final void addConfigureButtonActionListener(final ActionListener listener) {
        m_configureFilter.addActionListener(listener);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        m_includeSubfolders.setEnabled(enabled);
        m_filterFiles.setEnabled(enabled);
        m_configureFilter.setEnabled(enabled);
        m_fileFilterPanel.setEnabled(enabled);
    }

    final void syncComponents(final SettingsModelFileChooser2 settingsModel) {
        // sync sub folder check box
        final boolean includeSubfolders = settingsModel.getIncludeSubfolders();
        if (includeSubfolders != m_includeSubfolders.isSelected()) {
            m_includeSubfolders.setSelected(includeSubfolders);
        }
        // sync filter files check box
        final boolean filterFiles = settingsModel.getFilterFiles();
        if (filterFiles != m_filterFiles.isSelected()) {
            m_filterFiles.setSelected(filterFiles);
        }

        // sync filter mode combo box
        final String filterMode = settingsModel.getFilterMode();
        if ((filterMode != null)
            && !filterMode.equals(m_fileFilterPanel.getSelectedFilterType().getDisplayText())) {
            m_fileFilterPanel.setFilterType(FilterType.fromDisplayText(filterMode));
        }
        // sync filter expression combo box
        final String filterExpr = settingsModel.getFilterExpression();
        if ((filterExpr != null) && !filterExpr.equals(m_fileFilterPanel.getSelectedFilterExpression())) {
            m_fileFilterPanel.setFilterExpression(filterExpr);
        }
        // sync case sensitivity check box
        final boolean caseSensitive = settingsModel.getCaseSensitive();
        if (caseSensitive != m_fileFilterPanel.getCaseSensitive()) {
            m_fileFilterPanel.setCaseSensitive(caseSensitive);
        }
    }

    final void updateSettingsModel(final SettingsModelFileChooser2 settingsModel) {
        settingsModel.setIncludeSubfolders(m_includeSubfolders.isEnabled() && m_includeSubfolders.isSelected());
        settingsModel.setFilterFiles(m_filterFiles.isEnabled() && m_filterFiles.isSelected());
        updateFilterOptions(settingsModel);
    }

    final void updateFilterOptions(final SettingsModelFileChooser2 settingsModel) {
        settingsModel.setFilterConditions(m_fileFilterPanel.getSelectedFilterType(),
            m_fileFilterPanel.getSelectedFilterExpression(), m_fileFilterPanel.getCaseSensitive());
    }

    final FileFilterDialog getFileFilterDialog() {
        return m_fileFilterDialog;
    }

    final void setFileFilterDialog(final Frame f) {
        m_fileFilterDialog = new FileFilterDialog(f, m_fileFilterPanel);
    }

    final FilterType getSelectedFilterType() {
        return m_fileFilterPanel.getSelectedFilterType();
    }

    final String getSelectedFilterExpression() {
        return m_fileFilterPanel.getSelectedFilterExpression();
    }

    final boolean getCaseSensitive() {
        return m_fileFilterPanel.getCaseSensitive();
    }
}
