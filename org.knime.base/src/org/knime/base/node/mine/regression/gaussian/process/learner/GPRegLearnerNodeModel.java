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
 * -------------------------------------------------------------------
 *
 * History
 *   21.01.2010 (hofer): created
 */
package org.knime.base.node.mine.regression.gaussian.process.learner;

import java.io.File;
import java.io.IOException;

import org.knime.base.node.mine.regression.gaussian.process.model.GaussianProcessRegressionPortObject;
import org.knime.base.node.mine.regression.gaussian.process.model.GaussianProcessRegressionPortObjectSpec;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.CloseableRowIterator;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.NameFilterConfiguration;
import org.knime.core.node.util.filter.NameFilterConfiguration.FilterResult;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;

import smile.math.kernel.GaussianKernel;

/**
 * NodeModel to the logistic regression learner node. It delegates the calculation to <code>LogRegLearner</code>.
 *
 * @author Heiko Hofer
 * @author Gabor Bakos
 * @author Adrian Nembach, KNIME.com
 * @since 3.3
 */
public final class GPRegLearnerNodeModel extends NodeModel {
    private final GPRegLearnerSettings m_settings;

    /** Inits a new node model, it will have 1 data input, 1 model and 2 data output. */
    public GPRegLearnerNodeModel() {
        super(new PortType[]{BufferedDataTable.TYPE}, new PortType[]{PortObject.TYPE});
        m_settings = new GPRegLearnerSettings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        m_settings.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        m_settings.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final BufferedDataTable data = (BufferedDataTable)inObjects[0];
        final DataTableSpec tableSpec = data.getDataTableSpec();

        final GaussianProcessRegression<double[]> gp = trainGP(exec, data);

        final FilterResult fr = m_settings.getIncludedColumns().applyTo(tableSpec);
        final ColumnRearranger cr = new ColumnRearranger(tableSpec);
        cr.keepOnly(fr.getIncludes());
        cr.remove(m_settings.getTargetColumn());
        final GaussianProcessRegressionPortObjectSpec spec =
            new GaussianProcessRegressionPortObjectSpec(cr.createSpec());
        return new PortObject[]{
            GaussianProcessRegressionPortObject.createPortObject(spec, gp, exec.createFileStore("model"))};
    }

    private GaussianProcessRegression<double[]> trainGP(final ExecutionContext exec,
        final BufferedDataTable trainingData) {
        CheckUtils.checkArgument(trainingData.size() > 0, "The input table is empty. Please provide data to learn on.");
        CheckUtils.checkArgument(trainingData.size() <= Integer.MAX_VALUE, "The input table contains too many rows.");

        exec.setMessage("Building regression model");

        final int targetColId = trainingData.getDataTableSpec().findColumnIndex(m_settings.getTargetColumn());
        final int size = (int)trainingData.size();

        final double[][] x = new double[size][trainingData.getDataTableSpec().getNumColumns() - 1];
        final double[] y = new double[size];
        int iteration = 0;

        for (final CloseableRowIterator rowI = trainingData.iterator(); rowI.hasNext();) {
            int count = 0;
            for (final DataCell cell : rowI.next()) {
                if (targetColId == count) {
                    y[iteration] = ((DoubleValue)cell).getDoubleValue();
                } else {
                    x[iteration][count] = ((DoubleValue)cell).getDoubleValue();
                }
                count++;
            }
            iteration++;
        }
        return new GaussianProcessRegression<>(x, y, new GaussianKernel(m_settings.getSigma()), m_settings.getLambda());
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        final DataTableSpec inSpec = (DataTableSpec)inSpecs[0];
        //List<String> inputCols = new ArrayList<>();
        final FilterResult includedColumns = m_settings.getIncludedColumns().applyTo(inSpec);
        //        for (String column : includedColumns.getIncludes()) {
        //            inputCols.add(column);
        //        }
        //        inputCols.remove(m_settings.getTargetColumn());
        //        if (inputCols.isEmpty()) {
        //            throw new InvalidSettingsException("At least one column must " + "be included.");
        //        }

        final ColumnRearranger cr = new ColumnRearranger(inSpec);
        cr.keepOnly(includedColumns.getIncludes());
        final GaussianProcessRegressionPortObjectSpec spec =
            new GaussianProcessRegressionPortObjectSpec(cr.createSpec());
        return new PortObjectSpec[]{spec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do
    }

    /**
     * A new configuration to store the settings. Also enables the type filter.
     *
     * @return A new {@link DataColumnSpecFilterConfiguration}.
     */
    static final DataColumnSpecFilterConfiguration createDCSFilterConfiguration() {
        return new DataColumnSpecFilterConfiguration("column-filter", new InputFilter<DataColumnSpec>() {
            @Override
            public boolean include(final DataColumnSpec name) {
                final DataType type = name.getType();
                return type.isCompatible(DoubleValue.class);
            }
        }, NameFilterConfiguration.FILTER_BY_NAMEPATTERN | DataColumnSpecFilterConfiguration.FILTER_BY_DATATYPE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // nothing to do
    }

}
