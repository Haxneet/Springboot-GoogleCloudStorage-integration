package com.example.document_management.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class DocumentService {

    private final Storage storage;
    private final String bucketName = "bucket-for-dms"; // Replace with your actual bucket name

    public DocumentService() throws IOException {
        // Initializes the Google Cloud Storage client with default credentials
        this.storage = StorageOptions.newBuilder()
                                     .setCredentials(GoogleCredentials.getApplicationDefault())
                                     .build()
                                     .getService();
    }

    public String uploadDocument(MultipartFile file, String documentName) throws IOException {
        String blobName = documentName; // Use the provided document name as the blob name
        BlobId blobId = BlobId.of(bucketName, blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try (var inputStream = file.getInputStream()) {
            // Upload the file to Google Cloud Storage
            Blob blob = storage.create(blobInfo, inputStream.readAllBytes());
            return blob.getMediaLink(); // Returns the URL to the uploaded file
        } catch (Exception e) {
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }
    }
}
