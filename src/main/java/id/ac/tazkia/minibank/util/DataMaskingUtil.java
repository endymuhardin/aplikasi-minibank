package id.ac.tazkia.minibank.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for masking sensitive data in views
 */
public class DataMaskingUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DataMaskingUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Masks birthdate completely
     * Example: 1990-05-15 -> ****-**-**
     *
     * @param date the date to mask
     * @return masked date string
     */
    public static String maskBirthdate(LocalDate date) {
        if (date == null) {
            return "****-**-**";
        }
        return "****-**-**";
    }

    /**
     * Masks ID number showing only first 4 and last 4 digits
     * Example: 1234567890123456 -> 1234********3456
     *
     * @param identityNumber the identity number to mask (16 digits)
     * @return masked identity number
     */
    public static String maskIdentityNumber(String identityNumber) {
        if (identityNumber == null || identityNumber.isEmpty()) {
            return "****************";
        }

        if (identityNumber.length() < 8) {
            return "*".repeat(identityNumber.length());
        }

        if (identityNumber.length() == 16) {
            String first4 = identityNumber.substring(0, 4);
            String last4 = identityNumber.substring(12, 16);
            return first4 + "********" + last4;
        }

        // For non-standard lengths, show first 4 and last 4 with appropriate masking
        int maskLength = identityNumber.length() - 8;
        String first4 = identityNumber.substring(0, 4);
        String last4 = identityNumber.substring(identityNumber.length() - 4);
        return first4 + "*".repeat(maskLength) + last4;
    }

    /**
     * Masks phone number showing only last 4 digits
     * Example: 081234567890 -> 08XX-XXXX-7890
     *
     * @param phoneNumber the phone number to mask (10-13 digits starting with 08)
     * @return masked phone number
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "08XX-XXXX-XXXX";
        }

        // Remove any non-digit characters
        String digitsOnly = phoneNumber.replaceAll("[^0-9]", "");

        if (digitsOnly.length() < 4) {
            return "08XX-XXXX-XXXX";
        }

        // Get last 4 digits
        String last4 = digitsOnly.substring(digitsOnly.length() - 4);

        // Format as 08XX-XXXX-LAST4
        return "08XX-XXXX-" + last4;
    }
}
