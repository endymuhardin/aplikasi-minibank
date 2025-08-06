package id.ac.tazkia.minibank.integration.repository;

import id.ac.tazkia.minibank.entity.SequenceNumber;
import id.ac.tazkia.minibank.repository.SequenceNumberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class SequenceNumberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SequenceNumberRepository sequenceNumberRepository;

    @BeforeEach
    void setUp() {
        sequenceNumberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/sequences.csv", numLinesToSkip = 1)
    void shouldSaveAndFindSequenceNumberFromCsv(
            String sequenceName,
            String lastNumberStr,
            String prefix) {

        // Given - Create sequence number from CSV data
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName(sequenceName);
        sequenceNumber.setLastNumber(Long.parseLong(lastNumberStr));
        sequenceNumber.setPrefix(prefix);

        // When - Save sequence number
        SequenceNumber savedSequenceNumber = sequenceNumberRepository.save(sequenceNumber);
        entityManager.flush();

        // Then - Verify sequence number was saved correctly
        assertThat(savedSequenceNumber.getId()).isNotNull();
        assertThat(savedSequenceNumber.getSequenceName()).isEqualTo(sequenceName);
        assertThat(savedSequenceNumber.getLastNumber()).isEqualTo(Long.parseLong(lastNumberStr));
        assertThat(savedSequenceNumber.getPrefix()).isEqualTo(prefix);
        assertThat(savedSequenceNumber.getCreatedDate()).isNotNull();
        assertThat(savedSequenceNumber.getUpdatedDate()).isNotNull();

        // Verify we can find by sequence name
        Optional<SequenceNumber> foundSequenceNumber = sequenceNumberRepository.findBySequenceName(sequenceName);
        assertThat(foundSequenceNumber).isPresent();
        assertThat(foundSequenceNumber.get().getSequenceName()).isEqualTo(sequenceName);
        assertThat(foundSequenceNumber.get().getLastNumber()).isEqualTo(Long.parseLong(lastNumberStr));
    }

    @Test
    void shouldFindBySequenceNameWithLock() {
        // Given
        saveTestSequenceNumbers();

        // When
        Optional<SequenceNumber> sequenceNumber = sequenceNumberRepository.findBySequenceNameWithLock("CUSTOMER_NUMBER");

        // Then
        assertThat(sequenceNumber).isPresent();
        assertThat(sequenceNumber.get().getSequenceName()).isEqualTo("CUSTOMER_NUMBER");
        assertThat(sequenceNumber.get().getPrefix()).isEqualTo("C");
    }

    @Test
    @Transactional
    void shouldIncrementSequenceNumber() {
        // Given
        saveTestSequenceNumbers();
        Long originalValue = sequenceNumberRepository.findBySequenceName("CUSTOMER_NUMBER")
            .map(SequenceNumber::getLastNumber)
            .orElse(0L);

        // When
        int updatedRows = sequenceNumberRepository.incrementSequenceNumber("CUSTOMER_NUMBER");
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("CUSTOMER_NUMBER");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(originalValue + 1);
    }

    @Test
    @Transactional
    void shouldResetSequenceNumber() {
        // Given
        saveTestSequenceNumbers();
        Long newValue = 5000000L;

        // When
        int updatedRows = sequenceNumberRepository.resetSequenceNumber("CUSTOMER_NUMBER", newValue);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(updatedRows).isEqualTo(1);
        
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("CUSTOMER_NUMBER");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(newValue);
    }

    @Test
    void shouldGetCurrentSequenceValue() {
        // Given
        saveTestSequenceNumbers();

        // When
        Optional<Long> currentValue = sequenceNumberRepository.getCurrentSequenceValue("CUSTOMER_NUMBER");

        // Then
        assertThat(currentValue).isPresent();
        assertThat(currentValue.get()).isGreaterThan(0);
    }

    @Test
    void shouldCheckSequenceNameExistence() {
        // Given
        saveTestSequenceNumbers();

        // When & Then
        assertThat(sequenceNumberRepository.existsBySequenceName("CUSTOMER_NUMBER")).isTrue();
        assertThat(sequenceNumberRepository.existsBySequenceName("NONEXISTENT_SEQUENCE")).isFalse();
    }

    @Test
    void shouldTestSequenceNumberBusinessMethods() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("TEST_SEQUENCE");
        sequenceNumber.setLastNumber(1000L);
        sequenceNumber.setPrefix("TEST");

        // When - Test getNextNumber
        Long nextNumber1 = sequenceNumber.getNextNumber();
        Long nextNumber2 = sequenceNumber.getNextNumber();

        // Then
        assertThat(nextNumber1).isEqualTo(1001L);
        assertThat(nextNumber2).isEqualTo(1002L);
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(1002L);
    }

    @Test
    void shouldGenerateNextSequenceWithPrefix() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("CUSTOMER_NUMBER");
        sequenceNumber.setLastNumber(1000000L);
        sequenceNumber.setPrefix("C");

        // When
        String generatedSequence = sequenceNumber.generateNextSequence();

        // Then
        assertThat(generatedSequence).isEqualTo("C1000001");
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(1000001L);
    }

    @Test
    void shouldGenerateNextSequenceWithoutPrefix() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("TEST_SEQUENCE");
        sequenceNumber.setLastNumber(500L);
        sequenceNumber.setPrefix(null);

        // When
        String generatedSequence = sequenceNumber.generateNextSequence();

        // Then
        assertThat(generatedSequence).isEqualTo("0000501");
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(501L);
    }

    @Test
    void shouldGenerateNextSequenceWithEmptyPrefix() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("TEST_SEQUENCE");
        sequenceNumber.setLastNumber(999L);
        sequenceNumber.setPrefix("");

        // When
        String generatedSequence = sequenceNumber.generateNextSequence();

        // Then
        assertThat(generatedSequence).isEqualTo("0001000");
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(1000L);
    }

    @Test
    void shouldResetSequenceToNewValue() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("TEST_SEQUENCE");
        sequenceNumber.setLastNumber(5000L);

        // When
        sequenceNumber.resetSequence(1000L);

        // Then
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(1000L);
    }

    @Test
    void shouldHandleConcurrentSequenceGeneration() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("CONCURRENT_TEST");
        sequenceNumber.setLastNumber(0L);
        sequenceNumber.setPrefix("CT");

        // When - Simulate concurrent calls to getNextNumber
        Long number1 = sequenceNumber.getNextNumber();
        Long number2 = sequenceNumber.getNextNumber();
        Long number3 = sequenceNumber.getNextNumber();

        // Then - Each call should return a unique incremented number
        assertThat(number1).isEqualTo(1L);
        assertThat(number2).isEqualTo(2L);
        assertThat(number3).isEqualTo(3L);
        assertThat(sequenceNumber.getLastNumber()).isEqualTo(3L);
    }

    @Test
    void shouldTestSequenceNumberPersistence() {
        // Given
        SequenceNumber sequenceNumber = new SequenceNumber();
        sequenceNumber.setSequenceName("PERSISTENCE_TEST");
        sequenceNumber.setLastNumber(12345L);
        sequenceNumber.setPrefix("PT");

        // When - Save and retrieve
        SequenceNumber savedSequence = sequenceNumberRepository.save(sequenceNumber);
        entityManager.flush();
        entityManager.clear();

        Optional<SequenceNumber> retrievedSequence = sequenceNumberRepository.findBySequenceName("PERSISTENCE_TEST");

        // Then
        assertThat(retrievedSequence).isPresent();
        assertThat(retrievedSequence.get().getId()).isNotNull();
        assertThat(retrievedSequence.get().getSequenceName()).isEqualTo("PERSISTENCE_TEST");
        assertThat(retrievedSequence.get().getLastNumber()).isEqualTo(12345L);
        assertThat(retrievedSequence.get().getPrefix()).isEqualTo("PT");
        assertThat(retrievedSequence.get().getCreatedDate()).isNotNull();
        assertThat(retrievedSequence.get().getUpdatedDate()).isNotNull();
    }

    private void saveTestSequenceNumbers() {
        // Customer number sequence
        SequenceNumber customerSequence = new SequenceNumber();
        customerSequence.setSequenceName("CUSTOMER_NUMBER");
        customerSequence.setLastNumber(1000006L);
        customerSequence.setPrefix("C");

        // Account number sequence  
        SequenceNumber accountSequence = new SequenceNumber();
        accountSequence.setSequenceName("ACCOUNT_NUMBER");
        accountSequence.setLastNumber(2000008L);
        accountSequence.setPrefix("A");

        // Transaction number sequence
        SequenceNumber transactionSequence = new SequenceNumber();
        transactionSequence.setSequenceName("TRANSACTION_NUMBER");
        transactionSequence.setLastNumber(3000008L);
        transactionSequence.setPrefix("T");

        // Reference number sequence
        SequenceNumber referenceSequence = new SequenceNumber();
        referenceSequence.setSequenceName("REFERENCE_NUMBER");
        referenceSequence.setLastNumber(100000L);
        referenceSequence.setPrefix("REF");

        sequenceNumberRepository.save(customerSequence);
        sequenceNumberRepository.save(accountSequence);
        sequenceNumberRepository.save(transactionSequence);
        sequenceNumberRepository.save(referenceSequence);
        entityManager.flush();
    }
}