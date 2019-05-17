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
 *   May 16, 2019 (knime): created
 */
package org.knime.base.node.mine.regression.gaussian.process.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;

import org.knime.base.node.mine.regression.gaussian.process.learner.GaussianProcessRegression;
import org.knime.core.data.filestore.FileStore;
import org.knime.core.data.filestore.FileStorePortObject;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.ModelContent;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;

import com.google.common.collect.Lists;

/**
 *
 * @author knime
 */
public class GaussianProcessRegressionPortObject extends FileStorePortObject {

    /**
     *
     * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
     */
    public static final class Serializer extends PortObjectSerializer<GaussianProcessRegressionPortObject> {

        /**
         * {@inheritDoc}
         */
        @Override
        public void savePortObject(final GaussianProcessRegressionPortObject portObject,
            final PortObjectZipOutputStream out, final ExecutionMonitor exec)
            throws IOException, CanceledExecutionException {
            GaussianProcessRegressionPortObject.save(out, exec);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public GaussianProcessRegressionPortObject loadPortObject(final PortObjectZipInputStream in,
            final PortObjectSpec spec, final ExecutionMonitor exec) throws IOException, CanceledExecutionException {
            final GaussianProcessRegressionPortObject model = new GaussianProcessRegressionPortObject();
            model.load(in, spec, exec);
            return model;
        }
    }

    private static final String CFG_MODELCONTENT = "modelContent";

    /**
     * @param spec
     * @param gpModel
     * @param fileStore
     * @return gp
     */
    public static GaussianProcessRegressionPortObject createPortObject(
        final GaussianProcessRegressionPortObjectSpec spec, final GaussianProcessRegression<double[]> gpModel,
        final FileStore fileStore) {
        final GaussianProcessRegressionPortObject po =
            new GaussianProcessRegressionPortObject(gpModel, spec, fileStore);
        try {
            serialize(gpModel, fileStore);
        } catch (final IOException e) {
            throw new IllegalStateException("Something went wrong during serialization.", e);
        }
        return po;
    }

    private GaussianProcessRegression<double[]> m_gp;

    private GaussianProcessRegressionPortObjectSpec m_spec;

    private WeakReference<GaussianProcessRegression<double[]>> m_modelRef;

    /**
     * @param gp
     * @param spec
     *
     */
    private GaussianProcessRegressionPortObject(final GaussianProcessRegression<double[]> gp,
        final GaussianProcessRegressionPortObjectSpec spec, final FileStore fs) {
        super(Lists.newArrayList(fs));
        m_gp = gp;
        m_spec = spec;
    }

    /** Framework constructor, not to be used by node code. */
    public GaussianProcessRegressionPortObject() {
        // no op, load method to be called by framework
    }

    /**
     * @return the ensembleModel
     */
    public synchronized GaussianProcessRegression<double[]> getGPModel() {
        GaussianProcessRegression<double[]> gpModel = m_modelRef.get();
        if (gpModel == null) {
            try {
                gpModel = deserialize();
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException("Something went wrong during deserialization.", e);
            }
            m_modelRef = new WeakReference<>(gpModel);
        }
        return gpModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSummary() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PortObjectSpec getSpec() {
        return m_spec;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent[] getViews() {
        return null;
    }

    /**
     * @return the gaussian process regression
     */
    public GaussianProcessRegression<double[]> getGP() {
        return m_gp;
    }

    private static void serialize(final GaussianProcessRegression<double[]> gp, final FileStore fileStore)
        throws IOException {
        final File file = fileStore.getFile();
        try (FileOutputStream out = new FileOutputStream(file);
                ObjectOutputStream objOut = new ObjectOutputStream(out);) {
            objOut.writeObject(gp);
        }
    }

    private GaussianProcessRegression<double[]> deserialize() throws IOException, ClassNotFoundException {
        final File file = getFileStore(0).getFile();
        GaussianProcessRegression<double[]> gpModel;
        try (FileInputStream out = new FileInputStream(file); ObjectInputStream objIn = new ObjectInputStream(out);) {
            gpModel = (GaussianProcessRegression<double[]>)objIn.readObject();
            return gpModel;
        }
    }

    private static void save(final PortObjectZipOutputStream out, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        final ModelContent mc = new ModelContent(CFG_MODELCONTENT);
        mc.saveToXML(out);
    }

    private void load(final PortObjectZipInputStream in, final PortObjectSpec spec, final ExecutionMonitor exec)
        throws IOException, CanceledExecutionException {
        m_spec = (GaussianProcessRegressionPortObjectSpec)spec;
        m_modelRef = new WeakReference<GaussianProcessRegression<double[]>>(null);
    }

}
