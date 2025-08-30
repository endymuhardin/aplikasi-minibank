package id.ac.tazkia.minibank.config;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test data factory for generating realistic test data.
 * Uses atomic counters and ThreadLocalRandom to ensure uniqueness and proper randomization.
 */
@UtilityClass
@Slf4j
public class TestDataFactory {
    
    // Thread-safe counters for unique business keys
    private static final AtomicLong branchCodeCounter = new AtomicLong(ThreadLocalRandom.current().nextLong(1000, 9999));
    private static final AtomicLong customerCodeCounter = new AtomicLong(ThreadLocalRandom.current().nextLong(100000, 999999));
    private static final AtomicLong accountNumberCounter = new AtomicLong(ThreadLocalRandom.current().nextLong(1000000000L, 9999999999L));
    private static final AtomicLong transactionCounter = new AtomicLong(ThreadLocalRandom.current().nextLong(1000000L, 9999999L));
    
    // Thread-local Faker instance for better performance
    private static final ThreadLocal<Faker> faker = ThreadLocal.withInitial(() -> new Faker(Locale.forLanguageTag("id-ID")));
    
    /**
     * Gets the thread-local Faker instance
     */
    private static Faker getFaker() {
        return faker.get();
    }
    
    /**
     * Generates thread-safe unique branch code
     * Format: BR + 4 digits (BR1001, BR1002, etc.)
     */
    public static String generateBranchCode() {
        return "BR" + String.format("%04d", branchCodeCounter.incrementAndGet());
    }
    
    /**
     * Generates thread-safe unique customer code  
     * Format: CUS + 6 digits (CUS100001, CUS100002, etc.)
     */
    public static String generateCustomerCode() {
        return "CUS" + String.format("%06d", customerCodeCounter.incrementAndGet());
    }
    
    /**
     * Generates thread-safe unique account number
     * Format: 10 digits (1000000001, 1000000002, etc.)
     */
    public static String generateAccountNumber() {
        return String.format("%010d", accountNumberCounter.incrementAndGet());
    }
    
    /**
     * Generates thread-safe unique transaction number
     * Format: TXN + 7 digits (TXN1000001, TXN1000002, etc.)
     */
    public static String generateTransactionNumber() {
        return "TXN" + String.format("%07d", transactionCounter.incrementAndGet());
    }
    
    /**
     * Generates lifecycle test code with thread marker for cleanup
     * Format: LF + 4 random digits (LF1234, LF5678, etc.)
     */
    public static String generateLifecycleCode() {
        return "LF" + String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 9999));
    }
    
    
    // === BRANCH DATA GENERATION ===
    
    /**
     * Generates realistic branch name using Indonesian company naming patterns
     */
    public static String generateBranchName() {
        Faker f = getFaker();
        String[] prefixes = {"Kantor Cabang", "KC", "Cabang"};
        String[] areas = {
            "Jakarta Pusat", "Jakarta Selatan", "Jakarta Utara", "Jakarta Barat", "Jakarta Timur",
            "Bandung", "Surabaya", "Yogyakarta", "Semarang", "Medan", "Makassar", "Palembang",
            "Tangerang", "Bekasi", "Depok", "Bogor", "Cibinong", "Cikarang", "Karawang"
        };
        
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        String area = areas[ThreadLocalRandom.current().nextInt(areas.length)];
        
        return prefix + " " + area;
    }
    
    /**
     * Generates Indonesian address
     */
    public static String generateIndonesianAddress() {
        Faker f = getFaker();
        return f.address().streetName() + " No. " + 
               f.number().numberBetween(1, 999) + 
               ", " + f.address().city();
    }
    
    /**
     * Generates Indonesian city name
     */
    public static String generateIndonesianCity() {
        String[] cities = {
            "Jakarta", "Surabaya", "Bandung", "Bekasi", "Medan", "Tangerang", "Depok", 
            "Semarang", "Palembang", "Makassar", "South Tangerang", "Batam", "Bogor", 
            "Pekanbaru", "Bandar Lampung", "Padang", "Malang", "Yogyakarta", "Surakarta", "Denpasar"
        };
        return cities[ThreadLocalRandom.current().nextInt(cities.length)];
    }
    
    /**
     * Generates Indonesian postal code (5 digits)
     */
    public static String generateIndonesianPostalCode() {
        return String.format("%05d", ThreadLocalRandom.current().nextInt(10000, 99999));
    }
    
    /**
     * Generates Indonesian phone number format
     */
    public static String generateIndonesianPhoneNumber() {
        String[] prefixes = {"021", "022", "024", "031", "061", "0274", "0341", "0361", "0411", "0711"};
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        return prefix + "-" + ThreadLocalRandom.current().nextInt(1000000, 9999999);
    }
    
    /**
     * Generates professional Indonesian email address
     */
    public static String generateProfessionalEmail() {
        Faker f = getFaker();
        String[] domains = {"bankbsi.co.id", "mandiri.co.id", "bca.co.id", "bni.co.id", "bri.co.id", 
                           "company.co.id", "corporation.id", "finance.co.id"};
        String domain = domains[ThreadLocalRandom.current().nextInt(domains.length)];
        
        return f.name().firstName().toLowerCase() + "." + 
               f.name().lastName().toLowerCase().replaceAll("\\s", "") + 
               "@" + domain;
    }
    
    /**
     * Generates Indonesian manager name with proper titles
     */
    public static String generateManagerName() {
        Faker f = getFaker();
        String[] titles = {"Bapak", "Ibu", "Drs.", "Ir.", "Dr."};
        String title = titles[ThreadLocalRandom.current().nextInt(titles.length)];
        
        return title + " " + f.name().fullName();
    }
    
    // === CUSTOMER DATA GENERATION ===
    
    /**
     * Generates Indonesian personal name
     */
    public static String generateIndonesianPersonName() {
        return getFaker().name().fullName();
    }
    
    /**
     * Generates Indonesian company name
     */
    public static String generateIndonesianCompanyName() {
        Faker f = getFaker();
        String[] prefixes = {"PT", "CV", "UD", "Firma", "Koperasi", "Yayasan"};
        String[] suffixes = {"Indonesia", "Nusantara", "Mandiri", "Sejahtera", "Maju", "Jaya", "Abadi", "Sukses"};
        
        String prefix = prefixes[ThreadLocalRandom.current().nextInt(prefixes.length)];
        String suffix = suffixes[ThreadLocalRandom.current().nextInt(suffixes.length)];
        
        return prefix + " " + f.company().name() + " " + suffix;
    }
    
    /**
     * Generates Indonesian National Identity Number (NIK) format
     * Note: This generates a realistic format but not actual valid NIKs
     */
    public static String generateNIK() {
        // Format: DDMMYY-KKSS-SSSS where DD=date, MM=month, YY=year, KK=area code, SS=sequence
        int day = ThreadLocalRandom.current().nextInt(1, 32);
        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int year = ThreadLocalRandom.current().nextInt(70, 99); // Birth years 1970-1999
        int areaCode = ThreadLocalRandom.current().nextInt(10, 99);
        int sequence = ThreadLocalRandom.current().nextInt(1000, 9999);
        
        return String.format("%02d%02d%02d%02d%04d", day, month, year, areaCode, sequence);
    }
    
    /**
     * Generates Indonesian Tax ID (NPWP) format
     */
    public static String generateNPWP() {
        // Format: XX.XXX.XXX.X-XXX.XXX
        return String.format("%02d.%03d.%03d.%d-%03d.%03d",
                ThreadLocalRandom.current().nextInt(10, 99),
                ThreadLocalRandom.current().nextInt(100, 999),
                ThreadLocalRandom.current().nextInt(100, 999),
                ThreadLocalRandom.current().nextInt(1, 9),
                ThreadLocalRandom.current().nextInt(100, 999),
                ThreadLocalRandom.current().nextInt(100, 999));
    }
    
    // === FINANCIAL DATA GENERATION ===
    
    /**
     * Generates realistic deposit amount for Indonesian banking
     * Amounts are in IDR and follow common patterns
     */
    public static java.math.BigDecimal generateDepositAmount() {
        // Common deposit amounts in IDR: 100K - 100M
        long[] commonAmounts = {
            100_000L, 250_000L, 500_000L, 1_000_000L, 2_500_000L, 5_000_000L,
            10_000_000L, 25_000_000L, 50_000_000L, 100_000_000L
        };
        
        long amount = commonAmounts[ThreadLocalRandom.current().nextInt(commonAmounts.length)];
        return new java.math.BigDecimal(amount);
    }
    
    /**
     * Generates realistic withdrawal amount (smaller than deposits)
     */
    public static java.math.BigDecimal generateWithdrawalAmount() {
        // Common withdrawal amounts: 50K - 10M
        long[] commonAmounts = {
            50_000L, 100_000L, 250_000L, 500_000L, 1_000_000L, 2_000_000L, 5_000_000L, 10_000_000L
        };
        
        long amount = commonAmounts[ThreadLocalRandom.current().nextInt(commonAmounts.length)];
        return new java.math.BigDecimal(amount);
    }
    
    /**
     * Generates account balance with realistic patterns
     */
    public static java.math.BigDecimal generateAccountBalance() {
        // Account balances typically range from 0 to 500M IDR
        long minBalance = 0L;
        long maxBalance = 500_000_000L;
        long balance = ThreadLocalRandom.current().nextLong(minBalance, maxBalance);
        
        // Round to nearest 1000 for realism
        balance = (balance / 1000) * 1000;
        return new java.math.BigDecimal(balance);
    }
    
    // === DATE/TIME GENERATION ===
    
    /**
     * Generates recent timestamp for testing (within last 30 days)
     */
    public static LocalDateTime generateRecentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);
        
        long minEpoch = thirtyDaysAgo.toLocalDate().toEpochDay();
        long maxEpoch = now.toLocalDate().toEpochDay();
        long randomEpoch = ThreadLocalRandom.current().nextLong(minEpoch, maxEpoch + 1);
        
        return LocalDateTime.from(java.time.LocalDate.ofEpochDay(randomEpoch).atStartOfDay())
                .plusHours(ThreadLocalRandom.current().nextInt(0, 24))
                .plusMinutes(ThreadLocalRandom.current().nextInt(0, 60))
                .plusSeconds(ThreadLocalRandom.current().nextInt(0, 60));
    }
    
    // === STATUS/ENUM GENERATION ===
    
    /**
     * Generates random branch status
     */
    public static String generateBranchStatus() {
        String[] statuses = {"ACTIVE", "INACTIVE", "MAINTENANCE"};
        return statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
    }
    
    /**
     * Generates random account status
     */
    public static String generateAccountStatus() {
        String[] statuses = {"ACTIVE", "INACTIVE", "CLOSED", "FROZEN"};
        return statuses[ThreadLocalRandom.current().nextInt(statuses.length)];
    }
    
    /**
     * Generates random transaction channel
     */
    public static String generateTransactionChannel() {
        String[] channels = {"TELLER", "ATM", "ONLINE", "MOBILE", "TRANSFER"};
        return channels[ThreadLocalRandom.current().nextInt(channels.length)];
    }
    
}