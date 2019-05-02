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
 *   Mar 12, 2019 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.base.node.io.csvreader.strict;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;

/**
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public class SettingsModelChar extends SettingsModel implements SettingsModelFlowVariableCompatible {

    private char m_value;

    private final String m_key;

    SettingsModelChar(final String key, final char defValue) {
        m_key = key;
        m_value = defValue;
    }

    private SettingsModelChar(final SettingsModelChar orig) {
        this(orig.m_key, orig.m_value);
    }

    @Override
    public String getKey() {
        return m_key;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Type getFlowVariableType() {
        // TODO: we need a char type
        return FlowVariable.Type.STRING;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SettingsModelChar createClone() {
        return new SettingsModelChar(this);
    }

    @Override
    protected String getModelTypeID() {
        return "SMID_char";
    }

    @Override
    protected String getConfigName() {
        return m_key;
    }

    /**
     * Sets the new character. If the new values differs from the current value listeners get notified.
     *
     * @param c the {@code char} to set
     */
    public void setChar(final char c) {
        if (m_value != c) {
            m_value = c;
            notifyChangeListeners();
        }
    }

    /**
     * Returns the currently stored character.
     *
     * @return the currently stored character
     */
    public char getChar() {
        return m_value;
    }

    @Override
    protected void loadSettingsForDialog(final NodeSettingsRO settings, final PortObjectSpec[] specs)
        throws NotConfigurableException {
        setChar(settings.getChar(m_key, m_value));
    }

    @Override
    protected void saveSettingsForDialog(final NodeSettingsWO settings) throws InvalidSettingsException {
        saveSettingsForModel(settings);
    }

    @Override
    protected void validateSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        settings.getChar(m_key);
    }

    @Override
    protected void loadSettingsForModel(final NodeSettingsRO settings) throws InvalidSettingsException {
        try {
            setChar(settings.getChar(m_key));
        } catch (final IllegalArgumentException iae) {
            throw new InvalidSettingsException(iae.getMessage());
        }
    }

    @Override
    protected void saveSettingsForModel(final NodeSettingsWO settings) {
        settings.addChar(m_key, m_value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " ('" + m_key + "')";
    }

}
