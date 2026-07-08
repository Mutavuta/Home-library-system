package com.library.librarybackend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

// Runs once on startup - reads the service account key and opens the
// connection to Firebase. All repositories get the Firestore bean from here
@Configuration
public class FirebaseConfig {

    // Path to firebase-service-account.json - set in application.properties
    @Value("${firebase.credentials.path}")
    private String credentialsPath;

    // Firebase project URL - set in application.properties
    @Value("${firebase.database.url}")
    private String databaseUrl;

    // Initializes Firebase using the service account kay
    // All other Firebase beans depend on this one being ready first
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // Only initialize once - prevents errors if the app context ever reloads
        if(FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount;
            try {
                // Look for the json file in the project root (where pom.xml lives)
                serviceAccount = new FileInputStream(credentialsPath);
            } catch (Exception e) {
                // Fallback - look inside scr/main/resources instead
                serviceAccount = getClass().getClassLoader()
                        .getResourceAsStream(credentialsPath);
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();

            return FirebaseApp.initializeApp(options);
        }

        // Already initialized - just return the existing  instance
        return FirebaseApp.getInstance();
    }

    // Exposes the Firestore database client as a bean
    // Repositories @Autowired this to read and write documents
    // Takes firebaseApp as a parameter to ensure Firebase is ready before Firestore opens
    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

}
