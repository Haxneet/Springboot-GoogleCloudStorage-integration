package com.example.document_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.document_management.entity.DocumentVersion;

import java.util.List;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
    List<DocumentVersion> findByDocumentId(Long documentId);
}
