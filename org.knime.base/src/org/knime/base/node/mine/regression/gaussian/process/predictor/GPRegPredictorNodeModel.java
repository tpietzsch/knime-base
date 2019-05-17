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
package org.knime.base.node.mine.regression.gaussian.process.predictor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.base.node.mine.regression.gaussian.process.learner.GaussianProcessRegression;
import org.knime.base.node.mine.regression.gaussian.process.model.GaussianProcessRegressionPortObject;
import org.knime.base.node.mine.regression.gaussian.process.model.GaussianProcessRegressionPortObjectSpec;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataTableSpecCreator;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
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

/**
 */
public final class GPRegPredictorNodeModel extends NodeModel {

    private static class GPCellFactory extends AbstractCellFactory {

        private final int[] m_featureColumnIndices;

        private final boolean m_appendVariance;

        private final GaussianProcessRegression<double[]> m_gpModel;

        /**
         * @param gpModel
         * @param gpModel
         *
         */
        GPCellFactory(final String predictionColumnName, final boolean appendVariance,
            final GaussianProcessRegressionPortObject gpPo, final DataTableSpec tableSpec,
            final GaussianProcessRegression<double[]> gpModel) {
            super(createPredictionColumnSpecs(predictionColumnName, appendVariance));
            final GaussianProcessRegressionPortObjectSpec spec =
                (GaussianProcessRegressionPortObjectSpec)gpPo.getSpec();

            final List<String> fColNames = new ArrayList<>();
            // TODO check if feature mapping is the same
            spec.getFeatureSpec().forEach(col -> {
                if (tableSpec.containsName(col.getName())
                    && tableSpec.getColumnSpec(col.getName()).getType().equals(col.getType())) {
                    fColNames.add(col.getName());
                } else {
                    throw new IllegalArgumentException("Some feature columns are missing.");
                }
            });
            m_gpModel = gpModel;
            m_appendVariance = appendVariance;
            m_featureColumnIndices = tableSpec.columnsToIndices(fColNames.toArray(new String[fColNames.size()]));
        }

        @Override
        public DataCell[] getCells(final DataRow row) {
            final double[] x = new double[m_featureColumnIndices.length];
            for (int i = 0; i < m_featureColumnIndices.length; i++) {
                x[i] = ((DoubleCell)row.getCell(i)).getDoubleValue();
            }
            final DataCell[] newCells = new DataCell[m_appendVariance ? 2 : 1];
            if (m_appendVariance) {
                final double[] result = m_gpModel.predictWithVariance(x);
                newCells[0] = new DoubleCell(result[0]);
                newCells[1] = new DoubleCell(result[1]);
            } else {
                newCells[0] = new DoubleCell(m_gpModel.predict(x));
            }
            return newCells;
        }
    }

    private static DataColumnSpec[] createPredictionColumnSpecs(final String predictionColumnName,
        final boolean appendVariance) {
        final DataColumnSpec predictionSpec =
            new DataColumnSpecCreator(predictionColumnName, DoubleCell.TYPE).createSpec();
        if (appendVariance) {
            final DataColumnSpec varianceSpec =
                new DataColumnSpecCreator(predictionColumnName + " (Variance)", DoubleCell.TYPE).createSpec();
            return new DataColumnSpec[]{predictionSpec, varianceSpec};
        } else {
            return new DataColumnSpec[]{predictionSpec};
        }

    }

    /**
     * Inits a new node model
     */
    public GPRegPredictorNodeModel() {
        super(new PortType[]{PortObject.TYPE, BufferedDataTable.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        final GaussianProcessRegressionPortObject gpPo = (GaussianProcessRegressionPortObject)inObjects[0];
        final BufferedDataTable data = (BufferedDataTable)inObjects[1];
        final DataTableSpec spec = data.getSpec();
        final GaussianProcessRegression<double[]> gpModel = gpPo.getGPModel();

        final GPCellFactory gpCellFactory = new GPCellFactory("Prediction", true, gpPo, spec, gpModel);
        final ColumnRearranger cr = new ColumnRearranger(spec);
        cr.append(gpCellFactory);
        return new PortObject[]{exec.createColumnRearrangeTable(data, cr, exec)};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {
        return new PortObjectSpec[]{createOutputSpec((DataTableSpec)inSpecs[1])};
    }

    private static DataTableSpec createOutputSpec(final DataTableSpec inputSpec) {
        final DataTableSpecCreator creator = new DataTableSpecCreator(inputSpec);
        creator.addColumns(createPredictionColumnSpecs("Prediction", true));
        return creator.createSpec();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to do

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        // TODO Auto-generated method stub

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {

    }

}
