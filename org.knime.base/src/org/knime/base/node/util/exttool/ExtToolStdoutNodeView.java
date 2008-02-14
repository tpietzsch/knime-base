/* ------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2008
 * University of Konstanz, Germany.
 * Chair for Bioinformatics and Information Mining
 * Prof. Dr. Michael R. Berthold
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * --------------------------------------------------------------------- *
 *
 * History
 *   12.10.2006 (ohl): created
 */
package org.knime.base.node.util.exttool;

import java.util.Arrays;
import java.util.Collection;

import org.knime.core.node.NodeModel;

/**
 * The view showing the output to standard output. Listens to notifications
 * (see {@link ViewUpdateNotice}) of type <code>stdout</code>.
 *
 * @author ohl, University of Konstanz
 */
public class ExtToolStdoutNodeView extends ExtToolOutputNodeView {

    /**
     * The constructor.
     *
     * @param nodeModel the model associated with this view.
     */
    public ExtToolStdoutNodeView(final NodeModel nodeModel) {
        super(nodeModel);
        setViewTitle("Output to standard output");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void updateModel(final Object arg) {
        if (arg instanceof ViewUpdateNotice) {
            ViewUpdateNotice vun = (ViewUpdateNotice)arg;
            if (vun.TYPE == ViewUpdateNotice.ViewType.stdout) {
                addLineInSwingThreadLater(vun.getNewLine());
            }
        } else if (arg == null) {
            clearText();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getFullFailureOutput() {
        return ((ExtToolOutputNodeModel)getNodeModel()).
            getFailedExternalOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getFullOutput() {
        return ((ExtToolOutputNodeModel)getNodeModel()).
            getExternalOutput();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Collection<String> getNoOutputText() {
        return Arrays.asList(new String[]{"No output to standard output"});
    }
}
