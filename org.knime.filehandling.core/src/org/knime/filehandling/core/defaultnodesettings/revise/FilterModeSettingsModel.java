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
 *   Apr 15, 2020 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.defaultnodesettings.revise;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.filehandling.core.defaultnodesettings.revise.FilterModeDialogComponent.FilterOption;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public class FilterModeSettingsModel extends SettingsModel {

    private static final String MODEL_TYPE_ID = "SMID_FSLocation";

    private static final String CFG_FILTER_OTPION = "filter_selection";

    private static final String CFG_INCLUDE_SUBFOLDERS = "include_subfolders";

    private static final String CFG_FILTER_CONFIG = "filter_config";

    private static final boolean DEFAULT_INCLUDE_SUBFOLDERS = false;

    private final String m_configName;

    private FilterOption m_filterOption;

    private boolean m_includeSubfolders = DEFAULT_INCLUDE_SUBFOLDERS;

    private FilterDialogSettings m_filterConfigSettings = new FilterDialogSettings();

    /**
     *
     */
    public FilterModeSettingsModel(final String configName, final FilterOption defaultSelectionMode) {
        m_configName = configName;
        m_filterOption = defaultSelectionMode;
    }

    private FilterModeSettingsModel(final FilterModeSettingsModel toCopy) {
        m_configName = toCopy.m_configName;
        m_filterOption = toCopy.m_filterOption;
        m_includeSubfolders = toCopy.m_includeSubfolders;
        m_filterConfigSettings = toCopy.m_filterConfigSettings;
    }

    /**
     * @return the filterOption
     */
    public FilterOption getFilterOption() {
        return m_filterOption;
    }

    /**
     * @param filterOption the filterOption to set
     */
    public void setFilterOption(final FilterOption filterOption) {
        m_filterOption = filterOption;
        notifyChangeListeners();
    }

    /**
     * @return the filterConfigSettings
     */
    public FilterDialogSettings getFilterConfigSettings() {
        return m_filterConfigSettings;
    }

    /**
     * @param filterConfigSettings the filterConfigSettings to set
     */
    public void setFilterConfigSettings(final FilterDialogSettings filterConfigSettings) {
        m_filterConfigSettings = filterConfigSettings;
        notifyChangeListeners();
    }

    //    public void saveSettingsTo(final Config config) throws InvalidSettingsException {
    //        config.addString(CFG_FILTER_OTPION, m_filterOption.name());
    //        config.addBoolean(CFG_INCLUDE_SUBFOLDERS, m_includeSubfolders);
    //        m_filterConfigSettings.saveToConfig(config.addConfig(CFG_FILTER_CONFIG));
    //    }
    //
    //    public void loadSettingsFrom(final Config config) throws InvalidSettingsException {
    //        m_filterOption = FilterOption.valueOf(config.getString(CFG_FILTER_OTPION));
    //        m_includeSubfolders = config.getBoolean(CFG_INCLUDE_SUBFOLDERS);
    //        m_filterConfigSettings.loadFromConfig(config.getConfig(CFG_FILTER_CONFIG));
    //    }

    /**
     * @return the includeSubfolders
     */
    public boolean isIncludeSubfolders() {
        return m_includeSubfolders;
    }

    /**
     * @param includeSubfolders the includeSubfolders to set
     */
    public void setIncludeSubfolders(final boolean includeSubfolders) {
        m_includeSubfolders = includeSubfolders;
        notifyChangeListeners();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected FilterModeSettingsModel createClone() {
        return new FilterModeSettingsModel(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getModelTypeID() {
        return MODEL_TYPE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigName() {
        return m_configName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        m_filterOption = FilterOption.valueOf(settings.getString(CFG_FILTER_OTPION, m_filterOption.name()));
        m_includeSubfolders = settings.getBoolean(CFG_INCLUDE_SUBFOLDERS, DEFAULT_INCLUDE_SUBFOLDERS);
        try {
            m_filterConfigSettings.loadFromConfigForDialog(settings.getConfig(CFG_FILTER_CONFIG));
        } catch (InvalidSettingsException ex) {
            // nothing to do
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        FilterOption.valueOf(settings.getString(CFG_FILTER_OTPION));
        settings.getBoolean(CFG_INCLUDE_SUBFOLDERS);
        m_filterConfigSettings.validate(settings.getConfig(CFG_FILTER_CONFIG));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_filterOption = FilterOption.valueOf(settings.getString(CFG_FILTER_OTPION));
        m_includeSubfolders = settings.getBoolean(CFG_INCLUDE_SUBFOLDERS);
        m_filterConfigSettings.loadFromConfigForModel(settings.getConfig(CFG_FILTER_CONFIG));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsForModel(final NodeSettingsWO settings) {
        settings.addString(CFG_FILTER_OTPION, m_filterOption.name());
        settings.addBoolean(CFG_INCLUDE_SUBFOLDERS, m_includeSubfolders);
        m_filterConfigSettings.saveToConfig(settings.addConfig(CFG_FILTER_CONFIG));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + " ('" + m_configName + "')";
    }
}
