package com.example.document_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.document_management.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Document findByName(String name);
}
