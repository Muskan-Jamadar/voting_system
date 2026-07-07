package com.example.demo.controller;

import com.example.demo.model.Candidate;
import com.example.demo.model.Election;
import com.example.demo.repository.CandidateRepository;
import com.example.demo.repository.ElectionRepository;
import com.example.demo.repository.VoteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ResultController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private VoteRepository voteRepo;

    @Autowired
    private ElectionRepository electionRepo;

    @GetMapping("/result/{state}")
    public String resultsByState(@PathVariable String state,
                                 @RequestParam String type,
                                 Model model) {

        // FIND ELECTION

        Election election = electionRepo.findByTypeIgnoreCase(type);

        // GET CANDIDATES

        List<Candidate> candidates =
                candidateRepository
                        .findByStateIgnoreCaseAndElectionTypeIgnoreCase(
                                state.trim(),
                                type.trim()
                        );

        // VOTE COUNT MAP

        Map<Long, Long> voteCountMap = new HashMap<>();

        for (Candidate candidate : candidates) {

            long votes = voteRepo.countByCandidateId(candidate.getId());

            voteCountMap.put(candidate.getId(), votes);
        }

        // TOTAL VOTES

        long totalVotes = voteCountMap.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();

        // FIND WINNER

        Candidate winner = null;

        if (!candidates.isEmpty()) {

            winner = candidates.stream()
                    .max(Comparator.comparing(
                            c -> voteCountMap.getOrDefault(c.getId(), 0L)
                    ))
                    .orElse(null);
        }

        // WINNER PERCENTAGE

        double winnerPercentage = 0.0;

        if (winner != null && totalVotes > 0) {

            winnerPercentage =
                    (voteCountMap.getOrDefault(winner.getId(), 0L)
                            * 100.0) / totalVotes;
        }

        // SEND DATA TO HTML

        model.addAttribute("state", state);

        model.addAttribute("type", type);

        model.addAttribute("candidates", candidates);

        model.addAttribute("voteCountMap", voteCountMap);

        model.addAttribute("totalVotes", totalVotes);

        model.addAttribute("winner", winner);

        model.addAttribute("winnerPercentage",
                String.format("%.1f", winnerPercentage));

        // OPTIONAL STATUS

        if (election != null) {

            model.addAttribute("electionStatus",
                    election.isActive() ? "Active" : "Ended");
        }

        return "result";
    }
}