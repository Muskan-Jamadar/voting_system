package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vote")
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "candidate_id")
    private Long candidateId;

    @Column(name = "election_type")
    private String electionType;

    @Column(name = "state")
    private String state;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setId(Long id) {
		this.id = id;
	}

	// ✅ REQUIRED empty constructor
    public Vote() {}

    // ✅ FIXED constructor
    public Vote(Long candidateId, String electionType, String state) {
        this.candidateId = candidateId;
        this.electionType = electionType;
        this.state = state;
    }

    // ✅ getters & setters
    public Long getId() { return id; }

    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }

    public String getElectionType() { return electionType; }
    public void setElectionType(String electionType) { this.electionType = electionType; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}