/*
 * Copyright (C) 2016 patri_000
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package canreg.client.gui.dataentry2;

import canreg.common.cachingtableapi.DistributedTableDescriptionException;
import canreg.client.CanRegClientApp;
import canreg.client.gui.CanRegClientView;
import static canreg.client.gui.CanRegClientView.maximizeHeight;
import canreg.client.gui.dataentry.BrowseInternalFrame;
import canreg.client.gui.dataentry.EditChecksInternalFrame;
import canreg.client.gui.management.ComparePatientsInternalFrame;
import canreg.client.gui.tools.PrintUtilities;
import canreg.client.gui.tools.WaitFrame;
import canreg.common.DatabaseVariablesListElement;
import canreg.common.GlobalToolBox;
import canreg.common.Globals;
import canreg.common.JComponentToPDF;
import canreg.common.Tools;
import canreg.common.conversions.ConversionResult;
import canreg.common.conversions.Converter;
import canreg.common.qualitycontrol.CheckResult;
import canreg.common.qualitycontrol.DefaultMultiplePrimaryTester;
import canreg.common.qualitycontrol.MultiplePrimaryTesterInterface;
import canreg.common.database.DatabaseRecord;
import canreg.common.database.Dictionary;
import canreg.common.database.Patient;
import canreg.server.database.RecordLockedException;
import canreg.common.database.Source;
import canreg.common.database.Tumour;
import canreg.common.qualitycontrol.CheckResult.ResultCode;
import canreg.server.database.UnknownTableException;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDesktopPane;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.jdesktop.application.Action;
import org.w3c.dom.Document;

/**
 *
 * @author patri_000
 */
public class RecordEditor extends javax.swing.JInternalFrame implements ActionListener {
     
    public static final String CHANGED = "changed";
    public static final String CHECKS = "checks";
    public static final String DELETE = "delete"; 
    public static final String SAVE = "save";
    public static final String RUN_MP = "runMP";
    public static final String RUN_EXACT = "runExact";
    public static final String OBSOLETE = "obsolete";
    public static final String CHANGE_PATIENT_RECORD = "changePatientRecord";
    public static final String CALC_AGE = "calcAge";
    public static final String AUTO_FILL = "autoFill";
    public static final String PERSON_SEARCH = "person search";
    public static String REQUEST_FOCUS = "request focus";
    private Document doc;
    private Map<Integer, Dictionary> dictionary;
    private final Set<DatabaseRecord> patientRecords;
    private final TreeMap<Object, RecordEditorPatient> patientRecordsMap;
    private final Set<DatabaseRecord> tumourRecords;
    //private boolean changesDone = false;
    private final JDesktopPane desktopPane;
    private GlobalToolBox globalToolBox;
    private boolean titleSet = false;
    private String tumourObsoleteVariableName = null;
    private String patientObsoleteVariableName = null;
    private String tumourSequenceVariableName = null;
    private String tumourSequenceTotalVariableName = null;
    AutoFillHelper autoFillHelper;
    //unused private String tumourIDVariableName = null;
    private String patientIDVariableName = null;
    private String patientRecordIDVariableName = null;
    //unused private final String patientRecordIDTumourTableVariableName = null;
    private BrowseInternalFrame browseInternalFrame;
        
    
    public RecordEditor(JDesktopPane desktopPane) {        
        this.desktopPane = desktopPane;
        initComponents();
        patientRecords = new LinkedHashSet<DatabaseRecord>();
        tumourRecords = new LinkedHashSet<DatabaseRecord>();
        patientRecordsMap = new TreeMap<Object, RecordEditorPatient>();
        autoFillHelper = new AutoFillHelper();

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                int option = JOptionPane.NO_OPTION;
                // Go through all panels and ask if any changes has been done                
                boolean changesDone = false;
                for (Component component : patientTabbedPane.getComponents()) {
                    RecordEditorPatient panel = (RecordEditorPatient) component;
                    changesDone = changesDone || panel.isSaveNeeded();
                }
                                
                for (Component component : tumourTabbedPane.getComponents()) {
                    RecordEditorTumour panel = (RecordEditorTumour) component;
                    changesDone = changesDone || panel.isSaveNeeded();
                }
                
                if(changesDone) {
                    option = JOptionPane.showConfirmDialog(null, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor")
                                        .getString("REALLY CLOSE?CHANGES MADE WILL BE LOST."), 
                                "Warning!", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        releaseRecords();
                        dispose();
                    }
                } else {
                    releaseRecords();
                    dispose(); 
                }
            }
        });        
        // Add a listener for changing the active tab
        ChangeListener tabbedPaneChangeListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
                RecordEditorPanel rep = (RecordEditorPanel) pane.getSelectedComponent();
                if(rep != null)
                    setActiveRecord(rep);                
            }
        };
        // And add the listener to the tabbedPane
        patientTabbedPane.addChangeListener(tabbedPaneChangeListener);
        tumourTabbedPane.addChangeListener(tabbedPaneChangeListener);
    }

    private void addToPatientMap(RecordEditorPatient recordEditorPanel, DatabaseRecord dbr) {
        Object regno = dbr.getVariable(globalToolBox
                                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                                        .getDatabaseVariableName());
        if(regno != null) 
            patientRecordsMap.put(regno, recordEditorPanel);        
    }
    
    private void setActiveRecord(RecordEditorPanel rep) {
        DatabaseRecord dbr = rep.getDatabaseRecord();
        if(dbr != null && dbr instanceof Tumour) {
            Object patientRecordID = dbr.getVariable(globalToolBox
                                                      .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString())
                                                      .getDatabaseVariableName());
            Component comp = patientRecordsMap.get(patientRecordID);
            if(comp != null)
                patientTabbedPane.setSelectedComponent(comp);            
        }
    }
    
    public void toggleObsolete(boolean confirmed, RecordEditorTumour tumourSelected) {
        if (confirmed) {
            DatabaseVariablesListElement dbvle = tumourSelected.getObsoleteFlagVariableListElement();
            if (dbvle != null) {
                boolean obsolete = tumourObsoleteToggleButton.isSelected();
                if (obsolete) 
                    tumourSelected.getDatabaseRecord().setVariable(dbvle.getDatabaseVariableName(), Globals.OBSOLETE_VALUE);
                else 
                    tumourSelected.getDatabaseRecord().setVariable(dbvle.getDatabaseVariableName(), Globals.NOT_OBSOLETE_VALUE);                
            }
        } else 
            tumourObsoleteToggleButton.setSelected(!tumourObsoleteToggleButton.isSelected());        
    }
    
    public void setGlobalToolBox(GlobalToolBox globalToolBox) {
        this.globalToolBox = globalToolBox;
        this.doc = globalToolBox.getDocument();
        autoFillHelper.setGlobalToolBox(globalToolBox);

        patientObsoleteVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString()).getDatabaseVariableName();
        tumourObsoleteVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ObsoleteFlagTumourTable.toString()).getDatabaseVariableName();
        tumourSequenceVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimSeq.toString()).getDatabaseVariableName();
        tumourSequenceTotalVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.MultPrimTot.toString()).getDatabaseVariableName();

        //unused tumourIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName();
        patientIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName();
        patientRecordIDVariableName = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName();
    }
        
    public void setDictionary(Map<Integer, canreg.common.database.Dictionary> dictionary) {
        this.dictionary = dictionary;
    }
   
    public void addRecord(DatabaseRecord dbr) {                
        if (dbr instanceof Patient) {
            RecordEditorPatient rePanel = new RecordEditorPatient(this);
            rePanel.setDictionary(dictionary);
            rePanel.setDocument(doc);
            rePanel.setRecordAndBuildPanel(dbr);
            patientRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                    .getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0)
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
                else 
                    patientRecordsMap.put(regno, rePanel);               
            }
            Object patientObsoleteStatus = dbr.getVariable(patientObsoleteVariableName);
            if (patientObsoleteStatus != null && patientObsoleteStatus.equals(Globals.OBSOLETE_VALUE)) 
                regnoString += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString(" (OBSOLETE)");            
            patientTabbedPane.addTab(dbr.toString() + ": " + regnoString + " ", rePanel);
            this.patientTabbedPane.addTab(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PATIENT") 
                                     + ": " + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString(" RECORD ") 
                                     + (patientTabbedPane.getTabCount() + 1), rePanel);
            if (!titleSet) {
                Object patno = dbr.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                String patnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
                if (patno != null) {
                    patnoString = patno.toString();
                    if (patnoString.length() > 0) {
                        this.setTitle(java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PATIENT ID:") + patnoString);
                        titleSet = true;
                    }
                }
            }
        } else if (dbr instanceof Tumour) {
            RecordEditorTumour rePanel = new RecordEditorTumour((ActionListener)this, (RecordEditor)this);
            rePanel.setDictionary(dictionary);
            rePanel.setDocument(doc);
            rePanel.setRecordAndBuildPanel(dbr);
            tumourRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
            if (regno != null) {
                regnoString = regno.toString();
                if (regnoString.length() == 0)
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");                
            }
            Object tumourObsoleteStatus = dbr.getVariable(tumourObsoleteVariableName);
            if (tumourObsoleteStatus != null && tumourObsoleteStatus.equals(Globals.OBSOLETE_VALUE)) 
                regnoString += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString(" (OBSOLETE)");    
            
            tumourTabbedPane.addTab(dbr.toString() + ": " + regnoString + " ", rePanel);
            tumourTabbedPane.addTab(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("TUMOUR") 
                                    + ": " + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString(" RECORD ") 
                                    + (tumourTabbedPane.getTabCount() + 1), rePanel);
        }
        refreshShowObsolete();
    }
        
    @Action
    public void addTumourAction() {
        Tumour tumour = new Tumour();
        populateNewRecord(tumour, doc);
        addRecord(tumour);
    }
    
    @Action
    public void patientMenuAction() {
        patientPopupMenu.show(patientMenuButton, 0, 0);
    }
    
    @Action
    public void tumourMenuAction() {
        tumourPopupMenu.show(tumourMenuButton, 0, 0);
    }
  
    /*@Action
    public void addPatientAction() {
        Patient patient = new Patient();
        populateNewRecord(patient, doc);
        addRecord(patient);
    }*/

    private DatabaseRecord populateNewRecord(DatabaseRecord dbr, Document doc) {
        String tableName = "";
        if (dbr instanceof Tumour)
            tableName = Globals.TUMOUR_TABLE_NAME;
        else if (dbr instanceof Patient)
            tableName = Globals.PATIENT_TABLE_NAME;
        
        RecordEditorPatient activePatientPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
        Patient activePatient = (Patient) activePatientPanel.getDatabaseRecord();

        DatabaseVariablesListElement[] variablesInTable = canreg.common.Tools.getVariableListElements(doc, Globals.NAMESPACE, tableName);
        for (DatabaseVariablesListElement dbvle : variablesInTable) {
            String type = dbvle.getVariableType();
            if (type.equalsIgnoreCase(Globals.VARIABLE_TYPE_NUMBER_NAME)) {
                Object unk = dbvle.getUnknownCode();
                int number = -1;
                if (unk != null) {
                    if (unk instanceof String)
                        number = Integer.parseInt((String) unk);
                    else 
                        number = (Integer) unk;                    
                }
                dbr.setVariable(dbvle.getDatabaseVariableName(), number);
            } else 
                dbr.setVariable(dbvle.getDatabaseVariableName(), "");            
        }

        if (dbr instanceof Patient) {
            // copy all information
            for (String variableName : dbr.getVariableNames()) {
                if (variableName.equalsIgnoreCase(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) 
                    || variableName.equalsIgnoreCase(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString())
                            .getDatabaseVariableName())) {
                    /*Nothing here*/
                }
                else 
                    dbr.setVariable(variableName, activePatient.getVariable(variableName));                
            }
            // except the database record ID and the patientTable ID
            dbr.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, null);
            dbr.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals
                    .StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName(), null);
        }
        return dbr;
    }
    
    @Action
    public void saveAllAction() {             

    }
    
    private void refreshTitles(RecordEditorPanel recordEditorPanel, DatabaseRecord dbr) {
        if(dbr instanceof Patient) {
            // patientRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
            if(regno != null) {
                regnoString = regno.toString();
                if(regnoString.length() == 0)
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
                else {
                    // patientRecordsMap.put(regno, recordEditorPanel);
                }
            }
            int index = 0;
            for(Component comp : patientTabbedPane.getComponents()) {
                if(comp.equals(recordEditorPanel)) 
                    patientTabbedPane.setTitleAt(index, dbr.toString() + ": " + regnoString);                
                index++;
            }
            if(!titleSet) {
                Object patno = dbr.getVariable(globalToolBox
                        .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
                String patnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
                if(patno != null) {
                    patnoString = patno.toString();
                    if(patnoString.length() > 0) {
                        this.setTitle(java.util.ResourceBundle
                                .getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PATIENT ID:") + patnoString);
                        titleSet = true;
                    }
                }
            }
        } else if(dbr instanceof Tumour) {
            // tumourRecords.add(dbr);
            Object regno = dbr.getVariable(globalToolBox
                    .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourID.toString()).getDatabaseVariableName());
            String regnoString = java.util.ResourceBundle
                    .getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");
            if(regno != null) {
                regnoString = regno.toString();
                if(regnoString.length() == 0) 
                    regnoString = java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("N/A");                
            }
            int index = 0;
            for(Component comp : tumourTabbedPane.getComponents()) {
                if(comp.equals(recordEditorPanel)) 
                    tumourTabbedPane.setTitleAt(index, dbr.toString() + ": " + regnoString);                
                index++;
            }
        }
    }

    @Action
    public void printAction() {
        PrintUtilities.printComponent(patientTabbedPane.getSelectedComponent());
        PrintUtilities.printComponent(tumourTabbedPane.getSelectedComponent());
    }
    
    @Action
    public void changePatientRecord() {
        this.actionPerformed(new ActionEvent(this, 0, RecordEditor.CHANGE_PATIENT_RECORD));
    }
    
    @Action
    public void deleteRecord() {
        this.actionPerformed(new ActionEvent(this, 0, RecordEditor.DELETE));
    }
    
    @Action
    public void setObsoleteFlag() {
        this.actionPerformed(new ActionEvent(this, 0, RecordEditor.OBSOLETE));
    } 

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        //DatabaseRecord tumourRecord;        

        if (e.getActionCommand().equalsIgnoreCase(REQUEST_FOCUS)) {
            try {
                this.setSelected(true);
            } catch (PropertyVetoException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.WARNING, null, ex);
            }
        } else /*if (source instanceof RecordEditorPanel)*/ {
            RecordEditorPanel sourcePanel = (RecordEditorPanel) source;            
            if (e.getActionCommand().equalsIgnoreCase(CHANGED)) {
                //unused this.changesDone = true;
            } else if (e.getActionCommand().equalsIgnoreCase(DELETE)) {
                deleteRecord(sourcePanel);
            } else if (e.getActionCommand().equalsIgnoreCase(CHECKS)) {
                runChecks(sourcePanel);
            } else if (e.getActionCommand().equalsIgnoreCase(SAVE)) {
                saveRecord(sourcePanel);
            } else if (e.getActionCommand().equalsIgnoreCase(CHANGE_PATIENT_RECORD)) {
                changePatientRecord((RecordEditorTumour)sourcePanel);
            } else if (e.getActionCommand().equalsIgnoreCase(OBSOLETE)) {                
                int option = JOptionPane.NO_OPTION;
                option = JOptionPane.showConfirmDialog(null, 
                                                       java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("REALLY CHANGE OBSOLETE-STATUS?"), 
                                                       java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("REALLY CHANGE OBSOLETE-STATUS?"), 
                                                       JOptionPane.YES_NO_OPTION);
                boolean toggle = (option == JOptionPane.YES_OPTION);
                toggleObsolete(toggle, (RecordEditorTumour) tumourTabbedPane.getSelectedComponent());
                if (toggle) 
                    refreshShowObsolete();                
            } else if (e.getActionCommand().equalsIgnoreCase(RUN_MP)) {
                runMPsearch((RecordEditorTumour) source);
            } else if (e.getActionCommand().equalsIgnoreCase(RUN_EXACT)) {
                runExactSearch((RecordEditorPatient) source);            
            } else if (e.getActionCommand().equalsIgnoreCase(CALC_AGE)) {
                // this should be called at any time any of the fields birth date or incidence date gets changed                
                DatabaseRecord sourceDatabaseRecord = sourcePanel.getDatabaseRecord();
                //unused DatabaseRecord patientDatabaseRecord;
                // TODO: implement calculate age
                if (sourceDatabaseRecord instanceof Tumour) {
                    //unused RecordEditorPanel patientRecordEditorPanel = (RecordEditorPanel) patientTabbedPane.getSelectedComponent();
                    //unused patientDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
                } else {
                    // get all the tumour records
                }
            } else if (e.getActionCommand().equalsIgnoreCase(AUTO_FILL)) {                
                LinkedList<DatabaseVariablesListElement> autoFillList = sourcePanel.getAutoFillList();
                DatabaseRecord sourceOfActionDatabaseRecord = sourcePanel.getDatabaseRecord();
                DatabaseRecord otherDatabaseRecord = null;
                if (sourceOfActionDatabaseRecord instanceof Tumour) {
                    RecordEditorPatient patientRecordEditorPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
                    otherDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
                    autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, sourcePanel);
                    patientRecordEditorPanel.refreshDatabaseRecord(otherDatabaseRecord);
                    sourcePanel.refreshDatabaseRecord(sourceOfActionDatabaseRecord);
                } else if (sourceOfActionDatabaseRecord instanceof Patient) {
                    RecordEditorTumour tumourRecordEditorPanel = (RecordEditorTumour) tumourTabbedPane.getSelectedComponent();
                    otherDatabaseRecord = tumourRecordEditorPanel.getDatabaseRecord();
                    autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, sourcePanel);
                    tumourRecordEditorPanel.refreshDatabaseRecord(otherDatabaseRecord);
                    sourcePanel.refreshDatabaseRecord(sourceOfActionDatabaseRecord);
                }
//                 autoFillHelper.autoFill(autoFillList, sourceOfActionDatabaseRecord, otherDatabaseRecord, recordEditorPanel);
            } else if (e.getActionCommand().equalsIgnoreCase(PERSON_SEARCH))
                runPersonSearch((RecordEditorPatient)sourcePanel);            
        }
    }

    private DatabaseRecord saveRecord(DatabaseRecord databaseRecord) throws SecurityException, RemoteException, SQLException, RecordLockedException {
        // id is the internal database id
        DatabaseRecord newDatabaseRecord = null;
        if (databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) == null
                && databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME) == null) {
            int id = canreg.client.CanRegClientApp.getApplication().saveRecord(databaseRecord);
            if (databaseRecord instanceof Patient) {
                // databaseRecord.setVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME, id);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME, true);
                patientRecords.remove(databaseRecord);
                patientRecords.add(newDatabaseRecord);
            } else if (databaseRecord instanceof Tumour) {
                // databaseRecord.setVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME, id);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME, true);
                tumourRecords.remove(databaseRecord);
                tumourRecords.add(newDatabaseRecord);
            }
            JOptionPane.showInternalMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("NEW RECORD SAVED."));
        } else {
            int id = -1;
            if (databaseRecord instanceof Patient) {
                id = (Integer) databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.PATIENT_TABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord);
                id = (Integer) databaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.PATIENT_TABLE_NAME, true);
                patientRecords.remove(databaseRecord);
                patientRecords.add(newDatabaseRecord);
            } else if (databaseRecord instanceof Tumour) {
                id = (Integer) databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.TUMOUR_TABLE_NAME);
                canreg.client.CanRegClientApp.getApplication().editRecord(databaseRecord);
                id = (Integer) databaseRecord.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                newDatabaseRecord = canreg.client.CanRegClientApp.getApplication().getRecord(id, Globals.TUMOUR_TABLE_NAME, true);
                tumourRecords.remove(databaseRecord);
                tumourRecords.add(newDatabaseRecord);
            }
            JOptionPane.showInternalMessageDialog(this, java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD SAVED."));
        }
        return newDatabaseRecord;
    }

    private boolean associateTumourRecordToPatientRecord(DatabaseRecord tumourDatabaseRecord, DatabaseRecord patientDatabaseRecord) {
        boolean success;
        if (patientDatabaseRecord.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME) != null) {
            Object patientID = patientDatabaseRecord.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString()).getDatabaseVariableName());
            tumourDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString()).getDatabaseVariableName(), patientID);
            tumourDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordIDTumourTable.toString()).getDatabaseVariableName(),
                    patientDatabaseRecord.getVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientRecordID.toString()).getDatabaseVariableName()));
            success = true;
        } else 
            success = false;        
        return success;
    }

    private DatabaseRecord associatePatientRecordToPatientID(DatabaseRecord patientDatabaseRecord, String patientID) {
        patientDatabaseRecord.setVariable(globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientID.toString())
                                                       .getDatabaseVariableName(),
                                          patientID);
        return patientDatabaseRecord;
    }

    private DatabaseRecord associateTumourRecordToPatientID(DatabaseRecord tumourDatabaseRecord, String patientID) {
        tumourDatabaseRecord.setVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                .getDatabaseVariableName(),
                patientID);
        tumourDatabaseRecord.setVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PatientIDTumourTable.toString())
                .getDatabaseVariableName(),
                patientID);
        tumourDatabaseRecord.setVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourRecordStatus.toString())
                .getDatabaseVariableName(),
                Globals.RECORD_STATUS_PENDING_CODE);
        tumourDatabaseRecord.setVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.TumourUnduplicationStatus.toString())
                .getDatabaseVariableName(),
                Globals.UNDUPLICATION_NOT_DONE_CODE);
        return tumourDatabaseRecord;
    }

    private void refreshShowObsolete() {
        boolean showObsolete = showObsoleteRecordsCheckBox.isSelected();
        for (Component comp : patientTabbedPane.getComponents()) {
            RecordEditorPatient rep = (RecordEditorPatient) comp;
            DatabaseRecord dbr = rep.getDatabaseRecord();
            String obsoleteFlag = (String) dbr.getVariable(patientObsoleteVariableName);
            if (!showObsolete && obsoleteFlag.equals(Globals.OBSOLETE_VALUE))
                patientTabbedPane.setEnabledAt(patientTabbedPane.indexOfComponent(rep), false);
            else
                patientTabbedPane.setEnabledAt(patientTabbedPane.indexOfComponent(rep), true);           
        }
        for (Component comp : tumourTabbedPane.getComponents()) {
            RecordEditorTumour rep = (RecordEditorTumour) comp;
            DatabaseRecord dbr = rep.getDatabaseRecord();
            String obsoleteFlag = (String) dbr.getVariable(tumourObsoleteVariableName);
            if (!showObsolete && obsoleteFlag.equals(Globals.OBSOLETE_VALUE))
                tumourTabbedPane.setEnabledAt(tumourTabbedPane.indexOfComponent(rep), false);
            else 
                tumourTabbedPane.setEnabledAt(tumourTabbedPane.indexOfComponent(rep), true);            
        }
    }

    @Action
    public void toggleShowObsoleteRecords() {
        refreshShowObsolete();
    }

    @Action
    public void changePatientID() {
        String requestedPatientID = JOptionPane
                .showInputDialog(null, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PLEASE ENTER PATIENT ID:"), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("CHANGE TO WHICH PATIENTID?"), 
                                JOptionPane.QUESTION_MESSAGE);
        if (requestedPatientID != null) {
            Patient[] patientDatabaseRecord = null;
            try {
                patientDatabaseRecord = CanRegClientApp.getApplication().getPatientRecordsByID(requestedPatientID, false);
                if (patientDatabaseRecord != null && patientDatabaseRecord.length > 0) {
                    for (DatabaseRecord patient : patientRecords) {
                        patient = associatePatientRecordToPatientID(patient, requestedPatientID);
                        saveRecord(patient);
                    }
                    for (DatabaseRecord tumour : tumourRecords) {
                        tumour = associateTumourRecordToPatientID(tumour, requestedPatientID);
                        saveRecord(tumour);
                    }
                    JOptionPane.showInternalMessageDialog(this, 
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD MOVED."));
                } else {
                    JOptionPane.showInternalMessageDialog(this, 
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("NO SUCH PATIENT ID."), 
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"), 
                                        JOptionPane.WARNING_MESSAGE);
                    changePatientID();
                }
            } catch (canreg.server.database.RecordLockedException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showInternalMessageDialog(this, 
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD_LOCKED"), 
                                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD_LOCKED"), 
                                        JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnknownTableException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (DistributedTableDescriptionException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            // do nothing - cancel operation...
        }
    }

    private void releaseRecords() {
        // Release all patient records held
        for (DatabaseRecord record : patientRecords) {
            try {
                Object idObj = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.PATIENT_TABLE_NAME);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        patientRecords.clear();
        
        // Release all tumour records held
        for (DatabaseRecord record : tumourRecords) {
            Tumour tumour = (Tumour) record;            
            // Release all sources
            for (Source source : tumour.getSources()) {
                try {
                    Object idObj = source.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
                    if (idObj != null) {
                        int id = (Integer) idObj;
                        canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.SOURCE_TABLE_NAME);
                    }
                } catch (RemoteException ex) {
                    Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                } catch (SecurityException ex) {
                    Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                Object idObj = tumour.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
                if (idObj != null) {
                    int id = (Integer) idObj;
                    canreg.client.CanRegClientApp.getApplication().releaseRecord(id, Globals.TUMOUR_TABLE_NAME);
                }
            } catch (RemoteException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        tumourRecords.clear();
    }

    private void updateTumourSequences() {
        // first do the counting
        int totalTumours = 0;
        for (int i = 0; i < tumourTabbedPane.getComponentCount(); i++) {
            RecordEditorTumour rep = (RecordEditorTumour) tumourTabbedPane.getComponentAt(i);
            Tumour tumour = (Tumour) rep.getDatabaseRecord();
            boolean obsolete = tumour.getVariable(tumourObsoleteVariableName).toString().equalsIgnoreCase(Globals.OBSOLETE_VALUE);
            if (!obsolete) 
                totalTumours++;           
        }
        
        int tumourSequence = 0;
        for (int i = 0; i < tumourTabbedPane.getComponentCount(); i++) {
            RecordEditorTumour rep = (RecordEditorTumour) tumourTabbedPane.getComponentAt(i);
            Tumour tumour = (Tumour) rep.getDatabaseRecord();
            boolean obsolete = tumour.getVariable(tumourObsoleteVariableName).toString().equalsIgnoreCase(Globals.OBSOLETE_VALUE);
            if (!obsolete) {
                tumourSequence++;
                tumour.setVariable(tumourSequenceVariableName, tumourSequence + "");
            } else 
                tumour.setVariable(tumourSequenceVariableName, "-");
            tumour.setVariable(tumourSequenceTotalVariableName, totalTumours + "");
        }
    }

    private void deleteRecord(RecordEditorPanel recordEditorPanel) {
        int option = JOptionPane.NO_OPTION;
        option = JOptionPane.showConfirmDialog(null, 
                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PERMANENTLY DELETE RECORD?"));
        JTabbedPane tabbedPane = null;
        if(option == JOptionPane.YES_OPTION) {
            boolean success = false;
            DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
            success = deleteRecord(record);
            if(success) {
                if(record instanceof Patient) 
                    tabbedPane = patientTabbedPane;
                else if (record instanceof Tumour)
                    tabbedPane = tumourTabbedPane;                
                tabbedPane.remove((Component) recordEditorPanel);
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD DELETED."));
            } else 
                JOptionPane.showInternalMessageDialog(this,
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD NOT DELETED.ERROR OCCURED..."));            
        }
    }

    private void runChecks(RecordEditorPanel recordEditorPanel) {
        RecordEditorTumour tumourRecordEditorPanel;
        RecordEditorPatient patientRecordEditorPanel;
        DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
        ResultCode worstResultCodeFound = ResultCode.OK;
        String message = "";
        Patient patient;
        Tumour tumour;
        if (record instanceof Patient) {
            patient = (Patient) record;
            patientRecordEditorPanel = (RecordEditorPatient) recordEditorPanel;
            tumourRecordEditorPanel = (RecordEditorTumour) tumourTabbedPane.getSelectedComponent();
            tumour = (Tumour) tumourRecordEditorPanel.getDatabaseRecord();
        } else {
            tumour = (Tumour) record;
            tumourRecordEditorPanel = (RecordEditorTumour) recordEditorPanel;
            patientRecordEditorPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
            patient = (Patient) patientRecordEditorPanel.getDatabaseRecord();
        }

        EditChecksInternalFrame editChecksInternalFrame = new EditChecksInternalFrame();

        // Check to see if all mandatory variables are there
        boolean allPresent = patientRecordEditorPanel.areAllVariablesPresent();
        allPresent = allPresent && tumourRecordEditorPanel.areAllVariablesPresent();

        if (!allPresent) {
            editChecksInternalFrame.setMandatoryVariablesTextAreaText(
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("MANDATORY VARIABLES MISSING."));
            worstResultCodeFound = CheckResult.ResultCode.Missing;
            message += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("NOT PERFORMED.");
        } else {
            editChecksInternalFrame.setMandatoryVariablesTextAreaText(
                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("ALL MANDATORY VARIABLES PRESENT."));
            // Run the checks on the data
            LinkedList<CheckResult> checkResults = canreg.client.CanRegClientApp.getApplication().performChecks(patient, tumour);

            Map<Globals.StandardVariableNames, CheckResult.ResultCode> mapOfVariablesAndWorstResultCodes =
                    new TreeMap<Globals.StandardVariableNames, CheckResult.ResultCode>();
            worstResultCodeFound = CheckResult.ResultCode.OK;
            for (CheckResult result : checkResults) {
                if (result.getResultCode() != CheckResult.ResultCode.OK && result.getResultCode() != CheckResult.ResultCode.NotDone) {
                    if (!result.getResultCode().equals(CheckResult.ResultCode.Missing)) {
                        message += result + "\n";
                        worstResultCodeFound = CheckResult.decideWorstResultCode(result.getResultCode(), worstResultCodeFound);
                        for (Globals.StandardVariableNames standardVariableName : result.getVariablesInvolved()) {
                            CheckResult.ResultCode worstResultCodeFoundForThisVariable = mapOfVariablesAndWorstResultCodes.get(standardVariableName);
                            if (worstResultCodeFoundForThisVariable == null) 
                                mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());
                            else if (CheckResult.compareResultSets(result.getResultCode(), worstResultCodeFoundForThisVariable) > 0) 
                                mapOfVariablesAndWorstResultCodes.put(standardVariableName, result.getResultCode());                            
                        }
                    }
                }
                Logger.getLogger(RecordEditor.class.getName()).log(Level.INFO, result.toString());
            }

            if (worstResultCodeFound != CheckResult.ResultCode.Invalid && worstResultCodeFound != CheckResult.ResultCode.Missing) {
                // If no errors were found we generate ICD10 code
                ConversionResult[] conversionResult = canreg.client.CanRegClientApp.getApplication()
                        .performConversions(Converter.ConversionName.ICDO3toICD10, patient, tumour);
                if (conversionResult != null) {
                    if (conversionResult[0].getResultCode() != ConversionResult.ResultCode.Invalid) {
                        editChecksInternalFrame.setICD10TextFieldText(conversionResult[0].getValue() + "");
                        DatabaseVariablesListElement ICD10databaseVariablesElement = 
                                globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICD10.toString());
                        if (ICD10databaseVariablesElement != null) 
                            tumour.setVariable(ICD10databaseVariablesElement.getDatabaseVariableName(), conversionResult[0].getValue());                        
                    }
                }
                // ...and ICCC3 code
                conversionResult = canreg.client.CanRegClientApp.getApplication().performConversions(Converter.ConversionName.ICDO3toICCC3, patient, tumour);
                if (conversionResult != null) {
                    if (conversionResult[0].getResultCode() != ConversionResult.ResultCode.Invalid) {
                        editChecksInternalFrame.setICCCTextFieldText(conversionResult[0].getValue() + "");
                        DatabaseVariablesListElement ICCCdatabaseVariablesElement = globalToolBox.translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.ICCC.toString());
                        if (ICCCdatabaseVariablesElement != null) 
                            tumour.setVariable(ICCCdatabaseVariablesElement.getDatabaseVariableName(), conversionResult[0].getValue());                        
                    }
                }
            }

            tumourRecordEditorPanel.refreshDatabaseRecord(tumour);

            if (worstResultCodeFound == CheckResult.ResultCode.OK) {
                message += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("CROSS-CHECK CONCLUSION: VALID");
            } else {
                // set the various variable panels to respective warnings
                for (Globals.StandardVariableNames standardVariableName : mapOfVariablesAndWorstResultCodes.keySet()) {
                    DatabaseVariablesListElement dbvle = globalToolBox.translateStandardVariableNameToDatabaseListElement(standardVariableName.toString());

                    if (dbvle.getDatabaseTableName().equalsIgnoreCase(Globals.TUMOUR_TABLE_NAME))
                        tumourRecordEditorPanel.setResultCodeOfVariable(dbvle.getDatabaseVariableName(), mapOfVariablesAndWorstResultCodes.get(standardVariableName));
                    else if (dbvle.getDatabaseTableName().equalsIgnoreCase(Globals.PATIENT_TABLE_NAME))
                        patientRecordEditorPanel.setResultCodeOfVariable(dbvle.getDatabaseVariableName(), mapOfVariablesAndWorstResultCodes.get(standardVariableName));                    
                }
            }
        }
        tumourRecordEditorPanel.setChecksResultCode(worstResultCodeFound);

        editChecksInternalFrame.setCrossChecksTextAreaText(message);
        editChecksInternalFrame.setResultTextFieldText(worstResultCodeFound.toString());

        CanRegClientView.showAndPositionInternalFrame(desktopPane, editChecksInternalFrame);
    }

    private DatabaseRecord saveRecord(RecordEditorPanel recordEditorPanel) {
        boolean OK = true;
        DatabaseRecord databaseRecord = recordEditorPanel.getDatabaseRecord();
        if (databaseRecord instanceof Tumour) {
            // set the patient id to the active patient number
            RecordEditorPatient patientRecordEditorPanel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
            DatabaseRecord patientDatabaseRecord = patientRecordEditorPanel.getDatabaseRecord();
            updateTumourSequences();
            OK = associateTumourRecordToPatientRecord(databaseRecord, patientDatabaseRecord);
            if (!OK) 
                JOptionPane.showInternalMessageDialog(this, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PLEASE SAVE PATIENT RECORD FIRST."),
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"), 
                                JOptionPane.WARNING_MESSAGE);            
        } else if (databaseRecord instanceof Patient) {
            // see if the patient has been given ID already by looking at other patient records
            String patientID = null;
            for (DatabaseRecord patient : patientRecords) {
                Object tempPatientID = patient.getVariable(patientIDVariableName);
                if (patientID == null && tempPatientID != null) {
                    patientID = (String) tempPatientID;
                    if (patientID.trim().length() == 0)
                        patientID = null;                    
                }
            }
            if (patientID == null) {
                databaseRecord.setVariable(patientIDVariableName, null);
                databaseRecord.setVariable(patientRecordIDVariableName, null);
            } else 
                databaseRecord.setVariable(patientIDVariableName, patientID);            
        }
        if (OK) {
            try {
                databaseRecord = saveRecord(databaseRecord); 
                recordEditorPanel.refreshDatabaseRecord(databaseRecord);
                // refreshTitles(recordEditorPanel, dbr);
                if (databaseRecord instanceof Patient) {
                    addToPatientMap((RecordEditorPatient) recordEditorPanel, databaseRecord);
                    // String ID = (String) databaseRecord.getVariable(patientIDVariableName);
                    refreshTitles(recordEditorPanel, databaseRecord);
                }
            } catch (RecordLockedException ex) {
                JOptionPane.showInternalMessageDialog(this, 
                                ex.getLocalizedMessage(),
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"), 
                                JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                JOptionPane.showInternalMessageDialog(this, 
                                ex.getLocalizedMessage(), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"),
                                JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                JOptionPane.showInternalMessageDialog(this, 
                                ex.getLocalizedMessage(), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"), 
                                JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                JOptionPane.showInternalMessageDialog(this, 
                                ex.getLocalizedMessage(),
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"),
                                JOptionPane.WARNING_MESSAGE);
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return databaseRecord;
    }

    private void changePatientRecord(RecordEditorTumour tumourRecordEditorPanel) {
        Tumour tumourDatabaseRecord = (Tumour) tumourRecordEditorPanel.getDatabaseRecord();
        String requestedPatientRecordID = JOptionPane.showInputDialog(null,
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PLEASE ENTER PATIENT RECORD ID:"), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("MOVE TUMOUR TO WHICH PATIENT RECORD?"), 
                                JOptionPane.QUESTION_MESSAGE);
        if (requestedPatientRecordID != null) {
            // First see if it is one of the records shown
            RecordEditorPatient patientRecordEditorPanel = patientRecordsMap.get(requestedPatientRecordID);
            Patient patientDatabaseRecord = null;
            if (patientRecordEditorPanel != null)
                patientDatabaseRecord = (Patient) patientRecordEditorPanel.getDatabaseRecord();
            else {
                try {
                    patientDatabaseRecord = CanRegClientApp.getApplication().getPatientRecord(requestedPatientRecordID, false);
                } catch (Exception ex) {
                    Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (patientDatabaseRecord != null) {
                boolean OK = associateTumourRecordToPatientRecord(tumourDatabaseRecord, patientDatabaseRecord);
                if (OK) {
                    try {
                        saveRecord(tumourDatabaseRecord);
                        tumourTabbedPane.remove(tumourRecordEditorPanel);
                        JOptionPane.showInternalMessageDialog(this, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RECORD MOVED."));
                    } catch (Exception ex) {
                        Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                    tumourRecordEditorPanel.refreshDatabaseRecord(tumourDatabaseRecord);
                } else 
                    JOptionPane.showInternalMessageDialog(this, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PLEASE SAVE PATIENT RECORD FIRST."), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"), 
                                JOptionPane.WARNING_MESSAGE);
                
            } else 
                JOptionPane.showInternalMessageDialog(this, 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("NO SUCH PATIENT RECORD."), 
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("FAILED"),
                                JOptionPane.WARNING_MESSAGE);            
        }
    }

    private void runMPsearch(RecordEditorTumour recordEditorPanel) {
        DatabaseRecord databaseRecordA = recordEditorPanel.getDatabaseRecord();
        String topographyA = (String) databaseRecordA.getVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Topography.toString()).getDatabaseVariableName());
        String morphologyA = (String) databaseRecordA.getVariable(globalToolBox
                .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString()).getDatabaseVariableName());

        MultiplePrimaryTesterInterface multiplePrimaryTester = new DefaultMultiplePrimaryTester();
        if (tumourTabbedPane.getComponents().length > 1) {
            for (Component tumourPanelComponent : tumourTabbedPane.getComponents()) {
                RecordEditorTumour tumourPanel = (RecordEditorTumour) tumourPanelComponent;
                if (!recordEditorPanel.equals(tumourPanel)) {
                    DatabaseRecord dbr = tumourPanel.getDatabaseRecord();
                    String topographyB = (String) dbr.getVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Topography.toString()).getDatabaseVariableName());
                    String morphologyB = (String) dbr.getVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.Morphology.toString()).getDatabaseVariableName());
                    int result = multiplePrimaryTester.multiplePrimaryTest(topographyA, morphologyA, topographyB, morphologyB);
                    databaseRecordA.setVariable(globalToolBox
                            .translateStandardVariableNameToDatabaseListElement(Globals.StandardVariableNames.PersonSearch.toString())
                            .getDatabaseVariableName(), result);
                    
                    if (result == MultiplePrimaryTesterInterface.mptDuplicate ) {
                        // set pending
                        recordEditorPanel.setPending();
                    }
                    JOptionPane.showInternalMessageDialog(this, 
                                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RESULT: ")  
                                           + multiplePrimaryTester.mptCodes[result], 
                                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RESULT"), 
                                    JOptionPane.WARNING_MESSAGE);
                }
            }
        } else 
            JOptionPane.showInternalMessageDialog(this, 
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("ONLY ONE TUMOUR."), 
                            java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("RESULT"), 
                            JOptionPane.PLAIN_MESSAGE);        
    }

    private void runPersonSearch(RecordEditorPatient recordEditorPanel) {
        Cursor hourglassCursor = new Cursor(Cursor.WAIT_CURSOR);
        setCursor(hourglassCursor);
        WaitFrame waitFrame = new WaitFrame();
        waitFrame.setLabel(java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("SEARCHING..."));
        waitFrame.setIndeterminate(true);
        desktopPane.add(waitFrame, javax.swing.JLayeredPane.POPUP_LAYER);
        waitFrame.setVisible(true);
        waitFrame.setLocation((desktopPane.getWidth() - waitFrame.getWidth()) / 2, (desktopPane.getHeight() - waitFrame.getHeight()) / 2);
        Map<String, Float> map;
        try {
            DatabaseRecord sourceOfActionDatabaseRecord = recordEditorPanel.getDatabaseRecord();
            // buildDatabaseRecord();
            map = canreg.client.CanRegClientApp.getApplication().performDuplicateSearch((Patient) sourceOfActionDatabaseRecord, null);
            //remove patients with the same patientID -- already mapped
            String patientRecordID = (String) sourceOfActionDatabaseRecord.getVariable(patientIDVariableName);
            String records = "";
            waitFrame.dispose();
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            if (map.keySet().size() > 0) {
                // add records to the comparator
                ComparePatientsInternalFrame cpif = new ComparePatientsInternalFrame(desktopPane);
                cpif.addMainRecordSet((Patient) sourceOfActionDatabaseRecord, null);
                for (String prid : map.keySet()) {
                    if (patientRecordID.equals(prid)) {
                        // do nothing
                    } else {
                        try {
                            Patient patient2 = canreg.client.CanRegClientApp.getApplication().getPatientRecord(prid, false);
                            cpif.addRecordSet(patient2, null, map.get(prid));
                            records += java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("PATIENT ID: ") 
                                                    + patient2.getVariable(patientIDVariableName) 
                                                    + java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString(", SCORE: ")
                                                    + map.get(prid) + "%\n";
                        } catch (Exception ex) {
                            Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
                        } 
                    }
                }
                setCursor(normalCursor);
                JOptionPane.showInternalMessageDialog(this, 
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("POTENTIAL DUPLICATES FOUND:") + records);
                CanRegClientView.showAndPositionInternalFrame(desktopPane, cpif);
            } else {
                setCursor(normalCursor);
                JOptionPane.showInternalMessageDialog(this, 
                        java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("NO POTENTIAL DUPLICATES FOUND."));
                // recordEditorPanel.setPersonSearchStatus();
            }
        } catch (SecurityException ex) {
            Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
            setCursor(normalCursor);
        }
    }

    private boolean deleteRecord(DatabaseRecord record) {
        boolean success = false;
        int id = -1;
        String tableName = null;
        if (record instanceof Patient) {
            Object idObject = record.getVariable(Globals.PATIENT_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.PATIENT_TABLE_NAME;
        } else if (record instanceof Tumour) {
            // delete sources first.
            Tumour tumour = (Tumour) record;
            for (Source source : tumour.getSources()) 
                deleteRecord(source);
            
            Object idObject = record.getVariable(Globals.TUMOUR_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.TUMOUR_TABLE_NAME;
        } else if (record instanceof Source) {
            Object idObject = record.getVariable(Globals.SOURCE_TABLE_RECORD_ID_VARIABLE_NAME);
            if (idObject != null) 
                id = (Integer) idObject;            
            tableName = Globals.SOURCE_TABLE_NAME;
        }
        if (id >= 0) {
            try {
                canreg.client.CanRegClientApp.getApplication().releaseRecord(id, tableName);
                success = canreg.client.CanRegClientApp.getApplication().deleteRecord(id, tableName);
            } catch (SQLException ex) {
                JOptionPane.showInternalMessageDialog(this,
                                java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor")
                                .getString("THIS RECORD HAS OTHER RECORDS ASSIGNED TO IT.PLEASE DELETE OR MOVE THOSE FIRST."));
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.WARNING, null, ex);
            } catch (RecordLockedException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.WARNING, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return success;
    }

    @Action
    public void writePDF() {
        Set<DatabaseRecord> records = new LinkedHashSet();
        RecordEditorPatient panel = (RecordEditorPatient) patientTabbedPane.getSelectedComponent();
        records.add(panel.getDatabaseRecord());
        // String fileName = CanRegClientApp.getApplication().getLocalSettings().getProperty(LocalSettings.WORKING_DIR_PATH_KEY) + Globals.FILE_SEPARATOR + "Patient";
        String fileName = Globals.CANREG_PATIENT_PDFS_FOLDER + Globals.FILE_SEPARATOR;

        Object id = panel.getDatabaseRecord().getPatientID();
        if (id != null && id.toString().length() > 0) 
            fileName += id.toString();
        else 
            fileName += "Patient";        
        fileName += ".pdf";

        for (Component component : tumourTabbedPane.getComponents()) {
            RecordEditorTumour tumourPanel = (RecordEditorTumour) component;
            Tumour tumour = (Tumour) tumourPanel.getDatabaseRecord();
            records.add(tumour);
            records.addAll(tumour.getSources());
        }

        JComponentToPDF.databaseRecordsToPDF(records, fileName, globalToolBox);
        // System.out.println("Written to ");
        try {
            canreg.common.Tools.openFile(fileName);
        } catch (IOException ex) {
            Logger.getLogger(canreg.client.gui.dataentry2.RecordEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void runExactSearch(RecordEditorPatient recordEditorPanel) {
        DatabaseRecord record = recordEditorPanel.getDatabaseRecord();
        String[] variables = record.getVariableNames();
        LinkedList searchStringComponents = new LinkedList<String>();
        Map<String, DatabaseVariablesListElement> map = globalToolBox.getVariablesMap();
        // make a list of variables to skip
        // and numbers
        Set<String> skippable = new TreeSet<String>();
        Set<String> numbers = new TreeSet<String>();
        for(String key:map.keySet()) {
            DatabaseVariablesListElement databaseVariable = map.get(key);
            String stdVarbName = databaseVariable.getStandardVariableName();
            if (stdVarbName == null) stdVarbName = "";
            if (
                !stdVarbName.equals(Globals.StandardVariableNames.PatientCheckStatus.toString())   &&    
                !stdVarbName.equals(Globals.StandardVariableNames.PatientID.toString())   &&     
                !stdVarbName.equals(Globals.StandardVariableNames.PatientRecordID.toString())   &&     
                !stdVarbName.equals(Globals.StandardVariableNames.PatientRecordStatus.toString())   &&     
                !stdVarbName.equals(Globals.StandardVariableNames.PatientUpdateDate.toString())   &&     
                !stdVarbName.equals(Globals.StandardVariableNames.ObsoleteFlagPatientTable.toString())   &&     
                !stdVarbName.equals(Globals.StandardVariableNames.PatientUpdatedBy.toString())
                ) {
                skippable.add(canreg.common.Tools.toLowerCaseStandardized(key));
            } 
            if (!databaseVariable.getVariableType().equals(Globals.VARIABLE_TYPE_NUMBER_NAME)) 
                numbers.add(canreg.common.Tools.toLowerCaseStandardized(key));            
        }
        
        for (String varb:variables) {
            String content = record.getVariableAsString(varb);
            DatabaseVariablesListElement databaseVariable = map.get(varb);
            if (!content.trim().isEmpty() && skippable.contains(canreg.common.Tools.toLowerCaseStandardized(varb))) {
                if(!numbers.contains(canreg.common.Tools.toLowerCaseStandardized(varb)))
                    searchStringComponents.add(varb + "=" + content);
                else 
                    searchStringComponents.add(varb + " LIKE " + "'%" + content + "%'");                
            }
        }
        
        String searchString = canreg.common.Tools.combine(searchStringComponents, " AND ");

        if (searchString.trim().isEmpty()) {
            JOptionPane.showInternalMessageDialog(this, 
//                    java.util.ResourceBundle.getBundle("canreg/client/gui/dataentry2/resources/RecordEditor").getString("THIS RECORD HAS OTHER RECORDS ASSIGNED TO IT.PLEASE DELETE OR MOVE THOSE FIRST.")
                    "Please enter some data first.");
        } else {
            if (browseInternalFrame == null) 
                browseInternalFrame = new BrowseInternalFrame(desktopPane);
            else {
                browseInternalFrame.close();
                desktopPane.remove(browseInternalFrame);
                desktopPane.validate();
                browseInternalFrame = new BrowseInternalFrame(desktopPane);
            }
            CanRegClientView.showAndPositionInternalFrame(desktopPane, browseInternalFrame);
            maximizeHeight(desktopPane, browseInternalFrame);
            browseInternalFrame.setFilterField(searchString);
            browseInternalFrame.setTable(Globals.PATIENT_TABLE_NAME);
            browseInternalFrame.actionPerformed(new ActionEvent(this,1,"refresh"));
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        patientPopupMenu = new javax.swing.JPopupMenu();
        patientDeleteMenuItem = new javax.swing.JMenuItem();
        tumourPopupMenu = new javax.swing.JPopupMenu();
        tumourDeleteMenuItem = new javax.swing.JMenuItem();
        tumourObsoleteToggleButton = new javax.swing.JRadioButtonMenuItem();
        tumourChangePatientRecordMenuItem = new javax.swing.JMenuItem();
        jPanel2 = new javax.swing.JPanel();
        filler9 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler6 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton1 = new javax.swing.JButton();
        filler5 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        showObsoleteRecordsCheckBox = new javax.swing.JCheckBox();
        filler13 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        savetButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jButton3 = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        printButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler7 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        filler8 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel1 = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        filler16 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        patientMenuButton = new javax.swing.JButton();
        filler20 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel11 = new javax.swing.JPanel();
        filler15 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        patientTabbedPane = new canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        filler10 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        addTumourRecordButton = new javax.swing.JButton();
        filler11 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        tumourMenuButton = new javax.swing.JButton();
        filler12 = new javax.swing.Box.Filler(new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 0), new java.awt.Dimension(4, 32767));
        jPanel10 = new javax.swing.JPanel();
        filler14 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 10), new java.awt.Dimension(0, 10), new java.awt.Dimension(32767, 10));
        tumourTabbedPane = new canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane();

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getActionMap(RecordEditor.class, this);
        patientDeleteMenuItem.setAction(actionMap.get("deleteRecord")); // NOI18N
        patientDeleteMenuItem.setText("Delete record");
        patientPopupMenu.add(patientDeleteMenuItem);

        tumourDeleteMenuItem.setAction(actionMap.get("deleteRecord")); // NOI18N
        tumourDeleteMenuItem.setText("Delete record");
        tumourPopupMenu.add(tumourDeleteMenuItem);

        tumourObsoleteToggleButton.setAction(actionMap.get("setObsoleteFlag")); // NOI18N
        tumourObsoleteToggleButton.setSelected(true);
        tumourPopupMenu.add(tumourObsoleteToggleButton);

        tumourChangePatientRecordMenuItem.setAction(actionMap.get("changePatientRecord")); // NOI18N
        tumourPopupMenu.add(tumourChangePatientRecordMenuItem);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 28));
        jPanel2.setMinimumSize(new java.awt.Dimension(400, 28));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(0, 28));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add(filler9);
        jPanel2.add(filler6);

        jButton1.setAction(actionMap.get("changePatientID")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(canreg.client.CanRegClientApp.class).getContext().getResourceMap(RecordEditor.class);
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jPanel2.add(jButton1);
        jPanel2.add(filler5);

        showObsoleteRecordsCheckBox.setAction(actionMap.get("toggleShowObsoleteRecords")); // NOI18N
        showObsoleteRecordsCheckBox.setText(resourceMap.getString("showObsoleteRecordsCheckBox.text")); // NOI18N
        jPanel2.add(showObsoleteRecordsCheckBox);
        jPanel2.add(filler13);

        savetButton.setAction(actionMap.get("saveAllAction")); // NOI18N
        savetButton.setText(resourceMap.getString("saveAllAction.Action.text")); // NOI18N
        jPanel2.add(savetButton);
        jPanel2.add(filler1);
        jPanel2.add(filler4);

        jButton3.setAction(actionMap.get("writePDF")); // NOI18N
        jButton3.setText(resourceMap.getString("jButton3.text")); // NOI18N
        jPanel2.add(jButton3);
        jPanel2.add(filler2);

        printButton.setAction(actionMap.get("printAction")); // NOI18N
        printButton.setText(resourceMap.getString("printButton.text")); // NOI18N
        jPanel2.add(printButton);
        jPanel2.add(filler3);
        jPanel2.add(filler7);
        jPanel2.add(filler8);

        getContentPane().add(jPanel2);

        jPanel1.setMaximumSize(new java.awt.Dimension(20000, 20000));
        jPanel1.setOpaque(false);

        jSplitPane1.setDividerLocation(380);
        jSplitPane1.setDividerSize(7);
        jSplitPane1.setResizeWeight(0.25);
        jSplitPane1.setContinuousLayout(true);

        jPanel3.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel3.setLayout(new javax.swing.OverlayLayout(jPanel3));

        jPanel15.setOpaque(false);
        jPanel15.setPreferredSize(new java.awt.Dimension(450, 451));

        jPanel16.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel16.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel16.setOpaque(false);
        jPanel16.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel16.setLayout(new javax.swing.BoxLayout(jPanel16, javax.swing.BoxLayout.LINE_AXIS));
        jPanel16.add(filler16);

        patientMenuButton.setAction(actionMap.get("patientMenuAction")); // NOI18N
        patientMenuButton.setText("Menu\n");
        patientMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        patientMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel16.add(patientMenuButton);
        jPanel16.add(filler20);

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel16, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 379, Short.MAX_VALUE)
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 415, Short.MAX_VALUE))
        );

        jPanel3.add(jPanel15);

        jPanel11.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel11.setLayout(new javax.swing.BoxLayout(jPanel11, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel11.add(filler15);

        patientTabbedPane.setPreferredSize(new java.awt.Dimension(450, 451));
        jPanel11.add(patientTabbedPane);

        jPanel3.add(jPanel11);

        jSplitPane1.setLeftComponent(jPanel3);

        jPanel7.setPreferredSize(new java.awt.Dimension(700, 451));
        jPanel7.setLayout(new javax.swing.OverlayLayout(jPanel7));

        jPanel8.setOpaque(false);
        jPanel8.setPreferredSize(new java.awt.Dimension(700, 451));

        jPanel9.setMaximumSize(new java.awt.Dimension(32767, 36));
        jPanel9.setMinimumSize(new java.awt.Dimension(20, 36));
        jPanel9.setOpaque(false);
        jPanel9.setPreferredSize(new java.awt.Dimension(0, 36));
        jPanel9.setLayout(new javax.swing.BoxLayout(jPanel9, javax.swing.BoxLayout.LINE_AXIS));
        jPanel9.add(filler10);

        addTumourRecordButton.setAction(actionMap.get("addTumourAction")); // NOI18N
        addTumourRecordButton.setText("Add tumour record");
        addTumourRecordButton.setMaximumSize(new java.awt.Dimension(220, 23));
        addTumourRecordButton.setMinimumSize(new java.awt.Dimension(21, 23));
        jPanel9.add(addTumourRecordButton);
        jPanel9.add(filler11);

        tumourMenuButton.setAction(actionMap.get("tumourMenuAction")); // NOI18N
        tumourMenuButton.setText("Menu\n");
        tumourMenuButton.setMaximumSize(new java.awt.Dimension(100, 23));
        tumourMenuButton.setMinimumSize(new java.awt.Dimension(30, 23));
        jPanel9.add(tumourMenuButton);
        jPanel9.add(filler12);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 415, Short.MAX_VALUE))
        );

        jPanel7.add(jPanel8);

        jPanel10.setPreferredSize(new java.awt.Dimension(700, 451));
        jPanel10.setLayout(new javax.swing.BoxLayout(jPanel10, javax.swing.BoxLayout.PAGE_AXIS));
        jPanel10.add(filler14);

        tumourTabbedPane.setPreferredSize(new java.awt.Dimension(700, 451));
        jPanel10.add(tumourTabbedPane);

        jPanel7.add(jPanel10);

        jSplitPane1.setRightComponent(jPanel7);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1080, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
        );

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTumourRecordButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler10;
    private javax.swing.Box.Filler filler11;
    private javax.swing.Box.Filler filler12;
    private javax.swing.Box.Filler filler13;
    private javax.swing.Box.Filler filler14;
    private javax.swing.Box.Filler filler15;
    private javax.swing.Box.Filler filler16;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler20;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.Box.Filler filler5;
    private javax.swing.Box.Filler filler6;
    private javax.swing.Box.Filler filler7;
    private javax.swing.Box.Filler filler8;
    private javax.swing.Box.Filler filler9;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem patientDeleteMenuItem;
    private javax.swing.JButton patientMenuButton;
    private javax.swing.JPopupMenu patientPopupMenu;
    private canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane patientTabbedPane;
    private javax.swing.JButton printButton;
    private javax.swing.JButton savetButton;
    private javax.swing.JCheckBox showObsoleteRecordsCheckBox;
    private javax.swing.JMenuItem tumourChangePatientRecordMenuItem;
    private javax.swing.JMenuItem tumourDeleteMenuItem;
    private javax.swing.JButton tumourMenuButton;
    private javax.swing.JRadioButtonMenuItem tumourObsoleteToggleButton;
    private javax.swing.JPopupMenu tumourPopupMenu;
    private canreg.client.gui.dataentry2.components.FixedWidthRowTabbedPane tumourTabbedPane;
    // End of variables declaration//GEN-END:variables
}
