/*
 * ------------------------------------------------------------------------
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
 * ------------------------------------------------------------------------
 */
package org.knime.base.node.io.listfiles2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.filestore.FileStoreFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.context.NodeCreationConfiguration;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.connections.FSLocation;
import org.knime.filehandling.core.data.path.FSPathCellFactory;
import org.knime.filehandling.core.data.path.FSPathValueMetaData;
import org.knime.filehandling.core.defaultnodesettings.FileChooserHelper;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2;
import org.knime.filehandling.core.defaultnodesettings.SettingsModelFileChooser2.FileOrFolderEnum;
import org.knime.filehandling.core.port.FileSystemPortObject;

/**
 * This is the model implementation of List Files.
 *
 *
 * @author Peter
 */
public class ListFilesNodeModel extends NodeModel {

    private ListFilesSettings m_settings;

    final SettingsModelFileChooser2 m_modelFileChooser = createModel();

    static SettingsModelFileChooser2 createModel() {
        SettingsModelFileChooser2 fileChooser2 = new SettingsModelFileChooser2("asd");
        SettingsModelString fileOrFolderSettingsModel = fileChooser2.getFileOrFolderSettingsModel();
        fileOrFolderSettingsModel.setStringValue(FileOrFolderEnum.FILE_IN_FOLDER.name());
        return fileChooser2;
    }

    /**
     * Constructor for the node model.
     */
    protected ListFilesNodeModel(final NodeCreationConfiguration portsConfig) {
        super(portsConfig.getPortConfig().get().getInputPorts(), portsConfig.getPortConfig().get().getOutputPorts());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
        throws Exception {
        final Optional<FSConnection> fs = FileSystemPortObject.getFileSystemConnection(inData, 0);
        //                Optional.ofNullable(getPortsConfig().getInputPortLocation().get(CONNECTION_INPUT_PORT_GRP_NAME)) //
        //                    .map(arr -> FileSystemPortObject.getFileSystemConnection(data, arr[0]).get()); // save due to framework

        FileChooserHelper fch = new FileChooserHelper(fs, m_modelFileChooser, 1000);

        DataColumnSpecCreator dataColumnSpecCreator = new DataColumnSpecCreator("Paths", FSPathCellFactory.TYPE);
        String fsType = m_modelFileChooser.getFileSystemChoice().getType().name();
        dataColumnSpecCreator.addMetaData(new FSPathValueMetaData(fsType, null), false);
        BufferedDataContainer container =
            exec.createDataContainer(new DataTableSpec(dataColumnSpecCreator.createSpec()));
        final List<Path> paths = fch.getPaths();
        long i = 0;
        FSPathCellFactory pathCellFactory =
            new FSPathCellFactory(FileStoreFactory.createFileStoreFactory(exec), fsType, null);
        for (final Path path : paths) {
            FSLocation fsLocation = new FSLocation(fsType, path.toString());
            container.addRowToTable(new DefaultRow(RowKey.createRowKey(i++), pathCellFactory.createCell(fsLocation)));
        }
        container.close();
        return new BufferedDataTable[]{container.getTable()};
        //        ListFiles lister = new ListFiles(m_settings);
        //        BufferedDataTable table = lister.search(exec);
        //        return new BufferedDataTable[]{table};
    }

    /** {@inheritDoc} */
    @Override
    protected void reset() {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
        //        if (m_settings == null) {
        //            throw new InvalidSettingsException("No configuration available.");
        //        }
        //        // check valid config
        //        m_settings.getRootsFromLocationString();
        //        return new DataTableSpec[]{ListFiles.SPEC};
        DataColumnSpecCreator dataColumnSpecCreator = new DataColumnSpecCreator("Paths", FSPathCellFactory.TYPE);
        String fsType = m_modelFileChooser.getFileSystemChoice().getType().name();
        dataColumnSpecCreator.addMetaData(new FSPathValueMetaData(fsType, null), false);
        return new DataTableSpec[]{new DataTableSpec(dataColumnSpecCreator.createSpec())};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
        if (m_settings != null) {
            m_settings.saveSettingsTo(settings);
        }
        m_modelFileChooser.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        //        ListFilesSettings lsSettings = new ListFilesSettings();
        //        lsSettings.loadSettingsInModel(settings);
        //        m_settings = lsSettings;
        m_modelFileChooser.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        //        new ListFilesSettings().loadSettingsInModel(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // nothing to load
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        // no op
    }

}
