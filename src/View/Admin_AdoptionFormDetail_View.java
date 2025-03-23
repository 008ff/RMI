package View;

import Controller.PetsXML_Controller;
import Model.Pet_Model;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.File;

import static View.AdminDashboardView.username;

public class Admin_AdoptionFormDetail_View extends JFrame {
    private AdoptionForms_View parentView;
    private String submitter;
    private String petId;
    private String serverAddress;
    private int serverPort;

    // UI Components
    private JButton backButton;
    private JButton acceptButton;
    private JButton declineButton;
    private JButton viewPetButton;
    private JTextArea formDetailsArea;
    private static String breed;

    public Admin_AdoptionFormDetail_View(String submitter, String petId, String serverAddress, int serverPort)
    {
        this.submitter = submitter;
        this.petId = petId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.parentView = parentView;
        initComponents();
        loadFormDetails();
    }


    private void initComponents() {
        // Create buttons
        backButton = new JButton("< Back");
        backButton.addActionListener(e -> dispose());

        viewPetButton = new JButton("View Pet");
        // Set action for viewPetButton as needed:
        viewPetButton.addActionListener(e -> {
            // Use petId variable to fetch the pet details
            Pet_Model pet = PetsXML_Controller.getPetDetails(petId);
            if (pet != null) {
                // Open the Admin_Pet_Profile_View using the petId variable for the ID,
                // and other details from the retrieved pet model.
                Admin_Pet_Profile_View petProfileView = new Admin_Pet_Profile_View(
                        username,         // current username (admin's context)
                        petId,            // use the petId variable directly
                        pet.getBreed(),   // pet breed from the model
                        pet.getType(),    // pet type from the model
                        pet.getAge(),     // pet age from the model
                        pet.getHealthStatus(),   // pet health status from the model
                        pet.getAdoptionStatus(), // pet adoption status from the model
                        pet.getImageBase64(),          // pet image from the model (e.g., an ImageIcon)
                        serverAddress,    // server address from this view
                        serverPort        // server port from this view
                );
                petProfileView.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Unable to load details for Pet ID: " + petId, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });



        acceptButton = new JButton("Accept");
        acceptButton.addActionListener(e -> {
            boolean updated = updateAdoptionFormStatus("Accepted");
            if (updated) {
                JOptionPane.showMessageDialog(this, "Form accepted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Refresh the parent's table if a reference exists
                if (parentView != null) {
                    parentView.loadAdoptionForms();
                }
                // Close only this detail view
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating form status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        declineButton = new JButton("Decline");
        declineButton.addActionListener(e -> {
            boolean updated = updateAdoptionFormStatus("Declined");
            if (updated) {
                JOptionPane.showMessageDialog(this, "Form declined.", "Success", JOptionPane.INFORMATION_MESSAGE);
                if (parentView != null) {
                    parentView.loadAdoptionForms();
                }
                // Close only this detail view
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error updating form status.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Create a non-editable text area for the form details
        formDetailsArea = new JTextArea();
        formDetailsArea.setEditable(false);
        formDetailsArea.setLineWrap(true);
        formDetailsArea.setWrapStyleWord(true);
        JScrollPane detailsScrollPane = new JScrollPane(formDetailsArea);

        // Top panel with back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);

        // Bottom panel: separate rows for the "View Pet" button and for the action buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel viewPetPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewPetPanel.add(viewPetButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        actionPanel.add(acceptButton);
        actionPanel.add(Box.createHorizontalStrut(20)); // Spacing between buttons
        actionPanel.add(declineButton);

        bottomPanel.add(viewPetPanel);
        bottomPanel.add(actionPanel);

        // Use BorderLayout in the frame to maximize available space
        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(detailsScrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setTitle("Adoption Form Details");
        setSize(400, 330);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void loadFormDetails() {
        try {
            // Prepare XML parsing
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File xmlFile = new File("res/adoptionForms.xml");
            if(!xmlFile.exists()){
                formDetailsArea.setText("No adoption forms found.");
                return;
            }
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList formList = document.getElementsByTagName("AdoptionForm");
            boolean found = false;
            for (int i = 0; i < formList.getLength(); i++) {
                Node node = formList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element formElem = (Element) node;
                    // Get the username attribute and the PetID element
                    String formUsername = formElem.getAttribute("username");
                    String formPetId = getTagValue(formElem, "PetID");
                    // Check if this is the form we want (match both username and petId)
                    if (formUsername.equalsIgnoreCase(submitter) && formPetId.equalsIgnoreCase(petId)) {
                        // Found the matching form. Retrieve other details.
                        String living = getTagValue(formElem, "LivingSituation");
                        String experience = getTagValue(formElem, "ExperienceWithPets");
                        String reason = getTagValue(formElem, "ReasonForAdoption");
                        String status = getTagValue(formElem, "Status");
                        formDetailsArea.setFont(new Font("SansSerif", Font.BOLD, 23));
                        String details = "Submitted by: " + formUsername + "\n" +
                                "Pet ID: " + formPetId + "\n" +
                                "Living Situation: " + living + "\n" +
                                "Experience with Pets: " + experience + "\n" +
                                "Reason for Adoption: " + reason + "\n" +
                                "Status: " + status;
                        formDetailsArea.setText(details);
                        formDetailsArea.setFont(new Font("SansSerif", Font.BOLD, 18));
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                formDetailsArea.setText("No adoption form found for " + submitter + " with Pet ID: " + petId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            formDetailsArea.setText("Error loading form details.");
        }
    }

    // Helper method to get the text content of a given tag in an element.
    private String getTagValue(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }


    // Dummy method to update the adoption form status.
    // Replace with your actual XML update logic.
    private boolean updateAdoptionFormStatus(String newStatus) {
        try {
            // Load the existing XML file
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File xmlFile = new File("res/adoptionForms.xml");
            if (!xmlFile.exists()) {
                System.out.println("Adoption forms file not found.");
                return false;
            }
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // Get all <AdoptionForm> elements
            NodeList formList = document.getElementsByTagName("AdoptionForm");
            boolean updated = false;
            for (int i = 0; i < formList.getLength(); i++) {
                Node node = formList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element formElem = (Element) node;
                    // Get the username attribute and the PetID element
                    String formUsername = formElem.getAttribute("username");
                    String formPetId = "";
                    NodeList petIdList = formElem.getElementsByTagName("PetID");
                    if (petIdList.getLength() > 0) {
                        formPetId = petIdList.item(0).getTextContent().trim();
                    }
                    // Check if this is the form we want to update (match both submitter and petId)
                    if (formUsername.equalsIgnoreCase(submitter) && formPetId.equalsIgnoreCase(petId)) {
                        // Find the <Status> element, create one if it doesn't exist
                        NodeList statusList = formElem.getElementsByTagName("Status");
                        if (statusList.getLength() > 0) {
                            statusList.item(0).setTextContent(newStatus);
                        } else {
                            Element statusElem = document.createElement("Status");
                            statusElem.setTextContent(newStatus);
                            formElem.appendChild(statusElem);
                        }
                        updated = true;
                        break;
                    }
                }
            }
            if (updated) {
                // Save the updated XML document back to file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);
                return true;
            } else {
                System.out.println("No matching adoption form found for update.");
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
