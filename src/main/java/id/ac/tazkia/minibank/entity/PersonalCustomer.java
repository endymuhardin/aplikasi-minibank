package id.ac.tazkia.minibank.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "personal_customers")
@DiscriminatorValue("PERSONAL")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class PersonalCustomer extends Customer {

    // === Data Nama & Alamat ===

    @NotBlank(message = "Nama lengkap wajib diisi")
    @Size(max = 100, message = "Nama lengkap maksimal 100 karakter")
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank(message = "Nama belakang wajib diisi")
    @Size(max = 100, message = "Nama belakang maksimal 100 karakter")
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 100, message = "Nama alias maksimal 100 karakter")
    @Column(name = "alias_name", length = 100)
    private String aliasName;

    @Size(max = 20, message = "Kode pos maksimal 20 karakter")
    @Pattern(regexp = "^\\d{1,5}$", message = "Format kode pos tidak valid")
    @Column(name = "company_postal_code", length = 20) // Using existing column for postal code
    private String postalCode;

    // === Data Pribadi ===

    @NotNull(message = "Tanggal lahir wajib diisi")
    @Past(message = "Tanggal lahir harus di masa lalu")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Tempat lahir maksimal 100 karakter")
    @Column(name = "birth_place", length = 100)
    private String birthPlace;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", length = 50)
    private EducationLevel educationLevel;

    @NotNull(message = "Jenis kelamin wajib dipilih")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "religion", length = 50)
    private Religion religion;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 50)
    private MaritalStatus maritalStatus;

    @NotBlank(message = "Nama gadis ibu kandung wajib diisi")
    @Size(max = 100, message = "Nama ibu kandung maksimal 100 karakter")
    @Column(name = "mother_name", nullable = false, length = 100)
    private String mothersMaidenName;

    @Min(value = 0, message = "Jumlah tanggungan minimal 0")
    @Max(value = 20, message = "Jumlah tanggungan maksimal 20")
    @Column(name = "dependents")
    private Integer dependents = 0;

    // === Identitas ===

    @NotNull(message = "Kewarganegaraan wajib dipilih")
    @Enumerated(EnumType.STRING)
    @Column(name = "nationality", length = 20, nullable = false)
    private Nationality nationality = Nationality.WNI;

    @Size(max = 50, message = "Kode kependudukan maksimal 50 karakter")
    @Column(name = "residence_code", length = 50)
    private String residenceCode;

    @NotNull(message = "Jenis identitas diri wajib dipilih")
    @Enumerated(EnumType.STRING)
    @Column(name = "identity_type", nullable = false, length = 50)
    private IdentityType identityType;

    @NotBlank(message = "Nomor identitas wajib diisi")
    @Size(max = 100, message = "Nomor identitas maksimal 100 karakter")
    @Column(name = "identity_number", nullable = false, length = 100)
    private String identityNumber;

    @Future(message = "Tanggal berlaku harus di masa depan")
    @Column(name = "identity_expiry_date")
    private LocalDate identityExpiryDate;

    // === Data Pekerjaan ===

    @Enumerated(EnumType.STRING)
    @Column(name = "job_title", length = 100)
    private JobTitle jobTitle;

    @Size(max = 200, message = "Nama perusahaan maksimal 200 karakter")
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "company_address", columnDefinition = "TEXT")
    private String companyAddress;

    @Size(max = 20, message = "No. telp kantor maksimal 20 karakter")
    @Column(name = "office_phone", length = 20)
    private String officePhone;

    @Email(message = "Format email kantor tidak valid")
    @Size(max = 100, message = "Email kantor maksimal 100 karakter")
    @Column(name = "office_email", length = 100)
    private String officeEmail;

    @PastOrPresent(message = "Tanggal mulai kerja tidak boleh di masa depan")
    @Column(name = "employment_start_date")
    private LocalDate employmentStartDate;

    @Size(max = 20, message = "No. fax kantor maksimal 20 karakter")
    @Column(name = "office_fax", length = 20)
    private String officeFax;

    @Enumerated(EnumType.STRING)
    @Column(name = "profession", length = 100)
    private Profession profession;

    @Size(max = 100, message = "Provinsi maksimal 100 karakter")
    @Column(name = "province", length = 100)
    private String province;

    // Enums
    public enum EducationLevel {
        SD("Sekolah Dasar"),
        SMP("Sekolah Menengah Pertama"),
        SMA("Sekolah Menengah Atas"),
        D3("Diploma 3"),
        S1("Strata 1"),
        S2("Strata 2"),
        S3("Strata 3");

        private final String displayName;

        EducationLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Gender {
        MALE("Laki-laki"),
        FEMALE("Perempuan");

        private final String displayName;

        Gender(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Religion {
        ISLAM("Islam"),
        PROTESTAN("Protestan"),
        KATOLIK("Katolik"),
        BUDHA("Budha"),
        HINDU("Hindu"),
        LAINNYA("Lainnya");

        private final String displayName;

        Religion(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MaritalStatus {
        BELUM_KAWIN("Belum Kawin"),
        KAWIN("Kawin"),
        CERAI("Cerai");

        private final String displayName;

        MaritalStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Nationality {
        WNI("Warga Negara Indonesia"),
        WNA("Warga Negara Asing");

        private final String displayName;

        Nationality(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum IdentityType {
        KTP("Kartu Tanda Penduduk"),
        SIM("Surat Izin Mengemudi"),
        PASSPORT("Paspor");

        private final String displayName;

        IdentityType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum JobTitle {
        DIRECTOR("Direktur"),
        MANAGER("Manajer"),
        SUPERVISOR("Supervisor"),
        STAFF("Staff"),
        EMPLOYEE("Karyawan"),
        PROFESSIONAL("Professional"),
        ENTREPRENEUR("Wirausaha"),
        HOUSEWIFE("Ibu Rumah Tangga"),
        STUDENT("Pelajar/Mahasiswa"),
        RETIRED("Pensiunan"),
        OTHER("Lainnya");

        private final String displayName;

        JobTitle(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum Profession {
        DOCTOR("Dokter"),
        ENGINEER("Insinyur"),
        TEACHER("Guru"),
        LECTURER("Dosen"),
        LAWYER("Pengacara"),
        ACCOUNTANT("Akuntan"),
        ARCHITECT("Arsitek"),
        IT_PROFESSIONAL("IT Professional"),
        CIVIL_SERVANT("Pegawai Negeri Sipil"),
        PRIVATE_EMPLOYEE("Karyawan Swasta"),
        BUSINESS_OWNER("Pengusaha"),
        FREELANCER("Freelancer"),
        OTHER("Lainnya");

        private final String displayName;

        Profession(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.PERSONAL;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    public Integer getAge() {
        if (dateOfBirth == null) {
            return null;
        }
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    // Business validation methods
    public boolean isIdentityValid() {
        if (identityExpiryDate == null) return true;
        return LocalDate.now().isBefore(identityExpiryDate) || LocalDate.now().isEqual(identityExpiryDate);
    }

    public boolean isAdult() {
        if (dateOfBirth == null) return false;
        return getAge() != null && getAge() >= 17;
    }
}