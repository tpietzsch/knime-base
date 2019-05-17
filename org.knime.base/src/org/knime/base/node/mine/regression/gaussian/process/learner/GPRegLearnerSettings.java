/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 *
 * History
 *   21.01.2010 (hofer): created
 */
package org.knime.base.node.mine.regression.gaussian.process.learner;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

/**
 * This class hold the settings for the Logistic Learner Node.
 *
 * @author Heiko Hofer
 * @author Gabor Bakos
 * @author Adrian Nembach, KNIME.com
 * @since 3.1
 */
public class GPRegLearnerSettings {

    private static final String CFG_TARGET = "target";

    static final double DEFAULT_SIGMA = 1;

    static final boolean DEFAULT_CALC_COVMATRIX = true;

    static final double DEFAULT_LAMBDA = 1e-6;

    private static final String CFG_LAMBDA = "lambda";

    private static final String CFG_SIGMA = "sigma";

    private String m_targetColumn;

    /** The selected learning columns configuration. */
    private DataColumnSpecFilterConfiguration m_includedColumns = GPRegLearnerNodeModel.createDCSFilterConfiguration();

    private double m_sigma;

    private double m_lambda;

    /**
     * Create default settings.
     */
    public GPRegLearnerSettings() {
        m_targetColumn = null;
        m_sigma = DEFAULT_SIGMA;
        m_lambda = DEFAULT_LAMBDA;
    }

    /**
     * Loads the settings from the node settings object.
     *
     * @param settings a node settings object
     * @throws InvalidSettingsException if some settings are missing
     */
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_targetColumn = settings.getString(CFG_TARGET);
        m_includedColumns.loadConfigurationInModel(settings);

        m_lambda = settings.getDouble(CFG_LAMBDA);
        m_sigma = settings.getDouble(CFG_SIGMA);
        validate();
    }

    /**
     * Loads the settings from the node settings object using default values if some settings are missing.
     *
     * @param settings a node settings object
     * @param inputTableSpec The input table's spec.
     */
    public void loadSettingsForDialog(final NodeSettingsRO settings, final DataTableSpec inputTableSpec) {
        m_targetColumn = settings.getString(CFG_TARGET, null);
        m_includedColumns.loadConfigurationInDialog(settings, inputTableSpec);
        m_lambda = settings.getDouble(CFG_LAMBDA, DEFAULT_LAMBDA);
        m_sigma = settings.getDouble(CFG_SIGMA, DEFAULT_SIGMA);
    }

    /**
     * Saves the settings into the node settings object.
     *
     * @param settings a node settings object
     */
    public void saveSettings(final NodeSettingsWO settings) {
        settings.addString(CFG_TARGET, m_targetColumn);
        m_includedColumns.saveConfiguration(settings);
        settings.addDouble(CFG_LAMBDA, m_lambda);
        settings.addDouble(CFG_SIGMA, m_sigma);
    }

    /**
     * Checks if the provided settings make sense. E.g. that the maxEpochs or variance of the prior is larger than 0.
     *
     * @throws InvalidSettingsException if the settings are not valid
     */
    public void validate() throws InvalidSettingsException {
        CheckUtils.checkSetting(m_lambda >= 0, "Epsilon must be positive but was %g.", m_lambda);
        CheckUtils.checkSetting(m_sigma >= 0, "Epsilon must be positive but was %g.", m_sigma);
    }

    /**
     * The target column which is the dependent variable.
     *
     * @return the targetColumn
     */
    public String getTargetColumn() {
        return m_targetColumn;
    }

    /**
     * Set the target column which is the dependent variable.
     *
     * @param targetColumn the targetColumn to set
     */
    public void setTargetColumn(final String targetColumn) {
        m_targetColumn = targetColumn;
    }

    /**
     * @return the includedColumns
     */
    public DataColumnSpecFilterConfiguration getIncludedColumns() {
        return m_includedColumns;
    }

    /**
     * @param includedColumns the includedColumns to set
     */
    public void setIncludedColumns(final DataColumnSpecFilterConfiguration includedColumns) {
        m_includedColumns = includedColumns;
    }

    /**
     * @return the sigma
     */
    public double getSigma() {
        return m_sigma;
    }

    /**
     * @param sigma the sigma to set
     */
    public void setSigma(final double sigma) {
        m_sigma = sigma;
    }

    /**
     * @return the lambda
     */
    public double getLambda() {
        return m_lambda;
    }

    /**
     * @param lambda the lambda to set
     */
    public void setLambda(final double lambda) {
        m_lambda = lambda;
    }
}
