
import View.Login_Interface_View;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.net.Socket;

public class Client {
    private JFrame frame;
    private String serverAddress = "localhost"; // Default server address
    private int serverPort = 6969; // Default server port

    public Client() {
        // Prompt for server details
        getUserServerDetails();

        // Attempt to connect
        connectToServer();
    }

    private void getUserServerDetails() {
        serverAddress = JOptionPane.showInputDialog(frame, "Enter server address:", serverAddress);
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            serverAddress = "localhost"; // Default to localhost
        }

        String portStr = JOptionPane.showInputDialog(frame, "Enter server port:", String.valueOf(serverPort));
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                serverPort = Integer.parseInt(portStr.trim());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Invalid port number. Using default (6969).", "Error", JOptionPane.ERROR_MESSAGE);
                serverPort = 6969;
            }
        }
    }
    private void connectToServer() {
        try (Socket socket = new Socket(serverAddress, serverPort)) {
            JOptionPane.showMessageDialog(frame, "Connected to server at " + serverAddress + " on port " + serverPort, "Success", JOptionPane.INFORMATION_MESSAGE);
            System.out.println("Connected to serverAddress: " + serverAddress +" and Port: " + serverPort);

            runLoginFrame();
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(frame, "Unknown host: " + serverAddress, "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Unknown host: " + serverAddress);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Could not connect to " + serverAddress + " on port " + serverPort + ". Check the server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    private void runLoginFrame() {
        SwingUtilities.invokeLater(() -> {
            Login_Interface_View loginInterfaceView = new Login_Interface_View(serverAddress, serverPort);
            loginInterfaceView.setVisible(true);
            loginInterfaceView.setLocationRelativeTo(null);
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
