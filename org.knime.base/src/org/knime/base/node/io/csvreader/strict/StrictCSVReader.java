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
 *   Mar 5, 2019 (Mark Ortmann, KNIME GmbH, Berlin, Germany): created
 */
package org.knime.base.node.io.csvreader.strict;

import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory.FromString;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

/**
 * Required options: check for row headers; same prefix and incrementing suffix, see FileAnalyzer#checkRowHeader
 *
 * @author Mark Ortmann, KNIME GmbH, Berlin, Germany
 */
public class StrictCSVReader {

    /**
     * @param openStreamWithTimeout
     * @return
     * @throws CanceledExecutionException
     */
    public BufferedDataTable read(final ExecutionContext exec, final InputStream openStreamWithTimeout,
        final DataTableSpec spec) throws CanceledExecutionException {
        final CsvParserSettings settings = new CsvParserSettings();
        settings.setReadInputOnSeparateThread(false);



        final DataContainer cont = exec.createDataContainer(spec);
//        final ArrayList<Pair<Integer,Long>> time = new ArrayList<>(8000009);

        final int maxSize = spec.getNumColumns();
//        final int cSize = DataContainerSettings.getDefault().getAsyncCacheSize();
        final FromString[] factories = IntStream.range(0, maxSize)
                .mapToObj(i -> getFactory(spec.getColumnSpec(i).getType(), exec)).toArray(FromString[]::new);
        int pos = 0;
        final DataCell[] c = new DataCell[maxSize];
        Arrays.fill(c, DataType.getMissingCell());


        final long start = System.currentTimeMillis();
        final long sCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(openStreamWithTimeout);
        long totalTime = 0;


        int i;
        String[] r;
//        int regularTime = 0;
//        int offerTime = 0;
//        double offer =0;
//        double regular =0;
//        long iS = System.currentTimeMillis();


        while ((r = parser.parseNext()) != null) {
//            exec.checkCanceled();
            ++pos;
            for (i = 0; i < r.length; i++) {
                c[i] = factories[i].createCell(r[i]);
            }
            while (i < maxSize) {
                c[i++] = DataType.getMissingCell();
            }
            final DataRow row = new DefaultRow(RowKey.createRowKey(pos), c);
            cont.addRowToTable(row);

//            for(int j = 0 ; j <10; j++) {
//                totalTime +=ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
//                totalTime +=ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
//            if(pos % 100000 == 0)
//             {
//                long[] allThreadIds = ManagementFactory.getThreadMXBean().getAllThreadIds();
//                for(final long id : allThreadIds) {
//                    System.out.println("\n\n==== ID ====");
//                    System.out.println(ManagementFactory.getThreadMXBean().getThreadInfo(id));
//                }
//                System.out.println(ManagementFactory.getThreadMXBean().getAllThreadIds());
//            }
//            final long t;
//            if(pos % cSize == 0) {
//                t = System.currentTimeMillis() -  iS;
//                offerTime +=t;
//                ++offer;
//            } else {
//                t = System.currentTimeMillis() - iS;
//                regularTime += t;
//                ++regular;
//            }
//            time.add(new Pair<Integer,Long>(pos,t));
//            iS = System.currentTimeMillis();
//            }
        }
        System.out.println("\nThe cpu time is\t:" + ((ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime()-sCpu)));
//        System.out.println("\nRegular row generator:\t" + regularTime / regular / 1000d);
//        System.out.println("\nOffer row generator:\t" + offerTime / offer / 1000d);
//        System.out.println("Their combination:\t" + (offerTime + regularTime)/ 1000d);
        //        Record r;
        //        while ((r = parser.parseNextRecord()) != null) {
        //            //            exec.checkCanceled();
        //            ++pos;
        //            Arrays.fill(c, DataType.getMissingCell());
        //                        for (i = 0; i < r.getValues().length; i++) {
        //                            DoubleCellFactory.create(r.getDouble(i));
        ////                            factories[i].createCell(r[i]);
        //                        }
        //                        while (i < maxSize) {
        //                            c[i++] = DataType.getMissingCell();
        //                        }
        ////            cont.addRowToTable(new DefaultRow(RowKey.createRowKey(pos), c));
        //        }
        System.out.println("\nReader time without close:\t" + ((System.currentTimeMillis() - start) / 1000d));
//        cont.getBuffer().clear();
        cont.close();

        System.out.println("Reader time with close:\t" + ((System.currentTimeMillis() - start) / 1000d));
//        Collections.sort(time,new Comparator<Pair<Integer,Long>>() {
//
//            @Override
//            public int compare(final Pair<Integer,Long> o1, final Pair<Integer,Long> o2) {
//                return Long.compare(o2.getSecond(), o1.getSecond());
//            }
//        });
//        for(int k =0 ; k < 100; k++) {
//            System.out.println(time.get(k));
//        }

        return exec.createBufferedDataTable(cont.getTable(), exec);
    }

    private static final FromString getFactory(final DataType type, final ExecutionContext exec) {
        if (type != null) {
            org.knime.core.data.DataCellFactory cellFactory = type.getCellFactory(exec).orElseThrow(
                () -> new IllegalArgumentException("No data cell factory for data type '" + type + "' found"));
            return (FromString)cellFactory;
        }
        throw new NullPointerException("DataType and the data can't be null.");

    }

}
