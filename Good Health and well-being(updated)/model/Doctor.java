package model;

import data.Appointment;
import data.Prescription;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents a doctor in the HealthCare Management System.
 * Extends Person to demonstrate:
 *   - Inheritance     : inherits all fields and methods from Person
 *   - Polymorphism    : overrides getRole(), getDetails(), toCSV()
 *   - Encapsulation   : doctor-specific private fields with getters/setters
 *   - Collections     : ArrayList and HashMap to manage appointments/patients
 *
 * BUG FIXED: fromCSV() now correctly restores the isAvailable field (parts[13])
 * which was silently ignored before, causing all loaded doctors to appear
 * Available regardless of their saved state.
 */
public class Doctor extends Person {

    // ─── Doctor-Specific Private Fields (Encapsulation) ──────────────────────
    private String  licenseNumber;
    private String  specialization;
    private String  department;
    private int     yearsOfExperience;
    private double  consultationFee;
    private boolean isAvailable;

    // ─── Collections ──────────────────────────────────────────────────────────
    private ArrayList<Appointment>  schedule;
    private ArrayList<Prescription> issuedPrescriptions;
    private HashMap<String, String> patientNotes;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public Doctor(String personId, String fullName, int age, String gender,
                  String icNumber, String phoneNumber, String email, String address,
                  String licenseNumber, String specialization, String department,
                  int yearsOfExperience, double consultationFee) {

        super(personId, fullName, age, gender, icNumber, phoneNumber, email, address);

        this.licenseNumber      = licenseNumber;
        this.specialization     = specialization;
        this.department         = department;
        this.yearsOfExperience  = yearsOfExperience;
        this.consultationFee    = consultationFee;
        this.isAvailable        = true;

        this.schedule            = new ArrayList<>();
        this.issuedPrescriptions = new ArrayList<>();
        this.patientNotes        = new HashMap<>();
    }

    // ─── Abstract Method Implementations (Polymorphism) ───────────────────────

    @Override
    public String getRole() {
        return "Doctor";
    }

    @Override
    public String getDetails() {
        return "=== Doctor Details ===" +
               "\nID                 : " + getPersonId() +
               "\nName               : " + getFullName() +
               "\nAge                : " + getAge() +
               "\nGender             : " + getGender() +
               "\nIC/Passport        : " + getIcNumber() +
               "\nPhone              : " + getPhoneNumber() +
               "\nEmail              : " + getEmail() +
               "\nAddress            : " + getAddress() +
               "\nLicense No.        : " + licenseNumber +
               "\nSpecialization     : " + specialization +
               "\nDepartment         : " + department +
               "\nExperience         : " + yearsOfExperience + " year(s)" +
               "\nConsultation Fee   : RM " + String.format("%.2f", consultationFee) +
               "\nAvailability       : " + (isAvailable ? "Available" : "Unavailable") +
               "\nScheduled Appts    : " + schedule.size() +
               "\nPrescriptions Issued: " + issuedPrescriptions.size();
    }

    // ─── File Handling ────────────────────────────────────────────────────────

    /**
     * CSV format:
     * personId,fullName,age,gender,icNumber,phone,email,address,
     * licenseNumber,specialization,department,yearsOfExperience,
     * consultationFee,isAvailable
     */
    @Override
    public String toCSV() {
        return super.toCSV()     + "," +
               licenseNumber     + "," +
               specialization    + "," +
               department        + "," +
               yearsOfExperience + "," +
               consultationFee   + "," +
               isAvailable;
    }

    /**
     * Recreates a Doctor from a CSV string.
     *
     * BUG FIX: Previously this method only read up to parts[12] (consultationFee)
     * and never restored parts[13] (isAvailable). Doctors who were set as
     * Unavailable before saving would silently revert to Available on reload.
     * Now we call setAvailable() with the saved boolean value.
     */
    public static Doctor fromCSV(String csv) {
        String[] parts = csv.split(",");
        Doctor doctor = new Doctor(
            parts[0],                       // personId
            parts[1],                       // fullName
            Integer.parseInt(parts[2]),     // age
            parts[3],                       // gender
            parts[4],                       // icNumber
            parts[5],                       // phoneNumber
            parts[6],                       // email
            parts[7],                       // address
            parts[8],                       // licenseNumber
            parts[9],                       // specialization
            parts[10],                      // department
            Integer.parseInt(parts[11]),    // yearsOfExperience
            Double.parseDouble(parts[12])   // consultationFee
        );
        // FIX: restore the saved availability state (parts[13])
        if (parts.length > 13) {
            doctor.setAvailable(Boolean.parseBoolean(parts[13]));
        }
        return doctor;
    }

    // ─── Schedule Management ──────────────────────────────────────────────────

    public void addAppointment(Appointment appointment) {
        if (appointment != null) schedule.add(appointment);
    }

    public boolean removeAppointment(String appointmentId) {
        return schedule.removeIf(a -> a.getAppointmentId().equals(appointmentId));
    }

    public int getScheduleCount() {
        return schedule.size();
    }

    // ─── Prescription Management ──────────────────────────────────────────────

    public void issuePrescription(Prescription prescription) {
        if (prescription != null) issuedPrescriptions.add(prescription);
    }

    public int getPrescriptionCount() {
        return issuedPrescriptions.size();
    }

    // ─── Patient Notes (HashMap) ──────────────────────────────────────────────

    public void addPatientNote(String patientId, String note) {
        patientNotes.put(patientId, note);
    }

    public String getPatientNote(String patientId) {
        return patientNotes.getOrDefault(patientId, "No notes recorded.");
    }

    public void removePatientNote(String patientId) {
        patientNotes.remove(patientId);
    }

    // ─── Availability ─────────────────────────────────────────────────────────

    public void toggleAvailability() {
        this.isAvailable = !this.isAvailable;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String  getLicenseNumber()     { return licenseNumber;      }
    public String  getSpecialization()    { return specialization;     }
    public String  getDepartment()        { return department;         }
    public int     getYearsOfExperience() { return yearsOfExperience;  }
    public double  getConsultationFee()   { return consultationFee;    }
    public boolean isAvailable()          { return isAvailable;        }

    public ArrayList<Appointment>  getSchedule()            { return schedule;            }
    public ArrayList<Prescription> getIssuedPrescriptions() { return issuedPrescriptions; }
    public HashMap<String, String> getPatientNotes()        { return patientNotes;        }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setLicenseNumber(String licenseNumber)      { this.licenseNumber     = licenseNumber;     }
    public void setSpecialization(String specialization)    { this.specialization    = specialization;    }
    public void setDepartment(String department)            { this.department        = department;        }
    public void setYearsOfExperience(int years)             { this.yearsOfExperience = years;             }
    public void setConsultationFee(double fee)              { this.consultationFee   = fee;               }
    public void setAvailable(boolean available)             { this.isAvailable       = available;         }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return getPersonId() + " - Dr. " + getFullName() + " (" + specialization + ")";
    }
}