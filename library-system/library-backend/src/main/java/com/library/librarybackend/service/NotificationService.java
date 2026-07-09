package com.library.librarybackend.service;

import com.library.librarybackend.model.Notification;
import com.library.librarybackend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

// Creates and retrieves notifications for borrowers
// Other services call the notify methods here instead of building notifications themselves
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;
    

}
