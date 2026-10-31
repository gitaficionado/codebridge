package com.codebridge.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "existing_repo_url", nullable = false)
    private String existingRepoUrl;

    @Column(name = "incoming_repo_url", nullable = false)
    private String incomingRepoUrl;

    public Project() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExistingRepoUrl() {
        return existingRepoUrl;
    }

    public void setExistingRepoUrl(String existingRepoUrl) {
        this.existingRepoUrl = existingRepoUrl;
    }

    public String getIncomingRepoUrl() {
        return incomingRepoUrl;
    }

    public void setIncomingRepoUrl(String incomingRepoUrl) {
        this.incomingRepoUrl = incomingRepoUrl;
    }
}
