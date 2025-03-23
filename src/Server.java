import Controller.*;
import Model.Pet_Model;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Vector;

public class Server {
    private static final int PORT = 6969;

    public static void main(String[] args) throws IOException {
        System.out.println("Server IP: " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("Server is listening on port: " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress());

                    // Create input and output streams
                    InputStream inputStream = clientSocket.getInputStream();
                    OutputStream outputStream = clientSocket.getOutputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    PrintWriter writer = new PrintWriter(outputStream, true);

                    // Read action from client
                    String action = reader.readLine();
                    System.out.println("Action received: " + action);

                    // Logout Action
                    if ("Logout".equalsIgnoreCase(action)) {
                        String username = reader.readLine();
                        System.out.println("Server: Logout request received for user: " + username);
                        writer.println("Logout successful");
                        writer.flush();
                        clientSocket.close();
                        System.out.println("Server: Client " + username + " logged out and disconnected.");
                        // Handle Logout sa Settings_Controller pano yun
                        continue;
                    }

                    // Sign-up Action
                    else if ("Sign-up".equalsIgnoreCase(action)) {
                        System.out.println("Waiting for Sign-up request...");

                        String username = reader.readLine();
                        System.out.println("Received Username: " + username);

                        String password = reader.readLine();
                        System.out.println("Received Password: " + password);

                        String role = reader.readLine();
                        System.out.println("Received Role: " + role);

                        if (username != null && password != null && role != null) {
                            boolean success = UserXML_Controller.addUser(username, password, role);
                            if (success) {
                                writer.println("User added successfully");
                                System.out.println("User " + username + " signed up successfully.");
                            } else {
                                writer.println("User already exists");
                                System.out.println("Sign-up failed: User already exists.");
                            }
                        }
                        writer.flush();
                        continue;
                    }

                    // Login Action
                    else if ("Login".equalsIgnoreCase(action)) {
                        String username = reader.readLine();
                        String password = reader.readLine();

                        // Wait for valid username and password if needed.
                        while (username == null || password == null) {
                            username = reader.readLine();
                            password = reader.readLine();
                        }
                        System.out.println("Validating login for: " + username);

                        String userRole = UserXML_Controller.validateUser(username, password);
                        if (userRole != null) {
                            writer.println("Login successful, Role: " + userRole);
                            System.out.println("User " + username + " logged in successfully.");
                        } else {
                            writer.println("Invalid username or password");
                            System.out.println("Login failed for: " + username);
                        }
                        writer.flush();
                        continue;
                    }

                    // Delete Account Action
                    else if ("DeleteAccount".equalsIgnoreCase(action)) {
                        System.out.println("Received Delete Account request...");
                        String username = reader.readLine();
                        System.out.println("🔹 Username received for deletion: " + username);

                        if (username == null || username.isEmpty()) {
                            System.out.println("ERROR: Username is null or empty!");
                            writer.println("Error: Username is empty");
                            writer.flush();
                            continue;
                        }

                        boolean deleted = UserXML_Controller.deleteUser(username);
                        if (deleted) {
                            writer.println("Account deleted successfully");
                            System.out.println("✅ User " + username + " deleted successfully from the system.");
                        } else {
                            writer.println("Account deletion failed");
                            System.out.println("Failed to delete account for: " + username);
                        }
                        writer.flush();
                        continue;
                    }

                    // Display Pet Action
                    else if ("load-pets".equalsIgnoreCase(action)) {
                        Vector<Vector<Object>> pets = PetsXML_Controller.loadPets();

                        for (Vector<Object> pet : pets) {
                            for (Object value : pet) {
                                writer.println(value.toString());
                            }
                        }

                        writer.println("end");
                        writer.flush();
                        continue;
                    }
                    // Display Pet Image Icon
                    else if ("pet-icon".equalsIgnoreCase(action)) {
                        String petId = reader.readLine();

                        System.out.println("Fetching image for pet ID: " + petId);

                        String imageBase64 = PetsXML_Controller.getPetImageBase64(petId);

                        if (imageBase64 != null) {
                            writer.println(imageBase64);
                        } else {
                            writer.println("not-found");
                        }

                        writer.flush();
                        continue;
                    }

                    // Update Pet Action
                    else if ("update-pet".equalsIgnoreCase(action)) {
                        String id = reader.readLine();
                        String breed = reader.readLine();
                        String type = reader.readLine();
                        String age = reader.readLine();
                        String healthStatus = reader.readLine();
                        String adoptionStatus = reader.readLine();
                        String imageBase64 = reader.readLine();

                        Pet_Model updatedPet = new Pet_Model(id, breed, type, age, healthStatus, adoptionStatus, imageBase64);
                        boolean updateSuccess = PetsXML_Controller.updatePet(updatedPet);
                        if (updateSuccess) {
                            writer.println("Pet updated successfully!");
                        } else {
                            writer.println("Failed to update pet.");
                        }
                        writer.flush();
                        continue;
                    }

                    else if ("upload-profile-image".equalsIgnoreCase(action)) {
                        String username = reader.readLine();
                        String imageBase64 = reader.readLine();
                        System.out.println("Received upload-profile-image for user: " + username);

                        boolean saved = Image_Controller.saveImageToJSON(username, imageBase64);
                        if (saved) {
                            writer.println("Image uploaded successfully");
                        } else {
                            writer.println("Image upload failed");
                        }
                        writer.flush();
                    }


                    // Add Pet Action
                    else if ("add-pet".equalsIgnoreCase(action)) {
                        String id = reader.readLine();
                        String breed = reader.readLine();
                        String type = reader.readLine();
                        String age = reader.readLine();
                        String healthStatus = reader.readLine();
                        String adoptionStatus = reader.readLine();
                        String imageBase64 = reader.readLine();

                        Pet_Model addPet = new Pet_Model(id, breed, type, age, healthStatus, adoptionStatus, imageBase64);
                        PetsXML_Controller.savePet(addPet);

                        writer.println("Pet saved successfully!");
                        writer.flush();
                        continue;
                    }

                    // Delete Pet Action
                    else if ("delete-pet".equalsIgnoreCase(action)) {
                        String petID = reader.readLine();
                        boolean success = PetsXML_Controller.deletePet(petID);

                        if (success) {
                            writer.println("Pet deleted successfully");
                        } else {
                            writer.println("Failed to delete pet");
                        }
                        writer.flush();

                        continue;
                    }// Submit Adoption Form Action
                    else if ("submitAdoptionForm".equalsIgnoreCase(action)) {
                        String username = reader.readLine();

                        StringBuilder xmlBuilder = new StringBuilder();
                        String line;
                        // Read until we see "EOF"
                        while ((line = reader.readLine()) != null && !line.equals("EOF")) {
                            xmlBuilder.append(line).append("\n");
                        }
                        String xmlData = xmlBuilder.toString();

                        // Now store the XML data using your AdoptionXML_Controller
                        boolean stored = AdoptionXML_Controller.storeAdoptionForm(username, xmlData);
                        if (stored) {
                            writer.println("SUCCESS");
                            System.out.println(username + " submitted an Adoption form");
                        } else {
                            writer.println("FAILURE");
                        }
                        writer.flush();

                        continue;
                    }

                    // Unknown Action – close connection.
                    else {
                        System.out.println("Unknown action: " + action);
                        writer.println("Unknown action");
                        writer.flush();

                        continue;
                    }

                    // Optionally, if you expect further communication you might start a dedicated thread here:
                    // new Server_Controller(clientSocket).start();

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }
        }
    }
}