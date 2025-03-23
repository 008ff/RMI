package View;
import View.AdoptionForms_View;
import Controller.AdoptionXML_Controller;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class AdoptionForms_View extends JFrame {
    private int id;
    private String serverAddress;
    private int serverPort;
    // For admin identity (if needed)
    private String adminUsername;
    private DefaultTableModel tableModel;
    private JTable formsTable;
    private JButton backButton, refreshButton;

    public AdoptionForms_View(String username, int id, String serverAddress, String type, int age, String healthStatus, Object image, String address, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.id = id;
        initComponents();
        loadAdoptionForms();
    }

    private void initComponents() {
        backButton = new JButton("< Back");
        backButton.addActionListener(e -> {
            // Go back to admin dashboard (for example)
            dispose();
        });
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadAdoptionForms());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(refreshButton);
        bottomPanel.setPreferredSize(new Dimension(100, 50)); // Ensure the bottom panel has a visible height
        add(bottomPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Username", "Pet ID"}){
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
                        String submitter = tableModel.getValueAt(selectedRow, 0).toString();
                        String petId = tableModel.getValueAt(selectedRow, 1).toString();

                        Admin_AdoptionFormDetail_View detailView = new Admin_AdoptionFormDetail_View(submitter, petId, serverAddress, serverPort);
                        detailView.setVisible(true);

                        // Refresh the table after closing the detail view
                        detailView.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosed(WindowEvent e) {
                                loadAdoptionForms(); // Refresh table after accept/decline
                            }
                        });
                    }
                }
            }
        });



        // Layout: back button at top left, table fills rest.
        setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setTitle("Adoption Forms (Admin)");
        pack();  // pack components according to their preferred sizes
        setSize(700, 500);  // then override to your desired fixed size
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

    }

    // Loads the adoption forms from XML.
    // Expected to return a Vector of rows; each row is a Vector with elements: [submitter, petId, status, ...]
    public void loadAdoptionForms() {
        Vector<Vector<Object>> data = AdoptionXML_Controller.loadPendingAdoptionForms();
        tableModel.setRowCount(0);
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
        }
    }

}
