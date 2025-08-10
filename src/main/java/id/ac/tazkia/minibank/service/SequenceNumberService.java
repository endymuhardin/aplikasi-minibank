package id.ac.tazkia.minibank.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import id.ac.tazkia.minibank.entity.SequenceNumber;
import id.ac.tazkia.minibank.repository.SequenceNumberRepository;

@Service
@Transactional
public class SequenceNumberService {
    
    private final SequenceNumberRepository sequenceNumberRepository;
    
    public SequenceNumberService(SequenceNumberRepository sequenceNumberRepository) {
        this.sequenceNumberRepository = sequenceNumberRepository;
    }
    
    public String generateNextSequence(String sequenceName, String prefix) {
        SequenceNumber sequence = getOrCreateSequence(sequenceName, prefix);
        String result = sequence.generateNextSequence();
        sequenceNumberRepository.save(sequence);
        return result;
    }
    
    public String generateNextSequence(String sequenceName) {
        return generateNextSequence(sequenceName, null);
    }
    
    public Long getNextNumber(String sequenceName, String prefix) {
        SequenceNumber sequence = getOrCreateSequence(sequenceName, prefix);
        Long result = sequence.getNextNumber();
        sequenceNumberRepository.save(sequence);
        return result;
    }
    
    public Long getNextNumber(String sequenceName) {
        return getNextNumber(sequenceName, null);
    }
    
    public void resetSequence(String sequenceName, Long startNumber) {
        Optional<SequenceNumber> sequenceOpt = sequenceNumberRepository.findBySequenceName(sequenceName);
        if (sequenceOpt.isPresent()) {
            SequenceNumber sequence = sequenceOpt.get();
            sequence.resetSequence(startNumber);
            sequenceNumberRepository.save(sequence);
        }
    }
    
    public Long getCurrentNumber(String sequenceName) {
        Optional<SequenceNumber> sequenceOpt = sequenceNumberRepository.findBySequenceName(sequenceName);
        return sequenceOpt.map(SequenceNumber::getLastNumber).orElse(0L);
    }
    
    private SequenceNumber getOrCreateSequence(String sequenceName, String prefix) {
        Optional<SequenceNumber> sequenceOpt = sequenceNumberRepository.findBySequenceName(sequenceName);
        
        if (sequenceOpt.isPresent()) {
            return sequenceOpt.get();
        } else {
            SequenceNumber sequence = new SequenceNumber();
            sequence.setSequenceName(sequenceName);
            sequence.setPrefix(prefix);
            sequence.setLastNumber(0L);
            return sequenceNumberRepository.save(sequence);
        }
    }
}