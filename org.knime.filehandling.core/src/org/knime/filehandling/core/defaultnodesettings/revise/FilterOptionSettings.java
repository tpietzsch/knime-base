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
import org.knime.core.node.config.Config;
import org.knime.filehandling.core.defaultnodesettings.revise.FilterOptionPanel.FilterOption;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public class FilterOptionSettings {

    private static final String CFG_FILTER_OTPION = "filter_selection";

    private static final String CFG_INCLUDE_SUBFOLDERS = "include_subfolders";

    private static final String CFG_FILTER_CONFIG = "filter_config";

    //    private SettingsModelString a = new SettingsModelString(CFG_FILTER_OTPION, FilterOption.FILE.name());

    private FilterOption m_filterOption = FilterOption.FILE;

    private boolean m_includeSubfolders = false;

    private FileAndFolderFilterConfigSettings m_filterConfigSettings = new FileAndFolderFilterConfigSettings();

    /**
     *
     */
    public FilterOptionSettings() {
        // TODO Auto-generated constructor stub
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
    }

    /**
     * @return the filterConfigSettings
     */
    public FileAndFolderFilterConfigSettings getFilterConfigSettings() {
        return m_filterConfigSettings;
    }

    /**
     * @param filterConfigSettings the filterConfigSettings to set
     */
    public void setFilterConfigSettings(final FileAndFolderFilterConfigSettings filterConfigSettings) {
        m_filterConfigSettings = filterConfigSettings;
    }

    public void saveSettingsTo(final Config config) throws InvalidSettingsException {
        config.addString(CFG_FILTER_OTPION, m_filterOption.name());
        config.addBoolean(CFG_INCLUDE_SUBFOLDERS, m_includeSubfolders);
        m_filterConfigSettings.saveToConfig(config.addConfig(CFG_FILTER_CONFIG));
    }

    public void loadSettingsFrom(final Config config) throws InvalidSettingsException {
        m_filterOption = FilterOption.valueOf(config.getString(CFG_FILTER_OTPION));
        m_includeSubfolders = config.getBoolean(CFG_INCLUDE_SUBFOLDERS);
        m_filterConfigSettings.loadFromConfig(config.getConfig(CFG_FILTER_CONFIG));
    }
}
