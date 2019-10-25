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
 *   Oct 23, 2019 (julian): created
 */
package org.knime.filehandling.core.connections;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;

import org.knime.filehandling.core.connections.attributes.FSFileAttributes;
import org.knime.filehandling.core.connections.wrappedfs.WrappedFileSystem;

/**
 * Path wrapper that has a member defining a connection/read timeout.
 * The implementation of each method returns the implementation of the 'real' path object that has been passed
 * to the constructor of this class.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public final class TimeoutPath implements FSPath {

    /** The path */
    private final Path m_path;

    /** The timeout in milliseconds */
    private int m_timeout;

    /**
     * Creates a new instance of {@link TimeoutPath} wrapping another {@link Path} object to provide a timeout if
     * necessary.
     *
     * @param path Path object to be wrapped
     * @param timeoutInMillis timeout in milliseconds
     */
    public TimeoutPath(final Path path, final int timeoutInMillis) {
        m_path = path;
        m_timeout = timeoutInMillis;
    }

    @Override
    public FileSystem getFileSystem() {
        // Return a wrapped file system if the path is based on a default file system which cannot handle TimeoutPaths
        if (m_path.getFileSystem().equals(FileSystems.getDefault())) {
            return new WrappedFileSystem(m_path.getFileSystem());
        }
        return m_path.getFileSystem();
    }

    @Override
    public boolean isAbsolute() {
        return m_path.isAbsolute();
    }

    @Override
    public Path getRoot() {
        return m_path.getRoot();
    }

    @Override
    public Path getFileName() {
        return m_path.getFileName();
    }

    @Override
    public Path getParent() {
        return m_path.getParent();
    }

    @Override
    public int getNameCount() {
        return m_path.getNameCount();
    }

    @Override
    public Path getName(final int index) {
        return m_path.getName(index);
    }

    @Override
    public Path subpath(final int beginIndex, final int endIndex) {
        return m_path.subpath(beginIndex, endIndex);
    }

    @Override
    public boolean startsWith(final Path other) {
        return m_path.startsWith(other);
    }

    @Override
    public boolean startsWith(final String other) {
        return m_path.startsWith(other);
    }

    @Override
    public boolean endsWith(final Path other) {
        return m_path.endsWith(other);
    }

    @Override
    public boolean endsWith(final String other) {
        return m_path.endsWith(other);
    }

    @Override
    public Path normalize() {
        return m_path.normalize();
    }

    @Override
    public Path resolve(final Path other) {
        return m_path.resolve(other);
    }

    @Override
    public Path resolve(final String other) {
        return m_path.resolve(other);
    }

    @Override
    public Path resolveSibling(final Path other) {
        return m_path.resolveSibling(other);
    }

    @Override
    public Path resolveSibling(final String other) {
        return m_path.resolveSibling(other);
    }

    @Override
    public Path relativize(final Path other) {
        return m_path.relativize(other);
    }

    @Override
    public URI toUri() {
        return m_path.toUri();
    }

    @Override
    public Path toAbsolutePath() {
        return m_path.toAbsolutePath();
    }

    @Override
    public Path toRealPath(final LinkOption... options) throws IOException {
        return m_path.toRealPath(options);
    }

    @Override
    public File toFile() {
        return m_path.toFile();
    }

    @Override
    public WatchKey register(final WatchService watcher, final Kind<?>[] events, final Modifier... modifiers)
        throws IOException {
        return m_path.register(watcher, events, modifiers);
    }

    @Override
    public WatchKey register(final WatchService watcher, final Kind<?>... events) throws IOException {
        return m_path.register(watcher, events);
    }

    @Override
    public Iterator<Path> iterator() {
        return m_path.iterator();
    }

    @Override
    public int compareTo(final Path other) {
        return m_path.compareTo(other);
    }

    /**
     * Returns the connection/read timeout in milliseconds
     *
     * @return the timeout
     */
    public int getTimeout() {
        return m_timeout;
    }

    /**
     * Unwraps this instance of TimeoutPath to the original Path implementation.
     *
     * @param <T> The original interface of the wrapped Path.
     * @param iface the original interface of the wrapped path
     * @return the original path object
     */
    public <T extends Path> T unwrap(final Class<T> iface) {
        return iface.cast(m_path);
    }

    /**
     * Checks whether this instance is a wrapper for the given interface.
     *
     * @param iface the interface
     * @return true, if the given interface is the interface of the wrapped path object
     */
    public boolean isWrapperFor(final Class<? extends Path> iface) {
        return iface.isInstance(m_path.getClass());
    }

    @Override
    public String toString() {
        return m_path.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        final TimeoutPath path;
        if (!(obj instanceof TimeoutPath)) {
            return false;
        }
        path = (TimeoutPath)obj;
        return m_path.equals(path.unwrap(m_path.getClass()));
    }

    @Override
    public int hashCode() {
        return m_path.hashCode();
    }

    @Override
    public FSFileAttributes getFileAttributes(final Class<?> type) {
        if (m_path instanceof FSPath) {
            return ((FSPath)m_path).getFileAttributes(type);
        }
        throw new UnsupportedOperationException("Path does not implement FSPath");
    }
}
