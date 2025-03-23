package Controller;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;


public class Server_Controller extends Thread {
    private Socket clientSocket;

    public Server_Controller(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("Server: Received message -> " + message);

                if (message.equalsIgnoreCase("DeleteAccount")) {
                    String username = in.readLine();
                    // Process deletion (existing code)…
                    boolean deleted = UserXML_Controller.deleteUser(username);
                    if (deleted) {
                        out.println("Account deleted successfully");
                    } else {
                        out.println("Account deletion failed");
                    }
                    out.flush();
                } else if (message.trim().equalsIgnoreCase("Logout")) {
                    System.out.println("Server: Logout command received. Waiting for username...");

                    // Read the username from the client
                    String username = in.readLine();

                    // Validate username
                    if (username == null || username.trim().isEmpty()) {
                        System.out.println("Server: ERROR - No valid username received for logout.");
                    }else if ("UploadImage".equals(message)) {
                        username = in.readLine();
                        String base64Image = in.readLine();

                        // Decode the Base64 string into image bytes
                        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                        // Save the image in the profiles folder
                        Path profilesDir = Paths.get("profiles");
                        if (!Files.exists(profilesDir)) {
                            Files.createDirectory(profilesDir);
                        }

                        Path imagePath = profilesDir.resolve(username + ".jpg");
                        Files.write(imagePath, imageBytes);

                        // Update the XML with the new image path
                        updateUserImagePath(username, imagePath.toString());

                        out.println("Image uploaded successfully.");
                    }else {
                        System.out.println("Server: User '" + username + "' has logged out at " + java.time.LocalDateTime.now());
                    }

                    // Do NOT send a response to the client
                    break; // Break out of the loop to close the connection
                }



                else {
                    out.println("Echo: " + message);
                }
            }
        } catch (IOException e) {
            // Handle exception without printing Base64 data.
            System.out.println("Error in client handler: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }


    public void handleClientRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String command = in.readLine();
            if ("UploadImage".equals(command)) {
                String username = in.readLine();
                String base64Image = in.readLine();

                // Decode the Base64 string into image bytes
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);

                // Save the image in the profiles folder
                Path profilesDir = Paths.get("profiles");
                if (!Files.exists(profilesDir)) {
                    Files.createDirectory(profilesDir);
                }

                Path imagePath = profilesDir.resolve(username + ".jpg");
                Files.write(imagePath, imageBytes);

                // Update the XML with the new image path (Optional)
                updateUserImagePath(username, imagePath.toString());

                out.println("Image uploaded successfully.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // This method updates the profile image path for the user in the XML file
    private void updateUserImagePath(String username, String imagePath) {
        try {
            File file = new File("9334_Team5_MidProject/res/users.xml");

            // If the XML file doesn't exist, create it
            if (!file.exists()) {
                createInitialXML(file);
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);
            document.getDocumentElement().normalize();

            NodeList userList = document.getElementsByTagName("User");
            boolean userFound = false;
            for (int i = 0; i < userList.getLength(); i++) {
                Node node = userList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    if (element.getAttribute("username").equals(username)) {
                        // Set the profile image path for the user
                        element.setAttribute("profileImagePath", imagePath);
                        userFound = true;
                        break;
                    }
                }
            }

            if (userFound) {
                // Write the changes back to the XML
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(file);
                transformer.transform(source, result);
            } else {
                System.out.println("Error: User not found for image update.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // This method creates an initial XML file with default user data if it doesn't exist
    private void createInitialXML(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // Create root element
            Element rootElement = document.createElement("Users");
            document.appendChild(rootElement);

            // Create initial user entry (can be empty for now)
            Element userElement = document.createElement("User");
            userElement.setAttribute("username", "defaultUser");
            userElement.setAttribute("profileImagePath", "");
            rootElement.appendChild(userElement);

            // Write document to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleGetProfileImageRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String username = in.readLine();
            Path imagePath = Paths.get("profiles", username + ".jpg");

            if (Files.exists(imagePath)) {
                byte[] imageBytes = Files.readAllBytes(imagePath);
                String base64Image = Base64.getEncoder().encodeToString(imageBytes);
                out.println(base64Image); // Send the Base64 image back to the client
            } else {
                out.println("Image not found");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}