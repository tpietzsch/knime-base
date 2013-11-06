/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2013
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 * Created on 24.10.2013 by hofer
 */
package org.knime.base.node.mine.regression.predict2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.NominalValue;
import org.knime.core.data.container.AbstractCellFactory;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.pmml.PMMLPortObjectSpec;

/**
 * Abstraction for all predictro cell factories.
 *
 * @author Heiko Hofer
 */
public abstract class RegressionPredictorCellFactory extends AbstractCellFactory {

    /**
     * Creates the spec of the output if possible.
     *
     * @param portSpec the spec of the pmml input port
     * @param tableSpec the spec of the data input port
     * @param includeProbabilites add probabilities to the output
     * @return The spec of the output or null
     * @throws InvalidSettingsException when tableSpec and portSpec do not match
     */
    public static DataColumnSpec[] createColumnSpec(
            final PMMLPortObjectSpec portSpec,
            final DataTableSpec tableSpec,
            final boolean includeProbabilites) throws InvalidSettingsException {
        // Assertions
        if (portSpec.getTargetCols().isEmpty()) {
            throw new InvalidSettingsException("The general regression model"
                    + " does not specify a target column.");
        }

        for (DataColumnSpec learningColSpec : portSpec.getLearningCols()) {
            String learningCol = learningColSpec.getName();
            if (tableSpec.containsName(learningCol)) {
                DataColumnSpec colSpec = tableSpec.getColumnSpec(learningCol);
                if (learningColSpec.getType().isCompatible(NominalValue.class)
                    && !colSpec.getType().isCompatible(NominalValue.class)) {
                    throw new InvalidSettingsException("The column \""
                            + learningCol + "\" in the table of prediction "
                            + "is expected to be  compatible with "
                            + "\"NominalValue\".");
                } else if (learningColSpec.getType().isCompatible(
                        DoubleValue.class)
                        && !colSpec.getType().isCompatible(DoubleValue.class)) {
                    throw new InvalidSettingsException("The column \""
                            + learningCol + "\" in the table of prediction "
                            + "is expected to be numeric.");
                }
            } else {
                throw new InvalidSettingsException("The table for prediction "
                        + "does not contain the column \""
                        + learningCol + "\".");
            }
        }

        // The list of added columns
        List<DataColumnSpec> newColsSpec = new ArrayList<DataColumnSpec>();
        String targetCol = portSpec.getTargetFields().get(0);
        DataColumnSpec targetColSpec = portSpec.getDataTableSpec().getColumnSpec(targetCol);

        if (includeProbabilites && targetColSpec.getType().isCompatible(NominalValue.class)) {
            if (!targetColSpec.getDomain().hasValues()) {
                return null;
            }
            List<DataCell> targetCategories = new ArrayList<DataCell>();
            targetCategories.addAll(targetColSpec.getDomain().getValues());
            Collections.sort(targetCategories,
                targetColSpec.getType().getComparator());

            for (DataCell value : targetCategories) {
                String name = "P(" + targetCol + "=" + value.toString() + ")";
                String newColName =
                        DataTableSpec.getUniqueColumnName(tableSpec, name);
                DataColumnSpecCreator colSpecCreator =
                        new DataColumnSpecCreator(newColName, DoubleCell.TYPE);
                DataColumnDomainCreator domainCreator =
                        new DataColumnDomainCreator(new DoubleCell(0.0),
                                new DoubleCell(1.0));
                colSpecCreator.setDomain(domainCreator.createDomain());
                newColsSpec.add(colSpecCreator.createSpec());
            }
        }



        String oldTargetName = targetCol;
        if (tableSpec.containsName(oldTargetName)
                && !oldTargetName.toLowerCase().endsWith("(prediction)")) {
            oldTargetName = oldTargetName + " (prediction)";
        }
        String newTargetColName = DataTableSpec.getUniqueColumnName(tableSpec, oldTargetName);

        DataColumnSpecCreator targetColSpecCreator =
                new DataColumnSpecCreator(newTargetColName, targetColSpec.getType());
        DataColumnDomainCreator targetDomainCreator = new DataColumnDomainCreator(targetColSpec.getDomain());
        targetColSpecCreator.setDomain(targetDomainCreator.createDomain());
        newColsSpec.add(targetColSpecCreator.createSpec());

        return newColsSpec.toArray(new DataColumnSpec[0]);
    }

    /**
     * This constructor should be used during the configure phase of a node.
     * The created instance will give a valid spec of the output but cannot
     * be used to compute the cells.
     *
     * @param portSpec the spec of the pmml input port
     * @param tableSpec the spec of the data input port
     * @param includeProbabilites add probabilities to the output
     * @throws InvalidSettingsException when tableSpec and portSpec do not match
     */
    public RegressionPredictorCellFactory(final PMMLPortObjectSpec portSpec,
            final DataTableSpec tableSpec,
            final boolean includeProbabilites
            ) throws InvalidSettingsException {
        super(createColumnSpec(portSpec, tableSpec, includeProbabilites));
    }
}