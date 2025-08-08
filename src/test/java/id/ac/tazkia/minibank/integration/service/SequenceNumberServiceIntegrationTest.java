package id.ac.tazkia.minibank.integration.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.config.PostgresTestContainersConfiguration;
import id.ac.tazkia.minibank.entity.SequenceNumber;
import id.ac.tazkia.minibank.repository.SequenceNumberRepository;
import id.ac.tazkia.minibank.service.SequenceNumberService;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({PostgresTestContainersConfiguration.class, SequenceNumberService.class})
class SequenceNumberServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SequenceNumberRepository sequenceNumberRepository;

    @Autowired
    private SequenceNumberService sequenceNumberService;

    @BeforeEach
    void setUp() {
        sequenceNumberRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @Transactional
    void shouldGenerateNextSequenceWithPrefixForNewSequence() {
        // When
        String result = sequenceNumberService.generateNextSequence("ACCOUNT_NUMBER", "ACC");

        // Then
        assertThat(result).isEqualTo("ACC0000001");
        
        // Verify sequence was created in database
        Optional<SequenceNumber> sequence = sequenceNumberRepository.findBySequenceName("ACCOUNT_NUMBER");
        assertThat(sequence).isPresent();
        assertThat(sequence.get().getSequenceName()).isEqualTo("ACCOUNT_NUMBER");
        assertThat(sequence.get().getPrefix()).isEqualTo("ACC");
        assertThat(sequence.get().getLastNumber()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void shouldGenerateNextSequenceForExistingSequence() {
        // Given - Create existing sequence
        SequenceNumber existingSequence = new SequenceNumber();
        existingSequence.setSequenceName("CUSTOMER_NUMBER");
        existingSequence.setPrefix("C");
        existingSequence.setLastNumber(1000000L);
        sequenceNumberRepository.save(existingSequence);
        entityManager.flush();

        // When
        String result = sequenceNumberService.generateNextSequence("CUSTOMER_NUMBER", "C");

        // Then
        assertThat(result).isEqualTo("C1000001");
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify sequence was updated in database
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("CUSTOMER_NUMBER");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(1000001L);
    }

    @Test
    @Transactional
    void shouldGenerateConsecutiveSequences() {
        // When - Generate multiple sequences
        String seq1 = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
        String seq2 = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");
        String seq3 = sequenceNumberService.generateNextSequence("TRANSACTION_NUMBER", "TXN");

        // Then
        assertThat(seq1).isEqualTo("TXN0000001");
        assertThat(seq2).isEqualTo("TXN0000002");
        assertThat(seq3).isEqualTo("TXN0000003");
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify final state in database
        Optional<SequenceNumber> sequence = sequenceNumberRepository.findBySequenceName("TRANSACTION_NUMBER");
        assertThat(sequence).isPresent();
        assertThat(sequence.get().getLastNumber()).isEqualTo(3L);
    }

    @Test
    @Transactional
    void shouldGetNextNumberForNewSequence() {
        // When
        Long result = sequenceNumberService.getNextNumber("ORDER_NUMBER", "ORD");

        // Then
        assertThat(result).isEqualTo(1L);
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify sequence was created in database
        Optional<SequenceNumber> sequence = sequenceNumberRepository.findBySequenceName("ORDER_NUMBER");
        assertThat(sequence).isPresent();
        assertThat(sequence.get().getPrefix()).isEqualTo("ORD");
        assertThat(sequence.get().getLastNumber()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void shouldGetNextNumberForExistingSequence() {
        // Given - Create existing sequence
        SequenceNumber existingSequence = new SequenceNumber();
        existingSequence.setSequenceName("REFERENCE_NUMBER");
        existingSequence.setPrefix("REF");
        existingSequence.setLastNumber(5000L);
        sequenceNumberRepository.save(existingSequence);
        entityManager.flush();

        // When
        Long result = sequenceNumberService.getNextNumber("REFERENCE_NUMBER");

        // Then
        assertThat(result).isEqualTo(5001L);
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify sequence was updated in database by finding fresh entity
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("REFERENCE_NUMBER");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(5001L);
    }

    @Test
    @Transactional
    void shouldResetExistingSequence() {
        // Given - Create existing sequence
        SequenceNumber existingSequence = new SequenceNumber();
        existingSequence.setSequenceName("RESET_TEST");
        existingSequence.setPrefix("RST");
        existingSequence.setLastNumber(9999L);
        sequenceNumberRepository.save(existingSequence);
        entityManager.flush();

        // When
        sequenceNumberService.resetSequence("RESET_TEST", 1000L);

        // Then
        entityManager.flush();
        
        // Verify sequence was reset in database
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("RESET_TEST");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(1000L);
        
        // Verify next generation uses reset value
        String nextSequence = sequenceNumberService.generateNextSequence("RESET_TEST");
        assertThat(nextSequence).isEqualTo("RST0001001");
    }

    @Test
    @Transactional
    void shouldNotResetNonExistentSequence() {
        // When
        sequenceNumberService.resetSequence("NONEXISTENT", 5000L);

        // Then - No sequence should be created
        Optional<SequenceNumber> sequence = sequenceNumberRepository.findBySequenceName("NONEXISTENT");
        assertThat(sequence).isEmpty();
    }

    @Test
    @Transactional
    void shouldGetCurrentNumberForExistingSequence() {
        // Given - Create existing sequence
        SequenceNumber existingSequence = new SequenceNumber();
        existingSequence.setSequenceName("CURRENT_TEST");
        existingSequence.setPrefix("CUR");
        existingSequence.setLastNumber(12345L);
        sequenceNumberRepository.save(existingSequence);
        entityManager.flush();

        // When
        Long result = sequenceNumberService.getCurrentNumber("CURRENT_TEST");

        // Then
        assertThat(result).isEqualTo(12345L);
    }

    @Test
    @Transactional
    void shouldReturnZeroForNonExistentSequence() {
        // When
        Long result = sequenceNumberService.getCurrentNumber("NONEXISTENT");

        // Then
        assertThat(result).isEqualTo(0L);
    }

    @ParameterizedTest
    @CsvSource({
        "PARAM_TEST_1, PRM1, 100",
        "PARAM_TEST_2, PRM2, 200", 
        "PARAM_TEST_3, PRM3, 300"
    })
    @Transactional
    void shouldCreateAndGenerateSequencesFromParameters(String sequenceName, String prefix, Long startNumber) {
        // Given - Create sequence with start number
        SequenceNumber sequence = new SequenceNumber();
        sequence.setSequenceName(sequenceName);
        sequence.setPrefix(prefix);
        sequence.setLastNumber(startNumber);
        sequenceNumberRepository.save(sequence);
        entityManager.flush();

        // When
        String result = sequenceNumberService.generateNextSequence(sequenceName);

        // Then
        assertThat(result).isEqualTo(prefix + String.format("%07d", startNumber + 1));
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify in database by finding fresh entity
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName(sequenceName);
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(startNumber + 1);
    }

    @Test
    @Transactional
    void shouldHandleSequenceWithoutPrefix() {
        // Given - Create sequence without prefix
        SequenceNumber sequence = new SequenceNumber();
        sequence.setSequenceName("NO_PREFIX_TEST");
        sequence.setPrefix(null);
        sequence.setLastNumber(50L);
        sequenceNumberRepository.save(sequence);
        entityManager.flush();

        // When
        String result = sequenceNumberService.generateNextSequence("NO_PREFIX_TEST");

        // Then
        assertThat(result).isEqualTo("0000051");
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify in database by finding fresh entity
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("NO_PREFIX_TEST");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(51L);
    }

    @Test
    @Transactional
    void shouldHandleSequenceWithEmptyPrefix() {
        // When - Create new sequence with empty prefix
        String result = sequenceNumberService.generateNextSequence("EMPTY_PREFIX_TEST", "");

        // Then
        assertThat(result).isEqualTo("0000001");
        
        // Verify sequence was created in database
        Optional<SequenceNumber> sequence = sequenceNumberRepository.findBySequenceName("EMPTY_PREFIX_TEST");
        assertThat(sequence).isPresent();
        assertThat(sequence.get().getPrefix()).isEqualTo("");
        assertThat(sequence.get().getLastNumber()).isEqualTo(1L);
    }

    @Test
    @Transactional
    void shouldHandleLargeSequenceNumbers() {
        // Given - Create sequence with large number
        SequenceNumber sequence = new SequenceNumber();
        sequence.setSequenceName("LARGE_NUMBER_TEST");
        sequence.setPrefix("LRG");
        sequence.setLastNumber(9999999L);
        sequenceNumberRepository.save(sequence);
        entityManager.flush();

        // When
        String result = sequenceNumberService.generateNextSequence("LARGE_NUMBER_TEST");

        // Then
        assertThat(result).isEqualTo("LRG10000000");
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify in database by finding fresh entity
        Optional<SequenceNumber> updatedSequence = sequenceNumberRepository.findBySequenceName("LARGE_NUMBER_TEST");
        assertThat(updatedSequence).isPresent();
        assertThat(updatedSequence.get().getLastNumber()).isEqualTo(10000000L);
    }

    @Test
    @Transactional
    void shouldCreateMultipleIndependentSequences() {
        // When - Create multiple different sequences
        String account1 = sequenceNumberService.generateNextSequence("ACCOUNT_SEQ", "ACC");
        String customer1 = sequenceNumberService.generateNextSequence("CUSTOMER_SEQ", "CUS");
        String product1 = sequenceNumberService.generateNextSequence("PRODUCT_SEQ", "PRD");
        
        String account2 = sequenceNumberService.generateNextSequence("ACCOUNT_SEQ", "ACC");
        String customer2 = sequenceNumberService.generateNextSequence("CUSTOMER_SEQ", "CUS");
        String product2 = sequenceNumberService.generateNextSequence("PRODUCT_SEQ", "PRD");

        // Then - Each sequence should be independent
        assertThat(account1).isEqualTo("ACC0000001");
        assertThat(customer1).isEqualTo("CUS0000001");
        assertThat(product1).isEqualTo("PRD0000001");
        
        assertThat(account2).isEqualTo("ACC0000002");
        assertThat(customer2).isEqualTo("CUS0000002");
        assertThat(product2).isEqualTo("PRD0000002");
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify all sequences exist in database with correct values
        Optional<SequenceNumber> accountSeq = sequenceNumberRepository.findBySequenceName("ACCOUNT_SEQ");
        Optional<SequenceNumber> customerSeq = sequenceNumberRepository.findBySequenceName("CUSTOMER_SEQ");
        Optional<SequenceNumber> productSeq = sequenceNumberRepository.findBySequenceName("PRODUCT_SEQ");
        
        assertThat(accountSeq).isPresent();
        assertThat(accountSeq.get().getLastNumber()).isEqualTo(2L);
        assertThat(customerSeq).isPresent();
        assertThat(customerSeq.get().getLastNumber()).isEqualTo(2L);
        assertThat(productSeq).isPresent();
        assertThat(productSeq.get().getLastNumber()).isEqualTo(2L);
    }

    @Test
    @Transactional  
    void shouldHandleSequenceOperationsCorrectly() {
        // Given - Create multiple sequences and perform various operations
        String result1 = sequenceNumberService.generateNextSequence("TEST_SEQ_1", "T1");
        String result2 = sequenceNumberService.generateNextSequence("TEST_SEQ_2", "T2");
        Long number1 = sequenceNumberService.getNextNumber("TEST_SEQ_1");
        
        // When - Get current numbers
        Long current1 = sequenceNumberService.getCurrentNumber("TEST_SEQ_1");
        Long current2 = sequenceNumberService.getCurrentNumber("TEST_SEQ_2");
        
        // Then - Verify all operations work correctly
        assertThat(result1).isEqualTo("T10000001");
        assertThat(result2).isEqualTo("T20000001");
        assertThat(number1).isEqualTo(2L);
        assertThat(current1).isEqualTo(2L);
        assertThat(current2).isEqualTo(1L);
        
        // Force flush to ensure DB is updated
        entityManager.flush();
        
        // Verify in database
        Optional<SequenceNumber> seq1 = sequenceNumberRepository.findBySequenceName("TEST_SEQ_1");
        Optional<SequenceNumber> seq2 = sequenceNumberRepository.findBySequenceName("TEST_SEQ_2");
        
        assertThat(seq1).isPresent();
        assertThat(seq1.get().getLastNumber()).isEqualTo(2L);
        assertThat(seq2).isPresent();
        assertThat(seq2.get().getLastNumber()).isEqualTo(1L);
    }
}