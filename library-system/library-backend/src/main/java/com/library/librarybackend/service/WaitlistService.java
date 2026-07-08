package com.library.librarybackend.service;

import com.library.librarybackend.model.WaitlistEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    // In-memory waitlist - replaced with Firebase later
    private List<WaitlistEntry> waitlist = new ArrayList<>();

    // Returns all waitlist entries for one title
    // Sorted by position so position ! is always first
    public List<WaitlistEntry> getWaitlistForTitle(String titleId) {
        return waitlist.stream()
                .filter(e -> e.getTitleId().equals(titleId))
                .filter(e -> e.getStatus().equals("waiting"))
                .sorted(Comparator.comparingInt(WaitlistEntry::getPosition))
                .collect(Collectors.toList());
    }

    // How many people are currently waiting for a title
    // Used to set the position of new entry: count + 1
    public int countWaiting(String titleId) {
        // Used (int) to avoid warning as count() returns long type
        return (int) waitlist.stream()
                .filter(e -> e.getTitleId().equals(titleId))
                .filter(e -> e.getStatus().equals("waiting"))
                .count();
    }

    // Adds a new person to the waitlist
    // Position is automatically set based on how many are already waiting
    public WaitlistEntry addToWaitlist(String userId, String titleId) {
        WaitlistEntry entry = new WaitlistEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setUserId(userId);
        entry.setTitleId(titleId);
        entry.setRequestDate(LocalDate.now().toString());
        // Position = current count + 1 - they go to the back of the line
        entry.setPosition(countWaiting(titleId) + 1);
        entry.setStatus("waiting");
        waitlist.add(entry);
        return entry;
    }

    // Gets the first person in the queue for a title
    // Called when a copy becomes available
    // Returns Optional - empty if nobody is waiting
    public Optional<WaitlistEntry> getFirstWaiting(String titleId) {
        return waitlist.stream()
                .filter(e -> e.getTitleId().equals(titleId))
                .filter(e -> e.getStatus().equals("waiting"))
                .min(Comparator.comparingInt(WaitlistEntry::getPosition));
    }

    // Marks a waitlist entry as notified
    // Called after sending the "book available" notification
    public void markNotified(String entryId) {
        waitlist.stream()
                .filter(e -> e.getId().equals(entryId))
                .findFirst()
                .ifPresent(e -> e.setStatus("notified"));
    }

    // Checks if a user is already on the waitlist for a title
    // Prevents the same person from  joining twice
    public boolean isAlreadyWaiting(String userId, String titleId) {
        return waitlist.stream()
                .anyMatch(e -> e.getUserId().equals(userId)
                && e.getTitleId().equals(titleId)
                && e.getStatus().equals("waiting"));
    }

    // Returns all waitlist entries for user
    // Used for the website to show a user their waitlist positions
    public List<WaitlistEntry> getWaitlistForUser(String userId) {
        return waitlist.stream()
                .filter(e -> e.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

}
