/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PreviewFilePanel.java
 *
 * Created on 14-Jan-2010, 13:39:33
 */
package canreg.client.gui.components;

import canreg.client.CanRegClientApp;
import canreg.common.Globals;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.Action;

/**
 *
 * @author ervikm
 */
public class PreviewFilePanel extends javax.swing.JPanel {

    private File inFile;
    private String path;
    private boolean needToRebuildVariableMap = true;
    private JFileChooser chooser;
    private ActionListener listener;
    public static final String FILE_CHANGED_ACTION = "file_changed";

    /** Creates new form PreviewFilePanel */
    public PreviewFilePanel() {
        initComponents();
    }

    public void init(ActionListener listener) {
        this.listener = listener;
        previewPanel.setVisible(false);
        // get the available charsets
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        charsetsComboBox.setModel(new javax.swing.DefaultComboBoxModel(charsets.values().toArray()));
        // set the default mapping
        charsetsComboBox.setSelectedItem(CanRegClientApp.getApplication().getGlobalToolBox().getStandardCharset());
        // initializeVariableMappingTab();
    }

    public void setPath(String path) {
        if (path == null) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(path);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        chooseFilePanel = new javax.swing.JPanel();
        fileNameTextField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        previewPanel = new javax.swing.JPanel();
        numberOfRecordsLabel = new javax.swing.JLabel();
        numberOfRecordsTextField = new javax.swing.JTextField();
        previewTableScrollPane = new javax.swing.JScrollPane();
        previewTable = new javax.swing.JTable();
        numberOfRecordsShownLabel = new javax.swing.JLabel();
        numberOfRecordsShownTextField = new javax.swing.JTextField();
        previewButton = new javax.swing.JButton();
        fileLabel = new javax.swing.JLabel();
        separatingCharacterComboBox = new javax.swing.JComboBox();
        separatingCharacterLabel = new javax.swing.JLabel();
        autodetectButton = new javax.swing.JButton();
        fileEncodingLabel = new javax.swing.JLabel();
        charsetsComboBox = new javax.swing.JComboBox();

        setName("Form"); // NOI18N

        chooseFilePanel.setName("chooseFilePanel"); // NOI18N

        fileNameTextField.setName("fileNameTextField"); // NOI18N
        fileNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fileNameTextFieldFocusLost(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(PreviewFilePanel.class, this);
        browseButton.setAction(actionMap.get("browseAction")); // NOI18N
        browseButton.setName("browseButton"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(PreviewFilePanel.class);
        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("previewPanel.border.title"))); // NOI18N
        previewPanel.setEnabled(false);
        previewPanel.setName("previewPanel"); // NOI18N

        numberOfRecordsLabel.setText(resourceMap.getString("numberOfRecordsLabel.text")); // NOI18N
        numberOfRecordsLabel.setFocusable(false);
        numberOfRecordsLabel.setName("numberOfRecordsLabel"); // NOI18N

        numberOfRecordsTextField.setEditable(false);
        numberOfRecordsTextField.setFocusable(false);
        numberOfRecordsTextField.setName("numberOfRecordsTextField"); // NOI18N

        previewTableScrollPane.setName("previewTableScrollPane"); // NOI18N

        previewTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        previewTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        previewTable.setName("previewTable"); // NOI18N
        previewTableScrollPane.setViewportView(previewTable);

        numberOfRecordsShownLabel.setText(resourceMap.getString("numberOfRecordsShownLabel.text")); // NOI18N
        numberOfRecordsShownLabel.setFocusable(false);
        numberOfRecordsShownLabel.setName("numberOfRecordsShownLabel"); // NOI18N

        numberOfRecordsShownTextField.setEditable(false);
        numberOfRecordsShownTextField.setFocusable(false);
        numberOfRecordsShownTextField.setName("numberOfRecordsShownTextField"); // NOI18N

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewPanelLayout.createSequentialGroup()
                .addComponent(numberOfRecordsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberOfRecordsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(numberOfRecordsShownLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(numberOfRecordsShownTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 114, Short.MAX_VALUE))
            .addComponent(previewTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewPanelLayout.createSequentialGroup()
                .addGroup(previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(numberOfRecordsLabel)
                    .addComponent(numberOfRecordsTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(numberOfRecordsShownLabel)
                    .addComponent(numberOfRecordsShownTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE))
        );

        previewButton.setAction(actionMap.get("previewAction")); // NOI18N
        previewButton.setName("previewButton"); // NOI18N

        fileLabel.setText(resourceMap.getString("fileLabel.text")); // NOI18N
        fileLabel.setName("fileLabel"); // NOI18N

        separatingCharacterComboBox.setEditable(true);
        separatingCharacterComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tab", "Comma" }));
        separatingCharacterComboBox.setAction(actionMap.get("comboBoxChanged")); // NOI18N
        separatingCharacterComboBox.setName("separatingCharacterComboBox"); // NOI18N

        separatingCharacterLabel.setText(resourceMap.getString("separatingCharacterLabel.text")); // NOI18N
        separatingCharacterLabel.setName("separatingCharacterLabel"); // NOI18N

        autodetectButton.setAction(actionMap.get("autoDetectAction")); // NOI18N
        autodetectButton.setToolTipText(resourceMap.getString("autodetectButton.toolTipText")); // NOI18N
        autodetectButton.setName("autodetectButton"); // NOI18N

        fileEncodingLabel.setText(resourceMap.getString("fileEncodingLabel.text")); // NOI18N
        fileEncodingLabel.setName("fileEncodingLabel"); // NOI18N

        charsetsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        charsetsComboBox.setName("charsetsComboBox"); // NOI18N

        javax.swing.GroupLayout chooseFilePanelLayout = new javax.swing.GroupLayout(chooseFilePanel);
        chooseFilePanel.setLayout(chooseFilePanelLayout);
        chooseFilePanelLayout.setHorizontalGroup(
            chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chooseFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(previewPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(chooseFilePanelLayout.createSequentialGroup()
                        .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chooseFilePanelLayout.createSequentialGroup()
                                .addComponent(fileEncodingLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(charsetsComboBox, 0, 199, Short.MAX_VALUE)
                                .addGap(9, 9, 9)
                                .addComponent(separatingCharacterLabel))
                            .addGroup(chooseFilePanelLayout.createSequentialGroup()
                                .addComponent(fileLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(browseButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(separatingCharacterComboBox, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(previewButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(autodetectButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        chooseFilePanelLayout.setVerticalGroup(
            chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(chooseFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewButton)
                    .addComponent(browseButton)
                    .addComponent(fileLabel)
                    .addComponent(fileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(chooseFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autodetectButton)
                    .addComponent(separatingCharacterComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(separatingCharacterLabel)
                    .addComponent(fileEncodingLabel)
                    .addComponent(charsetsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 571, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(chooseFilePanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(chooseFilePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fileNameTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fileNameTextFieldFocusLost
        // TODO add your handling code here:
}//GEN-LAST:event_fileNameTextFieldFocusLost

    @Action
    public void browseAction() {
        if (chooser == null) {
            setPath(path);
        }
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                //set the file name
                fileNameTextField.setText(chooser.getSelectedFile().getCanonicalPath());
                changeFile();
            } catch (IOException ex) {
                Logger.getLogger(PreviewFilePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Action
    public void previewAction() {
        // show the contents of the file
        BufferedReader br = null;
        try {
            changeFile();
            // numberOfRecordsTextField.setText(""+(canreg.common.Tools.numberOfLinesInFile(inFile.getAbsolutePath())-1));
            FileInputStream fis = new FileInputStream(inFile);
            br = new BufferedReader(new InputStreamReader(fis, (Charset) charsetsComboBox.getSelectedItem()));
            // Read the parts of the file into the preview area...
            int i = 0;

            String line = br.readLine();
            String headers = new String();
            // String dataText = new String();
            Vector<Vector<String>> data = new Vector<Vector<String>>();

            while (i < Globals.NUMBER_OF_LINES_IN_IMPORT_PREVIEW && line != null) {
                if (i == 0) {
                    headers = line;
                } else {
                    String[] lineData = line.split(getSeparator() + "");
                    Vector vec = new Vector(Arrays.asList(lineData));
                    data.add(vec);
                    // dataText += line + "\n";
                }
                line = br.readLine();
                i++;
                numberOfRecordsShownTextField.setText(i + "");
            }

            // previewTextArea.setText(headers + "\n" + dataText);
            // previewTextArea.setCaretPosition(0);
            previewPanel.setVisible(true);
            Vector columnNames = new Vector(Arrays.asList(headers.split(getSeparator() + "")));
            previewTable.setModel(new DefaultTableModel(data, columnNames));
        } catch (FileNotFoundException fileNotFoundException) {
            JOptionPane.showInternalMessageDialog(CanRegClientApp.getApplication().getMainFrame().getContentPane(), "Could not preview file: \'" + fileNameTextField.getText().trim() + "\'.", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(PreviewFilePanel.class.getName()).log(Level.SEVERE, null, fileNotFoundException);
        } catch (IOException ex) {
            Logger.getLogger(PreviewFilePanel.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(PreviewFilePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Action
    public void autoDetectAction() {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autodetectButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JComboBox charsetsComboBox;
    private javax.swing.JPanel chooseFilePanel;
    private javax.swing.JLabel fileEncodingLabel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JTextField fileNameTextField;
    private javax.swing.JLabel numberOfRecordsLabel;
    private javax.swing.JLabel numberOfRecordsShownLabel;
    private javax.swing.JTextField numberOfRecordsShownTextField;
    private javax.swing.JTextField numberOfRecordsTextField;
    private javax.swing.JButton previewButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JTable previewTable;
    private javax.swing.JScrollPane previewTableScrollPane;
    private javax.swing.JComboBox separatingCharacterComboBox;
    private javax.swing.JLabel separatingCharacterLabel;
    // End of variables declaration//GEN-END:variables

    private void changeFile() {
        inFile = new File(fileNameTextField.getText().trim());
        path = inFile.getPath();
        needToRebuildVariableMap = true;
        try {
            numberOfRecordsTextField.setText("" + (canreg.common.Tools.numberOfLinesInFile(inFile.getCanonicalPath()) - 1));
            listener.actionPerformed(new ActionEvent(this, 0, FILE_CHANGED_ACTION));
        } catch (IOException ex) {
            Logger.getLogger(PreviewFilePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public char getSeparator() {
        String sc = separatingCharacterComboBox.getSelectedItem().toString();
        char schar = ','; // Default
        if (sc.equalsIgnoreCase("Tab")) {
            schar = '\t';
        } else if (sc.equalsIgnoreCase("Comma")) {
            schar = ',';
        } else if (sc.length() > 0) {
            schar = sc.charAt(0);
        }
        return schar;
    }

    public File getInFile() {
        // changeFile();
        return inFile;
    }

    public Charset getCharacterSet() {
        return (Charset) charsetsComboBox.getSelectedItem();
    }
}
