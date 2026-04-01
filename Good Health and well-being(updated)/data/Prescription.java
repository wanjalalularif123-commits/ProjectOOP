package data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import model.Doctor;
import model.Patient;

/**
 * Represents a medical prescription issued by a Doctor to a Patient.
 * Demonstrates:
 *   - Encapsulation  : all fields private with getters/setters
 *   - Collections    : ArrayList of Medication items per prescription
 *   - File Handling  : toCSV() and fromCSV() for data persistence
 *   - Decision-making: expiry check, refill logic, status validation
 */
public class Prescription {

    // ─── Status Constants ─────────────────────────────────────────────────────
    public static final String STATUS_ACTIVE    = "Active";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_EXPIRED   = "Expired";
    public static final String STATUS_CANCELLED = "Cancelled";

    // ─── Inner Class: Medication (one prescription can have many medicines) ───
    /**
     * Represents a single medicine entry inside a Prescription.
     * Using an inner class keeps medication data tightly coupled
     * to the Prescription — good OOP design.
     */
    public static class Medication {
        private String medicationName;
        private String dosage;          // e.g. "500mg"
        private String frequency;       // e.g. "Twice daily"
        private int    durationDays;    // number of days to take
        private String instructions;    // e.g. "Take after meals"

        public Medication(String medicationName, String dosage,
                          String frequency, int durationDays, String instructions) {
            this.medicationName = medicationName;
            this.dosage         = dosage;
            this.frequency      = frequency;
            this.durationDays   = durationDays;
            this.instructions   = instructions;
        }

        // Getters
        public String getMedicationName() { return medicationName; }
        public String getDosage()         { return dosage;         }
        public String getFrequency()      { return frequency;      }
        public int    getDurationDays()   { return durationDays;   }
        public String getInstructions()   { return instructions;   }

        // CSV format for saving: name|dosage|frequency|days|instructions
        public String toCSV() {
            return medicationName + "|" + dosage + "|" +
                   frequency + "|" + durationDays + "|" + instructions;
        }

        // Rebuild Medication from CSV segment
        public static Medication fromCSV(String csv) {
            String[] parts = csv.split("\\|");
            return new Medication(parts[0], parts[1], parts[2],
                                  Integer.parseInt(parts[3]), parts[4]);
        }

        @Override
        public String toString() {
            return medicationName + " " + dosage + " — " +
                   frequency + " for " + durationDays + " day(s). " + instructions;
        }
    }

    // ─── Private Fields (Encapsulation) ───────────────────────────────────────
    private String              prescriptionId;
    private String              patientId;
    private String              doctorId;
    private String              patientName;
    private String              doctorName;
    private String              diagnosis;        // condition being treated
    private LocalDate           issueDate;
    private LocalDate           expiryDate;
    private String              status;
    private int                 refillsAllowed;
    private int                 refillsUsed;
    private String              pharmacyNotes;

    // ─── Collections (Collections requirement) ────────────────────────────────
    private ArrayList<Medication> medications;    // list of medicines in this Rx

    // ─── Date formatter ───────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new Prescription issued by a Doctor to a Patient.
     *
     * @param prescriptionId  Unique ID (e.g. "RX001")
     * @param patient         The Patient receiving the prescription
     * @param doctor          The Doctor issuing the prescription
     * @param diagnosis       Condition being treated
     * @param validDays       Number of days the prescription is valid
     * @param refillsAllowed  How many times the prescription can be refilled
     */
    public Prescription(String prescriptionId, Patient patient, Doctor doctor,
                        String diagnosis, int validDays, int refillsAllowed) {
        this.prescriptionId = prescriptionId;
        this.patientId      = patient.getPersonId();
        this.doctorId       = doctor.getPersonId();
        this.patientName    = patient.getFullName();
        this.doctorName     = doctor.getFullName();
        this.diagnosis      = diagnosis;
        this.issueDate      = LocalDate.now();
        this.expiryDate     = LocalDate.now().plusDays(validDays);
        this.status         = STATUS_ACTIVE;
        this.refillsAllowed = refillsAllowed;
        this.refillsUsed    = 0;
        this.pharmacyNotes  = "";
        this.medications    = new ArrayList<>();
    }

    /**
     * Full constructor — used when loading from file.
     */
    public Prescription(String prescriptionId, String patientId, String doctorId,
                        String patientName, String doctorName, String diagnosis,
                        LocalDate issueDate, LocalDate expiryDate, String status,
                        int refillsAllowed, int refillsUsed, String pharmacyNotes) {
        this.prescriptionId = prescriptionId;
        this.patientId      = patientId;
        this.doctorId       = doctorId;
        this.patientName    = patientName;
        this.doctorName     = doctorName;
        this.diagnosis      = diagnosis;
        this.issueDate      = issueDate;
        this.expiryDate     = expiryDate;
        this.status         = status;
        this.refillsAllowed = refillsAllowed;
        this.refillsUsed    = refillsUsed;
        this.pharmacyNotes  = pharmacyNotes;
        this.medications    = new ArrayList<>();
    }

    // ─── Medication Management ────────────────────────────────────────────────

    /**
     * Adds a medicine to this prescription.
     *
     * @param medication the Medication object to add
     */
    public void addMedication(Medication medication) {
        if (medication != null) {
            medications.add(medication);
        }
    }

    /**
     * Convenience method — creates and adds a Medication in one step.
     *
     * @param name         medicine name
     * @param dosage       e.g. "500mg"
     * @param frequency    e.g. "Once daily"
     * @param durationDays days to take
     * @param instructions e.g. "Take after meals"
     */
    public void addMedication(String name, String dosage, String frequency,
                               int durationDays, String instructions) {
        medications.add(new Medication(name, dosage, frequency,
                                       durationDays, instructions));
    }

    /**
     * Removes a medication by name.
     *
     * @param medicationName the name of the medicine to remove
     * @return true if removed, false if not found
     */
    public boolean removeMedication(String medicationName) {
        return medications.removeIf(m ->
            m.getMedicationName().equalsIgnoreCase(medicationName));
    }

    /**
     * Returns all medications as a formatted numbered list.
     *
     * @return formatted medication list string
     */
    public String getMedicationsFormatted() {
        if (medications.isEmpty()) return "No medications listed.";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < medications.size(); i++) {
            sb.append(i + 1).append(". ").append(medications.get(i)).append("\n");
        }
        return sb.toString().trim();
    }

    // ─── Status & Validity Logic (Decision-making) ────────────────────────────

    /**
     * Checks if this prescription has passed its expiry date.
     * Automatically updates status to Expired if needed.
     *
     * @return true if expired
     */
    public boolean isExpired() {
        if (LocalDate.now().isAfter(expiryDate)) {
            if (status.equals(STATUS_ACTIVE)) {
                status = STATUS_EXPIRED;
            }
            return true;
        }
        return false;
    }

    /**
     * Checks if a refill can be dispensed.
     * Conditions: must be Active, not expired, and have remaining refills.
     *
     * @return true if refill is allowed
     */
    public boolean canRefill() {
        return status.equals(STATUS_ACTIVE)
            && !isExpired()
            && refillsUsed < refillsAllowed;
    }

    /**
     * Processes a refill request.
     * Increments refillsUsed if eligible, marks Completed when last refill used.
     *
     * @return true if refill was successful, false if not eligible
     */
    public boolean processRefill() {
        if (!canRefill()) return false;

        refillsUsed++;

        // Mark completed when all refills have been used
        if (refillsUsed >= refillsAllowed) {
            status = STATUS_COMPLETED;
        }
        return true;
    }

    /**
     * Cancels this prescription.
     * Only active prescriptions can be cancelled.
     *
     * @return true if cancelled, false if already ended
     */
    public boolean cancel() {
        if (status.equals(STATUS_ACTIVE)) {
            status = STATUS_CANCELLED;
            return true;
        }
        return false;
    }

    /**
     * Returns how many refills are remaining.
     *
     * @return remaining refills count
     */
    public int getRefillsRemaining() {
        return refillsAllowed - refillsUsed;
    }

    /**
     * Returns how many days until expiry (negative if already expired).
     *
     * @return days until expiry
     */
    public long getDaysUntilExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // ─── Full Summary ─────────────────────────────────────────────────────────

    /**
     * Returns a complete formatted summary of this prescription.
     * Used for display in the GUI and printed reports.
     *
     * @return formatted multi-line prescription details
     */
    public String getDetails() {
        return "=== Prescription Details ===" +
               "\nPrescription ID  : " + prescriptionId +
               "\nPatient          : " + patientName + " (" + patientId + ")" +
               "\nIssued By        : Dr. " + doctorName + " (" + doctorId + ")" +
               "\nDiagnosis        : " + diagnosis +
               "\nIssue Date       : " + issueDate.format(DATE_FORMAT) +
               "\nExpiry Date      : " + expiryDate.format(DATE_FORMAT) +
               "\nDays Until Expiry: " + getDaysUntilExpiry() +
               "\nStatus           : " + status +
               "\nRefills Allowed  : " + refillsAllowed +
               "\nRefills Used     : " + refillsUsed +
               "\nRefills Remaining: " + getRefillsRemaining() +
               "\nPharmacy Notes   : " + (pharmacyNotes.isEmpty() ? "None" : pharmacyNotes) +
               "\n\nMedications:\n" + getMedicationsFormatted();
    }

    // ─── File Handling ────────────────────────────────────────────────────────

    /**
     * Converts prescription data to CSV for file persistence.
     * Medications are serialised as a semicolon-separated segment.
     *
     * Format: prescriptionId,patientId,doctorId,patientName,doctorName,
     *         diagnosis,issueDate,expiryDate,status,refillsAllowed,
     *         refillsUsed,pharmacyNotes;med1CSV;med2CSV;...
     *
     * @return full CSV string
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(prescriptionId)               .append(",");
        sb.append(patientId)                    .append(",");
        sb.append(doctorId)                     .append(",");
        sb.append(patientName)                  .append(",");
        sb.append(doctorName)                   .append(",");
        sb.append(diagnosis)                    .append(",");
        sb.append(issueDate.format(DATE_FORMAT)).append(",");
        sb.append(expiryDate.format(DATE_FORMAT)).append(",");
        sb.append(status)                       .append(",");
        sb.append(refillsAllowed)               .append(",");
        sb.append(refillsUsed)                  .append(",");
        sb.append(pharmacyNotes.isEmpty() ? "N/A" : pharmacyNotes);

        // Append each medication separated by semicolons
        for (Medication med : medications) {
            sb.append(";").append(med.toCSV());
        }
        return sb.toString();
    }

    /**
     * Recreates a Prescription from a CSV string line.
     * Used by HealthcareSystem when loading data from file.
     *
     * @param csv a line read from the prescriptions data file
     * @return a reconstructed Prescription object
     */
    public static Prescription fromCSV(String csv) {
        // Split by semicolons: first segment = core fields, rest = medications
        String[] segments = csv.split(";");
        String[] parts    = segments[0].split(",");

        Prescription rx = new Prescription(
            parts[0],                                       // prescriptionId
            parts[1],                                       // patientId
            parts[2],                                       // doctorId
            parts[3],                                       // patientName
            parts[4],                                       // doctorName
            parts[5],                                       // diagnosis
            LocalDate.parse(parts[6], DATE_FORMAT),         // issueDate
            LocalDate.parse(parts[7], DATE_FORMAT),         // expiryDate
            parts[8],                                       // status
            Integer.parseInt(parts[9]),                     // refillsAllowed
            Integer.parseInt(parts[10]),                    // refillsUsed
            parts[11].equals("N/A") ? "" : parts[11]       // pharmacyNotes
        );

        // Rebuild medications from remaining semicolon segments
        for (int i = 1; i < segments.length; i++) {
            rx.addMedication(Medication.fromCSV(segments[i]));
        }
        return rx;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String              getPrescriptionId() { return prescriptionId; }
    public String              getPatientId()      { return patientId;      }
    public String              getDoctorId()       { return doctorId;       }
    public String              getPatientName()    { return patientName;    }
    public String              getDoctorName()     { return doctorName;     }
    public String              getDiagnosis()      { return diagnosis;      }
    public LocalDate           getIssueDate()      { return issueDate;      }
    public LocalDate           getExpiryDate()     { return expiryDate;     }
    public String              getStatus()         { return status;         }
    public int                 getRefillsAllowed() { return refillsAllowed; }
    public int                 getRefillsUsed()    { return refillsUsed;    }
    public String              getPharmacyNotes()  { return pharmacyNotes;  }
    public ArrayList<Medication> getMedications()  { return medications;    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setDiagnosis(String diagnosis)         { this.diagnosis      = diagnosis;      }
    public void setExpiryDate(LocalDate expiryDate)    { this.expiryDate     = expiryDate;     }
    public void setRefillsAllowed(int refillsAllowed)  { this.refillsAllowed = refillsAllowed; }
    public void setPharmacyNotes(String pharmacyNotes) { this.pharmacyNotes  = pharmacyNotes;  }

    // ─── toString override ────────────────────────────────────────────────────

    /**
     * Short display string for GUI components like JTable and JList.
     *
     * @return "RX001 | Ali Bin Abu | Dr. Ahmad | Active | Expires: 25-05-2026"
     */
    @Override
    public String toString() {
        return prescriptionId + " | " + patientName +
               " | Dr. " + doctorName  +
               " | " + status +
               " | Expires: " + expiryDate.format(DATE_FORMAT);
    }
}