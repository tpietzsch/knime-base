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
 *   Mar 26, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.streamable.RowOutput;
import org.knime.filehandling.core.node.table.reader.config.MultiTableReadConfig;
import org.knime.filehandling.core.node.table.reader.config.ReaderSpecificConfig;
import org.knime.filehandling.core.node.table.reader.config.TableReadConfig;
import org.knime.filehandling.core.node.table.reader.randomaccess.RandomAccessible;
import org.knime.filehandling.core.node.table.reader.read.Read;
import org.knime.filehandling.core.node.table.reader.spec.ReaderTableSpec;
import org.knime.filehandling.core.node.table.reader.util.IndividualTableReader;
import org.knime.filehandling.core.node.table.reader.util.MultiTableRead;
import org.knime.filehandling.core.node.table.reader.util.MultiTableReadFactory;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Contains unit tests for MultiTableReader.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiTableReaderTest {

    private interface DummyReaderSpecificConfig extends ReaderSpecificConfig<DummyReaderSpecificConfig> {
    }

    @Mock
    private MultiTableReadFactory<String, String> m_multiTableReadFactory = null;

    @Mock
    private MultiTableRead<String> m_multiTableRead = null;

    @Mock
    private IndividualTableReader<String> m_individualTableReader;

    @Mock
    private TableReader<DummyReaderSpecificConfig, String, String> m_reader = null;

    @Mock
    private Read<String> m_read;

    @Mock
    private RandomAccessible<String> m_randomAccessible;

    @Mock
    private MultiTableReadConfig<DummyReaderSpecificConfig> m_multiReadConfig = null;

    @Mock
    private TableReadConfig<DummyReaderSpecificConfig> m_tableReadConfig = null;

    @Mock
    private Path m_path1 = null;

    @Mock
    private Path m_path2 = null;

    @Mock
    private ExecutionContext m_exec = null;

    @Mock
    private ExecutionMonitor m_monitor = null;

    @Mock
    private RowOutput m_rowOutput = null;

    private ReaderTableSpec<String> m_readerSpec = null;

    private DataTableSpec m_knimeSpec = null;

    private MultiTableReader<DummyReaderSpecificConfig, String, String> m_testInstance = null;

    /**
     * Sets up the test instance before each unit test.
     */
    @Before
    public void init() {
        m_testInstance = new MultiTableReader<>(m_reader, m_multiTableReadFactory);
        m_readerSpec = ReaderTableSpec.create("foo", "bar");
        m_knimeSpec =
            new DataTableSpec(new String[]{"Column0", "Column1"}, new DataType[]{StringCell.TYPE, StringCell.TYPE});
    }

    /**
     * Tests the {@code createTableSpec} method.
     *
     * @throws IOException never thrown
     */
    @Test
    public void testCreateTableSpec() throws IOException {
        stubForCreateSpec();
        assertEquals(m_knimeSpec, m_testInstance.createTableSpec(asList(m_path1, m_path2), m_multiReadConfig));
    }

    private void stubForCreateSpec() throws IOException {
        when(m_reader.readSpec(any(), any())).thenReturn(m_readerSpec);
        when(m_multiTableReadFactory.create(any(), any())).thenReturn(m_multiTableRead);
        when(m_multiTableRead.getOutputSpec()).thenReturn(m_knimeSpec);
        when(m_multiReadConfig.getTableReadConfig()).thenReturn(m_tableReadConfig);
    }

    /**
     * Tests the {@code fillRowOutput} method if createSpec isn't called first i.e. it first has to create the spec.
     *
     * @throws Exception never thrown
     */

    @Test
    public void testFillRowOutputWithoutCallingCreateSpecFirst() throws Exception {
        stubForCreateSpec();
        stubForFillRowOutput();
        m_testInstance.fillRowOutput(asList(m_path1, m_path2), m_multiReadConfig, m_rowOutput, m_exec);
        verify(m_individualTableReader, times(2)).fillOutput(any(), eq(m_rowOutput), eq(m_monitor));
        verify(m_monitor, times(2)).setProgress(1.0);
        verify(m_rowOutput, times(1)).close();
    }

    /**
     * Tests if {@code fillRowOutput} reuses an existing spec if the paths match.
     *
     * @throws Exception never thrown
     */
    @Test
    public void testFillRowOutputWithCallingCreateSpecFirstAndValidPaths() throws Exception {
        stubForCreateSpec();
        m_testInstance.createTableSpec(asList(m_path1, m_path2), m_multiReadConfig);
        stubForFillRowOutput();
        when(m_multiTableRead.isValidFor(any())).thenReturn(true);
        m_testInstance.fillRowOutput(asList(m_path1, m_path2), m_multiReadConfig, m_rowOutput, m_exec);
        verify(m_individualTableReader, times(2)).fillOutput(any(), eq(m_rowOutput), eq(m_monitor));
        verify(m_monitor, times(2)).setProgress(1.0);
        verify(m_rowOutput, times(1)).close();
    }

    /**
     * Tests if {@code fillRowOutput} creates a new spec if the existing spec doesn't match the provided paths.
     *
     * @throws Exception never thrown
     */
    @Test
    public void testFillRowOutputWithCallingCreateSpecFirstAndInValidPaths() throws Exception {
        stubForCreateSpec();
        m_testInstance.createTableSpec(asList(m_path1, m_path2), m_multiReadConfig);
        stubForFillRowOutput();
        m_testInstance.fillRowOutput(asList(m_path1, m_path2), m_multiReadConfig, m_rowOutput, m_exec);
        verify(m_individualTableReader, times(2)).fillOutput(any(), eq(m_rowOutput), eq(m_monitor));
        verify(m_monitor, times(2)).setProgress(1.0);
        verify(m_rowOutput, times(1)).close();
    }

    /**
     * Tests the {@code reset} method.
     *
     * @throws Exception never thrown
     */
    @Test
    public void testReset() throws Exception {
        stubForCreateSpec();
        m_testInstance.createTableSpec(asList(m_path1, m_path2), m_multiReadConfig);
        m_testInstance.reset();
        stubForFillRowOutput();
        m_testInstance.fillRowOutput(asList(m_path1, m_path2), m_multiReadConfig, m_rowOutput, m_exec);
        verify(m_multiTableReadFactory, times(2)).create(any(), any());
    }

    private void stubForFillRowOutput() throws IOException {
        when(m_exec.createSubProgress(anyDouble())).thenReturn(m_monitor);
        when(m_tableReadConfig.copy()).thenReturn(m_tableReadConfig);
        when(m_reader.read(any(), any())).thenReturn(m_read);
        when(m_multiTableRead.createIndividualTableReader(any(), any(), any())).thenReturn(m_individualTableReader);
    }

}
