/*
 * VariablesExportDetailsPanel.java
 *
 * Created on 23 June 2008, 14:07
 */
package canreg.client.gui.components;

import canreg.common.DatabaseVariablesListElement;
import canreg.server.database.Dictionary;

/**
 *
 * @author  ervikm
 */
public class VariablesExportDetailsPanel extends javax.swing.JPanel {

    private DatabaseVariablesListElement variable;
    private Dictionary dictionary;

    /** Creates new form VariablesExportDetailsPanel */
    public VariablesExportDetailsPanel() {
        initComponents();
    }

    private void setVariableName(String variableName) {
        variableNameLabel.setText(variableName);
    }

    private void setVariableType(String type) {

        if (type.equalsIgnoreCase("Dict")) {
            dictionaryDescriptionCheckBox.setVisible(true);
            if (variable != null && variable.isDictionaryCompound()) {
                dictionaryCategoryCheckBox.setVisible(true);
            } else {
                dictionaryCategoryCheckBox.setVisible(false);
            }
        } else {
            dictionaryCategoryCheckBox.setVisible(false);
            dictionaryDescriptionCheckBox.setVisible(false);
        }
    }

    /**
     * 
     * @param variable
     */
    public void setVariable(DatabaseVariablesListElement variable) {
        this.variable = variable;
        setVariableName(variable.getFullName());
        setVariableType(variable.getVariableType());
    }

    /**
     * 
     * @param bool
     */
    public void setDataCheckBox(boolean bool) {
        dataCheckBox.setSelected(bool);
    }

    /**
     * 
     * @return
     */
    public boolean[] getCheckboxes() {
        return new boolean[]{dataCheckBox.isSelected(), dictionaryCategoryCheckBox.isSelected(), dictionaryDescriptionCheckBox.isSelected()};
    }

    public DatabaseVariablesListElement getVariable() {
        return variable;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        variableNameLabel = new javax.swing.JLabel();
        dataCheckBox = new javax.swing.JCheckBox();
        dictionaryCategoryCheckBox = new javax.swing.JCheckBox();
        dictionaryDescriptionCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();

        setName("Form"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(VariablesExportDetailsPanel.class);
        variableNameLabel.setText(resourceMap.getString("variableNameLabel.text")); // NOI18N
        variableNameLabel.setName("variableNameLabel"); // NOI18N

        dataCheckBox.setText(resourceMap.getString("dataCheckBox.text")); // NOI18N
        dataCheckBox.setToolTipText(resourceMap.getString("dataCheckBox.toolTipText")); // NOI18N
        dataCheckBox.setName("dataCheckBox"); // NOI18N

        dictionaryCategoryCheckBox.setText(resourceMap.getString("dictionaryCategoryCheckBox.text")); // NOI18N
        dictionaryCategoryCheckBox.setToolTipText(resourceMap.getString("dictionaryCategoryCheckBox.toolTipText")); // NOI18N
        dictionaryCategoryCheckBox.setEnabled(false);
        dictionaryCategoryCheckBox.setName("dictionaryCategoryCheckBox"); // NOI18N

        dictionaryDescriptionCheckBox.setText(resourceMap.getString("dictionaryDescriptionCheckBox.text")); // NOI18N
        dictionaryDescriptionCheckBox.setToolTipText(resourceMap.getString("dictionaryDescriptionCheckBox.toolTipText")); // NOI18N
        dictionaryDescriptionCheckBox.setEnabled(false);
        dictionaryDescriptionCheckBox.setName("dictionaryDescriptionCheckBox"); // NOI18N

        jSeparator1.setName("jSeparator1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(dataCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dictionaryCategoryCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dictionaryDescriptionCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(variableNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(variableNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dataCheckBox)
                    .addComponent(dictionaryCategoryCheckBox)
                    .addComponent(dictionaryDescriptionCheckBox)))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox dataCheckBox;
    private javax.swing.JCheckBox dictionaryCategoryCheckBox;
    private javax.swing.JCheckBox dictionaryDescriptionCheckBox;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel variableNameLabel;
    // End of variables declaration//GEN-END:variables

    void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
        if (dictionary != null){
            if (dictionary.isCompoundDictionary()){
                 dictionaryCategoryCheckBox.setEnabled(true);
            }
            dictionaryDescriptionCheckBox.setEnabled(true);
        }
    }

    /**
     * @return the dictionary
     */
    public Dictionary getDictionary() {
        return dictionary;
    }
}
