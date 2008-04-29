/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package canreg.server.management;

import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author morten
 */
public class SystemDefinitionConverter {

    private String canReg4FileName;
    private boolean debug = true;
    private String namespace = "ns3:";
    private Document doc;
    private DataInputStream dataStream;
    private String[] standardVariablesCR4 = {"RegistrationNo",
        "IncidenceDate",
        "BirthDate",
        "Age",
        "Sex",
        "Topography",
        "Morphology",
        "Behaviour",
        "BasisDiagnosis",
        "ICD10",
        "Mult.Prim.Code",
        "CheckStatus",
        "PersonSearch",
        "RecordSearch",
        "FirstName",
        "Surname",
        "UpdateDate",
        "Lastcontact",
        "Grade",
        "ICCC",
        "AddressCode",
        "Mult.Prim.Seq.",
        "Mult.Prim.Tot.",
        "Stage",
        "Source1",
        "Source2",
        "Source3",
        "Source4",
        "Source5",
        "Source6"
    };
    private String[] dictionaryFontTypeValues = {"Latin", "Asian"};
    private String[] dictionaryTypeValues = {"Simple", "Compound"};
    private String[] fillInStatusValues = {"Optional", "Mandatory", "Automatic", "System"};
    private String[] variableTypeValues = {"Number", "Alpha", "Date", "Dict", "AsianText"};
    private String[] mpCopyValues = {"Must", "Prob", "Intr", "Othr"};

    /**
     * @param CanReg4 system definition canReg4FileName 
     * Convert CanReg4 system definition files into CanReg5 system definition XML
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage:\nSystemDefinitionConverter <CanReg4 system definition file>");
        } else {
            try {
                new SystemDefinitionConverter(args[0]);
            } catch (FileNotFoundException ex) {
                System.out.println(args[0] + " not found. " + ex);
                Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private SystemDefinitionConverter(String canReg4FileName) throws FileNotFoundException {
        this.canReg4FileName = canReg4FileName;

        try {

            //Create instance of DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            //Get the DocumentBuilder
            DocumentBuilder parser = factory.newDocumentBuilder();
            //Create blank DOM Document
            doc = parser.newDocument();

            doc.setXmlStandalone(true);

            //Create the root
            Element root = doc.createElement(namespace + "canreg");
            root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            root.setAttribute("xmlns:ns3", "http://xml.netbeans.org/schema/canregSystemFileSchema");
            root.setAttribute("xsi:schemaLocation", "http://xml.netbeans.org/schema/canregSystemFileSchema ../../META-INF/canregSystemFileSchema.xsd");

            doc.appendChild(root);

            // Open the file
            InputStream istream = new FileInputStream(canReg4FileName);
            // Decode using a DataInputStream - you can use this if the
            // bytes are written as IEEE format.
            dataStream = new DataInputStream(istream);

            try {
                String temp;
                Element element;
                Element parentElement;


                // Create the general part
                //
                parentElement = doc.createElement(namespace + "general");
                root.appendChild(parentElement);

                // Read and add the 3 letter code                
                parentElement.appendChild(createElement(namespace + "registry_code", readBytes(3)));
                // Read the region code
                parentElement.appendChild(createElement(namespace + "region_code", readBytes(1)));
                // Read the Registry name
                parentElement.appendChild(createElement(namespace + "registry_name", readText().replace('|', ' ')));
                // Read the working language
                parentElement.appendChild(createElement(namespace + "working_language", readBytes(1)));

                // Create the Dictionary part
                //
                parentElement = doc.createElement(namespace + "dictionaries");
                root.appendChild(parentElement);

                int numberOfDictionaries = readNumber(2);
                System.out.println(numberOfDictionaries);

                for (int i = 0; i < numberOfDictionaries; i++) {
                    element = doc.createElement(namespace + "dictionary");
                    parentElement.appendChild(element);
                    element.appendChild(createElement(namespace + "dictionary_id", "" + i));
                    element.appendChild(createElement(namespace + "name", readText()));
                    element.appendChild(createElement(namespace + "font", dictionaryFontTypeValues[dataStream.readByte()]));
                    byte type = dataStream.readByte();
                    element.appendChild(createElement(namespace + "type", dictionaryTypeValues[type]));
                    if (type == (byte) 0) {
                        element.appendChild(createElement(namespace + "code_length", "" + 0));
                        element.appendChild(createElement(namespace + "category_description_length", "" + 0));
                    } else if (type == (byte) 1) {
                        element.appendChild(createElement(namespace + "code_length", "" + readNumber(2)));
                        element.appendChild(createElement(namespace + "category_description_length", "" + readNumber(2)));
                    } else {
                        System.out.println("Error among the dictionaries...");
                    }
                    element.appendChild(createElement(namespace + "full_dictionary_code_length", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "full_dictionary_description_length", "" + readNumber(2)));
                }

                // Create the Groups part
                //
                parentElement = doc.createElement(namespace + "groups");
                root.appendChild(parentElement);

                int numberOfGroups = readNumber(2);
                System.out.println(numberOfGroups);

                for (int i = 0; i < numberOfGroups; i++) {
                    element = doc.createElement(namespace + "group");
                    parentElement.appendChild(element);
                    element.appendChild(createElement(namespace + "group_id", "" + i));
                    element.appendChild(createElement(namespace + "name", readText()));
                    //skip unused variables
                    // Group order
                    readNumber(2);
                    //Group height
                    readNumber(2);
                }

                // Create the Variables part
                //
                parentElement = doc.createElement(namespace + "variables");
                root.appendChild(parentElement);

                int numberOfVariables = readNumber(2);
                System.out.println(numberOfVariables);

                for (int i = 0; i < numberOfVariables; i++) {
                    element = doc.createElement(namespace + "variable");
                    parentElement.appendChild(element);

                    element.appendChild(createElement(namespace + "variable_id", "" + i));
                    element.appendChild(createElement(namespace + "full_name", readText()));

                    // short_name is the name that will be used in the database
                    String nameInDatabase = readText();

                    if (canreg.server.database.Tools.isReservedWord(nameInDatabase)) {
                        System.out.println("Warning: " + nameInDatabase.toUpperCase() + " is a reserverd word.");
                        System.out.println("Please revise the XML file manually before building CanReg5 database...");
                    }
                    element.appendChild(createElement(namespace + "short_name", nameInDatabase));

                    element.appendChild(createElement(namespace + "english_name", readText()));

                    String groupIDString = "" + dataStream.readByte();
                    element.appendChild(createElement(namespace + "group_id", "" + groupIDString));
                    element.appendChild(createElement(namespace + "variable_name_X_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_name_Y_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_X_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "variable_Y_pos", "" + readNumber(2)));
                    element.appendChild(createElement(namespace + "fill_in_status", fillInStatusValues[dataStream.readByte()]));
                    element.appendChild(createElement(namespace + "multiple_primary_copy", mpCopyValues[dataStream.readByte()]));
                    byte type = dataStream.readByte();
                    element.appendChild(createElement(namespace + "variable_type", variableTypeValues[type]));
                    // Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
                    if (type == (byte) 0 || type == (byte) 1 || type == (byte) 4) {
                        element.appendChild(createElement(namespace + "variable_length", "" + readNumber(2)));
                    } else if (type == (byte) 2) {
                        element.appendChild(createElement(namespace + "variable_length", "8"));
                    } else if (type == (byte) 3) {
                        int dictionaryNumber = readNumber(2);
                        Element dictionaryElement = (Element) doc.getElementsByTagName(namespace + "dictionary").item(dictionaryNumber);
                        String dictionaryType = dictionaryElement.getElementsByTagName(namespace + "type").item(0).getTextContent();
                        // System.out.println(dictionaryElement.getTagName() + " " + dictionaryType);
                        element.appendChild(createElement(namespace + "use_dictionary", "" + dictionaryElement.getElementsByTagName(namespace + "name").item(0).getTextContent()));
                        // (0 Simple, 1 Compound)
                        if (dictionaryType.equalsIgnoreCase(dictionaryTypeValues[0])) {
                            element.appendChild(createElement(namespace + "category_X_pos", "0"));
                            element.appendChild(createElement(namespace + "category_Y_pos", "0"));
                        } else if (dictionaryType.equalsIgnoreCase(dictionaryTypeValues[1])) {
                            element.appendChild(createElement(namespace + "category_X_pos", "" + readNumber(2)));
                            element.appendChild(createElement(namespace + "category_Y_pos", "" + readNumber(2)));
                        } else {
                            System.out.println("Invalid dict type...");
                        }
                        element.appendChild(createElement(namespace + "dictionary_X_pos", "" + readNumber(2)));
                        element.appendChild(createElement(namespace + "dictionary_Y_pos", "" + readNumber(2)));
                    } else {
                        System.out.println("Invalid variable description...");
                    }
                    // Place variable in the right table
                    int groupID = Integer.parseInt(groupIDString);
                    Element groupElement = (Element) doc.getElementsByTagName(namespace + "group").item(groupID);
                    String groupName = groupElement.getElementsByTagName(namespace + "name").item(0).getTextContent();
                    if (groupName.equalsIgnoreCase("patient") || groupName.equalsIgnoreCase("follow up")) {
                        element.appendChild(createElement(namespace + "table", "Patient"));
                    } else {
                        element.appendChild(createElement(namespace + "table", "Tumour"));
                    }
                }
                
                
                // Add the new System variables
                //    private Element createVariable(int variableId, String fullName, String shortName,
                //    String englishName, int groupID, String fillInStatus, String multiplePrimaryCopy,
                //    String variableType, int variableLength, int useDictionary, String table, String standardVariableName) {
                int variableNumber = numberOfVariables;

                // Pointer to Patient from Tumour
                parentElement.appendChild(
                        createVariable(variableNumber++, "Patient ID", "PatientID", "PatientID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Tumour", "PatientID"));
                // Pointer to Tumour from Patient
                parentElement.appendChild(
                        createVariable(variableNumber++, "Tumour ID", "TumourID", "TumourID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Patient", "TumourID"));
                
                // Forward and backward pointers...
                // Pointer to records of the same Tumour information
                parentElement.appendChild(
                        createVariable(variableNumber++, "Next Tumour Record ID", "NextTumourRecID", "Next Tumour Record ID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Tumour", "NextTumourID"));
                parentElement.appendChild(
                        createVariable(variableNumber++, "Last Tumour Record ID", "LastTumourRecID", "Last Tumour Record ID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Tumour", "LastTumourID"));
                // Pointer to records of the same Patient information
                parentElement.appendChild(
                        createVariable(variableNumber++, "Next Patient Record ID", "NextPatientRecID", "Next Patient Record ID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Patient", "NextPatientID"));
                parentElement.appendChild(
                        createVariable(variableNumber++, "Last Patient Record ID", "LastPatientRecID", "Last Patient Record ID",
                        -1, "Automatic", "Othr", "Number", -1, -1, "Patient", "LastPatientID"));

                // Create the Indexes part
                //
                parentElement = doc.createElement(namespace + "indexes");
                root.appendChild(parentElement);

                int numberOfIndexes = readNumber(2);
                System.out.println(numberOfIndexes);
                for (int i = 0; i < numberOfIndexes; i++) {
                    element = doc.createElement(namespace + "index");
                    parentElement.appendChild(element);
                    Element childElement = createElement(namespace + "name", readText());
                    element.appendChild(childElement);
                    for (int j = 0; j < 3; j++) {
                        int variableIndex = readNumber(2);
                        if (variableIndex > 0) {
                            Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                            String variableName = variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent();
                            Element thisElement = doc.createElement(namespace + "indexed_variable");
                            element.appendChild(thisElement);
                            thisElement.appendChild(createElement(namespace + "variable_name", variableName));
                        }
                    }
                }

                // Create the Person Search part
                //
                parentElement = doc.createElement(namespace + "search_variables");
                root.appendChild(parentElement);

                int numberOfSearchVarbs = readNumber(2);
                System.out.println(numberOfSearchVarbs);
                for (int i = 0; i < numberOfSearchVarbs; i++) {
                    element = doc.createElement(namespace + "search_variable");
                    parentElement.appendChild(element);

                    int variableIndex = readNumber(2);
                    // Element childElement = createElement(namespace + "name", readText());
                    // element.appendChild(childElement);

                    Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                    String variableName = variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent();

                    element.appendChild(createElement(namespace + "variable_name", variableName));
                    element.appendChild(createElement(namespace + "weigth", "" + readNumber(2)));

                }
                parentElement.appendChild(createElement(namespace + "minimum_match", "" + readNumber(2)));


                // Create the Standard variable part
                //
                int numberOfStandardVarbs = readNumber(2);
                System.out.println(numberOfStandardVarbs);
                for (int i = 0; i < numberOfStandardVarbs; i++) {
                    int variableIndex = readNumber(2);
                    if (variableIndex > -1) {
                        Element variableElement = (Element) doc.getElementsByTagName(namespace + "variable").item(variableIndex);
                        // System.out.println(i+ " " + variableElement.getElementsByTagName(namespace + "short_name").item(0).getTextContent());
                        variableElement.appendChild(createElement(namespace + "standard_variable_name", standardVariablesCR4[i]));
                    }
                }

                // Create the Miscellaneous part
                //
                parentElement = doc.createElement(namespace + "miscellaneous");
                root.appendChild(parentElement);

                Element codingElement = doc.createElement(namespace + "coding");
                parentElement.appendChild(codingElement);
                Element settingsElement = doc.createElement(namespace + "settings");
                parentElement.appendChild(settingsElement);

                codingElement.appendChild(createElement(namespace + "male_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "female_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "unknown_sex_code", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "date_format", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "date_separator", readBytes(1)));
                settingsElement.appendChild(createElement(namespace + "fast_safe_mode", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "morphology_length", readBytes(1)));
                settingsElement.appendChild(createElement(namespace + "mult_prim_rules", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "special_registry", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "password_rules", "" + dataStream.readByte()));
                settingsElement.appendChild(createElement(namespace + "data_entry_language", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "registration_number_type", "" + dataStream.readByte()));
                codingElement.appendChild(createElement(namespace + "mult_prim_code_length", readBytes(1)));
                codingElement.appendChild(createElement(namespace + "basis_diag_codes", "" + dataStream.readByte()));

            } catch (EOFException e) {
            // Nothing to do
            } catch (IOException e) {
            // Nothing to do
            } finally {
                if (debug) {
                    canreg.server.xml.Tools.writeXmlFile(doc, "CanReg.xml");
                }
                dataStream.close();
            }
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(SystemDefinitionConverter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.out.println("Something wrong with the file... " + ex);
        }
    }

    public Document getDoc() {
        return doc;
    }

    private String translate(String variableName, String value) {
        String newValue = value;

        return value;
    }

    private Element createVariable(int variableId, String fullName, String shortName,
            String englishName, int groupID, String fillInStatus, String multiplePrimaryCopy,
            String variableType, int variableLength, int useDictionary, String table, String standardVariableName) {


        Element element = doc.createElement(namespace + "variable");

        element.appendChild(createElement(namespace + "variable_id", "" + variableId));
        element.appendChild(createElement(namespace + "full_name", fullName));
        element.appendChild(createElement(namespace + "short_name", shortName));
        element.appendChild(createElement(namespace + "english_name", englishName));
        element.appendChild(createElement(namespace + "group_id", "" + groupID));
        element.appendChild(createElement(namespace + "fill_in_status", fillInStatus));
        element.appendChild(createElement(namespace + "multiple_primary_copy", multiplePrimaryCopy));
        element.appendChild(createElement(namespace + "variable_type", variableType));
        // Varb Type  (0 number, 1 alpha, 2 date, 3 dict, 4 asian text)
        if (variableType.equalsIgnoreCase(variableTypeValues[0]) || variableType.equalsIgnoreCase(variableTypeValues[1]) || variableType.equalsIgnoreCase(variableTypeValues[4])) {
            element.appendChild(createElement(namespace + "variable_length", "" + variableLength));
        } else if (variableType.equalsIgnoreCase(variableTypeValues[2])) {
            element.appendChild(createElement(namespace + "variable_length", "8"));
        } else if (variableType.equalsIgnoreCase(variableTypeValues[3])) {
            element.appendChild(createElement(namespace + "use_dictionary", "" + useDictionary));
        // (0 Simple, 1 Compound)
        } else {
            System.out.println("Invalid variable description...");
        }
        // Place variable in the right table
        element.appendChild(createElement(namespace + "table", table));
        element.appendChild(createElement(namespace + "standard_variable_name", standardVariableName));
        return element;
    }

    private Element createElement(String variableName, String value) {
        Element childElement = doc.createElement(variableName);
        childElement.appendChild(doc.createTextNode(value));
        return childElement;
    }

    private String readBytes(int numberOfBytes) throws IOException {
        String temp = "";
        for (int i = 0; i < numberOfBytes; i++) {
            char c = (char) dataStream.readByte();
            temp += c;
        }
        return temp;
    }

    private String readText() throws IOException {
        String temp = "";
        int b = dataStream.readByte();

        while (b != 0) {
            // System.out.println(""+b);
            temp += (char) b;
            b = (char) dataStream.readByte();
        }
        return temp;
    }

    private int readNumber(int numberOfBytes) throws IOException {
        int value = 0;
        byte[] byteArray = new byte[numberOfBytes];
        for (int i = 0; i < numberOfBytes; i++) {
            byteArray[i] = dataStream.readByte();
        }
        value = byteArrayToIntLH(byteArray);
        return value;
    }

    // Convert a byte array with the most significant byte in the first position to integer
    public static final int byteArrayToIntHL(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == (b.length - 1)) {
                value += (b[i] & 0xFF);
            } else if (i == 0) {
                value += b[i] << ((b.length - i) * 8);
            } else {
                value += (b[i] & 0xFF) << ((b.length - i) * 8);
            }
        }
        return value;
    }

    // Convert a byte array with the most significant byte in the last position to integer
    public static final int byteArrayToIntLH(byte[] b) {
        int value = 0;
        for (int i = 0; i < b.length; i++) {
            if (i == 0) {
                value += (b[i] & 0xFF);
            } else if (i == (b.length - 1)) {
                value += b[i] << (i * 8);
            } else {
                value += (b[i] & 0xFF) << (i * 8);
            }
        }
        return value;
    }
}
