package View;

import javax.swing.*;
import java.awt.*;
import javax.xml.parsers.*;

import Controller.PetsXML_Controller;
import Model.Pet_Model;
import org.w3c.dom.*;
import java.io.File;

public class Customer_AdoptionFormDetail_View extends JFrame {
    private String username;
    private String petId;
    private String status;
    private String serverAddress;
    private int serverPort;
    private JButton backButton;
    private JButton viewPetButton;
    private JTextArea formDetailsArea;


    public Customer_AdoptionFormDetail_View(String username, String petId, String serverAddress, int serverPort) {
        this.username = username;
        this.petId = petId;
        this.status = status;
        this.serverPort = serverPort;
        this.serverAddress = serverAddress;
        initComponents();
        loadFormForViewing();

    }

    private void initComponents() {
        backButton = new JButton("< Back");
        backButton.addActionListener(e -> dispose());

        viewPetButton = new JButton("View Pet");
        viewPetButton.addActionListener(e -> {
            // Fetch the pet details from your XML using the petId.
            Pet_Model pet = PetsXML_Controller.getPetDetails(petId);
            if (pet != null) {
                // Create and display the customer pet profile view using the retrieved details.
                Customer_Pet_Profile_View petProfileView = new Customer_Pet_Profile_View(
                        username,  // Customer's username
                        petId,             // Use the petId variable directly
                        pet.getBreed(),    // Breed from the pet model
                        pet.getType(),     // Type from the pet model
                        pet.getAge(),      // Age from the pet model
                        pet.getHealthStatus(),   // Health status from the pet model
                        pet.getAdoptionStatus(), // Adoption status from the pet model
                        pet.getImageBase64(),          // Pet image (e.g., an ImageIcon) from the model
                        serverAddress,       // Server address (adjust if needed)
                        serverPort               // Server port (adjust if needed)
                );
                petProfileView.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Unable to load pet details for Pet ID: " + petId,
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });


        formDetailsArea = new JTextArea();
        formDetailsArea.setEditable(false);
        formDetailsArea.setLineWrap(true);
        formDetailsArea.setWrapStyleWord(true);
        formDetailsArea.setFont(new Font("SansSerif", Font.BOLD, 18));
        JScrollPane scrollPane = new JScrollPane(formDetailsArea);

        // Layout using BorderLayout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(viewPetButton);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setTitle("Adoption Form Details");
        setPreferredSize(new Dimension(500, 400));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }


    private String getTagValue(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }

    private void loadFormDetails() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File xmlFile = new File("res/adoptionForms.xml");

            if (!xmlFile.exists()) {
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
                    String formUsername = formElem.getAttribute("username");
                    String formPetId = getTagValue(formElem, "PetID");

                    // Ensure we're matching the correct username and pet ID
                    if (formUsername.equalsIgnoreCase(username) && formPetId.equalsIgnoreCase(petId)) {
                        String living = getTagValue(formElem, "LivingSituation");
                        String experience = getTagValue(formElem, "ExperienceWithPets");
                        String reason = getTagValue(formElem, "ReasonForAdoption");
                        String stat = getTagValue(formElem, "Status");

                        String details = "Submitted by: " + formUsername + "\n" +
                                "Pet ID: " + formPetId + "\n" +
                                "Living Situation: " + living + "\n" +
                                "Experience with Pets: " + experience + "\n" +
                                "Reason for Adoption: " + reason + "\n" +
                                "Status: " + stat;

                        formDetailsArea.setText(details);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                formDetailsArea.setText("No adoption form found for user: " + username + " with Pet ID: " + petId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            formDetailsArea.setText("Error loading form details.");
        }
    }

    private void loadFormForViewing() {
        try {

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File xmlFile = new File("res/adoptionForms.xml");

            if (!xmlFile.exists()) {
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
                    String formUsername = formElem.getAttribute("username");
                    String formPetId = getTagValue(formElem, "PetID");


                    // ✅ FIX: Ensure correct matching of username and pet ID
                    if (formUsername.equalsIgnoreCase(username) && formPetId.equalsIgnoreCase(petId)) {
                        String living = getTagValue(formElem, "LivingSituation");
                        String experience = getTagValue(formElem, "ExperienceWithPets");
                        String reason = getTagValue(formElem, "ReasonForAdoption");
                        String status = getTagValue(formElem, "Status");

                        // ✅ Display adoption form details correctly
                        String details = "Submitted by: " + formUsername + "\n" +
                                "Pet ID: " + formPetId + "\n" +
                                "Living Situation: " + living + "\n" +
                                "Experience with Pets: " + experience + "\n" +
                                "Reason for Adoption: " + reason + "\n" +
                                "Status: " + status;

                        formDetailsArea.setText(details);
                        found = true;
                        break;
                    }
                }
            }

            // Handle case where no matching form is found
            if (!found) {
                formDetailsArea.setText("No adoption form found for user: " + username + " with Pet ID: " + petId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            formDetailsArea.setText("Error loading form details.");
        }
    }
}