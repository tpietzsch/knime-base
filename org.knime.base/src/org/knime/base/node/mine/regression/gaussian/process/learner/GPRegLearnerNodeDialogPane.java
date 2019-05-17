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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.ColumnSelectionPanel;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

/**
 * Dialog for the logistic regression learner.
 *
 * @author Heiko Hofer
 * @author Gabor Bakos
 * @author Adrian Nembach, KNIME.com
 * @since 3.1
 */
public final class GPRegLearnerNodeDialogPane extends NodeDialogPane {

    private static int NUMBER_INPUT_FIELD_COLS = 10;

    private final DataColumnSpecFilterPanel m_filterPanel;

    private final ColumnSelectionPanel m_selectionPanel;

    private final JTextField m_lambdaField;

    private final JTextField m_sigmaField;

    /**
     * Create new dialog for linear regression model.
     */
    public GPRegLearnerNodeDialogPane() {
        super();
        // instantiate members
        @SuppressWarnings("unchecked")
        final ColumnSelectionPanel columnSelectionPanel =
            new ColumnSelectionPanel(new EmptyBorder(0, 0, 0, 0), DoubleValue.class);
        m_selectionPanel = columnSelectionPanel;

        m_filterPanel = new DataColumnSpecFilterPanel(false);

        m_lambdaField = new JTextField(Double.toString(GPRegLearnerSettings.DEFAULT_LAMBDA), NUMBER_INPUT_FIELD_COLS);
        m_sigmaField = new JTextField(Double.toString(GPRegLearnerSettings.DEFAULT_SIGMA), NUMBER_INPUT_FIELD_COLS);

        // register listeners
        m_selectionPanel.addActionListener(e -> updateFilterPanel());

        // create tabs
        final JPanel settingsPanel = createSettingsPanel();
        addTab("Settings", settingsPanel);
    }

    private void updateFilterPanel() {
        final DataColumnSpec targetSpec = m_selectionPanel.getSelectedColumnAsSpec();
        m_filterPanel.resetHiding();
        m_filterPanel.hideNames(targetSpec);
    }

    private JPanel createTerminationConditionsPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = makeSettingsConstraints();

        panel.add(new JLabel("Parameter:"), c);
        c.gridx++;
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Sigma:"), c);
        c.gridx++;
        panel.add(m_sigmaField, c);
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Lambda:"), c);
        c.gridx++;
        panel.add(m_lambdaField, c);

        return panel;
    }

    private static GridBagConstraints makeSettingsConstraints() {
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        c.insets = new Insets(5, 5, 0, 0);
        return c;
    }

    private JPanel createSettingsPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        c.insets = new Insets(5, 5, 0, 0);
        final JPanel northPanel = createTargetOptionsPanel();
        northPanel.setBorder(BorderFactory.createTitledBorder("Target"));
        panel.add(northPanel, c);
        c.gridy++;

        final JPanel centerPanel = createIncludesPanel();
        centerPanel.setBorder(BorderFactory.createTitledBorder("Feature selection"));
        panel.add(centerPanel, c);
        c.gridy++;

        final JPanel southPanel = createParameterPanel();
        southPanel.setBorder(BorderFactory.createTitledBorder("Parameter"));
        panel.add(southPanel, c);
        return panel;
    }

    private JPanel createParameterPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        final GridBagConstraints c = makeSettingsConstraints();
        //c.weightx = 1;
        p.add(new JLabel("Sigma:"), c);
        c.gridx++;
        p.add(m_sigmaField, c);
        c.gridx = 0;
        c.gridy++;
        p.add(new JLabel("Lambda:"), c);
        c.gridx++;
        p.add(m_lambdaField, c);

        return p;
    }

    /**
     * Create options panel for the target.
     */
    private JPanel createTargetOptionsPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        final GridBagConstraints c = makeSettingsConstraints();

        p.add(new JLabel("Target column:"), c);

        c.gridx++;

        p.add(m_selectionPanel, c);

        return p;
    }

    /**
     * Create options panel for the included columns.
     */
    private JPanel createIncludesPanel() {
        final JPanel p = new JPanel(new GridBagLayout());
        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        c.insets = new Insets(5, 5, 0, 0);

        p.add(m_filterPanel, c);

        return p;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO s, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        final GPRegLearnerSettings settings = new GPRegLearnerSettings();
        final DataTableSpec inSpec = (DataTableSpec)specs[0];
        settings.loadSettingsForDialog(s, inSpec);
        final DataColumnSpecFilterConfiguration config = settings.getIncludedColumns();
        m_filterPanel.loadConfiguration(config, inSpec);

        final String target = settings.getTargetColumn();

        m_selectionPanel.update(inSpec, target);
        //m_filterPanel.updateWithNewConfiguration(config); is not enough, we have to reload things as selection update might change the UI
        m_filterPanel.loadConfiguration(config, inSpec);
        // must hide the target from filter panel
        // updating m_filterPanel first does not work as the first
        // element in the spec will always be in the exclude list.
        String selected = m_selectionPanel.getSelectedColumn();
        if (null == selected) {
            for (final DataColumnSpec colSpec : inSpec) {
                if (colSpec.getType().isCompatible(DoubleValue.class)) {
                    selected = colSpec.getName();
                    break;
                }
            }
        }
        if (selected != null) {
            final DataColumnSpec colSpec = inSpec.getColumnSpec(selected);
            m_filterPanel.hideNames(colSpec);
        }

        final double lambda = settings.getLambda();
        m_lambdaField.setText(Double.toString(lambda));
        final double sigma = settings.getSigma();
        m_sigmaField.setText(Double.toString(sigma));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO s) throws InvalidSettingsException {
        final GPRegLearnerSettings settings = new GPRegLearnerSettings();
        final DataColumnSpecFilterConfiguration config = GPRegLearnerNodeModel.createDCSFilterConfiguration();
        m_filterPanel.saveConfiguration(config);
        settings.setIncludedColumns(config);
        settings.setTargetColumn(m_selectionPanel.getSelectedColumn());
        try {
            final String str = m_lambdaField.getText();
            final double epsilon = Double.valueOf(str);
            settings.setLambda(epsilon);
        } catch (final NumberFormatException nfe) {
            throw new InvalidSettingsException("Please provide a valid value for lambda.");
        }
        try {
            final String str = m_sigmaField.getText();
            final double epsilon = Double.valueOf(str);
            settings.setSigma(epsilon);
        } catch (final NumberFormatException nfe) {
            throw new InvalidSettingsException("Please provide a valid value for sigma.");
        }
        settings.validate();

        settings.saveSettings(s);
    }
}
