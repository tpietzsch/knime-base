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

import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import com.univocity.parsers.csv.CsvFormat;

/**
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public class FormatSettings {

    private final SettingsModelString m_delimiterModel = new SettingsModelString("row_delimiter", "\n");

    private final SettingsModelString m_lineSeparatorModel = new SettingsModelString("line_seperator", ",");

    private final SettingsModelString m_quoteModel = new SettingsModelString("quote_character", "\"");

    private final SettingsModelString m_quoteEscapeModel = new SettingsModelString("quote_escape_character", "\"");

    private final SettingsModelString m_escapeQuoteEscapingModel =
        new SettingsModelString("escape_quote_escape_character", "\0");

    private final SettingsModelString m_commentModel = new SettingsModelString("comment_character", "#");

    private final SettingsModel[] m_models = new SettingsModel[]{m_delimiterModel, m_lineSeparatorModel, m_quoteModel,
        m_quoteEscapeModel, m_escapeQuoteEscapingModel, m_commentModel};

    public SettingsModelString getDelimiterModel() {
        return m_delimiterModel;
    }

    public SettingsModelString getLineSeparatorModel() {
        return m_lineSeparatorModel;
    }

    public SettingsModelString getQuoteModel() {
        return m_quoteModel;
    }

    public SettingsModelString getQuoteEscapeModel() {
        return m_quoteEscapeModel;
    }

    public SettingsModelString getEscapeQuoteEscapingModel() {
        return m_escapeQuoteEscapingModel;
    }

    public SettingsModelString getCommentModel() {
        return m_commentModel;
    }

    public CsvFormat getCsvFormat() throws InvalidSettingsException {
        final CsvFormat csv = new CsvFormat();
        csv.setDelimiter(m_delimiterModel.getStringValue());
        csv.setLineSeparator(m_lineSeparatorModel.getStringValue());
        csv.setQuote(getChar(m_quoteModel));
        csv.setQuoteEscape(getChar(m_quoteEscapeModel));
        csv.setCharToEscapeQuoteEscaping(getChar(m_escapeQuoteEscapingModel));
        csv.setComment(getChar(m_commentModel));
        return csv;
    }

    private char getChar(final SettingsModelString s) throws InvalidSettingsException {
        final char[] chars = s.getStringValue().toCharArray();
        if (chars.length != 1) {
            throw new InvalidSettingsException("Can only store characters not strings in.");
        }
        return chars[0];
    }

    void saveSettingsTo(final NodeSettingsWO settings) {
        Arrays.stream(m_models).forEach(m -> m.saveSettingsTo(settings));
    }

    void loadSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        for (final SettingsModel m : m_models) {
            m.loadSettingsFrom(settings);
        }
    }

}
