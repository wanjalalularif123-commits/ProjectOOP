package model;

/**
 * Abstract base class representing any person in the healthcare system.
 * Demonstrates:
 *   - Abstraction      : abstract class with abstract method getRole()
 *   - Encapsulation    : private fields accessed via getters/setters
 *   - Inheritance      : Patient and Doctor extend this class
 *   - Polymorphism     : getRole() overridden by each subclass
 */
public abstract class Person {

    // ─── Private Fields (Encapsulation) ───────────────────────────────────────
    private String personId;
    private String fullName;
    private int    age;
    private String gender;
    private String icNumber;      // NRIC / passport
    private String phoneNumber;
    private String email;
    private String address;

    // ─── Constructor ──────────────────────────────────────────────────────────

    /**
     * Parameterised constructor — used by subclasses via super().
     *
     * @param personId    Unique ID (e.g. "P001" for Patient, "D001" for Doctor)
     * @param fullName    Full legal name
     * @param age         Age in years
     * @param gender      "Male", "Female", or "Other"
     * @param icNumber    NRIC or passport number
     * @param phoneNumber Contact number
     * @param email       Email address
     * @param address     Home address
     */
    public Person(String personId, String fullName, int age, String gender,
                  String icNumber, String phoneNumber, String email, String address) {
        this.personId    = personId;
        this.fullName    = fullName;
        this.age         = age;
        this.gender      = gender;
        this.icNumber    = icNumber;
        this.phoneNumber = phoneNumber;
        this.email       = email;
        this.address     = address;
    }

    // ─── Abstract Method (Abstraction + Polymorphism) ─────────────────────────

    /**
     * Returns the role of this person in the system.
     * Every subclass MUST override this method.
     *
     * Example returns:
     *   Patient → "Patient"
     *   Doctor  → "Doctor"
     *
     * @return role label as a String
     */
    public abstract String getRole();

    /**
     * Returns a formatted summary of this person's details.
     * Subclasses can override this to include role-specific info.
     *
     * @return multi-line display string
     */
    public abstract String getDetails();

    // ─── Concrete Method (shared behaviour) ───────────────────────────────────

    /**
     * Displays basic information about this person to the console.
     * Can be called from both Patient and Doctor without duplication.
     */
    public void displayInfo() {
        System.out.println("==============================");
        System.out.println("Role      : " + getRole());
        System.out.println("ID        : " + personId);
        System.out.println("Name      : " + fullName);
        System.out.println("Age       : " + age);
        System.out.println("Gender    : " + gender);
        System.out.println("IC/Passport: " + icNumber);
        System.out.println("Phone     : " + phoneNumber);
        System.out.println("Email     : " + email);
        System.out.println("Address   : " + address);
        System.out.println("==============================");
    }

    /**
     * Validates that essential fields are not null or empty.
     *
     * @return true if person data is valid
     */
    public boolean isValid() {
        return personId  != null && !personId.trim().isEmpty()
            && fullName  != null && !fullName.trim().isEmpty()
            && icNumber  != null && !icNumber.trim().isEmpty()
            && age > 0;
    }

    /**
     * Converts person data to a CSV-formatted string for file storage.
     * Subclasses should override and append their own fields.
     *
     * Format: personId,fullName,age,gender,icNumber,phoneNumber,email,address
     *
     * @return CSV string
     */
    public String toCSV() {
        return personId    + "," +
               fullName    + "," +
               age         + "," +
               gender      + "," +
               icNumber    + "," +
               phoneNumber + "," +
               email       + "," +
               address;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public String getPersonId()    { return personId;    }
    public String getFullName()    { return fullName;    }
    public int    getAge()         { return age;         }
    public String getGender()      { return gender;      }
    public String getIcNumber()    { return icNumber;    }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail()       { return email;       }
    public String getAddress()     { return address;     }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setPersonId(String personId)       { this.personId    = personId;    }
    public void setFullName(String fullName)        { this.fullName    = fullName;    }
    public void setAge(int age)                     { this.age         = age;         }
    public void setGender(String gender)            { this.gender      = gender;      }
    public void setIcNumber(String icNumber)        { this.icNumber    = icNumber;    }
    public void setPhoneNumber(String phoneNumber)  { this.phoneNumber = phoneNumber; }
    public void setEmail(String email)              { this.email       = email;       }
    public void setAddress(String address)          { this.address     = address;     }

    // ─── toString override ────────────────────────────────────────────────────

    /**
     * Returns a short readable representation.
     * Useful for displaying in JComboBox or JList in the GUI.
     *
     * @return "[Role] ID - Full Name"
     */
    @Override
    public String toString() {
        return "[" + getRole() + "] " + personId + " - " + fullName;
    }
}