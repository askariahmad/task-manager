package com.chatbot.whatsapp.service;

import com.chatbot.whatsapp.model.Counter;
import com.chatbot.whatsapp.repository.CounterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import java.util.List; // Added import
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class CounterService {

    private final CounterRepository counterRepository;

    @Autowired
    public CounterService(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    public String createCounter(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Counter name cannot be empty.";
        }
        String counterName = name.trim();
        if (counterRepository.existsByNameIgnoreCase(counterName)) {
            Counter existingCounter = counterRepository.findByNameIgnoreCase(counterName).get(); // Should be present if exists
            return "Counter '" + existingCounter.getName() + "' already exists with value: " + existingCounter.getValue();
        }
        try {
            Counter newCounter = new Counter(counterName);
            counterRepository.save(newCounter);
            return "Counter '" + newCounter.getName() + "' created with value 0.";
        } catch (DuplicateKeyException e) { // Should be caught by existsByNameIgnoreCase, but as a safeguard
            return "Counter '" + counterName + "' already exists.";
        }
    }

    public String incrementCounter(String name) {
        if (name == null || name.trim().isEmpty()) return "Counter name cannot be empty.";
        Optional<Counter> counterOpt = counterRepository.findByNameIgnoreCase(name.trim());
        if (counterOpt.isPresent()) {
            Counter counter = counterOpt.get();
            counter.increment();
            counterRepository.save(counter);
            return "Counter '" + counter.getName() + "' incremented. New value: " + counter.getValue();
        }
        return "Counter '" + name.trim() + "' not found. Create it first using 'create counter " + name.trim() + "'.";
    }

    public String decrementCounter(String name) {
        if (name == null || name.trim().isEmpty()) return "Counter name cannot be empty.";
        Optional<Counter> counterOpt = counterRepository.findByNameIgnoreCase(name.trim());
        if (counterOpt.isPresent()) {
            Counter counter = counterOpt.get();
            counter.decrement();
            counterRepository.save(counter);
            return "Counter '" + counter.getName() + "' decremented. New value: " + counter.getValue();
        }
        return "Counter '" + name.trim() + "' not found.";
    }

    public String getCounterValue(String name) {
        if (name == null || name.trim().isEmpty()) return "Counter name cannot be empty.";
        Optional<Counter> counterOpt = counterRepository.findByNameIgnoreCase(name.trim());
        if (counterOpt.isPresent()) {
            Counter counter = counterOpt.get();
            return "Counter '" + counter.getName() + "': " + counter.getValue();
        }
        return "Counter '" + name.trim() + "' not found.";
    }

    public String deleteCounter(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Counter name cannot be empty for deletion.";
        }
        String counterName = name.trim();
        // existsByNameIgnoreCase returns boolean, so we use it directly
        if (counterRepository.existsByNameIgnoreCase(counterName)) {
            // Need to fetch the actual name if we want to return it, or just use the input name
            Optional<Counter> counterOpt = counterRepository.findByNameIgnoreCase(counterName);
            String actualName = counterOpt.map(Counter::getName).orElse(counterName); // Get actual casing if possible
            counterRepository.deleteByNameIgnoreCase(counterName); // MongoDB driver handles case-insensitive delete if defined in repo
            return "Counter '" + actualName + "' deleted.";
        }
        return "Counter '" + counterName + "' not found.";
    }
    
    public String listCounters() {
        List<Counter> counters = counterRepository.findAll();
        if (counters.isEmpty()) {
            return "No counters available.";
        }
        return "Available counters:\n" +
                counters.stream()
                        .map(counter -> "- " + counter.getName() + ": " + counter.getValue())
                        .collect(Collectors.joining("\n"));
    }
}
