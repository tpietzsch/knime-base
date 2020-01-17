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
 *   Jan 15, 2020 (Simon Schmid, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.base.node.io.filereaderTEST;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.csvreader.CsvReader;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.ResultIterator;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 *
 * @author Simon Schmid, KNIME GmbH, Konstanz, Germany
 */
public class FileReaderTestNodeModel extends NodeModel {

    FileReaderTestNodeModel() {
        super(0, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        return null;
    }

    long start;

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        File csvFile = new File("/home/simon/Desktop/datasets/iris.csv");
        csvFile = new File("/home/simon/Desktop/datasets/adult.csv");
        csvFile = new File("/home/simon/Desktop/datasets/test_dataset.csv");
        csvFile = new File("/home/simon/Desktop/datasets/airlines_1mio.csv");
        csvFile = new File("/home/simon/Desktop/datasets/creditcard.csv");
        start = System.currentTimeMillis();
        BufferedDataTable[] out = univocity(csvFile, exec);
        System.out.println("total: " + (System.currentTimeMillis() - start));
        return out;
    }

    private BufferedDataTable[] jackson(final File csvFile, final ExecutionContext exec) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withColumnSeparator(',');
        mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
        MappingIterator<String[]> it = mapper.readerFor(String[].class).with(schema).readValues(csvFile);
        String[] header = it.next();
        DataColumnSpec[] colSpecs = new DataColumnSpec[header.length];
        for (int i = 0; i < header.length; i++) {
            colSpecs[i] = new DataColumnSpecCreator(header[i], StringCell.TYPE).createSpec();
        }
        BufferedDataContainer createDataContainer = exec.createDataContainer(new DataTableSpec(colSpecs));
        long idx = 0;
        while (it.hasNext()) {
            String[] row = it.next();
            createDataContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(idx++), row));
        }
        createDataContainer.close();
        it.close();
        return new BufferedDataTable[]{createDataContainer.getTable()};
    }

    private BufferedDataTable[] javaCSV(final File csvFile, final ExecutionContext exec) throws IOException {
        CsvReader csvReader = new CsvReader("arg0");
        // TODO
        return new BufferedDataTable[]{};
    }

    private BufferedDataTable[] openCSV(final File csvFile, final ExecutionContext exec)
        throws IOException, CsvException {
        FileReader fileReader = new FileReader(csvFile);
        CSVParser csvParser = new CSVParserBuilder().withSeparator(',').build();
        CSVReaderBuilder csvReaderBuilder = new CSVReaderBuilder(fileReader).withCSVParser(csvParser);
        CSVReader csvReader = csvReaderBuilder.build();
        List<String[]> readAll = csvReader.readAll();
        csvReader.close();
        fileReader.close();

        String[] header = readAll.remove(0);
        DataColumnSpec[] colSpecs = new DataColumnSpec[header.length];
        for (int i = 0; i < header.length; i++) {
            colSpecs[i] = new DataColumnSpecCreator(header[i], StringCell.TYPE).createSpec();
        }
        BufferedDataContainer createDataContainer = exec.createDataContainer(new DataTableSpec(colSpecs));
        long idx = 0;
        for (String[] row : readAll) {
            // and voila, column values in an array. Works with Lists as well
            createDataContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(idx++), row));
        }
        createDataContainer.close();
        return new BufferedDataTable[]{createDataContainer.getTable()};
    }

    private BufferedDataTable[] apacheCommonsCSV(final File csvFile, final ExecutionContext exec)
        throws IOException, CsvException {
        FileReader fileReader = new FileReader(csvFile);

        CSVFormat format = CSVFormat.RFC4180;
        format = format.withRecordSeparator("\\n");
        //        format = format.withAllowDuplicateHeaderNames(true);
        org.apache.commons.csv.CSVParser csvParser2 = new org.apache.commons.csv.CSVParser(fileReader, format);

        Iterator<CSVRecord> it = csvParser2.iterator();

        CSVRecord header = it.next();
        DataColumnSpec[] colSpecs = new DataColumnSpec[header.size()];
        for (int i = 0; i < header.size(); i++) {
            colSpecs[i] = new DataColumnSpecCreator(header.get(i), StringCell.TYPE).createSpec();
        }
        BufferedDataContainer createDataContainer = exec.createDataContainer(new DataTableSpec(colSpecs));
        long idx = 0;
        while (it.hasNext()) {
            CSVRecord record = it.next();
            String[] row = new String[record.size()];
            for (int i = 0; i < row.length; i++) {
                row[i] = record.get(i);
            }
            // and voila, column values in an array. Works with Lists as well
            createDataContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(idx++), row));
        }
        createDataContainer.close();
        return new BufferedDataTable[]{createDataContainer.getTable()};
    }

    private BufferedDataTable[] superCSV(final File csvFile, final ExecutionContext exec)
        throws IOException, CsvException {
        FileReader fileReader = new FileReader(csvFile);

        CsvPreference pref = new CsvPreference.Builder('"', ',', "asd").build();
        ICsvListReader listReader = new CsvListReader(fileReader, pref);
        List<String[]> rows = new ArrayList<String[]>();
        try {
            List<String> row = null;
            while ((row = listReader.read()) != null) {
                rows.add(row.toArray(new String[0]));
            }

        } finally {
            if (listReader != null) {
                listReader.close();
            }
        }

        String[] header = rows.remove(0);
        DataColumnSpec[] colSpecs = new DataColumnSpec[header.length];
        for (int i = 0; i < header.length; i++) {
            colSpecs[i] = new DataColumnSpecCreator(header[i], StringCell.TYPE).createSpec();
        }
        BufferedDataContainer createDataContainer = exec.createDataContainer(new DataTableSpec(colSpecs));
        long idx = 0;
        for (String[] row : rows) {
            // and voila, column values in an array. Works with Lists as well
            createDataContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(idx++), row));
        }
        createDataContainer.close();
        return new BufferedDataTable[]{createDataContainer.getTable()};
    }

    private BufferedDataTable[] univocity(final File csvFile, final ExecutionContext exec)
        throws IOException, CsvException {
        FileReader fileReader = new FileReader(csvFile);
        int numLines;
        try (FileInputStream stream = new FileInputStream(csvFile)) {
            numLines = countLinesNew(stream);
        }
        FileInputStream fileInputStream = new FileInputStream(csvFile);
        fileInputStream.skip(numLines);
        FileChannel channel = fileInputStream.getChannel();
        //        fileReader.skip(numLines / 2);
        long position2 = channel.position();
        long size = channel.size();
        MappedByteBuffer map = channel.map(MapMode.READ_ONLY, 0, channel.size() / 2);
        ByteBufferBackedInputStream byteBufferBackedInputStream = new ByteBufferBackedInputStream(map);
        //        long position = channel.position();
        //        long size = channel.size();
        //        long newPosition = size / 2;
        //        FileChannel channel1 = channel.truncate(newPosition-1);
        //        FileChannel channel2 = channel.position(newPosition);
        CsvParserSettings settings = new CsvParserSettings();
        settings.setNullValue("null");
        com.univocity.parsers.csv.CsvParser parser = new com.univocity.parsers.csv.CsvParser(settings);
        ResultIterator<String[], ParsingContext> it = parser.iterate(byteBufferBackedInputStream).iterator();
        String[] header = it.next();
        int length = header.length;
        DataColumnSpec[] colSpecs = new DataColumnSpec[header.length];
        for (int i = 0; i < header.length; i++) {
            colSpecs[i] = new DataColumnSpecCreator(header[i], StringCell.TYPE).createSpec();
        }
        BufferedDataContainer createDataContainer = exec.createDataContainer(new DataTableSpec(colSpecs));
        long idx = 0;
        while (it.hasNext()) {
            String[] row = it.next();
            if (row.length != length) {
                System.out.println("ASDASD");
                continue;
            }
            createDataContainer.addRowToTable(new DefaultRow(RowKey.createRowKey(idx++), row));
        }
        createDataContainer.close();
        byteBufferBackedInputStream.close();
        channel.close();
        return new BufferedDataTable[]{createDataContainer.getTable()};
    }

    // https://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
    private static int countLinesNew(final FileInputStream file) throws IOException {
        try (InputStream is = new BufferedInputStream(file)) {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                // bail out if nothing to read
                return 0;
            }

            // make it easy for the optimizer to tune this loop
            int count = 0;
            while (readChars == 1024) {
                for (int i = 0; i < 1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            // count remaining characters
            while (readChars != -1) {
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }

            return count == 0 ? 1 : count;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File nodeInternDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {

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
        // TODO Auto-generated method stub

    }

}
