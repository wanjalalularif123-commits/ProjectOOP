package system;

import data.Appointment;
import data.MedicalRecord;
import data.Prescription;
import java.util.ArrayList;

/**
 * Interface defining the monitoring contract for the HealthCare system.
 * Any class that implements HealthMonitor MUST provide all methods below.
 * Demonstrates:
 *   - Abstraction    : interface with abstract method declarations
 *   - Polymorphism   : different classes can implement this differently
 *
 * Implemented by: HealthcareSystem
 */
public interface HealthMonitor {

    // ─── Vital Signs Monitoring ───────────────────────────────────────────────

    /**
     * Monitors and evaluates a patient's vital signs from their medical record.
     * Should check BMI, heart rate, and blood pressure for abnormalities.
     *
     * @param patientId the ID of the patient to monitor
     * @return a summary string of the patient's current health status
     */
    String monitorVitals(String patientId);

    /**
     * Checks if any vital signs in the given record are outside normal range.
     * Used to trigger alerts in the GUI.
     *
     * @param record the MedicalRecord to evaluate
     * @return true if abnormal vitals are detected
     */
    boolean hasAbnormalVitals(MedicalRecord record);

    // ─── Health Report Generation ─────────────────────────────────────────────

    /**
     * Generates a full health report for a specific patient.
     * Should include vitals, visit history, current medications,
     * active prescriptions, and upcoming appointments.
     *
     * @param patientId the ID of the patient
     * @return formatted health report as a String
     */
    String generateHealthReport(String patientId);

    /**
     * Generates a summary report of all patients in the system.
     * Used for the admin/doctor dashboard overview.
     *
     * @return formatted summary report string
     */
    String generateSystemReport();

    // ─── Alert System ─────────────────────────────────────────────────────────

    /**
     * Sends (or displays) a health alert for a specific patient.
     * Called when abnormal vitals or critical conditions are detected.
     *
     * @param patientId the ID of the patient to alert
     * @param message   the alert message to send
     */
    void sendAlert(String patientId, String message);

    /**
     * Scans all patients and returns a list of alert messages
     * for any patient with abnormal health data.
     * Called on system startup and when doctor views the dashboard.
     *
     * @return list of alert strings, empty if no alerts
     */
    ArrayList<String> checkAllPatientAlerts();

    // ─── Appointment Monitoring ───────────────────────────────────────────────

    /**
     * Returns a list of all upcoming appointments scheduled for today.
     *
     * @return list of today's Appointment objects
     */
    ArrayList<Appointment> getTodayAppointments();

    /**
     * Returns all appointments for a specific doctor on a given date.
     *
     * @param doctorId the doctor's ID
     * @param date     the date string in "dd-MM-yyyy" format
     * @return list of matching Appointment objects
     */
    ArrayList<Appointment> getDoctorSchedule(String doctorId, String date);

    // ─── Prescription Monitoring ──────────────────────────────────────────────

    /**
     * Scans all prescriptions and returns those that are close to expiry
     * (within 7 days) or already expired.
     *
     * @return list of expiring/expired Prescription objects
     */
    ArrayList<Prescription> getExpiringPrescriptions();

    /**
     * Returns all active prescriptions for a specific patient.
     *
     * @param patientId the patient's ID
     * @return list of active Prescription objects
     */
    ArrayList<Prescription> getActivePrescriptions(String patientId);

    // ─── Default Method (Java 8+ interface feature) ───────────────────────────

    /**
     * Default method — provides a standard health status label
     * based on a BMI value. Can be used without overriding.
     *
     * Implementing classes inherit this for free but can override if needed.
     *
     * @param bmi the calculated BMI value
     * @return health status label string
     */
    default String getBMIStatus(double bmi) {
        if (bmi <= 0)    return "No data";
        if (bmi < 18.5)  return "Underweight — consider nutritional support";
        if (bmi < 25.0)  return "Normal — maintain healthy lifestyle";
        if (bmi < 30.0)  return "Overweight — lifestyle changes recommended";
        return                  "Obese — medical intervention advised";
    }

    /**
     * Default method — returns a standard heart rate status label.
     * Implementing classes inherit this for free.
     *
     * @param heartRate the patient's heart rate in bpm
     * @return status label string
     */
    default String getHeartRateStatus(int heartRate) {
        if (heartRate <= 0)   return "No data";
        if (heartRate < 60)   return "Bradycardia — heart rate too low";
        if (heartRate <= 100) return "Normal resting heart rate";
        return                       "Tachycardia — heart rate too high";
    }

    // ─── Constants (interface fields are public static final by default) ──────

    /** Alert threshold: days before prescription expiry to trigger a warning */
    int EXPIRY_WARNING_DAYS = 7;

    /** Normal BMI lower bound */
    double BMI_NORMAL_MIN = 18.5;

    /** Normal BMI upper bound */
    double BMI_NORMAL_MAX = 24.9;

    /** Normal resting heart rate lower bound (bpm) */
    int HEART_RATE_MIN = 60;

    /** Normal resting heart rate upper bound (bpm) */
    int HEART_RATE_MAX = 100;
}