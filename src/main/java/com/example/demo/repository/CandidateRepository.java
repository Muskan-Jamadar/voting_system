package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Candidate;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByElectionTypeIgnoreCase(String electionType);

    List<Candidate> findByState(String state);

    // ADD THIS
    List<Candidate> findByStateIgnoreCaseAndElectionTypeIgnoreCase(
            String state,
            String electionType
    );
}