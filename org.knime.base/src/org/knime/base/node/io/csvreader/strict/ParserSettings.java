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
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelLong;
import org.knime.core.node.defaultnodesettings.SettingsModelLongBounded;

import com.univocity.parsers.csv.CsvFormat;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.UnescapedQuoteHandling;

/**
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public class ParserSettings {

    private final SettingsModelBoolean m_escapeUnquotedValuesModel =
        new SettingsModelBoolean("escape_unquoted_values", false);

    private final SettingsModelBoolean m_columnHeadersModel = new SettingsModelBoolean("contains_column_headers", true);

    private final SettingsModelBoolean m_ignoreLeadingWhiteSpacesModel =
        new SettingsModelBoolean("ignore_leading_whitespaces", true);

    private final SettingsModelBoolean m_ignoreLeadingWhiteSpacesInQuotesModel =
        new SettingsModelBoolean("ignore_leading_whitespaces_in_quotes", true);

    private final SettingsModelBoolean m_ignoreTrailingWhiteSpacesModel =
        new SettingsModelBoolean("ignore_trailing_whitespaces", true);

    private final SettingsModelBoolean m_ignoreTrailingWhiteSpacesInQuotesModel =
        new SettingsModelBoolean("ignore_trailing_whitespaces_in_quotes", true);

    private final SettingsModelBoolean m_keepEscapeSequencesModel =
        new SettingsModelBoolean("keep_escape_sequences", false);

    private final SettingsModelBoolean m_keepQuotesModel = new SettingsModelBoolean("keep_quotes", false);

    private final SettingsModelBoolean m_normalizeLineEndingsWithinQuotesModel =
        new SettingsModelBoolean("normalize_line_endings_within_quotes", true);

    private final SettingsModelLong m_recordsToReadModel =
        new SettingsModelLongBounded("number_of_records_to_read", -1, -1, Long.MAX_VALUE);

    private final SettingsModelLong m_rowsToSkipModel =
        new SettingsModelLongBounded("number_of_rows_to_skip", 0, 0, Long.MAX_VALUE);

    private final SettingsModelBoolean m_skipBitsAsWhitespaceModel =
        new SettingsModelBoolean("skip_bits_as_whitespace", true);

    private SettingsModel[] m_models = new SettingsModel[]{m_escapeUnquotedValuesModel, m_columnHeadersModel,
        m_ignoreLeadingWhiteSpacesModel, m_ignoreLeadingWhiteSpacesInQuotesModel, m_ignoreTrailingWhiteSpacesModel,
        m_ignoreTrailingWhiteSpacesInQuotesModel, m_keepEscapeSequencesModel, m_keepQuotesModel,
        m_normalizeLineEndingsWithinQuotesModel, m_recordsToReadModel, m_rowsToSkipModel, m_skipBitsAsWhitespaceModel};

    CsvParserSettings getParserSettings(final CsvFormat format) {
        final CsvParserSettings settings = getParserSettings();
        settings.setFormat(format);
        return settings;
    }

    public SettingsModelBoolean getEscapeUnquotedValuesModel() {
        return m_escapeUnquotedValuesModel;
    }

    public SettingsModelBoolean getColumHeadersModel() {
        return m_columnHeadersModel;
    }

    public SettingsModelLong getRecordsToReadModel() {
        return m_recordsToReadModel;
    }

    public SettingsModelLong getRowsToSkipModel() {
        return m_rowsToSkipModel;
    }

    CsvParserSettings getParserSettings() {
        CsvParserSettings s = new CsvParserSettings();
        s.setEscapeUnquotedValues(m_escapeUnquotedValuesModel.getBooleanValue());
        s.setHeaderExtractionEnabled(m_columnHeadersModel.getBooleanValue());
        s.setIgnoreLeadingWhitespaces(m_ignoreLeadingWhiteSpacesModel.getBooleanValue());
        s.setIgnoreLeadingWhitespacesInQuotes(m_ignoreLeadingWhiteSpacesInQuotesModel.getBooleanValue());
        s.setIgnoreTrailingWhitespaces(m_ignoreTrailingWhiteSpacesModel.getBooleanValue());
        s.setIgnoreTrailingWhitespacesInQuotes(m_ignoreTrailingWhiteSpacesInQuotesModel.getBooleanValue());
        s.setKeepEscapeSequences(m_keepEscapeSequencesModel.getBooleanValue());
        s.setKeepQuotes(m_keepQuotesModel.getBooleanValue());
        s.setNormalizeLineEndingsWithinQuotes(m_normalizeLineEndingsWithinQuotesModel.getBooleanValue());
        s.setNumberOfRecordsToRead(m_recordsToReadModel.getLongValue());
        s.setNumberOfRowsToSkip(m_rowsToSkipModel.getLongValue());
        s.setSkipBitsAsWhitespace(m_skipBitsAsWhitespaceModel.getBooleanValue());
        // TODO: this has to be configurable
        s.setUnescapedQuoteHandling(UnescapedQuoteHandling.STOP_AT_CLOSING_QUOTE);
        return s;
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
