// File: gui/MainFrame.java
package gui;

import data.Appointment;
import data.MedicalRecord;
import data.Prescription;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.Doctor;
import model.Patient;
import system.HealthcareSystem;

/**
 * Main GUI window for the Healthcare Management System.
 */
public class MainFrame extends JFrame {
    
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT = 
        DateTimeFormatter.ofPattern("HH:mm");
    
    private HealthcareSystem system;
    private JTabbedPane tabbedPane;
    
    // Table models
    private DefaultTableModel patientTableModel;
    private DefaultTableModel doctorTableModel;
    private DefaultTableModel appointmentTableModel;
    private DefaultTableModel prescriptionTableModel;
    
    public MainFrame() {
        system = new HealthcareSystem();
        system.loadData();
        
        setTitle("Healthcare Management System - SDG 3: Good Health and Well-being");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1300, 800);
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                system.saveData();
                dispose();
                System.exit(0);
            }
        });
        
        initComponents();
        refreshAllTables();
    }
    
    private void initComponents() {
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Dashboard", createDashboardPanel());
        tabbedPane.addTab("Patients", createPatientPanel());
        tabbedPane.addTab("Doctors", createDoctorPanel());
        tabbedPane.addTab("Appointments", createAppointmentPanel());
        tabbedPane.addTab("Prescriptions", createPrescriptionPanel());
        tabbedPane.addTab("Health Monitoring", createHealthMonitorPanel());
        tabbedPane.addTab("Reports", createReportsPanel());
        
        add(tabbedPane);
    }
    
    // ==================== DASHBOARD PANEL ====================
    
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Top: Welcome
        JPanel topPanel = new JPanel();
        JLabel welcomeLabel = new JLabel("Welcome to the Healthcare Management System");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(0, 102, 102));
        topPanel.add(welcomeLabel);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(2, 4, 15, 15));
        statsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel[] statValues = new JLabel[8];
        String[] statTitles = {"Total Patients", "Total Doctors", "Total Appointments", "Total Prescriptions",
                                "Active Patients", "Available Doctors", "Pending Appointments", "Active Prescriptions"};
        Color[] colors = {new Color(52, 152, 219), new Color(46, 204, 113), new Color(155, 89, 182), 
                         new Color(241, 76, 139), new Color(52, 152, 219), new Color(46, 204, 113),
                         new Color(230, 126, 34), new Color(241, 76, 139)};
        
        for (int i = 0; i < 8; i++) {
            JPanel card = createStatCard(statTitles[i], "0", colors[i]);
            statsPanel.add(card);
        }
        
        // Bottom: Today's appointments and alerts
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        
        JPanel todayPanel = new JPanel(new BorderLayout());
        todayPanel.setBorder(BorderFactory.createTitledBorder("Today's Appointments"));
        JTextArea todayArea = new JTextArea();
        todayArea.setEditable(false);
        todayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane todayScroll = new JScrollPane(todayArea);
        todayPanel.add(todayScroll, BorderLayout.CENTER);
        
        JPanel alertsPanel = new JPanel(new BorderLayout());
        alertsPanel.setBorder(BorderFactory.createTitledBorder("System Alerts"));
        JTextArea alertsArea = new JTextArea();
        alertsArea.setEditable(false);
        alertsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane alertsScroll = new JScrollPane(alertsArea);
        alertsPanel.add(alertsScroll, BorderLayout.CENTER);
        
        bottomPanel.add(todayPanel);
        bottomPanel.add(alertsPanel);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        // Refresh dashboard data
        refreshDashboard(statsPanel, todayArea, alertsArea);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setName("value_" + title.replace(" ", "_"));
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        
        return card;
    }
    
    private void refreshDashboard(JPanel statsPanel, JTextArea todayArea, JTextArea alertsArea) {
        // Update stats
        Component[] cards = statsPanel.getComponents();
        
        // Update values
        updateStatLabel(cards[0], String.valueOf(system.getTotalPatients()));
        updateStatLabel(cards[1], String.valueOf(system.getTotalDoctors()));
        updateStatLabel(cards[2], String.valueOf(system.getTotalAppointments()));
        updateStatLabel(cards[3], String.valueOf(system.getTotalPrescriptions()));
        
        int activePatients = 0;
        for (Patient p : system.getAllPatients()) if (p.isActive()) activePatients++;
        updateStatLabel(cards[4], String.valueOf(activePatients));
        
        updateStatLabel(cards[5], String.valueOf(system.getAvailableDoctors().size()));
        
        int pendingAppointments = 0;
        for (Appointment a : system.getAllAppointments()) 
            if (a.getStatus().equals(Appointment.STATUS_PENDING)) pendingAppointments++;
        updateStatLabel(cards[6], String.valueOf(pendingAppointments));
        
        int activePrescriptions = 0;
        for (Prescription p : system.getAllPrescriptions())
            if (p.getStatus().equals(Prescription.STATUS_ACTIVE)) activePrescriptions++;
        updateStatLabel(cards[7], String.valueOf(activePrescriptions));
        
        // Update today's appointments
        StringBuilder todaySb = new StringBuilder();
        for (Appointment a : system.getTodayAppointments()) {
            todaySb.append(a.toString()).append("\n");
        }
        if (todaySb.length() == 0) todaySb.append("No appointments scheduled for today.");
        todayArea.setText(todaySb.toString());
        
        // Update alerts
        StringBuilder alertsSb = new StringBuilder();
        for (String alert : system.checkAllPatientAlerts()) {
            alertsSb.append(alert).append("\n");
        }
        if (alertsSb.length() == 0) alertsSb.append("No active alerts.");
        alertsArea.setText(alertsSb.toString());
    }
    
    private void updateStatLabel(Component card, String value) {
        JPanel panel = (JPanel) card;
        Component[] comps = panel.getComponents();
        for (Component comp : comps) {
            if (comp instanceof JLabel && ((JLabel) comp).getFont().getSize() == 28) {
                ((JLabel) comp).setText(value);
                break;
            }
        }
    }
    
    // ==================== PATIENT PANEL ====================
    
    private JPanel createPatientPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Name", "Age", "Gender", "Blood Type", "Status"};
        patientTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable patientTable = new JTable(patientTableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(patientTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Patient");
        JButton viewBtn = new JButton("View Details");
        JButton updateBtn = new JButton("Update Medical Record");
        JButton deactivateBtn = new JButton("Deactivate");
        JButton refreshBtn = new JButton("Refresh");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deactivateBtn);
        buttonPanel.add(refreshBtn);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search by Name:");
        JTextField searchField = new JTextField(20);
        JButton searchBtn = new JButton("Search");
        JButton clearSearchBtn = new JButton("Clear");
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(clearSearchBtn);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        addBtn.addActionListener(e -> showAddPatientDialog());
        viewBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) patientTableModel.getValueAt(row, 0);
                showPatientDetails(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a patient.");
            }
        });
        updateBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) patientTableModel.getValueAt(row, 0);
                showUpdateMedicalRecordDialog(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a patient.");
            }
        });
        deactivateBtn.addActionListener(e -> {
            int row = patientTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) patientTableModel.getValueAt(row, 0);
                int confirm = JOptionPane.showConfirmDialog(panel, "Deactivate this patient?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION && system.removePatient(id)) {
                    refreshPatientsTable();
                    JOptionPane.showMessageDialog(panel, "Patient deactivated.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a patient.");
            }
        });
        refreshBtn.addActionListener(e -> refreshPatientsTable());
        searchBtn.addActionListener(e -> searchPatients(searchField.getText()));
        clearSearchBtn.addActionListener(e -> {
            searchField.setText("");
            refreshPatientsTable();
        });
        
        refreshPatientsTable();
        return panel;
    }
    
    private void refreshPatientsTable() {
        patientTableModel.setRowCount(0);
        for (Patient p : system.getAllPatients()) {
            patientTableModel.addRow(new Object[]{
                p.getPersonId(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getBloodType() != null ? p.getBloodType() : "N/A",
                p.isActive() ? "Active" : "Inactive"
            });
        }
    }
    
    private void searchPatients(String keyword) {
        patientTableModel.setRowCount(0);
        for (Patient p : system.searchPatients(keyword)) {
            patientTableModel.addRow(new Object[]{
                p.getPersonId(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getBloodType() != null ? p.getBloodType() : "N/A",
                p.isActive() ? "Active" : "Inactive"
            });
        }
    }
    
    private void showAddPatientDialog() {
        JDialog dialog = new JDialog(this, "Register New Patient", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        
        JTextField nameField = new JTextField(20);
        JTextField ageField = new JTextField(20);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField icField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField bloodField = new JTextField(20);
        JTextField allergiesField = new JTextField(20);
        JTextField ecNameField = new JTextField(20);
        JTextField ecPhoneField = new JTextField(20);
        
        int row = 0;
        addFormRow(formPanel, gbc, "Full Name:", nameField, row++);
        addFormRow(formPanel, gbc, "Age:", ageField, row++);
        addFormRow(formPanel, gbc, "Gender:", genderCombo, row++);
        addFormRow(formPanel, gbc, "IC/Passport:", icField, row++);
        addFormRow(formPanel, gbc, "Phone:", phoneField, row++);
        addFormRow(formPanel, gbc, "Email:", emailField, row++);
        addFormRow(formPanel, gbc, "Address:", addressField, row++);
        addFormRow(formPanel, gbc, "Blood Type:", bloodField, row++);
        addFormRow(formPanel, gbc, "Allergies:", allergiesField, row++);
        addFormRow(formPanel, gbc, "Emergency Contact:", ecNameField, row++);
        addFormRow(formPanel, gbc, "Emergency Phone:", ecPhoneField, row++);
        
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        saveBtn.addActionListener(e -> {
            try {
                String id = system.generatePatientId();
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = (String) genderCombo.getSelectedItem();
                String ic = icField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String address = addressField.getText().trim();
                String blood = bloodField.getText().trim();
                String allergies = allergiesField.getText().trim();
                String ecName = ecNameField.getText().trim();
                String ecPhone = ecPhoneField.getText().trim();
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter patient name.");
                    return;
                }
                
                Patient patient = new Patient(id, name, age, gender, ic, phone, email, address,
                                               blood, allergies, ecName, ecPhone);
                if (system.addPatient(patient)) {
                    JOptionPane.showMessageDialog(dialog, "Patient registered! ID: " + id);
                    refreshPatientsTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to register patient.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid age.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
    
    private void showPatientDetails(String id) {
        Patient p = system.getPatient(id);
        if (p == null) return;
        
        JDialog dialog = new JDialog(this, "Patient Details", true);
        dialog.setSize(550, 550);
        dialog.setLocationRelativeTo(this);
        
        JTextArea textArea = new JTextArea(p.getDetails() + "\n\n" + p.getMedicalRecord().getSummary());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    private void showUpdateMedicalRecordDialog(String id) {
        Patient p = system.getPatient(id);
        if (p == null) return;
        
        MedicalRecord rec = p.getMedicalRecord();
        
        JDialog dialog = new JDialog(this, "Update Medical Record - " + p.getFullName(), true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField diagnosisField = new JTextField(rec.getCurrentDiagnosis(), 20);
        JTextField chronicField = new JTextField(rec.getChronicConditions(), 20);
        JTextField heightField = new JTextField(rec.getHeight() > 0 ? String.valueOf(rec.getHeight()) : "", 20);
        JTextField weightField = new JTextField(rec.getWeight() > 0 ? String.valueOf(rec.getWeight()) : "", 20);
        JTextField bpField = new JTextField(rec.getBloodPressure(), 20);
        JTextField hrField = new JTextField(rec.getHeartRate() > 0 ? String.valueOf(rec.getHeartRate()) : "", 20);
        JTextArea noteArea = new JTextArea(3, 20);
        
        int row = 0;
        addFormRow(formPanel, gbc, "Diagnosis:", diagnosisField, row++);
        addFormRow(formPanel, gbc, "Chronic Conditions:", chronicField, row++);
        addFormRow(formPanel, gbc, "Height (cm):", heightField, row++);
        addFormRow(formPanel, gbc, "Weight (kg):", weightField, row++);
        addFormRow(formPanel, gbc, "Blood Pressure:", bpField, row++);
        addFormRow(formPanel, gbc, "Heart Rate:", hrField, row++);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Visit Note:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(noteArea), gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        saveBtn.addActionListener(e -> {
            try {
                if (!diagnosisField.getText().trim().isEmpty())
                    rec.setCurrentDiagnosis(diagnosisField.getText().trim());
                if (!chronicField.getText().trim().isEmpty())
                    rec.setChronicConditions(chronicField.getText().trim());
                if (!heightField.getText().trim().isEmpty())
                    rec.setHeight(Double.parseDouble(heightField.getText().trim()));
                if (!weightField.getText().trim().isEmpty())
                    rec.setWeight(Double.parseDouble(weightField.getText().trim()));
                if (!bpField.getText().trim().isEmpty())
                    rec.setBloodPressure(bpField.getText().trim());
                if (!hrField.getText().trim().isEmpty())
                    rec.setHeartRate(Integer.parseInt(hrField.getText().trim()));
                if (!noteArea.getText().trim().isEmpty())
                    rec.addVisitNote(noteArea.getText().trim());
                
                JOptionPane.showMessageDialog(dialog, "Medical record updated.");
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for height, weight, and heart rate.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
    
    // ==================== DOCTOR PANEL ====================
    
    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Name", "Specialization", "Department", "Fee (RM)", "Available"};
        doctorTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable doctorTable = new JTable(doctorTableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(doctorTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Doctor");
        JButton viewBtn = new JButton("View Details");
        JButton toggleBtn = new JButton("Toggle Availability");
        JButton refreshBtn = new JButton("Refresh");
        
        buttonPanel.add(addBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(toggleBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        addBtn.addActionListener(e -> showAddDoctorDialog());
        viewBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) doctorTableModel.getValueAt(row, 0);
                showDoctorDetails(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a doctor.");
            }
        });
        toggleBtn.addActionListener(e -> {
            int row = doctorTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) doctorTableModel.getValueAt(row, 0);
                Doctor d = system.getDoctor(id);
                if (d != null) {
                    d.toggleAvailability();
                    refreshDoctorsTable();
                    JOptionPane.showMessageDialog(panel, "Doctor availability toggled.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a doctor.");
            }
        });
        refreshBtn.addActionListener(e -> refreshDoctorsTable());
        
        refreshDoctorsTable();
        return panel;
    }
    
    private void refreshDoctorsTable() {
        doctorTableModel.setRowCount(0);
        for (Doctor d : system.getAllDoctors()) {
            doctorTableModel.addRow(new Object[]{
                d.getPersonId(),
                "Dr. " + d.getFullName(),
                d.getSpecialization(),
                d.getDepartment(),
                String.format("%.2f", d.getConsultationFee()),
                d.isAvailable() ? "Yes" : "No"
            });
        }
    }
    
    private void showAddDoctorDialog() {
        JDialog dialog = new JDialog(this, "Register New Doctor", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JTextField ageField = new JTextField(20);
        JComboBox<String> genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JTextField icField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JTextField addressField = new JTextField(20);
        JTextField licenseField = new JTextField(20);
        JTextField specField = new JTextField(20);
        JTextField deptField = new JTextField(20);
        JTextField expField = new JTextField(20);
        JTextField feeField = new JTextField(20);
        
        int row = 0;
        addFormRow(formPanel, gbc, "Full Name:", nameField, row++);
        addFormRow(formPanel, gbc, "Age:", ageField, row++);
        addFormRow(formPanel, gbc, "Gender:", genderCombo, row++);
        addFormRow(formPanel, gbc, "IC/Passport:", icField, row++);
        addFormRow(formPanel, gbc, "Phone:", phoneField, row++);
        addFormRow(formPanel, gbc, "Email:", emailField, row++);
        addFormRow(formPanel, gbc, "Address:", addressField, row++);
        addFormRow(formPanel, gbc, "License Number:", licenseField, row++);
        addFormRow(formPanel, gbc, "Specialization:", specField, row++);
        addFormRow(formPanel, gbc, "Department:", deptField, row++);
        addFormRow(formPanel, gbc, "Years of Experience:", expField, row++);
        addFormRow(formPanel, gbc, "Consultation Fee (RM):", feeField, row++);
        
        JPanel buttonPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(saveBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        saveBtn.addActionListener(e -> {
            try {
                String id = system.generateDoctorId();
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String gender = (String) genderCombo.getSelectedItem();
                String ic = icField.getText().trim();
                String phone = phoneField.getText().trim();
                String email = emailField.getText().trim();
                String address = addressField.getText().trim();
                String license = licenseField.getText().trim();
                String spec = specField.getText().trim();
                String dept = deptField.getText().trim();
                int exp = Integer.parseInt(expField.getText().trim());
                double fee = Double.parseDouble(feeField.getText().trim());
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter doctor name.");
                    return;
                }
                
                Doctor doctor = new Doctor(id, name, age, gender, ic, phone, email, address,
                                           license, spec, dept, exp, fee);
                if (system.addDoctor(doctor)) {
                    JOptionPane.showMessageDialog(dialog, "Doctor registered! ID: " + id);
                    refreshDoctorsTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to register doctor.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
    
    private void showDoctorDetails(String id) {
        Doctor d = system.getDoctor(id);
        if (d == null) return;
        
        JDialog dialog = new JDialog(this, "Doctor Details", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        
        JTextArea textArea = new JTextArea(d.getDetails());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // ==================== APPOINTMENT PANEL ====================
    
    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Patient", "Doctor", "Date", "Time", "Type", "Status"};
        appointmentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable appointmentTable = new JTable(appointmentTableModel);
        appointmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton bookBtn = new JButton("Book Appointment");
        JButton viewBtn = new JButton("View Details");
        JButton confirmBtn = new JButton("Confirm");
        JButton completeBtn = new JButton("Complete");
        JButton cancelBtn = new JButton("Cancel");
        JButton refreshBtn = new JButton("Refresh");
        
        buttonPanel.add(bookBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(confirmBtn);
        buttonPanel.add(completeBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        bookBtn.addActionListener(e -> showBookAppointmentDialog());
        viewBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) appointmentTableModel.getValueAt(row, 0);
                showAppointmentDetails(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment.");
            }
        });
        confirmBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) appointmentTableModel.getValueAt(row, 0);
                Appointment a = system.getAppointment(id);
                if (a != null && a.confirm()) {
                    refreshAppointmentsTable();
                    JOptionPane.showMessageDialog(panel, "Appointment confirmed.");
                } else {
                    JOptionPane.showMessageDialog(panel, "Cannot confirm. Status: " + (a != null ? a.getStatus() : "Not found"));
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment.");
            }
        });
        completeBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) appointmentTableModel.getValueAt(row, 0);
                Appointment a = system.getAppointment(id);
                if (a != null && a.complete()) {
                    String notes = JOptionPane.showInputDialog(panel, "Doctor's notes:");
                    if (notes != null && !notes.trim().isEmpty()) a.setNotes(notes);
                    refreshAppointmentsTable();
                    JOptionPane.showMessageDialog(panel, "Appointment completed.");
                } else {
                    JOptionPane.showMessageDialog(panel, "Cannot complete. Appointment must be confirmed first.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment.");
            }
        });
        cancelBtn.addActionListener(e -> {
            int row = appointmentTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) appointmentTableModel.getValueAt(row, 0);
                if (system.cancelAppointment(id)) {
                    refreshAppointmentsTable();
                    JOptionPane.showMessageDialog(panel, "Appointment cancelled.");
                } else {
                    JOptionPane.showMessageDialog(panel, "Cannot cancel appointment.");
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select an appointment.");
            }
        });
        refreshBtn.addActionListener(e -> refreshAppointmentsTable());
        
        refreshAppointmentsTable();
        return panel;
    }
    
    private void refreshAppointmentsTable() {
        appointmentTableModel.setRowCount(0);
        for (Appointment a : system.getAllAppointments()) {
            appointmentTableModel.addRow(new Object[]{
                a.getAppointmentId(),
                a.getPatientName(),
                "Dr. " + a.getDoctorName(),
                a.getAppointmentDate().format(DATE_FORMAT),
                a.getAppointmentTime().format(TIME_FORMAT),
                a.getAppointmentType(),
                a.getStatus()
            });
        }
    }
    
    private void showBookAppointmentDialog() {
        JDialog dialog = new JDialog(this, "Book Appointment", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        ArrayList<Patient> patients = system.getAllPatients();
        ArrayList<Doctor> doctors = system.getAvailableDoctors();
        
        JComboBox<Patient> patientCombo = new JComboBox<>();
        for (Patient p : patients) {
            if (p.isActive()) patientCombo.addItem(p);
        }
        
        JComboBox<Doctor> doctorCombo = new JComboBox<>(doctors.toArray(new Doctor[0]));
        JTextField dateField = new JTextField(LocalDate.now().format(DATE_FORMAT), 20);
        JTextField timeField = new JTextField("09:00", 20);
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
            Appointment.TYPE_GENERAL,
            Appointment.TYPE_FOLLOWUP,
            Appointment.TYPE_EMERGENCY,
            Appointment.TYPE_SPECIALIST
        });
        JTextArea reasonArea = new JTextArea(3, 20);
        
        int row = 0;
        addFormRow(formPanel, gbc, "Patient:", patientCombo, row++);
        addFormRow(formPanel, gbc, "Doctor:", doctorCombo, row++);
        addFormRow(formPanel, gbc, "Date (dd-MM-yyyy):", dateField, row++);
        addFormRow(formPanel, gbc, "Time (HH:mm):", timeField, row++);
        addFormRow(formPanel, gbc, "Type:", typeCombo, row++);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(reasonArea), gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton bookBtn = new JButton("Book");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(bookBtn);
        buttonPanel.add(cancelBtn);
        
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        bookBtn.addActionListener(e -> {
            try {
                Patient p = (Patient) patientCombo.getSelectedItem();
                Doctor d = (Doctor) doctorCombo.getSelectedItem();
                if (p == null || d == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select patient and doctor.");
                    return;
                }
                LocalDate date = LocalDate.parse(dateField.getText().trim(), DATE_FORMAT);
                LocalTime time = LocalTime.parse(timeField.getText().trim(), TIME_FORMAT);
                String type = (String) typeCombo.getSelectedItem();
                String reason = reasonArea.getText().trim();
                
                if (reason.isEmpty()) reason = "General consultation";
                
                String aptId = system.generateAppointmentId();
                Appointment apt = new Appointment(aptId, p, d, date, time, type, reason);
                
                if (system.bookAppointment(apt)) {
                    JOptionPane.showMessageDialog(dialog, "Appointment booked! ID: " + aptId);
                    refreshAppointmentsTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Booking failed - scheduling conflict detected.");
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date/time format. Use dd-MM-yyyy and HH:mm");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage());
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
    
    private void showAppointmentDetails(String id) {
        Appointment a = system.getAppointment(id);
        if (a == null) return;
        
        JDialog dialog = new JDialog(this, "Appointment Details", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        
        JTextArea textArea = new JTextArea(a.getDetails());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
        dialog.setVisible(true);
    }
    
    // ==================== PRESCRIPTION PANEL ====================
    
    private JPanel createPrescriptionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        String[] columns = {"ID", "Patient", "Doctor", "Diagnosis", "Status", "Expires"};
        prescriptionTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable prescriptionTable = new JTable(prescriptionTableModel);
        prescriptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton issueBtn = new JButton("Issue Prescription");
        JButton viewBtn = new JButton("View Details");
        JButton refillBtn = new JButton("Process Refill");
        JButton cancelRxBtn = new JButton("Cancel");
        JButton refreshBtn = new JButton("Refresh");
        
        buttonPanel.add(issueBtn);
        buttonPanel.add(viewBtn);
        buttonPanel.add(refillBtn);
        buttonPanel.add(cancelRxBtn);
        buttonPanel.add(refreshBtn);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        issueBtn.addActionListener(e -> showIssuePrescriptionDialog());
        viewBtn.addActionListener(e -> {
            int row = prescriptionTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) prescriptionTableModel.getValueAt(row, 0);
                showPrescriptionDetails(id);
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a prescription.");
            }
        });
        refillBtn.addActionListener(e -> {
            int row = prescriptionTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) prescriptionTableModel.getValueAt(row, 0);
                for (Prescription rx : system.getAllPrescriptions()) {
                    if (rx.getPrescriptionId().equals(id)) {
                        if (rx.processRefill()) {
                            refreshPrescriptionsTable();
                            JOptionPane.showMessageDialog(panel, "Refill processed. Refills remaining: " + rx.getRefillsRemaining());
                        } else {
                            JOptionPane.showMessageDialog(panel, "Cannot refill - prescription expired or no refills left.");
                        }
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a prescription.");
            }
        });
        cancelRxBtn.addActionListener(e -> {
            int row = prescriptionTable.getSelectedRow();
            if (row >= 0) {
                String id = (String) prescriptionTableModel.getValueAt(row, 0);
                for (Prescription rx : system.getAllPrescriptions()) {
                    if (rx.getPrescriptionId().equals(id)) {
                        if (rx.cancel()) {
                            refreshPrescriptionsTable();
                            JOptionPane.showMessageDialog(panel, "Prescription cancelled.");
                        } else {
                            JOptionPane.showMessageDialog(panel, "Cannot cancel - prescription is already " + rx.getStatus());
                        }
                        break;
                    }
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please select a prescription.");
            }
        });
        refreshBtn.addActionListener(e -> refreshPrescriptionsTable());
        
        refreshPrescriptionsTable();
        return panel;
    }
    
    private void refreshPrescriptionsTable() {
        prescriptionTableModel.setRowCount(0);
        for (Prescription rx : system.getAllPrescriptions()) {
            prescriptionTableModel.addRow(new Object[]{
                rx.getPrescriptionId(),
                rx.getPatientName(),
                "Dr. " + rx.getDoctorName(),
                rx.getDiagnosis(),
                rx.getStatus(),
                rx.getExpiryDate().format(DATE_FORMAT)
            });
        }
    }
    
    private void showIssuePrescriptionDialog() {
        JDialog dialog = new JDialog(this, "Issue Prescription", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 550);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        ArrayList<Patient> patients = system.getAllPatients();
        ArrayList<Doctor> doctors = system.getAllDoctors();
        
        JComboBox<Patient> patientCombo = new JComboBox<>();
        for (Patient p : patients) {
            if (p.isActive()) patientCombo.addItem(p);
        }
        
        JComboBox<Doctor> doctorCombo = new JComboBox<>(doctors.toArray(new Doctor[0]));
        JTextField diagnosisField = new JTextField(20);
        JTextField validDaysField = new JTextField("30", 20);
        JTextField refillsField = new JTextField("2", 20);
        
        DefaultListModel<String> medsModel = new DefaultListModel<>();
        JList<String> medsList = new JList<>(medsModel);
        JScrollPane medsScroll = new JScrollPane(medsList);
        medsScroll.setPreferredSize(new Dimension(300, 80));
        
        JPanel medInputPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField medNameField = new JTextField(15);
        JTextField dosageField = new JTextField(15);
        JTextField freqField = new JTextField(15);
        JTextField durationField = new JTextField("30", 15);
        JTextField instrField = new JTextField(15);
        
        medInputPanel.add(new JLabel("Medication:"));
        medInputPanel.add(medNameField);
        medInputPanel.add(new JLabel("Dosage:"));
        medInputPanel.add(dosageField);
        medInputPanel.add(new JLabel("Frequency:"));
        medInputPanel.add(freqField);
        medInputPanel.add(new JLabel("Duration (days):"));
        medInputPanel.add(durationField);
        medInputPanel.add(new JLabel("Instructions:"));
        medInputPanel.add(instrField);
        
        JButton addMedBtn = new JButton("Add Medication");
        JButton removeMedBtn = new JButton("Remove Selected");
        
        int row = 0;
        addFormRow(formPanel, gbc, "Patient:", patientCombo, row++);
        addFormRow(formPanel, gbc, "Doctor:", doctorCombo, row++);
        addFormRow(formPanel, gbc, "Diagnosis:", diagnosisField, row++);
        addFormRow(formPanel, gbc, "Valid for (days):", validDaysField, row++);
        addFormRow(formPanel, gbc, "Refills Allowed:", refillsField, row++);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Medications:"), gbc);
        gbc.gridx = 1;
        formPanel.add(medsScroll, gbc);
        
        row++;
        JPanel medButtonPanel = new JPanel(new FlowLayout());
        medButtonPanel.add(addMedBtn);
        medButtonPanel.add(removeMedBtn);
        gbc.gridx = 1;
        gbc.gridy = row;
        formPanel.add(medButtonPanel, gbc);
        
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(medInputPanel, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton issueBtn = new JButton("Issue");
        JButton cancelBtn = new JButton("Cancel");
        buttonPanel.add(issueBtn);
        buttonPanel.add(cancelBtn);
        
        addMedBtn.addActionListener(e -> {
            String name = medNameField.getText().trim();
            String dosage = dosageField.getText().trim();
            String freq = freqField.getText().trim();
            String duration = durationField.getText().trim();
            
            if (!name.isEmpty() && !dosage.isEmpty()) {
                medsModel.addElement(name + "|" + dosage + "|" + freq + "|" + duration + "|" + instrField.getText().trim());
                medNameField.setText("");
                dosageField.setText("");
                freqField.setText("");
                durationField.setText("30");
                instrField.setText("");
            }
        });
        
        removeMedBtn.addActionListener(e -> {
            int idx = medsList.getSelectedIndex();
            if (idx >= 0) medsModel.remove(idx);
        });
        
        dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        issueBtn.addActionListener(e -> {
            try {
                Patient p = (Patient) patientCombo.getSelectedItem();
                Doctor d = (Doctor) doctorCombo.getSelectedItem();
                if (p == null || d == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select patient and doctor.");
                    return;
                }
                String diagnosis = diagnosisField.getText().trim();
                int validDays = Integer.parseInt(validDaysField.getText().trim());
                int refills = Integer.parseInt(refillsField.getText().trim());
                
                if (diagnosis.isEmpty()) diagnosis = "General consultation";
                
                String rxId = system.generatePrescriptionId();
                Prescription rx = new Prescription(rxId, p, d, diagnosis, validDays, refills);
                
                for (int i = 0; i < medsModel.size(); i++) {
                    String med = medsModel.get(i);
                    String[] parts = med.split("\\|");
                    if (parts.length >= 2) {
                        int duration = 30;
                        try {
                            if (parts.length > 3 && !parts[3].isEmpty()) duration = Integer.parseInt(parts[3]);
                        } catch (NumberFormatException ex) {}
                        rx.addMedication(parts[0], parts[1], parts.length > 2 ? parts[2] : "As directed",
                                         duration, parts.length > 4 ? parts[4] : "Take as prescribed");
                    }
                }
                
                system.issuePrescription(rx);
                JOptionPane.showMessageDialog(dialog, "Prescription issued! ID: " + rxId);
                refreshPrescriptionsTable();
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers.");
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }
    
    private void showPrescriptionDetails(String id) {
        for (Prescription rx : system.getAllPrescriptions()) {
            if (rx.getPrescriptionId().equals(id)) {
                JDialog dialog = new JDialog(this, "Prescription Details", true);
                dialog.setSize(550, 550);
                dialog.setLocationRelativeTo(this);
                
                JTextArea textArea = new JTextArea(rx.getDetails());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                
                dialog.add(new JScrollPane(textArea), BorderLayout.CENTER);
                dialog.setVisible(true);
                return;
            }
        }
    }
    
    // ==================== HEALTH MONITOR PANEL ====================
    
    private JPanel createHealthMonitorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel patientLabel = new JLabel("Select Patient:");
        JComboBox<Patient> patientCombo = new JComboBox<>();
        JButton monitorBtn = new JButton("Monitor Vitals");
        JButton alertsBtn = new JButton("Check All Alerts");
        JButton expiringBtn = new JButton("Expiring Prescriptions");
        
        topPanel.add(patientLabel);
        topPanel.add(patientCombo);
        topPanel.add(monitorBtn);
        topPanel.add(alertsBtn);
        topPanel.add(expiringBtn);
        
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        refreshPatientCombo(patientCombo);
        
        monitorBtn.addActionListener(e -> {
            Patient p = (Patient) patientCombo.getSelectedItem();
            if (p != null) {
                resultsArea.setText(system.monitorVitals(p.getPersonId()));
            } else {
                resultsArea.setText("Please select a patient.");
            }
        });
        
        alertsBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("=== SYSTEM ALERTS ===\n\n");
            ArrayList<String> alerts = system.checkAllPatientAlerts();
            if (alerts.isEmpty()) {
                sb.append("No alerts detected.");
            } else {
                for (String alert : alerts) {
                    sb.append(alert).append("\n");
                }
            }
            resultsArea.setText(sb.toString());
        });
        
        expiringBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("=== PRESCRIPTIONS EXPIRING SOON ===\n\n");
            ArrayList<Prescription> expiring = system.getExpiringPrescriptions();
            if (expiring.isEmpty()) {
                sb.append("No prescriptions expiring within the next 7 days.");
            } else {
                for (Prescription rx : expiring) {
                    sb.append(rx.toString()).append("\n");
                    sb.append("  Days until expiry: ").append(rx.getDaysUntilExpiry()).append("\n\n");
                }
            }
            resultsArea.setText(sb.toString());
        });
        
        return panel;
    }
    
    private void refreshPatientCombo(JComboBox<Patient> combo) {
        combo.removeAllItems();
        for (Patient p : system.getAllPatients()) {
            if (p.isActive()) {
                combo.addItem(p);
            }
        }
    }
    
    // ==================== REPORTS PANEL ====================
    
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton systemReportBtn = new JButton("System Overview Report");
        JButton patientReportBtn = new JButton("Patient Health Report");
        JButton scheduleReportBtn = new JButton("Doctor Schedule Report");
        
        buttonPanel.add(systemReportBtn);
        buttonPanel.add(patientReportBtn);
        buttonPanel.add(scheduleReportBtn);
        
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        systemReportBtn.addActionListener(e -> {
            reportArea.setText(system.generateSystemReport());
        });
        
        patientReportBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Select Patient", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 150);
            dialog.setLocationRelativeTo(this);
            
            JComboBox<Patient> combo = new JComboBox<>();
            for (Patient p : system.getAllPatients()) {
                combo.addItem(p);
            }
            
            JButton generateBtn = new JButton("Generate Report");
            JPanel panel2 = new JPanel(new BorderLayout());
            panel2.add(combo, BorderLayout.CENTER);
            panel2.add(generateBtn, BorderLayout.SOUTH);
            
            dialog.add(panel2, BorderLayout.CENTER);
            
            generateBtn.addActionListener(e2 -> {
                Patient p = (Patient) combo.getSelectedItem();
                if (p != null) {
                    reportArea.setText(system.generateHealthReport(p.getPersonId()));
                    dialog.dispose();
                }
            });
            
            dialog.setVisible(true);
        });
        
        scheduleReportBtn.addActionListener(e -> {
            JDialog dialog = new JDialog(this, "Doctor Schedule", true);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(400, 200);
            dialog.setLocationRelativeTo(this);
            
            JComboBox<Doctor> doctorCombo = new JComboBox<>();
            JTextField dateField = new JTextField(LocalDate.now().format(DATE_FORMAT), 15);
            
            for (Doctor d : system.getAllDoctors()) {
                doctorCombo.addItem(d);
            }
            
            JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            inputPanel.add(new JLabel("Doctor:"));
            inputPanel.add(doctorCombo);
            inputPanel.add(new JLabel("Date (dd-MM-yyyy):"));
            inputPanel.add(dateField);
            
            JButton generateBtn = new JButton("Generate Schedule");
            
            dialog.add(inputPanel, BorderLayout.CENTER);
            dialog.add(generateBtn, BorderLayout.SOUTH);
            
            generateBtn.addActionListener(e2 -> {
                Doctor d = (Doctor) doctorCombo.getSelectedItem();
                String dateStr = dateField.getText().trim();
                try {
                    LocalDate.parse(dateStr, DATE_FORMAT);
                    ArrayList<Appointment> schedule = system.getDoctorSchedule(d.getPersonId(), dateStr);
                    StringBuilder sb = new StringBuilder();
                    sb.append("Schedule for Dr. ").append(d.getFullName()).append(" on ").append(dateStr).append("\n\n");
                    if (schedule.isEmpty()) {
                        sb.append("No appointments scheduled.");
                    } else {
                        for (Appointment a : schedule) {
                            sb.append(a.toString()).append("\n");
                        }
                    }
                    reportArea.setText(sb.toString());
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Invalid date format. Use dd-MM-yyyy");
                }
            });
            
            dialog.setVisible(true);
        });
        
        return panel;
    }
    
    // ==================== HELPER METHODS ====================
    
    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(field, gbc);
    }
    
    private void addFormRow(JPanel panel, GridBagConstraints gbc, String label, JComboBox<?> combo, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        panel.add(combo, gbc);
    }
    
    private void refreshAllTables() {
        refreshPatientsTable();
        refreshDoctorsTable();
        refreshAppointmentsTable();
        refreshPrescriptionsTable();
    }
}