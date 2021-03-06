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
 *   Mar 30, 2020 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.filehandling.core.node.table.reader.type.mapping;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.knime.filehandling.core.node.table.reader.type.mapping.TypeMappingTestUtils.mockProductionPath;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.convert.map.ProducerRegistry;
import org.knime.core.data.def.StringCell;
import org.knime.filehandling.core.node.table.reader.ReadAdapter;
import org.knime.filehandling.core.node.table.reader.ReadAdapterFactory;
import org.knime.filehandling.core.node.table.reader.spec.ReaderTableSpec;
import org.knime.filehandling.core.node.table.reader.type.mapping.TypeMappingTestUtils.TestReadAdapter;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Contains unit tests for {@link DefaultTypeMappingFactory}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultTypeMappingFactoryTest {

    @Mock
    private ProducerRegistry<String, TestReadAdapter> m_producerRegistry = null;

    private DefaultTypeMappingFactory<String, String> m_testInstance;

    /**
     * Initializes the test instance.
     */
    @Before
    public void init() {
        final Map<String, DataType> defaultTypes = new HashMap<>();
        defaultTypes.put("frieda", StringCell.TYPE);
        defaultTypes.put("berta", StringCell.TYPE);
        ReadAdapterFactory<String, String> readAdapterFactory = new ReadAdapterFactory<String, String>() {

            @Override
            public ReadAdapter<String, String> createReadAdapter() {
                return new TestReadAdapter();
            }

            @Override
            public ProducerRegistry<String, ? extends ReadAdapter<String, String>> getProducerRegistry() {
                return m_producerRegistry;
            }

            @Override
            public Map<String, DataType> getDefaultTypeMap() {
                return defaultTypes;
            }

        };
        m_testInstance = new DefaultTypeMappingFactory<>(readAdapterFactory);
    }

    /**
     * Tests the {@link TypeMappingFactory#create(org.knime.filehandling.core.node.table.reader.spec.ReaderTableSpec)}
     * implementation.
     */
    @Test
    public void testCreate() {
        when(m_producerRegistry.getAvailableProductionPaths("berta")).thenReturn(asList(mockProductionPath("berta")));
        when(m_producerRegistry.getAvailableProductionPaths("frieda")).thenReturn(asList(mockProductionPath("frieda")));
        ReaderTableSpec<String> spec = ReaderTableSpec.create(asList("hans", "franz"), asList("frieda", "berta"));
        TypeMapping<String> typeMapping = m_testInstance.create(spec);
        DataTableSpec expected = new DataTableSpec("default", new String[]{"hans", "franz"},
            new DataType[]{StringCell.TYPE, StringCell.TYPE});
        DataTableSpec actual = typeMapping.map(spec);
        assertEquals(expected, actual);
    }

    /**
     * Tests if {@code create} fails if no production path can be found for a specified type.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateFailsIfNoProductionPathForExternalTypeCanBeFound() {
        when(m_producerRegistry.getAvailableProductionPaths("frieda")).thenReturn(Collections.emptyList());
        ReaderTableSpec<String> spec = ReaderTableSpec.create(asList("hans", "franz"), asList("frieda", "berta"));
        m_testInstance.create(spec);
    }

    /**
     * Tests if {@code create} fails if no production path can be found for a specified type.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateFailsIfDefaultTypeIsNOTSpecified() {
        ReaderTableSpec<String> spec = ReaderTableSpec.create(asList("hans", "franz"), asList("gunter", "berta"));
        m_testInstance.create(spec);
    }

}
