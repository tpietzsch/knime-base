/* 
 * 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2006
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
 * If you have any quesions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 * 
 * History
 *   23.07.2005 (mb): created
 */
package org.knime.base.node.mine.decisiontree.predictor.decisiontree;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.knime.base.data.util.DataCellStringMapper;

/**
 * 
 * @author Michael Berthold, University of Konstanz
 */
public class DecisionTreeNodeSplitContinuous extends DecisionTreeNodeSplit {
    /** The node logger for this class. */
    // private static final NodeLogger LOGGER =
    // NodeLogger.getLogger(DecisionTreeNodeSplitContinuous.class);
    private double m_threshold = 0.0;

    private HashMap<Color, Double> m_coveredColors = new HashMap<Color, Double>();

    /**
     * Empty Constructor visible only within package.
     */
    DecisionTreeNodeSplitContinuous() {
    }

    /**
     * Constructor of derived class. Read all type-specific information from XML
     * File.
     * 
     * @param xmlNode XML node info
     * @param mapper map translating column names to DataCells and vice versa
     */
    public DecisionTreeNodeSplitContinuous(final Node xmlNode,
            final DataCellStringMapper mapper) {
        super(xmlNode, mapper); // let super read all type-invariant info
        super.makeRoomForKids(2);
        // now read information related to a split on a continuous attribute
        Node splitNode = xmlNode.getChildNodes().item(3);
        assert splitNode.getNodeName().equals("SPLIT");
        String nrBranches = splitNode.getAttributes().getNamedItem("branches")
                .getNodeValue();
        assert (nrBranches.equals("2"));
        NodeList splitKids = splitNode.getChildNodes();
        for (int i = 0; i < splitKids.getLength(); i++) {
            if (splitKids.item(i).getNodeName().equals("BRANCH")) {
                Node branchNode = splitKids.item(i);
                String id = branchNode.getAttributes().getNamedItem("id")
                        .getNodeValue();
                String nodeId = branchNode.getAttributes().getNamedItem(
                        "nodeId").getNodeValue();
                String cond = branchNode.getAttributes().getNamedItem("cond")
                        .getNodeValue();
                if (id.equals("1")) {
                    super.setChildNodeIndex(0, Integer.parseInt(nodeId));
                    // branch no. 0 (left) should always tbe the "<=" one
                    assert cond.equals("leq");
                }
                if (id.equals("2")) {
                    super.setChildNodeIndex(1, Integer.parseInt(nodeId));
                    // branch no. 1 (right) should always tbe the ">" one
                    assert cond.equals("gt");
                }
            }
            if (splitKids.item(i).getNodeName().equals("CONTINUOUS")) {
                Node contNode = splitKids.item(i);
                String cut = contNode.getAttributes().getNamedItem("Cut")
                        .getNodeValue();
                String lower = contNode.getAttributes().getNamedItem("Lower")
                        .getNodeValue();
                String upper = contNode.getAttributes().getNamedItem("Upper")
                        .getNodeValue();
                assert cut.equals(lower);
                assert cut.equals(upper);
                m_threshold = Double.parseDouble(cut);
            }
        }
    }

    /**
     * Determine class counts for a new pattern given as a row of values.
     * Returns a HashMap listing counts for all classes. For the continuous
     * split we need to analyse the attribute for this split and then ask the
     * left resp. right subtree for it's prediction. Whoever calls us was nice
     * enough to already pick out the DataCell used for this split so we do not
     * need to find it. It is also guaranteed that it is not missing and of the
     * right type.
     * 
     * @param cell the cell to be used for the split at this level
     * @param row input pattern
     * @param spec the corresponding table spec
     * @return HashMap class/count
     * @throws Exception if something went wrong (unknown attriubte for example)
     */
    @Override
    public HashMap<DataCell, Double> getClassCounts(final DataCell cell,
            final DataRow row, final DataTableSpec spec) throws Exception {
        assert cell.getType().isCompatible(DoubleValue.class);
        double value = ((DoubleValue)cell).getDoubleValue();
        if (value <= m_threshold) {
            return super.getChildNodeAt(0).getClassCounts(row, spec);
        }
        return super.getChildNodeAt(1).getClassCounts(row, spec);
    }

    /**
     * Add patterns given as a row of values if they fall within a specific
     * node. This node simply forwards this request to the appropriate child.
     * 
     * @param cell the cell to be used for the split at this level
     * @param row input pattern
     * @param spec the corresponding table spec
     * @throws Exception if something went wrong (unknown attriubte for example)
     */
    @Override
    public void addCoveredPattern(final DataCell cell, final DataRow row,
            final DataTableSpec spec) throws Exception {
        double value = ((DoubleValue)cell).getDoubleValue();
        if (value <= m_threshold) {
            super.getChildNodeAt(0).addCoveredPattern(row, spec);
        } else {
            super.getChildNodeAt(1).addCoveredPattern(row, spec);
        }
        Color col = spec.getRowColor(row).getColor();
        if (m_coveredColors.containsKey(col)) {
            Double oldCount = m_coveredColors.get(col);
            m_coveredColors.remove(col);
            m_coveredColors.put(col, new Double(oldCount.doubleValue() + 1.0));
        } else {
            m_coveredColors.put(col, new Double(1.0));
        }
    }

    /**
     * @see DecisionTreeNode
     *      #coveredColors()
     */
    @Override
    public HashMap<Color, Double> coveredColors() {
        return m_coveredColors;
    }

    /**
     * @see DecisionTreeNode#coveredPattern()
     */
    @Override
    public Set<DataCell> coveredPattern() {
        Set<DataCell> resultL = null;
        Set<DataCell> resultR = null;
        if (super.getChildNodeAt(0) != null) {
            resultL = super.getChildNodeAt(0).coveredPattern();
        }
        if (super.getChildNodeAt(1) != null) {
            resultR = super.getChildNodeAt(1).coveredPattern();
        }
        if (resultR == null) {
            return resultL;
        }
        if (resultL == null) {
            return resultR;
        }
        HashSet<DataCell> result = new HashSet<DataCell>(resultL);
        result.addAll(resultR);
        return result;
    }

    /**
     * @see DecisionTreeNode
     *      #getStringSummary()
     */
    @Override
    public String getStringSummary() {
        return "split attr. '" + getSplitAttr() + "' at " + m_threshold
                + " (<=,>)";
    }

    /**
     * @see DecisionTreeNode
     *      #addNodeToTreeDepthFirst(DecisionTreeNode,
     *      int)
     */
    @Override
    public boolean addNodeToTreeDepthFirst(final DecisionTreeNode node,
            final int ix) {
        if (!super.addNodeToTreeDepthFirst(node, ix)) {
            return false;
        }
        if (super.getChildNodeAt(0) != null) {
            super.getChildNodeAt(0).setPrefix(
                    getSplitAttr() + " <= " + m_threshold);
        }
        if (super.getChildNodeAt(1) != null) {
            super.getChildNodeAt(1).setPrefix(
                    getSplitAttr() + " > " + m_threshold);
        }
        return true;
    }

    /**
     * @see DecisionTreeNodeSplit
     *      #saveNodeSplitInternalsToPredParams(org.knime.core.node.ModelContentWO)
     */
    @Override
    public void saveNodeSplitInternalsToPredParams(final ModelContentWO pConf) {
        pConf.addDouble("threshold", m_threshold);
    }

    /**
     * 
     * @see DecisionTreeNodeSplit
     *      #loadNodeSplitInternalsFromPredParams(org.knime.core.node.ModelContentRO)
     */
    @Override
    public void loadNodeSplitInternalsFromPredParams(final ModelContentRO pConf)
            throws InvalidSettingsException {
        m_threshold = pConf.getDouble("threshold");
    }

}
