package data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import model.Doctor;
import model.Patient;

/**
 * Represents a scheduled appointment between a Patient and a Doctor.
 * Acts as a link class connecting the two main entities in the system.
 * Demonstrates:
 *   - Encapsulation  : all fields private with getters/setters
 *   - Collections    : stored in ArrayLists inside Patient, Doctor,
 *                      and HealthcareSystem
 *   - File Handling  : toCSV() and fromCSV() for data persistence
 *   - Decision-making: status transitions with validation logic
 */
public class Appointment {

    // ─── Appointment Status Constants ─────────────────────────────────────────
    public static final String STATUS_PENDING   = "Pending";
    public static final String STATUS_CONFIRMED = "Confirmed";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_CANCELLED = "Cancelled";

    // ─── Appointment Type Constants ───────────────────────────────────────────
    public static final String TYPE_GENERAL    = "General Consultation";
    public static final String TYPE_FOLLOWUP   = "Follow-Up";
    public static final String TYPE_EMERGENCY  = "Emergency";
    public static final String TYPE_SPECIALIST = "Specialist Referral";

    // ─── Private Fields (Encapsulation) ───────────────────────────────────────
    private String     appointmentId;
    private String     patientId;        // links to Patient
    private String     doctorId;         // links to Doctor
    private String     patientName;      // stored for display without full object
    private String     doctorName;       // stored for display without full object
    private LocalDate  appointmentDate;
    private LocalTime  appointmentTime;
    private String     appointmentType;
    private String     status;
    private String     reason;           // reason for visit
    private String     notes;            // doctor's notes after visit
    private double     fee;              // consultation fee (copied from Doctor)

    // ─── Date / Time Formatters ───────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HH:mm");

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new appointment linking a Patient and Doctor.
     *
     * @param appointmentId   Unique ID (e.g. "APT001")
     * @param patient         The Patient object booking the appointment
     * @param doctor          The Doctor object being booked
     * @param appointmentDate The scheduled date
     * @param appointmentTime The scheduled time
     * @param appointmentType One of the TYPE_* constants
     * @param reason          Reason for the visit
     */
    public Appointment(String appointmentId, Patient patient, Doctor doctor,
                       LocalDate appointmentDate, LocalTime appointmentTime,
                       String appointmentType, String reason) {
        this.appointmentId   = appointmentId;
        this.patientId       = patient.getPersonId();
        this.doctorId        = doctor.getPersonId();
        this.patientName     = patient.getFullName();
        this.doctorName      = doctor.getFullName();
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.appointmentType = appointmentType;
        this.reason          = reason;
        this.notes           = "";
        this.fee             = doctor.getConsultationFee();
        this.status          = STATUS_PENDING;  // always starts as Pending
    }

    /**
     * Full constructor — used when loading from file.
     */
    public Appointment(String appointmentId, String patientId, String doctorId,
                       String patientName, String doctorName,
                       LocalDate appointmentDate, LocalTime appointmentTime,
                       String appointmentType, String status,
                       String reason, String notes, double fee) {
        this.appointmentId   = appointmentId;
        this.patientId       = patientId;
        this.doctorId        = doctorId;
        this.patientName     = patientName;
        this.doctorName      = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.appointmentType = appointmentType;
        this.status          = status;
        this.reason          = reason;
        this.notes           = notes;
        this.fee             = fee;
    }

    // ─── Status Management (Decision-making logic) ────────────────────────────

    /**
     * Confirms a pending appointment.
     * Only works if current status is Pending.
     *
     * @return true if status changed, false if transition not allowed
     */
    public boolean confirm() {
        if (status.equals(STATUS_PENDING)) {
            status = STATUS_CONFIRMED;
            return true;
        }
        return false;
    }

    /**
     * Marks the appointment as completed.
     * Only works if current status is Confirmed.
     *
     * @return true if status changed, false if transition not allowed
     */
    public boolean complete() {
        if (status.equals(STATUS_CONFIRMED)) {
            status = STATUS_COMPLETED;
            return true;
        }
        return false;
    }

    /**
     * Cancels the appointment.
     * Can be cancelled from Pending or Confirmed state only.
     *
     * @return true if cancelled, false if already Completed or Cancelled
     */
    public boolean cancel() {
        if (status.equals(STATUS_PENDING) || status.equals(STATUS_CONFIRMED)) {
            status = STATUS_CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * Checks whether this appointment is still upcoming (not past today).
     *
     * @return true if appointment date is today or in the future
     */
    public boolean isUpcoming() {
        return !appointmentDate.isBefore(LocalDate.now());
    }

    /**
     * Checks whether this appointment is currently active
     * (Pending or Confirmed — not ended).
     *
     * @return true if active
     */
    public boolean isActive() {
        return status.equals(STATUS_PENDING) || status.equals(STATUS_CONFIRMED);
    }

    /**
     * Checks if this appointment clashes with another on the same
     * doctor's schedule (same doctor, same date, same time).
     *
     * @param other another Appointment to compare against
     * @return true if there is a scheduling conflict
     */
    public boolean clashesWith(Appointment other) {
        return this.doctorId.equals(other.doctorId)
            && this.appointmentDate.equals(other.appointmentDate)
            && this.appointmentTime.equals(other.appointmentTime)
            && !this.appointmentId.equals(other.appointmentId);
    }

    // ─── Display ──────────────────────────────────────────────────────────────

    /**
     * Returns a complete formatted summary of this appointment.
     *
     * @return formatted multi-line appointment details
     */
    public String getDetails() {
        return "=== Appointment Details ===" +
               "\nAppointment ID : " + appointmentId +
               "\nPatient        : " + patientName + " (" + patientId + ")" +
               "\nDoctor         : Dr. " + doctorName + " (" + doctorId + ")" +
               "\nDate           : " + appointmentDate.format(DATE_FORMAT) +
               "\nTime           : " + appointmentTime.format(TIME_FORMAT) +
               "\nType           : " + appointmentType +
               "\nReason         : " + reason +
               "\nStatus         : " + status +
               "\nFee            : RM " + String.format("%.2f", fee) +
               "\nNotes          : " + (notes.isEmpty() ? "None" : notes);
    }

    // ─── File Handling ────────────────────────────────────────────────────────

    /**
     * Converts appointment data to CSV format for file persistence.
     *
     * Format: appointmentId,patientId,doctorId,patientName,doctorName,
     *         date,time,type,status,reason,notes,fee
     *
     * @return CSV string
     */
    public String toCSV() {
        return appointmentId                        + "," +
               patientId                           + "," +
               doctorId                            + "," +
               patientName                         + "," +
               doctorName                          + "," +
               appointmentDate.format(DATE_FORMAT) + "," +
               appointmentTime.format(TIME_FORMAT) + "," +
               appointmentType                     + "," +
               status                              + "," +
               reason                              + "," +
               (notes.isEmpty() ? "N/A" : notes)  + "," +
               fee;
    }

    /**
     * Recreates an Appointment from a CSV string line.
     * Used by HealthcareSystem when loading data from file.
     *
     * @param csv a line read from the appointments data file
     * @return a reconstructed Appointment object
     */
    public static Appointment fromCSV(String csv) {
        String[] parts = csv.split(",");
        return new Appointment(
            parts[0],                                           // appointmentId
            parts[1],                                           // patientId
            parts[2],                                           // doctorId
            parts[3],                                           // patientName
            parts[4],                                           // doctorName
            LocalDate.parse(parts[5], DATE_FORMAT),             // appointmentDate
            LocalTime.parse(parts[6], TIME_FORMAT),             // appointmentTime
            parts[7],                                           // appointmentType
            parts[8],                                           // status
            parts[9],                                           // reason
            parts[10].equals("N/A") ? "" : parts[10],          // notes
            Double.parseDouble(parts[11])                       // fee
        );
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String    getAppointmentId()   { return appointmentId;   }
    public String    getPatientId()       { return patientId;       }
    public String    getDoctorId()        { return doctorId;        }
    public String    getPatientName()     { return patientName;     }
    public String    getDoctorName()      { return doctorName;      }
    public LocalDate getAppointmentDate() { return appointmentDate; }
    public LocalTime getAppointmentTime() { return appointmentTime; }
    public String    getAppointmentType() { return appointmentType; }
    public String    getStatus()          { return status;          }
    public String    getReason()          { return reason;          }
    public String    getNotes()           { return notes;           }
    public double    getFee()             { return fee;             }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }
    public void setAppointmentType(String appointmentType)    { this.appointmentType = appointmentType; }
    public void setReason(String reason)                      { this.reason          = reason;          }
    public void setNotes(String notes)                        { this.notes           = notes;           }
    public void setFee(double fee)                            { this.fee             = fee;             }

    // ─── toString override ────────────────────────────────────────────────────

    /**
     * Short display string for GUI components like JTable and JList.
     *
     * @return "APT001 | 25-04-2026 09:00 | Ali → Dr. Ahmad | Pending"
     */
    @Override
    public String toString() {
        return appointmentId                        + " | " +
               appointmentDate.format(DATE_FORMAT) + " " +
               appointmentTime.format(TIME_FORMAT) + " | " +
               patientName + " -> Dr. " + doctorName + " | " +
               status;
    }
}