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
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Map;
import java.util.Set;

import org.knime.filehandling.core.connections.TimeoutPath;

/**
 * Wrapped {@link FileSystemProvider} that simply delegates methods of the input file system provider. Paths that are
 * instances of {@link TimeoutPath} will be unwrapped before they are passed to the specific file system provide call.
 * In case that methods are called that return {@link FileSystem FileSystems}, the file system will be wrapped in a
 * {@link WrappedFileSystem}.
 * This class is used to wrap {@link FileSystem FileSystems} that cannot handle {@link TimeoutPath TimeoutPaths}.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class WrappedFileSystemProvider extends FileSystemProvider {

    /** The file system provider    */
    private final FileSystemProvider m_provider;

    /**
     * Creates a new instance of {@link WrappedFileSystemProvider}.
     *
     * @param provider the file system provider to wrap
     */
    public WrappedFileSystemProvider(final FileSystemProvider provider) {
        m_provider = provider;
    }

    /** Unwraps a given path if it's an instance of {@link TimeoutPath} */
    private static final Path unwrap(final Path path) {
        return (path instanceof TimeoutPath) ? ((TimeoutPath)path).unwrap(Path.class) : path;
    }

    @Override
    public String getScheme() {
        return m_provider.getScheme();
    }

    @Override
    public FileSystem newFileSystem(final URI uri, final Map<String, ?> env) throws IOException {
        return new WrappedFileSystem(m_provider.newFileSystem(uri, env));
    }

    @Override
    public FileSystem getFileSystem(final URI uri) {
        return new WrappedFileSystem(m_provider.getFileSystem(uri));
    }

    @Override
    public Path getPath(final URI uri) {
        return m_provider.getPath(uri);
    }

    @Override
    public SeekableByteChannel newByteChannel(final Path path, final Set<? extends OpenOption> options, final FileAttribute<?>... attrs)
        throws IOException {
        return m_provider.newByteChannel(unwrap(path), options, attrs);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(final Path dir, final Filter<? super Path> filter) throws IOException {
        return m_provider.newDirectoryStream(unwrap(dir), filter);
    }

    @Override
    public void createDirectory(final Path dir, final FileAttribute<?>... attrs) throws IOException {
        m_provider.createDirectory(unwrap(dir), attrs);
    }

    @Override
    public void delete(final Path path) throws IOException {
        m_provider.delete(unwrap(path));
    }

    @Override
    public void copy(final Path source, final Path target, final CopyOption... options) throws IOException {
        m_provider.copy(unwrap(source), unwrap(target), options);
    }

    @Override
    public void move(final Path source, final Path target, final CopyOption... options) throws IOException {
        m_provider.move(unwrap(source), unwrap(target), options);
    }

    @Override
    public boolean isSameFile(final Path path, final Path path2) throws IOException {
        return isSameFile(unwrap(path), unwrap(path2));
    }

    @Override
    public boolean isHidden(final Path path) throws IOException {
        return m_provider.isHidden(unwrap(path));
    }

    @Override
    public FileStore getFileStore(final Path path) throws IOException {
        return m_provider.getFileStore(unwrap(path));
    }

    @Override
    public void checkAccess(final Path path, final AccessMode... modes) throws IOException {
        m_provider.checkAccess(unwrap(path));
    }

    @Override
    public <V extends FileAttributeView> V getFileAttributeView(final Path path, final Class<V> type, final LinkOption... options) {
        return m_provider.getFileAttributeView(unwrap(path), type, options);
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(final Path path, final Class<A> type, final LinkOption... options)
        throws IOException {
        return m_provider.readAttributes(unwrap(path), type, options);
    }

    @Override
    public Map<String, Object> readAttributes(final Path path, final String attributes, final LinkOption... options) throws IOException {
        return m_provider.readAttributes(unwrap(path), attributes, options);
    }

    @Override
    public void setAttribute(final Path path, final String attribute, final Object value, final LinkOption... options) throws IOException {
        m_provider.setAttribute(unwrap(path), attribute, value, options);
    }
}
