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
 *   Aug 15, 2019 (bjoern): created
 */
package org.knime.filehandling.core.defaultnodesettings;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.knime.core.node.FlowVariableModel;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.util.FileSystemBrowser.DialogType;
import org.knime.core.node.util.FileSystemBrowser.FileSelectionMode;
import org.knime.core.node.util.LocalFileSystemBrowser;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.knime.core.util.FileUtil;
import org.knime.filehandling.core.connections.FSConnection;
import org.knime.filehandling.core.filefilter.FileFilter.FilterType;
import org.knime.filehandling.core.port.FileSystemPortObject;
import org.knime.filehandling.core.port.FileSystemPortObjectSpec;
import org.knime.filehandling.core.util.MountPointIDProviderService;

/**
 * Dialog component that allows selecting a file or multiple files in a folder. It provides the possibility to connect
 * to different file systems, as well as file filtering based on file extensions, regular expressions or wildcard.
 *
 * @author Bjoern Lohrmann, KNIME GmbH, Berlin, Germany
 * @author Julian Bunzel, KNIME GmbH, Berlin, Germany
 */
public class DialogComponentFileChooser2 extends DialogComponent {

    /** Node logger */
    static final NodeLogger LOGGER = NodeLogger.getLogger(DialogComponentFileChooser2.class);

    /** Flow variable provider used to retrieve connection information to different file systems */
    private Optional<FSConnection> m_fs;

    /** Flow variable model */
    private final FlowVariableModel m_pathFlowVariableModel;

    /** JPanel holding the file system connection label and combo boxes */
    private final JPanel m_connectionSettingsPanel = new JPanel();

    /** Card layout used to swap between different views (show KNIME file systems or not) */
    private final CardLayout m_connectionSettingsCardLayout = new CardLayout();

    /** JPanel holding the folder and filter settings */
    private FolderAndFilterOptionsPanel m_folderAndFilterSettings;

    /** Label for the file system connection combo boxes */
    private final JLabel m_connectionLabel;

    /** Combo box to select the file system */
    private final JComboBox<FileSystemChoice> m_connections;

    /** Combo box to select the KNIME file system connections */
    private final JComboBox<KNIMEConnection> m_knimeConnections;

    /** Label for the {@code FilesHistoryPanel} */
    private final JLabel m_fileFolderLabel;

    /** FilesHistoryPanel used to select a file or a folder */
    private final FilesHistoryPanel m_fileHistoryPanel;

    /** {@link DialogType} defining whether the component is used to read or write */
    private final DialogType m_dialogType;

    /** {@link DialogType} defining whether the component is allowed to select files, folders or both */
    private final FileSelectionMode m_fileSelectionMode;

    /** Label containing status messages */
    private final JLabel m_statusMessage;

    /** Swing worker used to do file scanning in the background */
    private StatusMessageSwingWorker m_statusMessageSwingWorker;

    /** Swing worker used to do check if file/folder options are needed */
    private FolderAndFilterPanelSwingWorker m_folderFilterPanelSwingWorker;

    /** Flag to temporarily disable event processing from Swing components or the settings model */
    private boolean m_ignoreUpdates;

    /** String used for file system connection label. */
    private static final String CONNECTION_LABEL = "Read from: ";

    /** String used for the label next to the {@code FilesHistoryPanel} */
    private static final String FILE_FOLDER_LABEL = "File/Folder:";

    /** String used for the label next to the {@code FilesHistoryPanel} */
    private static final String URL_LABEL = "URL:";

    /** Empty string used for the status message label */
    private static final String EMPTY_STRING = " ";

    /** Identifier for the KNIME file system connection view in the card layout */
    private static final String KNIME_CARD_VIEW_IDENTIFIER = "KNIME";

    /** Identifier for the default file system connection view in the card layout */
    private static final String DEFAULT_CARD_VIEW_IDENTIFIER = "DEFAULT";

    /** An optional exception message from the status message SwingWorker */
    private Optional<String> m_exceptionMsg;

    /** Index of optional input port */
    private int m_inPort;

    /** Timeout in milliseconds */
    private int m_timeoutInMillis = FileUtil.getDefaultURLTimeoutMillis();

    /**
     * Creates a new instance of {@code DialogComponentFileChooser2}.
     *
     * @param inPort the index of the optional {@link FileSystemPortObject}
     * @param settingsModel the settings model storing all the necessary information
     * @param historyId id used to store file history used by {@link FilesHistoryPanel}
     * @param dialogType integer defining the dialog type (see {@link JFileChooser#OPEN_DIALOG},
     *            {@link JFileChooser#SAVE_DIALOG})
     * @param selectionMode integer defining the dialog type (see {@link JFileChooser#FILES_ONLY},
     *            {@link JFileChooser#FILES_AND_DIRECTORIES} and {@link JFileChooser#DIRECTORIES_ONLY}
     * @param dialogPane the {@link NodeDialogPane} this component is used for
     * @param suffixes array of file suffixes used as defaults for file filtering options
     */
    public DialogComponentFileChooser2(final int inPort, final SettingsModelFileChooser2 settingsModel,
        final String historyId, final int dialogType, final int selectionMode, final NodeDialogPane dialogPane,
        final String... suffixes) {
        super(settingsModel);
        m_inPort = inPort;

        m_ignoreUpdates = false;

        final Optional<String> legacyConfigKey = settingsModel.getLegacyConfigKey();
        m_pathFlowVariableModel =
            legacyConfigKey.isPresent() ? dialogPane.createFlowVariableModel(legacyConfigKey.get(), Type.STRING)
                : dialogPane.createFlowVariableModel(
                    new String[]{settingsModel.getConfigName(), SettingsModelFileChooser2.PATH_OR_URL_KEY},
                    Type.STRING);

        m_connectionLabel = new JLabel(CONNECTION_LABEL);

        m_connections = new JComboBox<>(new FileSystemChoice[0]);
        m_connections.setEnabled(true);

        m_knimeConnections = new JComboBox<>();

        m_fileFolderLabel = new JLabel(FILE_FOLDER_LABEL);

        m_dialogType = DialogType.fromJFileChooserCode(dialogType);
        m_fileSelectionMode = FileSelectionMode.fromJFileChooserCode(selectionMode);
        m_fileHistoryPanel = new FilesHistoryPanel(m_pathFlowVariableModel, historyId, new LocalFileSystemBrowser(),
            m_fileSelectionMode, m_dialogType, suffixes);

        if (showFolderOptions()) {
            m_folderAndFilterSettings = new FolderAndFilterOptionsPanel(suffixes);
        }

        m_statusMessage = new JLabel(EMPTY_STRING);

        m_statusMessageSwingWorker = null;
        m_folderFilterPanelSwingWorker = null;

        initLayout();

        // Fill combo boxes
        updateConnectionsCombo();
        updateKNIMEConnectionsCombo();
        addEventHandlers();
        updateComponent();
        getModel().addChangeListener(e -> updateComponent());
    }

    /** Initialize the layout of the dialog component */
    private final void initLayout() {
        final JPanel panel = getComponentPanel();
        panel.setLayout(new GridBagLayout());

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 0, 5, 5);
        panel.add(m_connectionLabel, gbc);

        gbc.gridx++;
        panel.add(m_connections, gbc);

        gbc.gridx++;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        initConnectionSettingsPanelLayout();
        panel.add(m_connectionSettingsPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.insets = new Insets(5, 5, 5, 0);
        panel.add(m_fileFolderLabel, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(5, 0, 5, 0);
        panel.add(m_fileHistoryPanel, gbc);

        if (showFolderOptions()) {
            gbc.gridx = 1;
            gbc.gridy++;
            gbc.gridwidth = 1;
            gbc.weightx = 0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(m_folderAndFilterSettings, gbc);
        }

        gbc.gridx = 1;
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        panel.add(m_statusMessage, gbc);

    }

    /** Initialize the file system connection panel layout */
    private final void initConnectionSettingsPanelLayout() {
        m_connectionSettingsPanel.setLayout(m_connectionSettingsCardLayout);

        m_connectionSettingsPanel.add(initKNIMEConnectionPanel(), KNIME_CARD_VIEW_IDENTIFIER);
        m_connectionSettingsPanel.add(new JPanel(), DEFAULT_CARD_VIEW_IDENTIFIER);
    }

    /** Initialize the KNIME file system connection component */
    private final Component initKNIMEConnectionPanel() {
        final JPanel knimeConnectionPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        knimeConnectionPanel.add(m_knimeConnections, gbc);

        gbc.gridx++;
        gbc.weightx = 1;
        knimeConnectionPanel.add(Box.createHorizontalGlue(), gbc);

        return knimeConnectionPanel;
    }

    /** Add event handlers for UI components */
    private void addEventHandlers() {
        m_connections.addActionListener(e -> handleUIChange());
        m_knimeConnections.addActionListener(e -> handleUIChange());
        m_fileHistoryPanel.addChangeListener(e -> handleUIChange());
        if (showFolderOptions()) {
            m_folderAndFilterSettings.addActionListener(e -> handleUIChange());
            m_folderAndFilterSettings.addConfigureButtonActionListener(e -> showFileFilterConfigurationDialog());
        }
    }

    /** Method to update settings model */
    private void handleUIChange() {
        if (m_ignoreUpdates) {
            return;
        }
        updateSettingsModel();
        triggerStatusMessageUpdate();
        updateEnabledness();
    }

    /**
     *
     */
    private void updateFileHistoryPanel() {
        m_fileHistoryPanel.setEnabled(true);
        final FileSystemChoice fsChoice = ((FileSystemChoice)m_connections.getSelectedItem());

        switch (fsChoice.getType()) {
            case CUSTOM_URL_FS:
                m_fileHistoryPanel.setFileSystemBrowser(new LocalFileSystemBrowser());
                m_fileHistoryPanel.setBrowseable(false);
                break;
            case CONNECTED_FS:
                final Optional<FSConnection> fs =
                    FileSystemPortObjectSpec.getFileSystemConnection(getLastTableSpecs(), m_inPort);
                applySettingsForConnection(fsChoice, fs);
                break;
            case KNIME_FS:
                //FIXME for remote set Browser and browsable depending on connection
                m_fileHistoryPanel.setFileSystemBrowser(new LocalFileSystemBrowser());
                m_fileHistoryPanel.setBrowseable(true);
                break;
            case LOCAL_FS:
                m_fileHistoryPanel.setFileSystemBrowser(new LocalFileSystemBrowser());
                m_fileHistoryPanel.setBrowseable(true);
                break;
            default:
                m_fileHistoryPanel.setFileSystemBrowser(new LocalFileSystemBrowser());
                m_fileHistoryPanel.setBrowseable(false);
        }

    }

    private void applySettingsForConnection(final FileSystemChoice fsChoice, final Optional<FSConnection> fs) {
        if (fs.isPresent()) {
            m_fileHistoryPanel.setFileSystemBrowser(fs.get().getFileSystemBrowser());
            m_fileHistoryPanel.setBrowseable(true);
            m_statusMessage.setText("");
        } else {
            m_fileHistoryPanel.setFileSystemBrowser(new LocalFileSystemBrowser());
            m_fileHistoryPanel.setBrowseable(false);
            m_statusMessage.setForeground(Color.RED);
            m_statusMessage.setText(
                String.format("Connection to %s not available. Please execute the connector node.", fsChoice.getId()));
        }
    }

    /** Method called if file filter configuration button is clicked */
    private void showFileFilterConfigurationDialog() {
        Frame f = null;
        Container c = getComponentPanel().getParent();
        while (c != null) {
            if (c instanceof Frame) {
                f = (Frame)c;
                break;
            }
            c = c.getParent();
        }
        if (m_folderAndFilterSettings.getFileFilterDialog() == null) {
            m_folderAndFilterSettings.setFileFilterDialog(f);
        }
        final FilterType filterType = m_folderAndFilterSettings.getSelectedFilterType();
        final String filterExpression = m_folderAndFilterSettings.getSelectedFilterExpression();
        final boolean caseSensitive = m_folderAndFilterSettings.getCaseSensitive();

        m_folderAndFilterSettings.getFileFilterDialog().setLocationRelativeTo(c);
        m_folderAndFilterSettings.getFileFilterDialog().setVisible(true);

        if (m_folderAndFilterSettings.getFileFilterDialog().getResultStatus() == JOptionPane.OK_OPTION) {
            // updates the settings model, which in turn updates the UI
            updateSettingsModel();
            triggerStatusMessageUpdate();
        } else {
            // overwrites the values in the file filter panel components with those
            // from the settings model
            m_folderAndFilterSettings.updateFilterOptions((SettingsModelFileChooser2)getModel());
        }
    }

    /** Method to update enabledness of components */
    private void updateEnabledness() {
        if (m_connections.getSelectedItem().equals(FileSystemChoice.getKnimeFsChoice())) {
            // KNIME connections are selected
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, KNIME_CARD_VIEW_IDENTIFIER);

            m_knimeConnections.setEnabled(true);
            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText(FILE_FOLDER_LABEL);
        } else if (m_connections.getSelectedItem().equals(FileSystemChoice.getCustomFsUrlChoice())) {
            // Custom URLs are selected
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, DEFAULT_CARD_VIEW_IDENTIFIER);

            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText(URL_LABEL);
        } else {
            // some flow variable connection is selected, or we are using the local FS
            m_connectionSettingsCardLayout.show(m_connectionSettingsPanel, DEFAULT_CARD_VIEW_IDENTIFIER);

            m_fileFolderLabel.setEnabled(true);
            m_fileFolderLabel.setText(FILE_FOLDER_LABEL);
        }

        if (showFolderOptions()) {
            triggerFolderOptionsEnablednessCheck();
        }

        // if flow variable model contains a value disable options
        if (m_pathFlowVariableModel.getVariableValue().isPresent()) {
            m_folderAndFilterSettings.setEnabled(false);
        }

        getComponentPanel().repaint();
    }

    /** Method to check the necessity of folder and filter panel. */
    private void triggerFolderOptionsEnablednessCheck() {
        if (m_folderFilterPanelSwingWorker != null) {
            m_folderFilterPanelSwingWorker.cancel(true);
            m_folderFilterPanelSwingWorker = null;
        }

        final SettingsModelFileChooser2 model = (SettingsModelFileChooser2)getModel();
        if (model.getPathOrURL() != null && !model.getPathOrURL().isEmpty()) {
            try {
                final FileChooserHelper helper = new FileChooserHelper(m_fs, model.clone(), m_timeoutInMillis);
                m_folderFilterPanelSwingWorker = new FolderAndFilterPanelSwingWorker(helper, m_folderAndFilterSettings);
                m_folderFilterPanelSwingWorker.execute();
            } catch (final IOException ex) {
                // just catch
                // other swing worker will update status message if helper could not be instantiated
            }
        }
    }

    /** Method to update the status message */
    private void triggerStatusMessageUpdate() {
        if (m_statusMessageSwingWorker != null) {
            m_exceptionMsg = m_statusMessageSwingWorker.getLatestWarning();
            m_statusMessageSwingWorker.cancel(true);
            m_statusMessageSwingWorker = null;
        }

        final SettingsModelFileChooser2 model = (SettingsModelFileChooser2)getModel();
        if (model.getPathOrURL() != null && !model.getPathOrURL().isEmpty()) {
            try {
                final FileChooserHelper helper =
                    new FileChooserHelper(m_fs, model.clone(), m_timeoutInMillis);
                m_statusMessageSwingWorker = new StatusMessageSwingWorker(helper, m_statusMessage, m_dialogType, m_fileSelectionMode);
                m_statusMessageSwingWorker.execute();
            } catch (Exception ex) {
                m_statusMessage.setForeground(Color.RED);
                m_statusMessage
                    .setText("Could not get file system: " + ExceptionUtil.getDeepestErrorMessage(ex, false));
            }
        } else {
            m_statusMessage.setText("");
        }
    }

    /**
     * @param timeoutInMillis the timeout in milliseconds for the custom url file system
     */
    public void setTimeout(final int timeoutInMillis) {
        m_timeoutInMillis = timeoutInMillis;
    }

    @Override
    protected void updateComponent() {
        if (m_ignoreUpdates) {
            return;
        }

        m_fs = FileSystemPortObjectSpec.getFileSystemConnection(getLastTableSpecs(), m_inPort);

        m_ignoreUpdates = true;

        // add any connected connections
        updateConnectedConnectionsCombo();

        final SettingsModelFileChooser2 model = (SettingsModelFileChooser2)getModel();

        // sync connection combo box
        final FileSystemChoice fileSystem = model.getFileSystemChoice();
        if ((fileSystem != null) && !fileSystem.equals(m_connections.getSelectedItem())) {
            m_connections.setSelectedItem(fileSystem);
        }
        // sync knime connection check box
        final KNIMEConnection knimeFileSystem =
            KNIMEConnection.getOrCreateMountpointAbsoluteConnection(model.getKNIMEFileSystem());

        if ((knimeFileSystem != null) && !knimeFileSystem.equals(m_knimeConnections.getSelectedItem())) {
            final DefaultComboBoxModel<KNIMEConnection> knimeConnectionsModel =
                (DefaultComboBoxModel<KNIMEConnection>)m_knimeConnections.getModel();
            if (knimeConnectionsModel.getIndexOf(knimeFileSystem) == -1) {
                //If the Connection did not exsist before
                knimeConnectionsModel.addElement(knimeFileSystem);
            }
            m_knimeConnections.setSelectedItem(knimeFileSystem);
        }

        // sync file history panel
        final String pathOrUrl = model.getPathOrURL() != null ? model.getPathOrURL() : "";
        if (!pathOrUrl.equals(m_fileHistoryPanel.getSelectedFile())) {
            m_fileHistoryPanel.setSelectedFile(pathOrUrl);
        }

        // sync filter mode combo box
        if (showFolderOptions()) {
            m_folderAndFilterSettings.syncComponents(model);
        }

        setEnabledComponents(model.isEnabled());
        updateFileHistoryPanel();
        triggerStatusMessageUpdate();
        m_ignoreUpdates = false;
    }

    /** Method to update and add default file system connections to combo box */
    private void updateConnectionsCombo() {
        final DefaultComboBoxModel<FileSystemChoice> connectionsModel =
            (DefaultComboBoxModel<FileSystemChoice>)m_connections.getModel();
        connectionsModel.removeAllElements();
        FileSystemChoice.getDefaultChoices().stream().forEach(c -> connectionsModel.addElement(c));
    }

    /** Method to update and add connected file system connections to combo box */
    private void updateConnectedConnectionsCombo() {
        updateConnectionsCombo();
        final DefaultComboBoxModel<FileSystemChoice> connectionsModel =
            (DefaultComboBoxModel<FileSystemChoice>)m_connections.getModel();
        if (getLastTableSpecs() != null && getLastTableSpecs().length > 0) {
            final FileSystemPortObjectSpec fspos = (FileSystemPortObjectSpec)getLastTableSpec(m_inPort);
            if (fspos != null) {
                final FileSystemChoice choice =
                    FileSystemChoice.createConnectedFileSystemChoice(fspos.getFileSystemType());
                if (connectionsModel.getIndexOf(choice) < 0) {
                    connectionsModel.insertElementAt(choice, 0);
                }
            }
        }
    }

    /** Method to update and add KNIME file system connections to combo box */
    private void updateKNIMEConnectionsCombo() {
        final DefaultComboBoxModel<KNIMEConnection> knimeConnectionsModel =
            (DefaultComboBoxModel<KNIMEConnection>)m_knimeConnections.getModel();
        knimeConnectionsModel.removeAllElements();
        MountPointIDProviderService.instance().getAllMountedIDs().stream().forEach(
            id -> knimeConnectionsModel.addElement(KNIMEConnection.getOrCreateMountpointAbsoluteConnection(id)));
        knimeConnectionsModel.addElement(KNIMEConnection.MOUNTPOINT_RELATIVE_CONNECTION);
        knimeConnectionsModel.addElement(KNIMEConnection.WORKFLOW_RELATIVE_CONNECTION);
        knimeConnectionsModel.addElement(KNIMEConnection.NODE_RELATIVE_CONNECTION);

    }

    private void updateSettingsModel() {
        m_ignoreUpdates = true;
        final SettingsModelFileChooser2 model = (SettingsModelFileChooser2)getModel();
        final FileSystemChoice fsChoice = ((FileSystemChoice)m_connections.getSelectedItem());
        model.setFileSystem(fsChoice.getId());
        if (fsChoice.equals(FileSystemChoice.getKnimeFsChoice())) {
            final KNIMEConnection connection = (KNIMEConnection)m_knimeConnections.getModel().getSelectedItem();
            model.setKNIMEFileSystem(connection.getId());
        }
        model.setPathOrURL(m_fileHistoryPanel.getSelectedFile());
        if (showFolderOptions()) {
            m_folderAndFilterSettings.updateSettingsModel(model);
        }
        m_ignoreUpdates = false;
    }

    private boolean showFolderOptions() {
        return m_dialogType.equals(DialogType.OPEN_DIALOG)
            && (m_fileSelectionMode.equals(FileSelectionMode.DIRECTORIES_ONLY)
                || m_fileSelectionMode.equals(FileSelectionMode.FILES_AND_DIRECTORIES));
    }

    @Override
    protected void validateSettingsBeforeSave() throws InvalidSettingsException {
        updateSettingsModel();
        //FIXME in case of KNIME connection check whether it is a valid connection
        m_fileHistoryPanel.addToHistory();
        if (m_exceptionMsg.isPresent()) {
            throw new InvalidSettingsException(m_exceptionMsg.get());
        }
    }

    @Override
    protected void checkConfigurabilityBeforeLoad(final PortObjectSpec[] specs) throws NotConfigurableException {
        // this 'should' be done during #loadSettingsFrom (which is final) -- this method is called from there
        m_fileHistoryPanel.updateHistory();
        // otherwise we are good - independent of the incoming spec
    }

    @Override
    protected void setEnabledComponents(final boolean enabled) {
        m_connectionLabel.setEnabled(enabled);
        m_fileHistoryPanel.setEnabled(enabled);
        if (enabled) {
            updateEnabledness();
        } else if (showFolderOptions()) {
            //disable only
            m_folderAndFilterSettings.setEnabled(enabled);
        }
    }

    /**
     * Forces the given file extension when the user enters a path in the text field that does not end with the argument
     * extension.
     *
     * @param forcedExtension optional parameter to force a file extension to be appended to the selected file name,
     *            e.g. ".txt" (null and blanks not force any extension).
     */
    public final void setForceExtensionOnSave(final String forcedExtension) {
        m_fileHistoryPanel.setForceExtensionOnSave(forcedExtension);
    }

    @Override
    public void setToolTipText(final String text) {
        // Nothing to show here ...
    }
}
