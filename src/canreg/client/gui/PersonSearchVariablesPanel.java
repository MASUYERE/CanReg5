/*
 * PersonSearchVariablesPanel.java
 *
 * Created on 21 October 2008, 14:02
 */
package canreg.client.gui;

import canreg.common.DatabaseVariablesListElement;
import canreg.common.Globals;
import canreg.common.PersonSearchVariable;
import canreg.common.Tools;
import canreg.common.qualitycontrol.DefaultPersonSearch;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author  ervikm
 */
public class PersonSearchVariablesPanel extends javax.swing.JPanel implements ActionListener {
    private Document doc;
    private DatabaseVariablesListElement[] variablesInDB;
    private DefaultPersonSearch searcher;

    /** Creates new form PersonSearchVariablesPanel */
    public PersonSearchVariablesPanel() {
        initComponents();
    }
    
    public void setDoc(Document doc) {
        this.doc=doc;
        variablesInDB = Tools.getVariableListElements(doc, Globals.NAMESPACE, "Patient");
        PersonSearchVariable[] searchVariables = Tools.getPersonSearchVariables(doc, Globals.NAMESPACE);
        searcher = new DefaultPersonSearch(variablesInDB);
        searcher.setSearchVariables(searchVariables);
        searcher.setThreshold(Tools.getPersonSearchMinimumMatch(doc, Globals.NAMESPACE));
        setSearcher(searcher);
    }
    
    private void setSearcher(DefaultPersonSearch searcher){
        PersonSearchVariable[] searchVariables = searcher.getPersonSearchVariables();
        for (PersonSearchVariable searchVariable:searchVariables){
            PersonSearchVariablePanel psvp = new PersonSearchVariablePanel();
            addPersonSearchVariablePanel(psvp);
            psvp.setPersonSearchVariable(searchVariable);
        }
        thresholdTextField.setText(searcher.getThreshold()+"");
    }
    
    public DefaultPersonSearch getSearcher(){
        return buildSearcher();
    }
    
    private DefaultPersonSearch buildSearcher(){
        DefaultPersonSearch newPersonSearch = new DefaultPersonSearch(variablesInDB);
        Component[] components = variablesListPanel.getComponents();
        PersonSearchVariable[] variables = new PersonSearchVariable[components.length];
        int i = 0;
        for(Component component:components){
            PersonSearchVariablePanel psvp = (PersonSearchVariablePanel) component;
            variables[i]=psvp.getPersonSearchVariable();
            i++;
        }
        newPersonSearch.setSearchVariables(variables);
        float threshold = 70;
        try {
            threshold = Float.parseFloat(thresholdTextField.getText());
        } catch (NumberFormatException nfe){
            
        }
        newPersonSearch.setThreshold(threshold);
        return newPersonSearch;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        variablesListPanel = new javax.swing.JPanel();
        thresholdTextField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        setName("Form"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(PersonSearchVariablesPanel.class, this);
        jButton1.setAction(actionMap.get("addVariableAction")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setAction(actionMap.get("revertToDefaultAction")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        variablesListPanel.setName("variablesListPanel"); // NOI18N
        variablesListPanel.setLayout(new java.awt.GridLayout(0, 1, 2, 0));
        jScrollPane1.setViewportView(variablesListPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
        );

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PersonSearchVariablesPanel.class);
        thresholdTextField.setText(resourceMap.getString("thresholdTextField.text")); // NOI18N
        thresholdTextField.setName("thresholdTextField"); // NOI18N

        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N

        jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
        jLabel2.setName("jLabel2"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jButton2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(thresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel2)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(thresholdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void addVariableAction() {
        System.out.println("coucou");
        PersonSearchVariablePanel psvp = new PersonSearchVariablePanel();
        addPersonSearchVariablePanel(psvp);
    }
    
    private void addPersonSearchVariablePanel(PersonSearchVariablePanel psvp){
        psvp.setActionListener(this);
        psvp.setDatabaseVariables(variablesInDB);
        variablesListPanel.add(psvp);
        psvp.setVisible(true);
        variablesListPanel.revalidate();
        variablesListPanel.repaint();
    }

    @Action
    public void revertToDefaultAction() {

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField thresholdTextField;
    private javax.swing.JPanel variablesListPanel;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if ("remove".equalsIgnoreCase(command)){
            variablesListPanel.remove((Component) e.getSource());       
            variablesListPanel.revalidate();
            variablesListPanel.repaint();
        }
    }

}
