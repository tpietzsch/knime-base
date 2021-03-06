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
 * -------------------------------------------------------------------
 *
 * History
 *   21.08.2007 (ohl): created
 */
package org.knime.base.node.io.filereader;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Optional;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.knime.core.node.util.SharedIcons;

/**
 * Implements the tab panel for the character set settings (in the advanced settings dialog).
 *
 * @author Peter Ohl, University of Konstanz
 */
public class CharsetNamePanel extends JPanel {

    private static final long serialVersionUID = 2016L;

    // action command for the "default" button
    private static final String DEFAULT_LABEL = "OS default (" + Charset.defaultCharset().name() + ")";

    // action command for the "enter your own char set name" button
    private static final String CUSTOM_LABEL = "Other";

    private static final Icon ERROR_ICON = SharedIcons.ERROR.get();

    private final ButtonGroup m_group = new ButtonGroup();

    /*
     * use labels that are valid charset names (we use them later
     * directly as parameter). Except for "default" and "user defined".
     */
    private final JRadioButton m_default =
        createButton(DEFAULT_LABEL, "Uses the default decoding set by the operating system");

    private final JRadioButton m_usASCII = createButton("US-ASCII", "Seven-bit ASCII, also referred to as US-ASCII");

    private final JRadioButton m_iso8859 = createButton("ISO-8859-1", "ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1");

    private final JRadioButton m_utf8 = createButton("UTF-8", "Eight-bit UCS Transformation Format");

    private final JRadioButton m_utf16le =
        createButton("UTF-16LE", "Sixteen-bit UCS Transformation Format, little-endian byte order");

    private final JRadioButton m_utf16be =
        createButton("UTF-16BE", "Sixteen-bit UCS Transformation Format, big-endian byte order");

    private final JRadioButton m_utf16 = createButton("UTF-16",
        "Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark in the file");

    private final JRadioButton m_custom =
        createButton(CUSTOM_LABEL, "Enter a valid charset name supported by the Java Virtual Machine");

    private final JTextField m_customName = createTextField();

    private final JLabel m_customError = createCustomErrorLabel();

    private final JLabel m_encodingWarning = new JLabel();

    private static JLabel createCustomErrorLabel() {
        final JLabel label = new JLabel();
        label.setIcon(ERROR_ICON);
        label.setVisible(false);
        return label;
    }

    /** Init UI. */
    private CharsetNamePanel() {
        this.setSize(520, 375);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(getSelectionPanel());
    }

    /**
     * Creates a panel to select the character set name and initializes it from the passed object.
     *
     * @param settings the settings to initialize to panel from
     * @since 3.1
     */
    public CharsetNamePanel(final FileReaderSettings settings) {
        this();
        loadSettings(settings);
    }

    /**
     * Creates a panel to select the character set name and initializes it from the passed object.
     *
     * @param charsetName the name of the charset, possibly null, see {@link #setCharsetName(String)}.
     * @since 4.0
     */
    public CharsetNamePanel(final String charsetName) {
        this();
        setCharsetName(charsetName);
    }

    private JRadioButton createButton(final String label, final String tooltip) {
        final JRadioButton button = new JRadioButton(label);
        button.setToolTipText(tooltip);
        button.addChangeListener(e -> buttonsChanged());
        m_group.add(button);
        return button;
    }

    private JTextField createTextField() {
        final JTextField textField = new JTextField(20);
        textField.setPreferredSize(new Dimension(250, 25));
        textField.setMaximumSize(new Dimension(250, 25));
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent e) {
                checkCustomCharsetName();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                checkCustomCharsetName();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                checkCustomCharsetName();
            }
        });
        return textField;
    }

    private Container getSelectionPanel() {


//        m_group.add(m_default);
//
//        m_group.add(m_usASCII);
//
//        m_group.add(m_iso8859);
//
//        m_group.add(m_utf8);
//
//        m_group.add(m_utf16le);
//
//        m_group.add(m_utf16be);
//
//        m_group.add(m_utf16);
//
//        m_group.add(m_custom);


        JPanel result = new JPanel(new GridBagLayout());
        result.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
            "Select a character set for the encoding type:"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        result.add(m_default, gbc);
        gbc.gridy++;
        result.add(m_iso8859, gbc);
        gbc.gridy++;
        result.add(m_usASCII, gbc);
        gbc.gridy++;
        result.add(m_utf8, gbc);
        gbc.gridy++;
        result.add(m_utf16, gbc);
        gbc.gridy++;
        result.add(m_utf16be, gbc);
        gbc.gridy++;
        result.add(m_utf16le, gbc);
        gbc.gridy++;
        result.add(m_custom, gbc);

        gbc.gridy++;
        result.add(createCustomPanel(), gbc);
        gbc.gridx++;
        gbc.gridy++;
        gbc.weightx = 1;
        gbc.weighty = 1;
        result.add(new JPanel(), gbc);

        return result;
    }

    private JPanel createCustomPanel() {
        final JPanel customItems = new JPanel(new GridBagLayout());
        final GridBagConstraints gbcCustom = new GridBagConstraints();
        gbcCustom.gridx = 0;
        gbcCustom.insets = new Insets(0, 25, 0, 0);
        customItems.add(m_customName, gbcCustom);
        gbcCustom.gridx++;
        gbcCustom.anchor = GridBagConstraints.CENTER;
        gbcCustom.insets = new Insets(0, 10, 0, 0);
        customItems.add(m_customError, gbcCustom);
        gbcCustom.gridx++;
        gbcCustom.anchor = GridBagConstraints.CENTER;
        gbcCustom.insets = new Insets(0, 3, 0, 0);
        customItems.add(m_encodingWarning, gbcCustom);
        return customItems;
    }

    /**
     * Sets the enable status according to the current selection.
     */
    private void buttonsChanged() {
        m_customName.setEnabled(m_custom.isSelected());
        checkCustomCharsetName();
    }

    /**
     * Tests the entered charset name (if the textfield is enabled), and colors the textfield in case of an error.
     *
     * @return true if the entered charset name is supported or the textfield is disabled.
     */
    private boolean checkCustomCharsetName() {
        if (!m_custom.isSelected()) {
            m_customName.setText("");
            m_encodingWarning.setText("");
            m_customError.setVisible(false);
            return true;
        }

        String cs = m_customName.getText();
        try {
            if (Charset.isSupported(cs)) {
                m_encodingWarning.setText("");
                m_customError.setVisible(false);
                return true;
            } else {
                m_encodingWarning.setText(String.format("The encoding \"%s\" is not supported.", cs));
                m_customError.setVisible(true);
                return false;
            }
        } catch (IllegalArgumentException iae) {
            m_encodingWarning.setText(String.format("The encoding \"%s\" is not supported.", cs));
            m_customError.setVisible(true);
            return false;
        }
    }

    /**
     * @param settings
     * @since 3.1
     */
    public void loadSettings(final FileReaderSettings settings) {
        String csName = settings.getCharsetName();
        setCharsetName(csName);
    }

    /**
     * Sets the new charset name. Null will choose the 'default' value.
     *
     * @param charsetName Name of charset or null
     * @since 4.0
     */
    public final void setCharsetName(final String charsetName) {
        if (charsetName == null) {
            // the default
            m_default.setSelected(true);
        } else {
            boolean foundIt = false;
            Enumeration<AbstractButton> buttons = m_group.getElements();
            while (buttons.hasMoreElements()) {
                AbstractButton b = buttons.nextElement();
                if (charsetName.equals(b.getActionCommand())) {
                    foundIt = true;
                    b.setSelected(true);
                    break;
                }
            }
            if (!foundIt) {
                m_custom.setSelected(true);
                m_customName.setText(charsetName);
            }
        }

    }

    /**
     * Checks if the settings in the panel are good for applying them.
     *
     * @return null if all settings are okay, or an error message if settings can't be taken over.
     * @since 3.1
     *
     */
    public String checkSettings() {
        Enumeration<AbstractButton> buttons = m_group.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton b = buttons.nextElement();
            if (b.isSelected()) {
                if (CUSTOM_LABEL.equals(b.getActionCommand())) {
                    if ((m_customName.getText() == null) || (m_customName.getText().isEmpty())) {
                        return "Please enter a character set name";
                    }
                    if (!checkCustomCharsetName()) {
                        return "The entered character set \"" + m_customName.getText()
                            + "\" is not supported by this Java VM";
                    }
                }
                break;
            }
        }
        return null;
    }

    /**
     * Writes the current settings of the panel into the passed settings object.
     *
     * @param settings the object to write settings in
     * @return true if the new settings are different from the one passed in.
     * @since 3.2
     */
    public boolean overrideSettings(final FileReaderNodeSettings settings) {
        String oldCSN = settings.getCharsetName();
        String newCSN = getSelectedCharsetName().orElse(null);

        settings.setCharsetName(newCSN);

        boolean changed;
        if (oldCSN == null) {
            changed = newCSN != null;
        } else {
            changed = !oldCSN.equals(newCSN);
        }
        if (changed) {
            settings.setCharsetUserSet(true);
        }

        return changed;
    }

    /**
     * Get the name of the selected charset or an empty optional if 'default' was chosen.
     *
     * @return that value
     * @since 4.0
     */
    public final Optional<String> getSelectedCharsetName() {
        Enumeration<AbstractButton> buttons = m_group.getElements();
        while (buttons.hasMoreElements()) {
            AbstractButton b = buttons.nextElement();
            if (b.isSelected()) {
                String newCSN = b.getActionCommand();
                if (CUSTOM_LABEL.equals(newCSN)) {
                    return Optional.of(m_customName.getText());
                } else if (DEFAULT_LABEL.equals(newCSN)) {
                    return Optional.empty();
                } else {
                    return Optional.of(newCSN);
                }
            }
        }
        return Optional.empty();
    }
}
