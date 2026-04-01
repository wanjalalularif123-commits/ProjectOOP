package model;

import data.Appointment;
import data.MedicalRecord;
import data.Prescription;
import java.util.ArrayList;

/**
 * Represents a patient in the HealthCare Management System.
 * Extends Person to demonstrate:
 *   - Inheritance     : inherits all fields and methods from Person
 *   - Polymorphism    : overrides getRole(), getDetails(), toCSV()
 *   - Encapsulation   : patient-specific private fields with getters/setters
 *   - Collections     : ArrayList to store appointments and prescriptions
 *
 * BUG FIXED: fromCSV() now correctly restores the isActive field (parts[12])
 * which was silently ignored before, causing all loaded patients to appear
 * as Active regardless of their saved state.
 */
public class Patient extends Person {

    // ─── Patient-Specific Private Fields (Encapsulation) ─────────────────────
    private String  bloodType;
    private String  allergies;
    private String  emergencyContact;
    private String  emergencyPhone;
    private boolean isActive;

    // ─── Collections ──────────────────────────────────────────────────────────
    private ArrayList<Appointment>  appointments;
    private ArrayList<Prescription> prescriptions;
    private MedicalRecord           medicalRecord;

    // ─── Constructor ──────────────────────────────────────────────────────────

    public Patient(String personId, String fullName, int age, String gender,
                   String icNumber, String phoneNumber, String email, String address,
                   String bloodType, String allergies,
                   String emergencyContact, String emergencyPhone) {

        super(personId, fullName, age, gender, icNumber, phoneNumber, email, address);

        this.bloodType        = bloodType;
        this.allergies        = allergies;
        this.emergencyContact = emergencyContact;
        this.emergencyPhone   = emergencyPhone;
        this.isActive         = true;

        this.appointments  = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.medicalRecord = new MedicalRecord(personId);
    }

    // ─── Abstract Method Implementations (Polymorphism) ───────────────────────

    @Override
    public String getRole() {
        return "Patient";
    }

    @Override
    public String getDetails() {
        return "=== Patient Details ===" +
               "\nID               : " + getPersonId() +
               "\nName             : " + getFullName() +
               "\nAge              : " + getAge() +
               "\nGender           : " + getGender() +
               "\nIC/Passport      : " + getIcNumber() +
               "\nPhone            : " + getPhoneNumber() +
               "\nEmail            : " + getEmail() +
               "\nAddress          : " + getAddress() +
               "\nBlood Type       : " + bloodType +
               "\nAllergies        : " + allergies +
               "\nEmergency Contact: " + emergencyContact +
               "\nEmergency Phone  : " + emergencyPhone +
               "\nStatus           : " + (isActive ? "Active" : "Inactive") +
               "\nAppointments     : " + appointments.size() +
               "\nPrescriptions    : " + prescriptions.size();
    }

    // ─── File Handling ────────────────────────────────────────────────────────

    /**
     * CSV format:
     * personId,fullName,age,gender,icNumber,phone,email,address,
     * bloodType,allergies,emergencyContact,emergencyPhone,isActive
     */
    @Override
    public String toCSV() {
        return super.toCSV()    + "," +
               bloodType        + "," +
               allergies        + "," +
               emergencyContact + "," +
               emergencyPhone   + "," +
               isActive;
    }

    /**
     * Recreates a Patient from a CSV string.
     *
     * BUG FIX: Previously this method only read up to parts[11] (emergencyPhone)
     * and never restored parts[12] (isActive). Deactivated patients would
     * silently revert to Active on the next startup.
     * Now we call setActive() with the saved boolean value.
     */
    public static Patient fromCSV(String csv) {
        String[] parts = csv.split(",");
        Patient patient = new Patient(
            parts[0],                      // personId
            parts[1],                      // fullName
            Integer.parseInt(parts[2]),    // age
            parts[3],                      // gender
            parts[4],                      // icNumber
            parts[5],                      // phoneNumber
            parts[6],                      // email
            parts[7],                      // address
            parts[8],                      // bloodType
            parts[9],                      // allergies
            parts[10],                     // emergencyContact
            parts[11]                      // emergencyPhone
        );
        // FIX: restore the saved active state (parts[12])
        if (parts.length > 12) {
            patient.setActive(Boolean.parseBoolean(parts[12]));
        }
        return patient;
    }

    // ─── Appointment Management ───────────────────────────────────────────────

    public void addAppointment(Appointment appointment) {
        if (appointment != null) appointments.add(appointment);
    }

    public boolean removeAppointment(String appointmentId) {
        return appointments.removeIf(a -> a.getAppointmentId().equals(appointmentId));
    }

    public int getAppointmentCount() {
        return appointments.size();
    }

    // ─── Prescription Management ──────────────────────────────────────────────

    public void addPrescription(Prescription prescription) {
        if (prescription != null) prescriptions.add(prescription);
    }

    public Prescription getLatestPrescription() {
        if (prescriptions.isEmpty()) return null;
        return prescriptions.get(prescriptions.size() - 1);
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String              getBloodType()        { return bloodType;        }
    public String              getAllergies()         { return allergies;        }
    public String              getEmergencyContact()  { return emergencyContact; }
    public String              getEmergencyPhone()    { return emergencyPhone;   }
    public boolean             isActive()             { return isActive;         }
    public ArrayList<Appointment>  getAppointments()  { return appointments;     }
    public ArrayList<Prescription> getPrescriptions() { return prescriptions;    }
    public MedicalRecord       getMedicalRecord()     { return medicalRecord;    }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setBloodType(String bloodType)               { this.bloodType        = bloodType;        }
    public void setAllergies(String allergies)               { this.allergies        = allergies;        }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public void setEmergencyPhone(String emergencyPhone)     { this.emergencyPhone   = emergencyPhone;   }
    public void setActive(boolean active)                    { this.isActive         = active;           }
    public void setMedicalRecord(MedicalRecord medicalRecord){ this.medicalRecord    = medicalRecord;    }

    // ─── toString ─────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return getPersonId() + " - " + getFullName() + " (" + bloodType + ")";
    }
}