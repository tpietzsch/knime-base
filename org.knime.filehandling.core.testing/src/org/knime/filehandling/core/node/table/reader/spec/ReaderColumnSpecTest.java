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
package org.knime.filehandling.core.node.table.reader.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for {@link ReaderColumnSpec}.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public class ReaderColumnSpecTest {

    /**
     * Tests the {@link ReaderColumnSpec#create(Object)} and {@link ReaderColumnSpec#createWithName(String, Object)}
     * name methods as well as {@link ReaderColumnSpec#getName()} and {@link ReaderColumnSpec#getType()}
     * implementations.
     */
    @Test
    public void testCreation() {
        ReaderColumnSpec<String> noName = ReaderColumnSpec.create("frieda");
        assertEquals(Optional.empty(), noName.getName());
        assertEquals("frieda", noName.getType());
        ReaderColumnSpec<String> nameNull = ReaderColumnSpec.createWithName(null, "frieda");
        assertEquals(noName, nameNull);
        ReaderColumnSpec<String> named = ReaderColumnSpec.createWithName("hans", "franz");
        assertEquals("hans", named.getName().get());
        assertEquals("franz", named.getType());
    }

    /**
     * Tests the equals implementation
     */
    @SuppressWarnings("unlikely-arg-type")
	@Test
    public void testEquals() {
        ReaderColumnSpec<String> spec = ReaderColumnSpec.create("frieda");
        assertTrue(spec.equals(spec));
        assertFalse(spec.equals(null));
        assertFalse(spec.equals("foo"));
        ReaderColumnSpec<String> same = ReaderColumnSpec.create("frieda");
        assertTrue(spec.equals(same));
        assertTrue(same.equals(spec));
        ReaderColumnSpec<String> different = ReaderColumnSpec.createWithName("hans", "franz");
        assertFalse(spec.equals(different));
        assertFalse(different.equals(spec));
    }

    /**
     * Tests the hashcode implementation.
     */
    @Test
    public void testHashCode() {
        ReaderColumnSpec<String> spec = ReaderColumnSpec.create("frieda");
        ReaderColumnSpec<String> same = ReaderColumnSpec.create("frieda");
        assertEquals(spec.hashCode(), same.hashCode());
        ReaderColumnSpec<String> different = ReaderColumnSpec.createWithName("hans", "franz");
        assertNotEquals(spec.hashCode(), different.hashCode());
        different = ReaderColumnSpec.create("berta");
        assertNotEquals(spec.hashCode(), different.hashCode());
    }

    /**
     * Tests the toString implementation.
     */
    @Test
    public void testToString() {
        ReaderColumnSpec<String> different = ReaderColumnSpec.createWithName("hans", "franz");
        assertEquals("[hans, franz]", different.toString());
        ReaderColumnSpec<String> spec = ReaderColumnSpec.create("frieda");
        assertEquals("[<no name>, frieda]", spec.toString());
    }

}
