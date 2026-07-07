package com.example.demo.repository;

import com.example.demo.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    long countByCandidateId(Long candidateId);

    long countByElectionType(String electionType);

    long countByState(String state);
}