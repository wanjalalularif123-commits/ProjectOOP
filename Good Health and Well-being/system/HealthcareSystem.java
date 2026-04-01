package system;

import data.Appointment;
import data.MedicalRecord;
import data.Prescription;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import model.Doctor;
import model.Patient;

/**
 * The central controller class of the HealthCare Management System.
 * Manages all data, business logic, and file persistence.
 * Demonstrates:
 *   - Abstraction    : implements HealthMonitor interface
 *   - Collections    : HashMap for fast lookup, ArrayList for lists
 *   - File Handling  : saveData() and loadData() using FileWriter/BufferedReader
 *   - Polymorphism   : processes Person references as Patient or Doctor
 *   - Encapsulation  : all collections private, accessed via methods
 */
public class HealthcareSystem implements HealthMonitor {

    // ─── File Path Constants ──────────────────────────────────────────────────
    private static final String FILE_PATIENTS      = "data/patients.txt";
    private static final String FILE_DOCTORS       = "data/doctors.txt";
    private static final String FILE_APPOINTMENTS  = "data/appointments.txt";
    private static final String FILE_PRESCRIPTIONS = "data/prescriptions.txt";
    private static final String FILE_RECORDS       = "data/medical_records.txt";

    // ─── Date formatter ───────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Collections (HashMap for O(1) lookup, ArrayList for ordered lists) ───
    private HashMap<String, Patient>      patients;       // patientId  -> Patient
    private HashMap<String, Doctor>       doctors;        // doctorId   -> Doctor
    private ArrayList<Appointment>        appointments;   // all appointments
    private ArrayList<Prescription>       prescriptions;  // all prescriptions

    // ─── ID Counters (auto-increment) ─────────────────────────────────────────
    private int patientCounter;
    private int doctorCounter;
    private int appointmentCounter;
    private int prescriptionCounter;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Initialises the system with empty collections.
     * Call loadData() after construction to restore saved data.
     */
    public HealthcareSystem() {
        patients             = new HashMap<>();
        doctors              = new HashMap<>();
        appointments         = new ArrayList<>();
        prescriptions        = new ArrayList<>();
        patientCounter       = 1;
        doctorCounter        = 1;
        appointmentCounter   = 1;
        prescriptionCounter  = 1;
    }

    // ─── ID Generation ────────────────────────────────────────────────────────

    /** Generates a unique Patient ID e.g. "P001" */
    public String generatePatientId() {
        return String.format("P%03d", patientCounter++);
    }

    /** Generates a unique Doctor ID e.g. "D001" */
    public String generateDoctorId() {
        return String.format("D%03d", doctorCounter++);
    }

    /** Generates a unique Appointment ID e.g. "APT001" */
    public String generateAppointmentId() {
        return String.format("APT%03d", appointmentCounter++);
    }

    /** Generates a unique Prescription ID e.g. "RX001" */
    public String generatePrescriptionId() {
        return String.format("RX%03d", prescriptionCounter++);
    }

    // ─── Patient Management ───────────────────────────────────────────────────

    /**
     * Registers a new patient into the system.
     *
     * @param patient the Patient object to register
     * @return true if added, false if ID already exists
     */
    public boolean addPatient(Patient patient) {
        if (patients.containsKey(patient.getPersonId())) {
            return false;
        }
        patients.put(patient.getPersonId(), patient);
        return true;
    }

    /**
     * Retrieves a patient by their ID.
     *
     * @param patientId the patient's unique ID
     * @return the Patient object, or null if not found
     */
    public Patient getPatient(String patientId) {
        return patients.get(patientId);
    }

    /**
     * Updates an existing patient's details.
     *
     * @param patient the updated Patient object (must have existing ID)
     * @return true if updated, false if patient not found
     */
    public boolean updatePatient(Patient patient) {
        if (!patients.containsKey(patient.getPersonId())) {
            return false;
        }
        patients.put(patient.getPersonId(), patient);
        return true;
    }

    /**
     * Removes a patient from the system (sets inactive).
     *
     * @param patientId the ID of the patient to deactivate
     * @return true if deactivated, false if not found
     */
    public boolean removePatient(String patientId) {
        Patient patient = patients.get(patientId);
        if (patient == null) return false;
        patient.setActive(false);
        return true;
    }

    /**
     * Returns all patients as an ArrayList.
     * Useful for displaying in GUI tables.
     *
     * @return list of all Patient objects
     */
    public ArrayList<Patient> getAllPatients() {
        return new ArrayList<>(patients.values());
    }

    /**
     * Searches patients by name (case-insensitive partial match).
     *
     * @param name the search keyword
     * @return list of matching Patient objects
     */
    public ArrayList<Patient> searchPatients(String name) {
        ArrayList<Patient> results = new ArrayList<>();
        for (Patient p : patients.values()) {
            if (p.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(p);
            }
        }
        return results;
    }

    // ─── Doctor Management ────────────────────────────────────────────────────

    /**
     * Registers a new doctor into the system.
     *
     * @param doctor the Doctor object to register
     * @return true if added, false if ID already exists
     */
    public boolean addDoctor(Doctor doctor) {
        if (doctors.containsKey(doctor.getPersonId())) {
            return false;
        }
        doctors.put(doctor.getPersonId(), doctor);
        return true;
    }

    /**
     * Retrieves a doctor by their ID.
     *
     * @param doctorId the doctor's unique ID
     * @return the Doctor object, or null if not found
     */
    public Doctor getDoctor(String doctorId) {
        return doctors.get(doctorId);
    }

    /**
     * Returns all doctors as an ArrayList.
     *
     * @return list of all Doctor objects
     */
    public ArrayList<Doctor> getAllDoctors() {
        return new ArrayList<>(doctors.values());
    }

    /**
     * Returns only available doctors (isAvailable == true).
     * Used to populate doctor selection in the booking GUI.
     *
     * @return list of available Doctor objects
     */
    public ArrayList<Doctor> getAvailableDoctors() {
        ArrayList<Doctor> available = new ArrayList<>();
        for (Doctor d : doctors.values()) {
            if (d.isAvailable()) {
                available.add(d);
            }
        }
        return available;
    }

    /**
     * Returns all doctors of a given specialization.
     *
     * @param specialization e.g. "Cardiology"
     * @return list of matching Doctor objects
     */
    public ArrayList<Doctor> getDoctorsBySpecialization(String specialization) {
        ArrayList<Doctor> results = new ArrayList<>();
        for (Doctor d : doctors.values()) {
            if (d.getSpecialization().equalsIgnoreCase(specialization)) {
                results.add(d);
            }
        }
        return results;
    }

    // ─── Appointment Management ───────────────────────────────────────────────

    /**
     * Books a new appointment after checking for scheduling conflicts.
     *
     * @param appointment the Appointment to book
     * @return true if booked, false if a clash was detected
     */
    public boolean bookAppointment(Appointment appointment) {
        // Check for clashes with existing appointments
        for (Appointment existing : appointments) {
            if (existing.isActive() && appointment.clashesWith(existing)) {
                return false; // clash detected
            }
        }
        appointments.add(appointment);

        // Also add to patient and doctor records
        Patient patient = patients.get(appointment.getPatientId());
        Doctor  doctor  = doctors.get(appointment.getDoctorId());
        if (patient != null) patient.addAppointment(appointment);
        if (doctor  != null) doctor.addAppointment(appointment);

        return true;
    }

    /**
     * Cancels an existing appointment by ID.
     *
     * @param appointmentId the ID of the appointment to cancel
     * @return true if cancelled, false if not found or already ended
     */
    public boolean cancelAppointment(String appointmentId) {
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(appointmentId)) {
                return a.cancel();
            }
        }
        return false;
    }

    /**
     * Retrieves an appointment by its ID.
     *
     * @param appointmentId the appointment's unique ID
     * @return the Appointment object, or null if not found
     */
    public Appointment getAppointment(String appointmentId) {
        for (Appointment a : appointments) {
            if (a.getAppointmentId().equals(appointmentId)) {
                return a;
            }
        }
        return null;
    }

    /**
     * Returns all appointments for a specific patient.
     *
     * @param patientId the patient's ID
     * @return list of appointments for that patient
     */
    public ArrayList<Appointment> getPatientAppointments(String patientId) {
        ArrayList<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            if (a.getPatientId().equals(patientId)) {
                result.add(a);
            }
        }
        return result;
    }

    // ─── Prescription Management ──────────────────────────────────────────────

    /**
     * Issues a new prescription and links it to patient and doctor.
     *
     * @param prescription the Prescription to issue
     */
    public void issuePrescription(Prescription prescription) {
        prescriptions.add(prescription);

        Patient patient = patients.get(prescription.getPatientId());
        Doctor  doctor  = doctors.get(prescription.getDoctorId());
        if (patient != null) patient.addPrescription(prescription);
        if (doctor  != null) doctor.issuePrescription(prescription);
    }

    /**
     * Returns all prescriptions for a specific patient.
     *
     * @param patientId the patient's ID
     * @return list of prescriptions for that patient
     */
    public ArrayList<Prescription> getPatientPrescriptions(String patientId) {
        ArrayList<Prescription> result = new ArrayList<>();
        for (Prescription rx : prescriptions) {
            if (rx.getPatientId().equals(patientId)) {
                result.add(rx);
            }
        }
        return result;
    }

    // ─── HealthMonitor Interface Implementations ──────────────────────────────

    /**
     * Monitors and evaluates a patient's vital signs.
     * Implements HealthMonitor.monitorVitals()
     *
     * @param patientId the ID of the patient to monitor
     * @return health status summary string
     */
    @Override
    public String monitorVitals(String patientId) {
        Patient patient = patients.get(patientId);
        if (patient == null) return "Patient not found.";

        MedicalRecord record = patient.getMedicalRecord();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Vital Signs Monitor: ").append(patient.getFullName()).append(" ===\n");
        sb.append("BMI         : ").append(record.calculateBMI())
          .append(" — ").append(getBMIStatus(record.calculateBMI())).append("\n");
        sb.append("Heart Rate  : ").append(record.getHeartRate()).append(" bpm")
          .append(" — ").append(getHeartRateStatus(record.getHeartRate())).append("\n");
        sb.append("Blood Pressure: ").append(record.getBloodPressure()).append("\n");
        sb.append("Abnormal Vitals: ").append(hasAbnormalVitals(record) ? "YES — Alert sent" : "None detected");
        return sb.toString();
    }

    /**
     * Checks if any vital signs are outside the normal range.
     * Implements HealthMonitor.hasAbnormalVitals()
     *
     * @param record the MedicalRecord to evaluate
     * @return true if abnormal vitals detected
     */
    @Override
    public boolean hasAbnormalVitals(MedicalRecord record) {
        double bmi = record.calculateBMI();
        int    hr  = record.getHeartRate();
        boolean abnormalBMI = bmi > 0 && (bmi < BMI_NORMAL_MIN || bmi > BMI_NORMAL_MAX);
        boolean abnormalHR  = hr  > 0 && (hr  < HEART_RATE_MIN || hr  > HEART_RATE_MAX);
        return abnormalBMI || abnormalHR;
    }

    /**
     * Generates a full health report for a patient.
     * Implements HealthMonitor.generateHealthReport()
     *
     * @param patientId the patient's ID
     * @return formatted report string
     */
    @Override
    public String generateHealthReport(String patientId) {
        Patient patient = patients.get(patientId);
        if (patient == null) return "Patient not found.";

        StringBuilder sb = new StringBuilder();
        sb.append(patient.getDetails()).append("\n\n");
        sb.append(patient.getMedicalRecord().getSummary()).append("\n\n");
        sb.append("--- Appointments ---\n");
        for (Appointment a : getPatientAppointments(patientId)) {
            sb.append(a).append("\n");
        }
        sb.append("\n--- Prescriptions ---\n");
        for (Prescription rx : getPatientPrescriptions(patientId)) {
            sb.append(rx).append("\n");
        }
        return sb.toString();
    }

    /**
     * Generates a summary report of the entire system.
     * Implements HealthMonitor.generateSystemReport()
     *
     * @return formatted system summary
     */
    @Override
    public String generateSystemReport() {
        int activePatients     = 0;
        int pendingAppts       = 0;
        int activePrescriptions = 0;

        for (Patient p : patients.values())     if (p.isActive())                               activePatients++;
        for (Appointment a : appointments)       if (a.getStatus().equals(Appointment.STATUS_PENDING))   pendingAppts++;
        for (Prescription rx : prescriptions)   if (rx.getStatus().equals(Prescription.STATUS_ACTIVE))  activePrescriptions++;

        return "=== Healthcare System Report ===" +
               "\nTotal Patients       : " + patients.size() +
               "\nActive Patients      : " + activePatients +
               "\nTotal Doctors        : " + doctors.size() +
               "\nAvailable Doctors    : " + getAvailableDoctors().size() +
               "\nTotal Appointments   : " + appointments.size() +
               "\nPending Appointments : " + pendingAppts +
               "\nTotal Prescriptions  : " + prescriptions.size() +
               "\nActive Prescriptions : " + activePrescriptions +
               "\nExpiring Soon        : " + getExpiringPrescriptions().size() +
               "\nReport Date          : " + LocalDate.now().format(DATE_FORMAT);
    }

    /**
     * Sends a health alert for a patient (prints to console / GUI picks it up).
     * Implements HealthMonitor.sendAlert()
     *
     * @param patientId the patient's ID
     * @param message   the alert message
     */
    @Override
    public void sendAlert(String patientId, String message) {
        Patient patient = patients.get(patientId);
        String name = (patient != null) ? patient.getFullName() : patientId;
        System.out.println("[ALERT] Patient: " + name + " | " + message);
    }

    /**
     * Scans all patients and collects health alerts.
     * Implements HealthMonitor.checkAllPatientAlerts()
     *
     * @return list of alert message strings
     */
    @Override
    public ArrayList<String> checkAllPatientAlerts() {
        ArrayList<String> alerts = new ArrayList<>();
        for (Patient p : patients.values()) {
            if (!p.isActive()) continue;
            MedicalRecord record = p.getMedicalRecord();
            if (hasAbnormalVitals(record)) {
                alerts.add("[VITAL ALERT] " + p.getFullName() +
                           " — BMI: " + record.calculateBMI() +
                           " | HR: " + record.getHeartRate() + " bpm");
            }
        }
        for (Prescription rx : getExpiringPrescriptions()) {
            alerts.add("[RX EXPIRY] " + rx.getPatientName() +
                       " — " + rx.getPrescriptionId() +
                       " expires in " + rx.getDaysUntilExpiry() + " day(s)");
        }
        return alerts;
    }

    /**
     * Returns all appointments scheduled for today.
     * Implements HealthMonitor.getTodayAppointments()
     *
     * @return list of today's appointments
     */
    @Override
    public ArrayList<Appointment> getTodayAppointments() {
        ArrayList<Appointment> today = new ArrayList<>();
        LocalDate now = LocalDate.now();
        for (Appointment a : appointments) {
            if (a.getAppointmentDate().equals(now) && a.isActive()) {
                today.add(a);
            }
        }
        return today;
    }

    /**
     * Returns all appointments for a doctor on a specific date.
     * Implements HealthMonitor.getDoctorSchedule()
     *
     * @param doctorId the doctor's ID
     * @param date     date string in "dd-MM-yyyy" format
     * @return list of matching appointments
     */
    @Override
    public ArrayList<Appointment> getDoctorSchedule(String doctorId, String date) {
        ArrayList<Appointment> schedule = new ArrayList<>();
        LocalDate target = LocalDate.parse(date, DATE_FORMAT);
        for (Appointment a : appointments) {
            if (a.getDoctorId().equals(doctorId) &&
                a.getAppointmentDate().equals(target)) {
                schedule.add(a);
            }
        }
        return schedule;
    }

    /**
     * Returns prescriptions expiring within EXPIRY_WARNING_DAYS days.
     * Implements HealthMonitor.getExpiringPrescriptions()
     *
     * @return list of expiring Prescription objects
     */
    @Override
    public ArrayList<Prescription> getExpiringPrescriptions() {
        ArrayList<Prescription> expiring = new ArrayList<>();
        for (Prescription rx : prescriptions) {
            if (rx.getStatus().equals(Prescription.STATUS_ACTIVE) &&
                rx.getDaysUntilExpiry() <= EXPIRY_WARNING_DAYS) {
                expiring.add(rx);
            }
        }
        return expiring;
    }

    /**
     * Returns all active prescriptions for a specific patient.
     * Implements HealthMonitor.getActivePrescriptions()
     *
     * @param patientId the patient's ID
     * @return list of active prescriptions
     */
    @Override
    public ArrayList<Prescription> getActivePrescriptions(String patientId) {
        ArrayList<Prescription> active = new ArrayList<>();
        for (Prescription rx : prescriptions) {
            if (rx.getPatientId().equals(patientId) &&
                rx.getStatus().equals(Prescription.STATUS_ACTIVE)) {
                active.add(rx);
            }
        }
        return active;
    }

    // ─── File Handling — Save ─────────────────────────────────────────────────

    /**
     * Saves all system data to text files.
     * Called on application exit via MainFrame's windowClosing listener.
     * Uses FileWriter and BufferedWriter for file output.
     */
    public void saveData() {
        savePatients();
        saveDoctors();
        saveAppointments();
        savePrescriptions();
        saveMedicalRecords();
        System.out.println("[SYSTEM] All data saved successfully.");
    }

    private void savePatients() {
        try {
            new File("data").mkdirs(); // create data folder if missing
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATIENTS));
            for (Patient p : patients.values()) {
                writer.write(p.toCSV());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save patients: " + e.getMessage());
        }
    }

    private void saveDoctors() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_DOCTORS));
            for (Doctor d : doctors.values()) {
                writer.write(d.toCSV());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save doctors: " + e.getMessage());
        }
    }

    private void saveAppointments() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_APPOINTMENTS));
            for (Appointment a : appointments) {
                writer.write(a.toCSV());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save appointments: " + e.getMessage());
        }
    }

    private void savePrescriptions() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PRESCRIPTIONS));
            for (Prescription rx : prescriptions) {
                writer.write(rx.toCSV());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save prescriptions: " + e.getMessage());
        }
    }

    private void saveMedicalRecords() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_RECORDS));
            for (Patient p : patients.values()) {
                writer.write(p.getMedicalRecord().toCSV());
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to save medical records: " + e.getMessage());
        }
    }

    // ─── File Handling — Load ─────────────────────────────────────────────────

    /**
     * Loads all system data from text files on startup.
     * Called from MainFrame constructor after system is created.
     * Uses BufferedReader for file input.
     */
    public void loadData() {
        loadPatients();
        loadDoctors();
        loadAppointments();
        loadPrescriptions();
        loadMedicalRecords();
        System.out.println("[SYSTEM] Data loaded successfully.");
    }

    private void loadPatients() {
        File file = new File(FILE_PATIENTS);
        if (!file.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PATIENTS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Patient p = Patient.fromCSV(line);
                    patients.put(p.getPersonId(), p);
                    patientCounter++;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load patients: " + e.getMessage());
        }
    }

    private void loadDoctors() {
        File file = new File(FILE_DOCTORS);
        if (!file.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_DOCTORS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Doctor d = Doctor.fromCSV(line);
                    doctors.put(d.getPersonId(), d);
                    doctorCounter++;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load doctors: " + e.getMessage());
        }
    }

    private void loadAppointments() {
        File file = new File(FILE_APPOINTMENTS);
        if (!file.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_APPOINTMENTS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Appointment a = Appointment.fromCSV(line);
                    appointments.add(a);
                    appointmentCounter++;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load appointments: " + e.getMessage());
        }
    }

    private void loadPrescriptions() {
        File file = new File(FILE_PRESCRIPTIONS);
        if (!file.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_PRESCRIPTIONS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Prescription rx = Prescription.fromCSV(line);
                    prescriptions.add(rx);
                    prescriptionCounter++;
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load prescriptions: " + e.getMessage());
        }
    }

    private void loadMedicalRecords() {
        File file = new File(FILE_RECORDS);
        if (!file.exists()) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FILE_RECORDS));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    MedicalRecord record = MedicalRecord.fromCSV(line);
                    Patient patient = patients.get(record.getPatientId());
                    if (patient != null) {
                        patient.setMedicalRecord(record);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.out.println("[ERROR] Failed to load medical records: " + e.getMessage());
        }
    }

    // ─── Getters for collections ──────────────────────────────────────────────

    public HashMap<String, Patient>  getPatientsMap()   { return patients;      }
    public HashMap<String, Doctor>   getDoctorsMap()    { return doctors;       }
    public ArrayList<Appointment>    getAllAppointments(){ return appointments;  }
    public ArrayList<Prescription>   getAllPrescriptions(){ return prescriptions;}
    public int getTotalPatients()     { return patients.size();      }
    public int getTotalDoctors()      { return doctors.size();       }
    public int getTotalAppointments() { return appointments.size();  }
    public int getTotalPrescriptions(){ return prescriptions.size(); }
}