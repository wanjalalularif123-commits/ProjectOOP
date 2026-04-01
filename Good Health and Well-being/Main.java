import data.Appointment;
import data.MedicalRecord;
import data.Prescription;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;
import model.Doctor;
import model.Patient;
import system.HealthcareSystem;

/**
 * Entry point for the Healthcare Management System.
 * Provides a full console-based menu interface demonstrating all system features:
 *   - Patient & Doctor management
 *   - Appointment booking and status transitions
 *   - Prescription issuing and refills
 *   - Health monitoring and alerts
 *   - Report generation
 *
 * Data is loaded on startup and saved automatically on exit.
 */
public class Main {

    private static final HealthcareSystem system  = new HealthcareSystem();
    private static final Scanner          scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ─── Entry Point ──────────────────────────────────────────────────────────

    public static void main(String[] args) {
        printBanner();
        system.loadData();
        System.out.println();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            System.out.println();

            switch (choice) {
                case 1  -> managePatients();
                case 2  -> manageDoctors();
                case 3  -> manageAppointments();
                case 4  -> managePrescriptions();
                case 5  -> healthMonitoring();
                case 6  -> viewReports();
                case 0  -> running = false;
                default -> System.out.println("[!] Invalid choice. Please try again.\n");
            }
        }

        system.saveData();
        System.out.println("\nThank you for using the Healthcare Management System. Goodbye!");
        scanner.close();
    }

    // ─── Banner / Main Menu ───────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      HEALTHCARE MANAGEMENT SYSTEM                ║");
        System.out.println("║      BIT1123 OOP Group Project                   ║");
        System.out.println("║      SDG 3 : Good Health and Well-being          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }

    private static void printMainMenu() {
        System.out.println("═══════════════ MAIN MENU ═══════════════");
        System.out.println("  1. Patient Management");
        System.out.println("  2. Doctor Management");
        System.out.println("  3. Appointment Management");
        System.out.println("  4. Prescription Management");
        System.out.println("  5. Health Monitoring");
        System.out.println("  6. Reports");
        System.out.println("  0. Exit & Save");
        System.out.println("═════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PATIENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void managePatients() {
        System.out.println("── Patient Management ──");
        System.out.println("  1. Register New Patient");
        System.out.println("  2. View All Patients");
        System.out.println("  3. Search Patient by Name");
        System.out.println("  4. View Patient Details");
        System.out.println("  5. Update Patient Medical Record");
        System.out.println("  6. Deactivate Patient");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> registerPatient();
            case 2  -> listAllPatients();
            case 3  -> searchPatient();
            case 4  -> viewPatientDetails();
            case 5  -> updateMedicalRecord();
            case 6  -> deactivatePatient();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void registerPatient() {
        System.out.println("\n─── Register New Patient ───");
        String id = system.generatePatientId();
        System.out.println("Auto-generated ID : " + id);

        System.out.print("Full Name              : "); String name    = scanner.nextLine().trim();
        int age = readInt(              "Age                    : ");
        System.out.print("Gender (Male/Female/Other): "); String gender  = scanner.nextLine().trim();
        System.out.print("IC / Passport No.      : "); String ic      = scanner.nextLine().trim();
        System.out.print("Phone Number           : "); String phone   = scanner.nextLine().trim();
        System.out.print("Email                  : "); String email   = scanner.nextLine().trim();
        System.out.print("Address                : "); String address = scanner.nextLine().trim();
        System.out.print("Blood Type (A+/B-/O+...): "); String blood  = scanner.nextLine().trim();
        System.out.print("Known Allergies (or 'None'): "); String allergies = scanner.nextLine().trim();
        System.out.print("Emergency Contact Name  : "); String ecName  = scanner.nextLine().trim();
        System.out.print("Emergency Contact Phone : "); String ecPhone = scanner.nextLine().trim();

        Patient patient = new Patient(id, name, age, gender, ic, phone, email, address,
                                      blood, allergies, ecName, ecPhone);
        if (system.addPatient(patient)) {
            System.out.println("\n[✓] Patient registered successfully! ID: " + id + "\n");
        } else {
            System.out.println("[!] Failed to register patient (ID conflict).\n");
        }
    }

    private static void listAllPatients() {
        System.out.println("\n─── All Registered Patients ───");
        ArrayList<Patient> list = system.getAllPatients();
        if (list.isEmpty()) {
            System.out.println("No patients registered yet.\n");
            return;
        }
        for (Patient p : list) {
            System.out.println("  " + p + (p.isActive() ? "" : " [INACTIVE]"));
        }
        System.out.println("Total: " + list.size() + "\n");
    }

    private static void searchPatient() {
        System.out.print("\nEnter name keyword to search: ");
        String keyword = scanner.nextLine().trim();
        ArrayList<Patient> results = system.searchPatients(keyword);
        if (results.isEmpty()) {
            System.out.println("No patients found matching '" + keyword + "'.\n");
        } else {
            System.out.println("Search results (" + results.size() + " found):");
            for (Patient p : results) System.out.println("  " + p);
            System.out.println();
        }
    }

    private static void viewPatientDetails() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        Patient p = system.getPatient(id);
        if (p == null) { System.out.println("[!] Patient not found.\n"); return; }
        System.out.println("\n" + p.getDetails());
        System.out.println("\n" + p.getMedicalRecord().getSummary() + "\n");
    }

    private static void updateMedicalRecord() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        Patient p = system.getPatient(id);
        if (p == null) { System.out.println("[!] Patient not found.\n"); return; }

        MedicalRecord rec = p.getMedicalRecord();
        System.out.println("Updating medical record for: " + p.getFullName());
        System.out.println("(Press Enter to keep current value)\n");

        System.out.print("Diagnosis [" + rec.getCurrentDiagnosis() + "]: ");
        String diag = scanner.nextLine().trim();

        System.out.print("Chronic Conditions [" + rec.getChronicConditions() + "]: ");
        String chronic = scanner.nextLine().trim();

        System.out.print("Height in cm [" + rec.getHeight() + "]: ");
        String heightStr = scanner.nextLine().trim();

        System.out.print("Weight in kg [" + rec.getWeight() + "]: ");
        String weightStr = scanner.nextLine().trim();

        System.out.print("Blood Pressure [" + rec.getBloodPressure() + "]: ");
        String bp = scanner.nextLine().trim();

        System.out.print("Heart Rate bpm [" + rec.getHeartRate() + "]: ");
        String hrStr = scanner.nextLine().trim();

        System.out.print("Add Visit Note (optional): ");
        String note = scanner.nextLine().trim();

        if (!diag.isEmpty())    rec.setCurrentDiagnosis(diag);
        if (!chronic.isEmpty()) rec.setChronicConditions(chronic);
        if (!heightStr.isEmpty()) {
            try { rec.setHeight(Double.parseDouble(heightStr)); }
            catch (NumberFormatException ignored) {}
        }
        if (!weightStr.isEmpty()) {
            try { rec.setWeight(Double.parseDouble(weightStr)); }
            catch (NumberFormatException ignored) {}
        }
        if (!bp.isEmpty())   rec.setBloodPressure(bp);
        if (!hrStr.isEmpty()) {
            try { rec.setHeartRate(Integer.parseInt(hrStr)); }
            catch (NumberFormatException ignored) {}
        }
        if (!note.isEmpty()) rec.addVisitNote(note);

        System.out.println("[✓] Medical record updated.\n");
    }

    private static void deactivatePatient() {
        System.out.print("\nEnter Patient ID to deactivate: ");
        String id = scanner.nextLine().trim();
        if (system.removePatient(id)) System.out.println("[✓] Patient deactivated.\n");
        else System.out.println("[!] Patient not found.\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DOCTOR MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void manageDoctors() {
        System.out.println("── Doctor Management ──");
        System.out.println("  1. Register New Doctor");
        System.out.println("  2. View All Doctors");
        System.out.println("  3. View Available Doctors");
        System.out.println("  4. View Doctor Details");
        System.out.println("  5. Toggle Doctor Availability");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> registerDoctor();
            case 2  -> listAllDoctors();
            case 3  -> listAvailableDoctors();
            case 4  -> viewDoctorDetails();
            case 5  -> toggleDoctorAvailability();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void registerDoctor() {
        System.out.println("\n─── Register New Doctor ───");
        String id = system.generateDoctorId();
        System.out.println("Auto-generated ID : " + id);

        System.out.print("Full Name           : "); String name    = scanner.nextLine().trim();
        int age = readInt(            "Age                 : ");
        System.out.print("Gender              : "); String gender  = scanner.nextLine().trim();
        System.out.print("IC / Passport No.   : "); String ic      = scanner.nextLine().trim();
        System.out.print("Phone Number        : "); String phone   = scanner.nextLine().trim();
        System.out.print("Email               : "); String email   = scanner.nextLine().trim();
        System.out.print("Address             : "); String address = scanner.nextLine().trim();
        System.out.print("License Number      : "); String license = scanner.nextLine().trim();
        System.out.print("Specialization      : "); String spec    = scanner.nextLine().trim();
        System.out.print("Department          : "); String dept    = scanner.nextLine().trim();
        int exp   = readInt(          "Years of Experience : ");
        double fee = readDouble(      "Consultation Fee RM : ");

        Doctor doctor = new Doctor(id, name, age, gender, ic, phone, email, address,
                                   license, spec, dept, exp, fee);
        if (system.addDoctor(doctor)) {
            System.out.println("\n[✓] Doctor registered successfully! ID: " + id + "\n");
        } else {
            System.out.println("[!] Failed to register doctor (ID conflict).\n");
        }
    }

    private static void listAllDoctors() {
        System.out.println("\n─── All Registered Doctors ───");
        ArrayList<Doctor> list = system.getAllDoctors();
        if (list.isEmpty()) { System.out.println("No doctors registered.\n"); return; }
        for (Doctor d : list) {
            System.out.println("  " + d + (d.isAvailable() ? " [AVAILABLE]" : " [UNAVAILABLE]"));
        }
        System.out.println("Total: " + list.size() + "\n");
    }

    private static void listAvailableDoctors() {
        System.out.println("\n─── Available Doctors ───");
        ArrayList<Doctor> list = system.getAvailableDoctors();
        if (list.isEmpty()) { System.out.println("No available doctors at this time.\n"); return; }
        for (Doctor d : list) System.out.println("  " + d);
        System.out.println();
    }

    private static void viewDoctorDetails() {
        System.out.print("\nEnter Doctor ID: ");
        String id = scanner.nextLine().trim();
        Doctor d = system.getDoctor(id);
        if (d == null) { System.out.println("[!] Doctor not found.\n"); return; }
        System.out.println("\n" + d.getDetails() + "\n");
    }

    private static void toggleDoctorAvailability() {
        System.out.print("\nEnter Doctor ID: ");
        String id = scanner.nextLine().trim();
        Doctor d = system.getDoctor(id);
        if (d == null) { System.out.println("[!] Doctor not found.\n"); return; }
        d.toggleAvailability();
        System.out.println("[✓] " + d.getFullName() + " is now " +
                           (d.isAvailable() ? "AVAILABLE" : "UNAVAILABLE") + ".\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APPOINTMENT MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void manageAppointments() {
        System.out.println("── Appointment Management ──");
        System.out.println("  1. Book New Appointment");
        System.out.println("  2. View All Appointments");
        System.out.println("  3. View Patient Appointments");
        System.out.println("  4. View Today's Appointments");
        System.out.println("  5. Confirm Appointment");
        System.out.println("  6. Complete Appointment");
        System.out.println("  7. Cancel Appointment");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> bookAppointment();
            case 2  -> listAllAppointments();
            case 3  -> listPatientAppointments();
            case 4  -> listTodayAppointments();
            case 5  -> confirmAppointment();
            case 6  -> completeAppointment();
            case 7  -> cancelAppointment();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void bookAppointment() {
        System.out.println("\n─── Book New Appointment ───");

        System.out.print("Patient ID  : "); String pid = scanner.nextLine().trim();
        Patient patient = system.getPatient(pid);
        if (patient == null) { System.out.println("[!] Patient not found.\n"); return; }

        System.out.print("Doctor ID   : "); String did = scanner.nextLine().trim();
        Doctor doctor = system.getDoctor(did);
        if (doctor == null)        { System.out.println("[!] Doctor not found.\n"); return; }
        if (!doctor.isAvailable()) { System.out.println("[!] Doctor is currently unavailable.\n"); return; }

        LocalDate date = readDate("Date (dd-MM-yyyy): ");
        if (date == null) return;
        LocalTime time = readTime("Time (HH:mm)     : ");
        if (time == null) return;

        System.out.println("Appointment Types:");
        System.out.println("  1. " + Appointment.TYPE_GENERAL);
        System.out.println("  2. " + Appointment.TYPE_FOLLOWUP);
        System.out.println("  3. " + Appointment.TYPE_EMERGENCY);
        System.out.println("  4. " + Appointment.TYPE_SPECIALIST);
        int typeChoice = readInt("Select type     : ");
        String type = switch (typeChoice) {
            case 1  -> Appointment.TYPE_GENERAL;
            case 2  -> Appointment.TYPE_FOLLOWUP;
            case 3  -> Appointment.TYPE_EMERGENCY;
            case 4  -> Appointment.TYPE_SPECIALIST;
            default -> Appointment.TYPE_GENERAL;
        };

        System.out.print("Reason for visit: "); String reason = scanner.nextLine().trim();

        String aptId = system.generateAppointmentId();
        Appointment appointment = new Appointment(aptId, patient, doctor,
                                                   date, time, type, reason);

        if (system.bookAppointment(appointment)) {
            System.out.println("[✓] Appointment booked! ID: " + aptId + "\n");
            System.out.println(appointment.getDetails() + "\n");
        } else {
            System.out.println("[!] Booking failed — scheduling conflict detected " +
                               "(same doctor, date, and time).\n");
        }
    }

    private static void listAllAppointments() {
        System.out.println("\n─── All Appointments ───");
        ArrayList<Appointment> list = system.getAllAppointments();
        if (list.isEmpty()) { System.out.println("No appointments found.\n"); return; }
        for (Appointment a : list) System.out.println("  " + a);
        System.out.println("Total: " + list.size() + "\n");
    }

    private static void listPatientAppointments() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        ArrayList<Appointment> list = system.getPatientAppointments(id);
        if (list.isEmpty()) { System.out.println("No appointments for this patient.\n"); return; }
        System.out.println("Appointments for patient " + id + ":");
        for (Appointment a : list) System.out.println("  " + a);
        System.out.println();
    }

    private static void listTodayAppointments() {
        System.out.println("\n─── Today's Appointments ───");
        ArrayList<Appointment> list = system.getTodayAppointments();
        if (list.isEmpty()) { System.out.println("No active appointments scheduled for today.\n"); return; }
        for (Appointment a : list) System.out.println("  " + a);
        System.out.println();
    }

    private static void confirmAppointment() {
        System.out.print("\nEnter Appointment ID to confirm: ");
        String id = scanner.nextLine().trim();
        Appointment a = system.getAppointment(id);
        if (a == null) { System.out.println("[!] Appointment not found.\n"); return; }
        if (a.confirm()) System.out.println("[✓] Appointment confirmed: " + id + "\n");
        else System.out.println("[!] Cannot confirm — current status is: " + a.getStatus() + "\n");
    }

    private static void completeAppointment() {
        System.out.print("\nEnter Appointment ID to complete: ");
        String id = scanner.nextLine().trim();
        Appointment a = system.getAppointment(id);
        if (a == null) { System.out.println("[!] Appointment not found.\n"); return; }
        System.out.print("Doctor's notes (optional): ");
        String notes = scanner.nextLine().trim();
        if (!notes.isEmpty()) a.setNotes(notes);
        if (a.complete()) System.out.println("[✓] Appointment marked as completed.\n");
        else System.out.println("[!] Cannot complete — current status is: " + a.getStatus() +
                                ". Appointment must be Confirmed first.\n");
    }

    private static void cancelAppointment() {
        System.out.print("\nEnter Appointment ID to cancel: ");
        String id = scanner.nextLine().trim();
        if (system.cancelAppointment(id)) System.out.println("[✓] Appointment cancelled.\n");
        else System.out.println("[!] Cannot cancel — not found or already completed/cancelled.\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PRESCRIPTION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static void managePrescriptions() {
        System.out.println("── Prescription Management ──");
        System.out.println("  1. Issue New Prescription");
        System.out.println("  2. View All Prescriptions");
        System.out.println("  3. View Patient Prescriptions");
        System.out.println("  4. View Prescription Details");
        System.out.println("  5. Process Refill");
        System.out.println("  6. Cancel Prescription");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> issuePrescription();
            case 2  -> listAllPrescriptions();
            case 3  -> listPatientPrescriptions();
            case 4  -> viewPrescriptionDetails();
            case 5  -> processRefill();
            case 6  -> cancelPrescription();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void issuePrescription() {
        System.out.println("\n─── Issue New Prescription ───");

        System.out.print("Patient ID        : "); String pid = scanner.nextLine().trim();
        Patient patient = system.getPatient(pid);
        if (patient == null) { System.out.println("[!] Patient not found.\n"); return; }

        System.out.print("Doctor ID         : "); String did = scanner.nextLine().trim();
        Doctor doctor = system.getDoctor(did);
        if (doctor == null) { System.out.println("[!] Doctor not found.\n"); return; }

        System.out.print("Diagnosis         : "); String diag = scanner.nextLine().trim();
        int validDays = readInt("Valid for (days)  : ");
        int refills   = readInt("Refills allowed   : ");

        String rxId = system.generatePrescriptionId();
        Prescription rx = new Prescription(rxId, patient, doctor, diag, validDays, refills);

        // Add medications interactively
        System.out.print("Add a medication? (y/n): ");
        String yn = scanner.nextLine().trim();
        while (yn.equalsIgnoreCase("y")) {
            System.out.print("  Medication Name   : "); String mName = scanner.nextLine().trim();
            System.out.print("  Dosage            : "); String dosage = scanner.nextLine().trim();
            System.out.print("  Frequency         : "); String freq = scanner.nextLine().trim();
            int dur = readInt("  Duration (days)   : ");
            System.out.print("  Instructions      : "); String instr = scanner.nextLine().trim();
            rx.addMedication(mName, dosage, freq, dur, instr);
            System.out.print("Add another medication? (y/n): ");
            yn = scanner.nextLine().trim();
        }

        system.issuePrescription(rx);
        System.out.println("[✓] Prescription issued! ID: " + rxId + "\n");
        System.out.println(rx.getDetails() + "\n");
    }

    private static void listAllPrescriptions() {
        System.out.println("\n─── All Prescriptions ───");
        ArrayList<Prescription> list = system.getAllPrescriptions();
        if (list.isEmpty()) { System.out.println("No prescriptions found.\n"); return; }
        for (Prescription rx : list) System.out.println("  " + rx);
        System.out.println("Total: " + list.size() + "\n");
    }

    private static void listPatientPrescriptions() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        ArrayList<Prescription> list = system.getPatientPrescriptions(id);
        if (list.isEmpty()) { System.out.println("No prescriptions for this patient.\n"); return; }
        System.out.println("Prescriptions for patient " + id + ":");
        for (Prescription rx : list) System.out.println("  " + rx);
        System.out.println();
    }

    private static void viewPrescriptionDetails() {
        System.out.print("\nEnter Prescription ID: ");
        String id = scanner.nextLine().trim();
        for (Prescription rx : system.getAllPrescriptions()) {
            if (rx.getPrescriptionId().equals(id)) {
                System.out.println("\n" + rx.getDetails() + "\n");
                return;
            }
        }
        System.out.println("[!] Prescription not found.\n");
    }

    private static void processRefill() {
        System.out.print("\nEnter Prescription ID: ");
        String id = scanner.nextLine().trim();
        for (Prescription rx : system.getAllPrescriptions()) {
            if (rx.getPrescriptionId().equals(id)) {
                if (rx.processRefill()) {
                    System.out.println("[✓] Refill processed. Refills remaining: " +
                                       rx.getRefillsRemaining() + "\n");
                } else {
                    System.out.println("[!] Cannot refill — prescription may be expired, " +
                                       "inactive, or all refills used.\n");
                }
                return;
            }
        }
        System.out.println("[!] Prescription not found.\n");
    }

    private static void cancelPrescription() {
        System.out.print("\nEnter Prescription ID: ");
        String id = scanner.nextLine().trim();
        for (Prescription rx : system.getAllPrescriptions()) {
            if (rx.getPrescriptionId().equals(id)) {
                if (rx.cancel()) System.out.println("[✓] Prescription cancelled.\n");
                else System.out.println("[!] Cannot cancel — prescription is already " +
                                        rx.getStatus() + ".\n");
                return;
            }
        }
        System.out.println("[!] Prescription not found.\n");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEALTH MONITORING
    // ═══════════════════════════════════════════════════════════════════════════

    private static void healthMonitoring() {
        System.out.println("── Health Monitoring ──");
        System.out.println("  1. Monitor Patient Vitals");
        System.out.println("  2. Check All Patient Alerts");
        System.out.println("  3. View Expiring Prescriptions (next 7 days)");
        System.out.println("  4. View Active Prescriptions for Patient");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> monitorVitals();
            case 2  -> checkAllAlerts();
            case 3  -> viewExpiringPrescriptions();
            case 4  -> viewActivePrescriptions();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void monitorVitals() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        System.out.println("\n" + system.monitorVitals(id) + "\n");
    }

    private static void checkAllAlerts() {
        System.out.println("\n─── System Health Alerts ───");
        ArrayList<String> alerts = system.checkAllPatientAlerts();
        if (alerts.isEmpty()) {
            System.out.println("No alerts. All monitored patients are within normal ranges.\n");
        } else {
            System.out.println(alerts.size() + " alert(s) detected:");
            for (String alert : alerts) System.out.println("  " + alert);
            System.out.println();
        }
    }

    private static void viewExpiringPrescriptions() {
        System.out.println("\n─── Prescriptions Expiring Within 7 Days ───");
        ArrayList<Prescription> list = system.getExpiringPrescriptions();
        if (list.isEmpty()) { System.out.println("No prescriptions expiring soon.\n"); return; }
        for (Prescription rx : list) {
            System.out.println("  " + rx +
                               " | Days left: " + rx.getDaysUntilExpiry());
        }
        System.out.println();
    }

    private static void viewActivePrescriptions() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        ArrayList<Prescription> list = system.getActivePrescriptions(id);
        if (list.isEmpty()) {
            System.out.println("No active prescriptions for this patient.\n");
            return;
        }
        System.out.println("Active prescriptions for patient " + id + ":");
        for (Prescription rx : list) System.out.println("  " + rx);
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REPORTS
    // ═══════════════════════════════════════════════════════════════════════════

    private static void viewReports() {
        System.out.println("── Reports ──");
        System.out.println("  1. System Overview Report");
        System.out.println("  2. Patient Health Report");
        System.out.println("  3. Doctor Schedule for a Date");
        System.out.println("  0. Back");
        int choice = readInt("Choice: ");
        System.out.println();

        switch (choice) {
            case 1  -> System.out.println("\n" + system.generateSystemReport() + "\n");
            case 2  -> generatePatientReport();
            case 3  -> viewDoctorSchedule();
            case 0  -> {}
            default -> System.out.println("[!] Invalid choice.\n");
        }
    }

    private static void generatePatientReport() {
        System.out.print("\nEnter Patient ID: ");
        String id = scanner.nextLine().trim();
        System.out.println("\n" + system.generateHealthReport(id) + "\n");
    }

    private static void viewDoctorSchedule() {
        System.out.print("\nEnter Doctor ID       : "); String did = scanner.nextLine().trim();
        LocalDate date = readDate("Enter Date (dd-MM-yyyy): ");
        if (date == null) return;
        String dateStr = date.format(DATE_FMT);

        ArrayList<Appointment> schedule = system.getDoctorSchedule(did, dateStr);
        if (schedule.isEmpty()) {
            System.out.println("No appointments found for Doctor " + did + " on " + dateStr + ".\n");
            return;
        }
        System.out.println("\nSchedule for Doctor " + did + " on " + dateStr + ":");
        for (Appointment a : schedule) System.out.println("  " + a);
        System.out.println();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INPUT HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    /** Reads an integer from the console; repeats if input is invalid. */
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a whole number.");
            }
        }
    }

    /** Reads a double from the console; repeats if input is invalid. */
    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("[!] Please enter a valid number (e.g. 50.5).");
            }
        }
    }

    /**
     * Reads and parses a date in "dd-MM-yyyy" format.
     * Returns null and prints an error if the format is wrong.
     */
    private static LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        try {
            return LocalDate.parse(line, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("[!] Invalid date format. Please use dd-MM-yyyy (e.g. 25-04-2026).\n");
            return null;
        }
    }

    /**
     * Reads and parses a time in "HH:mm" format (24-hour).
     * Returns null and prints an error if the format is wrong.
     */
    private static LocalTime readTime(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim();
        try {
            return LocalTime.parse(line, TIME_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("[!] Invalid time format. Please use HH:mm (e.g. 09:30).\n");
            return null;
        }
    }
}