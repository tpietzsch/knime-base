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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public class Analyser {

    private static Function<String, Boolean> intMapper = s -> {
        try {
            Integer.parseInt(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    };

    private static Function<String, Boolean> doubleMapper = s -> {
        try {
            Double.parseDouble(s);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    };

    public DataTableSpec analyze(final InputStream iS, final long numOfRecsToRead) {
        final CsvParserSettings s = new CsvParserSettings();
//        s.detectFormatAutomatically('\n');
        return analyze(iS, s, numOfRecsToRead);
    }

    public DataTableSpec analyze(final InputStream iS, final CsvParserSettings settings) {
        return analyze(iS, settings, -1);
    }

    DataTableSpec analyze(final InputStream iS, final CsvParserSettings settings, final long numOfRecsToRead) {

        settings.setNumberOfRecordsToRead(numOfRecsToRead);
        final CsvParser parser = new CsvParser(settings);

        parser.beginParsing(iS);
        String[] row = parser.parseNext();

        final ArrayList<DataType> cTypes = new ArrayList<>();
        final ArrayList<Integer> toGuess = new ArrayList<>();
        // the file only contains the header
        if (row == null) {
            throw new IllegalArgumentException("The file does not contain any rows"
                + (parser.getContext().headers() != null ? " except for the header" : ""));
        }
        do {
            final int rowLength = row.length;
            while (rowLength > cTypes.size()) {
                toGuess.add(cTypes.size());
                cTypes.add(IntCell.TYPE);
            }
            ListIterator<Integer> toGuessIter = toGuess.listIterator();
            // TODO: missing that we can have values that represent missings
            while (toGuessIter.hasNext()) {
                int pos = toGuessIter.next();
                DataType curType = cTypes.get(pos);
                final String curVal = row[pos];
                if (curVal != null) {
                    if (curType == IntCell.TYPE) {
                        if (!intMapper.apply(curVal)) {
                            curType = DoubleCell.TYPE;
                            cTypes.set(pos, curType);
                        }
                    }
                    if (curType == DoubleCell.TYPE) {
                        if (!doubleMapper.apply(curVal)) {
                            curType = StringCell.TYPE;
                            cTypes.set(pos, curType);
                        }
                    }
                }
                if (curType == StringCell.TYPE) {
                    toGuessIter.remove();
                }
            }
        } while ((row = parser.parseNext()) != null);

        final String[] header;
        if (parser.getContext().headers() != null) {
            header = parser.getContext().headers();
        } else {
            header =
                IntStream.range(0, cTypes.size()).mapToObj(i -> i > 0 ? "Col" + i : "Row ID").toArray(String[]::new);
        }
        final DataColumnSpec[] cSpecs = IntStream.range(0, cTypes.size())
            .mapToObj(i -> new DataColumnSpecCreator(header[i], cTypes.get(i)).createSpec())
            .toArray(DataColumnSpec[]::new);

        return new DataTableSpec(cSpecs);
    }

    /**
     * @param object
     */
    private void checkType(final Consumer c, final String val) {
        c.accept(val);
    }

}
