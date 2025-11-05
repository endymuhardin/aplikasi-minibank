package id.ac.tazkia.minibank.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import id.ac.tazkia.minibank.entity.PersonalCustomer;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PersonalCustomerCreateDto {

    // === Data Nama & Alamat ===

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    private String firstName;

    @NotBlank(message = "Nama belakang wajib diisi")
    @Size(max = 100, message = "Nama belakang maksimal 100 karakter")
    private String lastName;

    @Size(max = 100, message = "Nama alias maksimal 100 karakter")
    private String aliasName;

    @NotBlank(message = "Alamat wajib diisi")
    @Size(max = 500, message = "Alamat maksimal 500 karakter")
    private String address;

    @Size(max = 20, message = "Kode pos maksimal 20 karakter")
    @Pattern(regexp = "^\\d{1,5}$", message = "Format kode pos tidak valid")
    private String postalCode;

    @Size(max = 100, message = "Kota maksimal 100 karakter")
    private String city;

    @Size(max = 50, message = "Negara maksimal 50 karakter")
    private String country = "Indonesia";

    @NotBlank(message = "No. telp wajib diisi")
    @Size(max = 20, message = "No. telp maksimal 20 karakter")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format no. telp tidak valid")
    private String phoneNumber;

    @NotBlank(message = "Email pribadi wajib diisi")
    @Email(message = "Format email tidak valid")
    @Size(max = 100, message = "Email maksimal 100 karakter")
    private String email;

    // === Data Pribadi ===

    @NotNull(message = "Tanggal lahir wajib diisi")
    @Past(message = "Tanggal lahir harus di masa lalu")
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Tempat lahir maksimal 100 karakter")
    private String birthPlace;

    @NotNull(message = "Pendidikan wajib dipilih")
    private PersonalCustomer.EducationLevel educationLevel;

    @NotNull(message = "Jenis kelamin wajib dipilih")
    private PersonalCustomer.Gender gender;

    @NotNull(message = "Agama wajib dipilih")
    private PersonalCustomer.Religion religion;

    @NotNull(message = "Status pernikahan wajib dipilih")
    private PersonalCustomer.MaritalStatus maritalStatus;

    @NotBlank(message = "Nama gadis ibu kandung wajib diisi")
    @Size(max = 100, message = "Nama ibu kandung maksimal 100 karakter")
    private String mothersMaidenName;

    @Min(value = 0, message = "Jumlah tanggungan minimal 0")
    @Max(value = 20, message = "Jumlah tanggungan maksimal 20")
    private Integer dependents = 0;

    // === Identitas ===

    @NotNull(message = "Kewarganegaraan wajib dipilih")
    private PersonalCustomer.Nationality nationality = PersonalCustomer.Nationality.WNI;

    @Size(max = 50, message = "Kode kependudukan maksimal 50 karakter")
    private String residenceCode;

    @NotNull(message = "Jenis identitas diri wajib dipilih")
    private PersonalCustomer.IdentityType identityType;

    @NotBlank(message = "Nomor identitas wajib diisi")
    @Size(max = 100, message = "Nomor identitas maksimal 100 karakter")
    private String identityNumber;

    @Future(message = "Tanggal berlaku harus di masa depan")
    private LocalDate identityExpiryDate;

    // === Data Pekerjaan ===

    private PersonalCustomer.JobTitle jobTitle;

    @Size(max = 200, message = "Nama perusahaan maksimal 200 karakter")
    private String companyName;

    @Size(max = 500, message = "Alamat perusahaan maksimal 500 karakter")
    private String companyAddress;

    @Size(max = 20, message = "No. telp kantor maksimal 20 karakter")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Format no. telp kantor tidak valid")
    private String officePhone;

    @Email(message = "Format email kantor tidak valid")
    @Size(max = 100, message = "Email kantor maksimal 100 karakter")
    private String officeEmail;

    @PastOrPresent(message = "Tanggal mulai kerja tidak boleh di masa depan")
    private LocalDate employmentStartDate;

    @Size(max = 20, message = "No. fax kantor maksimal 20 karakter")
    private String officeFax;

    @Size(max = 20, message = "Kode pos perusahaan maksimal 20 karakter")
    @Pattern(regexp = "^\\d{1,5}$", message = "Format kode pos perusahaan tidak valid")
    private String companyPostalCode;

    private PersonalCustomer.Profession profession;

    @Size(max = 100, message = "Provinsi maksimal 100 karakter")
    private String province;

    // Additional fields for customer creation
    private String customerNumber; // Auto-generated customer number
    private UUID customerLocation; // Branch ID
    private String customerType = "INDIVIDU";

    // Getter for customerNumber for form display
    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    // Business validation methods
    public boolean isComplete() {
        return firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               dateOfBirth != null &&
               identityNumber != null && !identityNumber.trim().isEmpty() &&
               identityType != null &&
               email != null && !email.trim().isEmpty() &&
               phoneNumber != null && !phoneNumber.trim().isEmpty();
    }

    public int getAge() {
        if (dateOfBirth == null) return 0;
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    public boolean isIdentityValid() {
        if (identityExpiryDate == null) return true;
        return LocalDate.now().isBefore(identityExpiryDate) || LocalDate.now().isEqual(identityExpiryDate);
    }

    public boolean isAdult() {
        return getAge() >= 17;
    }

    public boolean hasWorkInformation() {
        return jobTitle != null || companyName != null || profession != null;
    }

    public boolean isCompleteForVerification() {
        return isComplete() &&
               nationality != null &&
               religion != null &&
               maritalStatus != null &&
               mothersMaidenName != null && !mothersMaidenName.trim().isEmpty();
    }
}