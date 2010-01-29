/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ElementPanel.java
 *
 * Created on 21-Jan-2010, 14:06:49
 */
package canreg.client.gui.management;

import canreg.common.DatabaseElement;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class ElementPanel extends javax.swing.JPanel {

    private DatabaseElement databaseElement;
    private ActionListener listener;
    public static String EDIT_ACTION = "edit";
    public static String MOVE_UP_ACTION = "move_up";
    public static String MOVE_DOWN_ACTION = "move_down";
    public static String REMOVE_ACTION = "remove";

    /** Creates new form ElementPanel */
    public ElementPanel() {
        initComponents();
    }

    public ElementPanel(DatabaseElement databaseElement) {
        this();
        this.databaseElement = databaseElement;
        setDatabaseElement(databaseElement);
    }

    /**
     *
     * @param listener
     */
    public void setActionListener(ActionListener listener) {
        this.listener = listener;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        removeButton = new javax.swing.JButton();
        nameTextField = new javax.swing.JTextField();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setName("Form"); // NOI18N

        nameLabel.setName("nameLabel"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(ElementPanel.class, this);
        removeButton.setAction(actionMap.get("removeAction")); // NOI18N
        removeButton.setName("removeButton"); // NOI18N

        nameTextField.setEditable(false);
        nameTextField.setName("nameTextField"); // NOI18N

        upButton.setAction(actionMap.get("moveUpAction")); // NOI18N
        upButton.setName("upButton"); // NOI18N

        downButton.setAction(actionMap.get("moveDown")); // NOI18N
        downButton.setName("downButton"); // NOI18N

        jButton1.setAction(actionMap.get("editAction")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(280, 280, 280)
                        .addComponent(nameLabel))
                    .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(upButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downButton, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(downButton)
            .addComponent(upButton)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(removeButton)
                .addComponent(jButton1))
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(nameLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    @Action
    public void removeAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, REMOVE_ACTION));
        }
    }

    @Action
    public void moveUpAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, MOVE_UP_ACTION));
        }
    }

    @Action
    public void moveDown() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, MOVE_DOWN_ACTION));
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton downButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the databaseElement
     */
    public DatabaseElement getDatabaseElement() {
        return databaseElement;
    }

    /**
     * @param databaseElement the databaseElement to set
     */
    public void setDatabaseElement(DatabaseElement databaseElement) {
        this.databaseElement = databaseElement;
        nameTextField.setText(databaseElement.toString());
    }

    @Action
    public void editAction() {
        if (listener != null) {
            listener.actionPerformed(new ActionEvent(this, 0, EDIT_ACTION));
        }
    }
}