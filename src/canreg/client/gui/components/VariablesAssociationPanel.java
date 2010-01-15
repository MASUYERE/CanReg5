/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * VariablesAssociationPanel.java
 *
 * Created on 14-Jan-2010, 14:25:54
 */
package canreg.client.gui.components;

import canreg.client.dataentry.Relation;
import canreg.common.DatabaseVariablesListElement;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ervikm
 */
public class VariablesAssociationPanel extends javax.swing.JPanel {

    private List<VariableMappingAlternativePanel> panelList;

    /** Creates new form VariablesAssociationPanel */
    public VariablesAssociationPanel() {
        initComponents();
    }

    public void initializeVariableMappingPanel(List<Relation> map, DatabaseVariablesListElement[] databaseVariablesListElements, String[] fileElements) {
        // Remove all variable mappings
        variablesPanel.removeAll();
        panelList = new LinkedList<VariableMappingAlternativePanel>();

        // Add the panels
        for (DatabaseVariablesListElement dbvle : databaseVariablesListElements) {
            VariableMappingAlternativePanel vmp = new VariableMappingAlternativePanel();
            panelList.add(vmp);
            vmp.setFileElements(fileElements);
            boolean found = false;
            int i = 0;
            Relation rel = null;
            while (!found && i < map.size()) {
                rel = map.get(i++);
                if (rel != null) {
                    found = rel.getDatabaseTableVariableID() == dbvle.getDatabaseTableVariableID();
                }
            }
            if (found) {
                vmp.setSelectedFileElement(rel.getFileVariableName());
            } else {
                vmp.setSelectedFileElement(null);
            }
            vmp.setDBVariable(dbvle);
            variablesPanel.add(vmp);
            vmp.setVisible(true);
        }

        variablesPanel.revalidate();
        variablesPanel.repaint();
    }

    public List<VariableMappingAlternativePanel> getPanelList() {
        return panelList;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        associateVariablesPanel = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        variablesScrollPane = new javax.swing.JScrollPane();
        variablesPanel = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        associateVariablesPanel.setName("associateVariablesPanel"); // NOI18N

        jLabel8.setName("jLabel8"); // NOI18N

        variablesScrollPane.setName("variablesScrollPane"); // NOI18N

        variablesPanel.setName("variablesPanel"); // NOI18N
        variablesPanel.setLayout(new java.awt.GridLayout(0, 1));
        variablesScrollPane.setViewportView(variablesPanel);

        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariablesAssociationPanel.class);
        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N
        jSplitPane1.setLeftComponent(jLabel2);

        jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
        jLabel4.setMaximumSize(new java.awt.Dimension(139, 14));
        jLabel4.setMinimumSize(new java.awt.Dimension(139, 14));
        jLabel4.setName("jLabel4"); // NOI18N
        jSplitPane1.setRightComponent(jLabel4);

        javax.swing.GroupLayout associateVariablesPanelLayout = new javax.swing.GroupLayout(associateVariablesPanel);
        associateVariablesPanel.setLayout(associateVariablesPanelLayout);
        associateVariablesPanelLayout.setHorizontalGroup(
            associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(associateVariablesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
                    .addComponent(jLabel8)
                    .addComponent(variablesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE))
                .addContainerGap())
        );
        associateVariablesPanelLayout.setVerticalGroup(
            associateVariablesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(associateVariablesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variablesScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 531, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(associateVariablesPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 538, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(associateVariablesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel associateVariablesPanel;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel variablesPanel;
    private javax.swing.JScrollPane variablesScrollPane;
    // End of variables declaration//GEN-END:variables
}
