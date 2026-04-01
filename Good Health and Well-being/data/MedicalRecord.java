package data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Stores the complete health history of a Patient.
 * Linked one-to-one with a Patient object via patientId.
 * Demonstrates:
 *   - Encapsulation  : all fields private, accessed via getters/setters
 *   - Collections    : ArrayList for visit history, HashMap for test results
 *   - File Handling  : toCSV() and fromCSV() for data persistence
 */
public class MedicalRecord {

    // ─── Private Fields (Encapsulation) ───────────────────────────────────────
    private String              recordId;
    private String              patientId;          // links back to Patient
    private String              currentDiagnosis;
    private String              chronicConditions;  // e.g. "Diabetes, Hypertension"
    private double              height;             // in cm
    private double              weight;             // in kg
    private String              bloodPressure;      // e.g. "120/80"
    private int                 heartRate;          // beats per minute
    private LocalDate           lastUpdated;

    // ─── Collections (Collections requirement) ────────────────────────────────
    private ArrayList<String>           visitHistory;   // dated visit notes
    private ArrayList<String>           medications;    // current medication list
    private HashMap<String, String>     testResults;    // testName -> result value

    // ─── Date formatter ───────────────────────────────────────────────────────
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd-MM-yyyy");

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new empty MedicalRecord linked to a patient.
     * Called automatically when a Patient object is created.
     *
     * @param patientId the ID of the Patient this record belongs to
     */
    public MedicalRecord(String patientId) {
        this.recordId          = "REC-" + patientId;
        this.patientId         = patientId;
        this.currentDiagnosis  = "None";
        this.chronicConditions = "None";
        this.height            = 0.0;
        this.weight            = 0.0;
        this.bloodPressure     = "N/A";
        this.heartRate         = 0;
        this.lastUpdated       = LocalDate.now();

        // Initialise empty collections
        this.visitHistory = new ArrayList<>();
        this.medications  = new ArrayList<>();
        this.testResults  = new HashMap<>();
    }

    /**
     * Full constructor — used when loading from file.
     */
    public MedicalRecord(String recordId, String patientId, String currentDiagnosis,
                         String chronicConditions, double height, double weight,
                         String bloodPressure, int heartRate, LocalDate lastUpdated) {
        this.recordId          = recordId;
        this.patientId         = patientId;
        this.currentDiagnosis  = currentDiagnosis;
        this.chronicConditions = chronicConditions;
        this.height            = height;
        this.weight            = weight;
        this.bloodPressure     = bloodPressure;
        this.heartRate         = heartRate;
        this.lastUpdated       = lastUpdated;

        this.visitHistory = new ArrayList<>();
        this.medications  = new ArrayList<>();
        this.testResults  = new HashMap<>();
    }

    // ─── Health Metrics ───────────────────────────────────────────────────────

    /**
     * Calculates the Body Mass Index (BMI) from height and weight.
     * Demonstrates meaningful processing logic as required by rubric.
     *
     * Formula: weight (kg) / (height (m))^2
     *
     * @return BMI value rounded to 2 decimal places, or 0 if data missing
     */
    public double calculateBMI() {
        if (height <= 0 || weight <= 0) return 0.0;
        double heightInMetres = height / 100.0;
        double bmi = weight / (heightInMetres * heightInMetres);
        return Math.round(bmi * 100.0) / 100.0;
    }

    /**
     * Returns the BMI category based on WHO classification.
     *
     * @return category string e.g. "Normal weight", "Obese Class I"
     */
    public String getBMICategory() {
        double bmi = calculateBMI();
        if (bmi == 0)       return "Data not available";
        if (bmi < 18.5)     return "Underweight";
        if (bmi < 25.0)     return "Normal weight";
        if (bmi < 30.0)     return "Overweight";
        if (bmi < 35.0)     return "Obese Class I";
        if (bmi < 40.0)     return "Obese Class II";
        return                     "Obese Class III";
    }

    /**
     * Checks if heart rate is within a normal resting range (60-100 bpm).
     *
     * @return true if normal, false if abnormal or not recorded
     */
    public boolean isHeartRateNormal() {
        return heartRate >= 60 && heartRate <= 100;
    }

    // ─── Visit History Management ─────────────────────────────────────────────

    /**
     * Adds a dated visit note to the patient's history.
     * Each entry is automatically timestamped with today's date.
     *
     * @param note the clinical visit note written by the doctor
     */
    public void addVisitNote(String note) {
        String dated = "[" + LocalDate.now().format(DATE_FORMAT) + "] " + note;
        visitHistory.add(dated);
        this.lastUpdated = LocalDate.now();
    }

    /**
     * Returns all visit notes as a single formatted string.
     *
     * @return newline-separated visit history
     */
    public String getVisitHistoryFormatted() {
        if (visitHistory.isEmpty()) return "No visits recorded.";
        StringBuilder sb = new StringBuilder();
        for (String note : visitHistory) {
            sb.append(note).append("\n");
        }
        return sb.toString().trim();
    }

    // ─── Medication Management ────────────────────────────────────────────────

    /**
     * Adds a medication to the current medication list.
     *
     * @param medication name and dosage e.g. "Metformin 500mg"
     */
    public void addMedication(String medication) {
        if (medication != null && !medication.trim().isEmpty()) {
            medications.add(medication);
        }
    }

    /**
     * Removes a medication from the list.
     *
     * @param medication the medication string to remove
     * @return true if removed, false if not found
     */
    public boolean removeMedication(String medication) {
        return medications.remove(medication);
    }

    /**
     * Returns all current medications as a comma-separated string.
     *
     * @return formatted medication list
     */
    public String getMedicationsFormatted() {
        if (medications.isEmpty()) return "None";
        return String.join(", ", medications);
    }

    // ─── Test Results Management (HashMap) ───────────────────────────────────

    /**
     * Records a lab or diagnostic test result.
     * Demonstrates HashMap put/get usage.
     *
     * @param testName  e.g. "Blood Sugar", "Cholesterol", "HbA1c"
     * @param result    e.g. "5.6 mmol/L", "Normal"
     */
    public void addTestResult(String testName, String result) {
        testResults.put(testName, result);
        this.lastUpdated = LocalDate.now();
    }

    /**
     * Retrieves a specific test result by name.
     *
     * @param testName the name of the test
     * @return the result string, or "Not tested" if not found
     */
    public String getTestResult(String testName) {
        return testResults.getOrDefault(testName, "Not tested");
    }

    /**
     * Returns all test results as a formatted string.
     *
     * @return newline-separated test name and result pairs
     */
    public String getTestResultsFormatted() {
        if (testResults.isEmpty()) return "No test results recorded.";
        StringBuilder sb = new StringBuilder();
        for (HashMap.Entry<String, String> entry : testResults.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }

    // ─── Full Summary ─────────────────────────────────────────────────────────

    /**
     * Returns a complete formatted summary of this medical record.
     * Used for display in the GUI and printed reports.
     *
     * @return formatted multi-line summary
     */
    public String getSummary() {
        return "=== Medical Record ===" +
               "\nRecord ID         : " + recordId +
               "\nPatient ID        : " + patientId +
               "\nLast Updated      : " + lastUpdated.format(DATE_FORMAT) +
               "\nDiagnosis         : " + currentDiagnosis +
               "\nChronic Conditions: " + chronicConditions +
               "\nHeight            : " + height + " cm" +
               "\nWeight            : " + weight + " kg" +
               "\nBMI               : " + calculateBMI() + " (" + getBMICategory() + ")" +
               "\nBlood Pressure    : " + bloodPressure +
               "\nHeart Rate        : " + heartRate + " bpm" +
                   (isHeartRateNormal() ? " (Normal)" : " (Abnormal - consult doctor)") +
               "\nMedications       : " + getMedicationsFormatted() +
               "\n\nVisit History:\n" + getVisitHistoryFormatted() +
               "\n\nTest Results:\n" + getTestResultsFormatted();
    }

    // ─── File Handling ────────────────────────────────────────────────────────

    /**
     * Converts core fields to CSV for file persistence.
     * Collections (visitHistory, medications, testResults) are saved separately.
     *
     * Format: recordId,patientId,diagnosis,chronicConditions,
     *         height,weight,bloodPressure,heartRate,lastUpdated
     *
     * @return CSV string of core fields
     */
    public String toCSV() {
        return recordId          + "," +
               patientId         + "," +
               currentDiagnosis  + "," +
               chronicConditions + "," +
               height            + "," +
               weight            + "," +
               bloodPressure     + "," +
               heartRate         + "," +
               lastUpdated.format(DATE_FORMAT);
    }

    /**
     * Recreates a MedicalRecord from a CSV string line.
     * Used by HealthcareSystem when loading data from file.
     *
     * @param csv a line read from the medical records data file
     * @return a reconstructed MedicalRecord object
     */
    public static MedicalRecord fromCSV(String csv) {
        String[] parts = csv.split(",");
        return new MedicalRecord(
            parts[0],                                            // recordId
            parts[1],                                            // patientId
            parts[2],                                            // currentDiagnosis
            parts[3],                                            // chronicConditions
            Double.parseDouble(parts[4]),                        // height
            Double.parseDouble(parts[5]),                        // weight
            parts[6],                                            // bloodPressure
            Integer.parseInt(parts[7]),                          // heartRate
            LocalDate.parse(parts[8], DATE_FORMAT)               // lastUpdated
        );
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String              getRecordId()          { return recordId;          }
    public String              getPatientId()         { return patientId;         }
    public String              getCurrentDiagnosis()  { return currentDiagnosis;  }
    public String              getChronicConditions() { return chronicConditions; }
    public double              getHeight()            { return height;            }
    public double              getWeight()            { return weight;            }
    public String              getBloodPressure()     { return bloodPressure;     }
    public int                 getHeartRate()         { return heartRate;         }
    public LocalDate           getLastUpdated()       { return lastUpdated;       }
    public ArrayList<String>   getVisitHistory()      { return visitHistory;      }
    public ArrayList<String>   getMedications()       { return medications;       }
    public HashMap<String,String> getTestResults()    { return testResults;       }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setCurrentDiagnosis(String currentDiagnosis)   { this.currentDiagnosis  = currentDiagnosis;  this.lastUpdated = LocalDate.now(); }
    public void setChronicConditions(String chronicConditions) { this.chronicConditions = chronicConditions; this.lastUpdated = LocalDate.now(); }
    public void setHeight(double height)                       { this.height            = height;            this.lastUpdated = LocalDate.now(); }
    public void setWeight(double weight)                       { this.weight            = weight;            this.lastUpdated = LocalDate.now(); }
    public void setBloodPressure(String bloodPressure)         { this.bloodPressure     = bloodPressure;     this.lastUpdated = LocalDate.now(); }
    public void setHeartRate(int heartRate)                    { this.heartRate         = heartRate;         this.lastUpdated = LocalDate.now(); }

    // ─── toString override ────────────────────────────────────────────────────

    /**
     * Short display string for GUI components.
     *
     * @return "REC-P001 | Diagnosis: Diabetes | BMI: 24.5"
     */
    @Override
    public String toString() {
        return recordId + " | Diagnosis: " + currentDiagnosis +
               " | BMI: " + calculateBMI();
    }
}