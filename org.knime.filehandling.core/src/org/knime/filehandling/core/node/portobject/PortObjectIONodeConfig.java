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
 */
package org.knime.filehandling.core.node.portobject;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.util.FileUtil;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2;
import org.knime.filehandling.core.defaultnodesettings.revise.FilterModeDialogComponent.FilterOption;
import org.knime.filehandling.core.defaultnodesettings.revise.FilterModeSettingsModel;
import org.knime.filehandling.core.node.portobject.writer.PortObjectWriterNodeConfig;

/**
 * Configuration class for port object reader and writer nodes that can be extended with additional configurations.
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 * @noextend extend either {@link PortObject} or {@link PortObjectWriterNodeConfig}
 */
public abstract class PortObjectIONodeConfig {

    /** Config key for file chooser. */
    private static final String CFG_FILE_CHOOSER = "filechooser";

    /** Config key for connection timeout. */
    private static final String CFG_CONNECTION_TIMEOUT = "timeout";

    /** The file chooser model. */
    private final SettingsModelFileChooser2 m_fileChooserModel;

    /** The timeout settings model. */
    private final SettingsModelIntegerBounded m_timeoutModel = new SettingsModelIntegerBounded(CFG_CONNECTION_TIMEOUT,
        FileUtil.getDefaultURLTimeoutMillis(), 0, Integer.MAX_VALUE);

    private FilterModeSettingsModel m_s = new FilterModeSettingsModel("asd", FilterOption.FILE);

    /**
     * Constructor for configs in which the file chooser doesn't filter on file suffixes.
     */
    protected PortObjectIONodeConfig() {
        m_fileChooserModel = new SettingsModelFileChooser2(CFG_FILE_CHOOSER);
    }

    /**
     * Constructor for configs in which the file chooser filters on a set of file suffixes.
     *
     * @param fileSuffixes the suffixes to filter on
     */
    protected PortObjectIONodeConfig(final String[] fileSuffixes) {
        m_fileChooserModel = new SettingsModelFileChooser2(CFG_FILE_CHOOSER, fileSuffixes);
    }

    /**
     * Returns the file chooser model.
     *
     * @return the file chooser model
     */
    public final SettingsModelFileChooser2 getFileChooserModel() {
        return m_fileChooserModel;
    }

    /**
     * Returns the timeout model.
     *
     * @return the timeout model
     */
    public final SettingsModelIntegerBounded getTimeoutModel() {
        return m_timeoutModel;
    }

    /**
     * Validates the given settings.
     *
     * @param settings the node settings
     * @throws InvalidSettingsException if settings are invalid
     */
    protected void validateConfigurationForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_fileChooserModel.validateSettings(settings);
        m_timeoutModel.validateSettings(settings);
    }

    /**
     * Saves the configuration for the {@code NodeModel}.
     *
     * @param settings the settings to save to
     */
    protected void saveConfigurationForModel(final NodeSettingsWO settings) {
        m_fileChooserModel.saveSettingsTo(settings);
        m_timeoutModel.saveSettingsTo(settings);
//        try {
//            m_s.saveSettingsTo(settings.addConfig("asd"));
//        } catch (InvalidSettingsException ex) {
//            System.out.println(ex);
//        }
    }

    /**
     * Load configuration in {@code NodeModel}.
     *
     * @param settings the settings to load from
     * @throws InvalidSettingsException - If loading the configuration failed
     */
    protected void loadConfigurationForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_fileChooserModel.loadSettingsFrom(settings);
        m_timeoutModel.loadSettingsFrom(settings);
//        m_s.loadSettingsFrom(settings.getConfig("asd"));
    }

    /**
     * Saves the configuration in the node dialog. Don't save any settings that are used by dialog components here.
     *
     * @param settings the settings to save to
     * @throws InvalidSettingsException - If saving the settings failed
     */
    protected void saveConfigurationForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        // nothing to do here
    }

    /**
     * Loads the configuration for the {@code NodeDialog}. Don't load any settings that are used by dialog components
     * here.
     *
     * @param settings the settings to load from
     * @param specs the portobject spec
     * @throws NotConfigurableException - If loading the configuration failed
     */
    protected void loadConfigurationForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        // nothing to do here
    }

    /**
     * @return the s
     */
    public FilterModeSettingsModel getS() {
        return m_s;
    }

    /**
     * @param s the s to set
     */
    public void setS(final FilterModeSettingsModel s) {
        m_s = s;
    }

}
