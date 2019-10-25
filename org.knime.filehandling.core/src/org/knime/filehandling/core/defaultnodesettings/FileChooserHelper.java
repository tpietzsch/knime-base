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
 *   Aug 28, 2019 (julian): created
 */
package org.knime.filehandling.core.defaultnodesettings;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knime.core.node.FSConnectionFlowVariableProvider;
import org.knime.core.util.FileUtil;
import org.knime.core.util.Pair;
import org.knime.filehandling.core.connections.TimeoutPath;
import org.knime.filehandling.core.filefilter.FileFilter;

/**
 * Helper class used to get a list of {@link Path Paths} based on the given {@link FSConnectionFlowVariableProvider} and
 * {@link SettingsModelFileChooser2}. It retrieves the necessary {@link FileSystem}, sets a {@link FileFilter} if needed
 * and stores counts about filtered files.
 *
 * @author Julian Bunzel, KNIME GmbH, Berlin
 */
public final class FileChooserHelper {

    /** FileSystem used to resolve the path */
    private final FileSystem m_fileSystem;

    /** Settings object containing necessary information about e.g. file filtering */
    private final SettingsModelFileChooser2 m_settings;

    /** Optional containing a {@link FileFilter} if selected */
    private final Optional<FileFilter> m_filter;

    /** Pair of integer containing the number of listed files and the number of filtered files. */
    private Pair<Integer, Integer> m_counts;

    /** Timeout in milliseconds */
    private final int m_timeout;

    /**
     * Creates a new instance of {@link FileChooserHelper}. Uses the default timeout, if a timeout is necessary.
     *
     * @param provider the {@link FSConnectionFlowVariableProvider} used to retrieve a file system from a flow variable
     *            if necessary
     * @param settings the settings object containing necessary information about e.g. file filtering
     * @throws IOException thrown when the file system could not be retrieved.
     */
    public FileChooserHelper(final FSConnectionFlowVariableProvider provider, final SettingsModelFileChooser2 settings)
        throws IOException {
        this(provider, settings, FileUtil.getDefaultURLTimeoutMillis());
    }

    /**
     * Creates a new instance of {@link FileChooserHelper}.
     *
     * @param provider the {@link FSConnectionFlowVariableProvider} used to retrieve a file system from a flow variable
     *            if necessary
     * @param settings the settings object containing necessary information about e.g. file filtering
     * @param timeoutInMillis the connection/read timeout in milliseconds
     * @throws IOException thrown when the file system could not be retrieved.
     */
    public FileChooserHelper(final FSConnectionFlowVariableProvider provider, final SettingsModelFileChooser2 settings,
        final int timeoutInMillis) throws IOException {

        m_filter = settings.getFilterFiles() ? Optional.of(new FileFilter(settings)) : Optional.empty();
        m_settings = settings;
        m_fileSystem = FileSystemHelper.retrieveFileSystem(provider, settings);
        m_timeout = timeoutInMillis;
    }

    /**
     * Returns the file system.
     *
     * @return the file system
     */
    public final FileSystem getFileSystem() {
        return m_fileSystem;
    }

    /**
     * Assumes that the file specified in the settings model is a folder, scans the folder for files matching the filter
     * from the settings model, and returns a list of matching {@link TimeoutPath TimeoutPaths}.
     *
     * @return a list of TimeoutPath that matched the filter from the settings model.
     * @throws IOException thrown if directory could not be scanned
     */
    public final List<TimeoutPath> scanDirectoryTree() throws IOException {
        setCounts(0, 0);
        final TimeoutPath dirPath = getPathFromSettings();
        final boolean includeSubfolders = m_settings.getIncludeSubfolders();

        final List<Path> paths;
        try (final Stream<Path> stream = includeSubfolders
            ? Files.walk(dirPath, Integer.MAX_VALUE, FileVisitOption.FOLLOW_LINKS) : Files.list(dirPath)) {
            if (m_filter.isPresent()) {
                final FileFilter filter = m_filter.get();
                filter.resetCount();
                paths = stream.filter(filter::isSatisfied).collect(Collectors.toList());
                setCounts(paths.size(), filter.getNumberOfFilteredFiles());
            } else {
                paths = stream.filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());
                setCounts(paths.size(), 0);
            }
        }

        return paths.stream().map(p -> new TimeoutPath(p, m_timeout)).collect(Collectors.toList());
    }

    /**
     * Returns a list of {@link TimeoutPath} if the input String represents a directory its contents are scanned,
     * otherwise the list contains the file, if it is readable.
     *
     * @return a list of TimeoutPaths to read
     * @throws IOException if an I/O error occurs
     */
    public final List<TimeoutPath> getPaths() throws IOException {

        final TimeoutPath pathOrUrl = getPathFromSettings();
        final List<TimeoutPath> toReturn;

        if (Files.isDirectory(pathOrUrl)) {
            toReturn = scanDirectoryTree();
        } else {
            toReturn = Collections.singletonList(pathOrUrl);
        }

        return toReturn;
    }

    /**
     * Creates and returns a new Path object according to the path or URL provided by the underlying settings model.
     *
     * @return TimeoutPath leading to the path or url provided by the underlying settings model
     */
    public TimeoutPath getPathFromSettings() {
        final TimeoutPath pathOrUrl;
        if (FileSystemChoice.getCustomFsUrlChoice().equals(m_settings.getFileSystemChoice())) {
            final URI uri = URI.create(m_settings.getPathOrURL());
            pathOrUrl = new TimeoutPath(m_fileSystem.provider().getPath(uri), m_timeout);
        } else {
            pathOrUrl = new TimeoutPath(m_fileSystem.getPath(m_settings.getPathOrURL()), m_timeout);
        }

        return pathOrUrl;
    }

    /**
     * Sets a pair of integers containing the number of listed files and the number of filtered files
     *
     * @param numberOfRemainingFiles number of remaining files
     * @param numberOfFilteredFiles number of filtered files
     */
    private final void setCounts(final int numberOfRemainingFiles, final int numberOfFilteredFiles) {
        m_counts = new Pair<>(numberOfRemainingFiles, numberOfFilteredFiles);
    }

    /**
     * Returns the number of files that matched the filter, and the number of files that did not match the filter. The
     * sum of the two numbers is the total amount of files that were scanned.
     *
     * @return pair of integer containing the number of files that matched the filter, and the number of files that did
     *         not match the filter.
     */
    public final Pair<Integer, Integer> getCounts() {
        return m_counts;
    }

    /**
     * Returns a clone of the underlying {@link SettingsModelFileChooser2}.
     *
     * @return a clone of the underlying {@code SettingsModelFileChooser2}
     */
    public final SettingsModelFileChooser2 getSettingsModel() {
        return m_settings.clone();
    }
}
