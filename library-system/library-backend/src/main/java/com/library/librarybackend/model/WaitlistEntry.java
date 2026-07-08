package com.library.librarybackend.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaitlistEntry {

    // Firebase document ID
    private String id;

    // Who is waiting
    private String userId;

    // Which book title they are waiting for
    private String titleId;

    // When  they joined the queue
    private String requestDate;

    // Their place in line
    // 1 = first - gets notified when a copy is available
    // Set as: count of current waiters + 1
    private int position;

    // Status of this waitlist entry
    // "waiting" = still in the queue
    // "notified" = told that a copy is available
    // "expired" = did not act on the notification in time
    private String status;

}
