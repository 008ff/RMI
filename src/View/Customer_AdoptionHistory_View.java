package View;

import Controller.AdoptionXML_Controller;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class Customer_AdoptionHistory_View extends JFrame {
    private String username;
    private DefaultTableModel tableModel;
    private JTable formsTable;
    private JButton backButton, refreshButton;
    private static int id;
    private static String serverAddress;
    private static String healthStatus;
    private static int serverPort;
    private String submitter;
    private static String status;
    public Customer_AdoptionHistory_View(String username, int id, String serverAddress, String type, int age, String healthStatus, Object image, int serverPort) {
        this.username = username;
        this.id = id;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.status=status;
        initComponents();
        loadAdoptionForms();
    }

    private void initComponents() {
        backButton = new JButton("< Back");
        backButton.addActionListener(e -> dispose());

        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAdoptionForms());

        // Create a non-editable table model with columns: "Pet ID", "Status"
        tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Pet ID", "Status"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        formsTable = new JTable(tableModel);
        formsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(formsTable);
        scrollPane.setPreferredSize(new Dimension(630, 380));

        // When a row is double-clicked, open the detail view.
        formsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = formsTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String petId = tableModel.getValueAt(selectedRow, 0).toString();  // ✅ Get Pet ID from column 0
                        String status = tableModel.getValueAt(selectedRow, 1).toString(); // ✅ Get Status from column 1

                        // Pass the correct username and pet ID to the detail view
                        Customer_AdoptionFormDetail_View detailView = new Customer_AdoptionFormDetail_View(username, petId, serverAddress, serverPort);
                        detailView.setVisible(true);
                    }
                }
            }
        });

        // Layout using BorderLayout
        setLayout(new BorderLayout());

        // Top panel with back button
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // Center: the table
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);

        setTitle("My Adoption Forms");
        setPreferredSize(new Dimension(800, 600));
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    // Load all adoption forms for the current customer.
    public void loadAdoptionForms() {
        Vector<Vector<Object>> data = AdoptionXML_Controller.loadAdoptionFormsForUser(username);
        tableModel.setRowCount(0);

        for (Vector<Object> row : data) {
            if (row.size() >= 2) {  //  Ensure it has at least 2 elements (Pet ID & Status)
                String petId = row.get(0).toString();  //  Pet ID is first column
                String status = row.get(1).toString(); //  Status is second column

                tableModel.addRow(new Object[]{petId, status}); //  Add to table correctly
            } else {
                System.err.println("Warning: Incomplete adoption form data - " + row);
            }
        }
    }


}
