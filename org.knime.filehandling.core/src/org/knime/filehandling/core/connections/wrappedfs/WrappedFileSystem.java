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
 *   Oct 24, 2019 (julian): created
 */
package org.knime.filehandling.core.connections.wrappedfs;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;

/**
 * Wrapped {@link FileSystem} that simply delegates methods of the input file system, but wraps the associated
 * {@link FileSystemProvider} in a {@link WrappedFileSystemProvider} when {@link #provider()} is called.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class WrappedFileSystem extends FileSystem {

    /** The file system */
    private FileSystem m_fileSystem;

    /**
     * Creates a new instance of {@link WrappedFileSystem}.
     *
     * @param fileSystem The file system to be wrapped
     */
    public WrappedFileSystem(final FileSystem fileSystem) {
        m_fileSystem = fileSystem;
    }

    @Override
    public FileSystemProvider provider() {
        return new WrappedFileSystemProvider(m_fileSystem.provider());
    }

    @Override
    public void close() throws IOException {
        m_fileSystem.close();
    }

    @Override
    public boolean isOpen() {
        return m_fileSystem.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return m_fileSystem.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return m_fileSystem.getSeparator();
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        return m_fileSystem.getRootDirectories();
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return m_fileSystem.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return m_fileSystem.supportedFileAttributeViews();
    }

    @Override
    public Path getPath(final String first, final String... more) {
        return m_fileSystem.getPath(first, more);
    }

    @Override
    public PathMatcher getPathMatcher(final String syntaxAndPattern) {
        return m_fileSystem.getPathMatcher(syntaxAndPattern);
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return m_fileSystem.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        return m_fileSystem.newWatchService();
    }
}
