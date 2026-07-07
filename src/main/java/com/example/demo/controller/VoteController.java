package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.*;
import com.example.demo.repository.*;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class VoteController {

    @Autowired
    private CandidateRepository candidateRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ElectionRepository electionRepo;

    @Autowired
    private VoteRepository voteRepo;

    @Autowired
    private AuditLogRepository auditRepo;

    // ================= SHOW VOTE PAGE =================
    @GetMapping("/vote")
    public String showVotePage(Model model, HttpSession session) {

        String username = (String) session.getAttribute("user");

        if (username == null) {
            return "redirect:/login";
        }

        Optional<User> optionalUser = userRepo.findByUsername(username);

        if (optionalUser.isEmpty()) {
            session.invalidate();
            return "redirect:/login";
        }

        User user = optionalUser.get();

        // ✅ FIXED ACTIVE ELECTION HANDLING
        List<Election> activeElections = electionRepo.findByActiveTrue();
        Election election = activeElections.isEmpty() ? null : activeElections.get(0);

        if (election == null) {
            model.addAttribute("error", "No active election available");
            model.addAttribute("user", user);
            model.addAttribute("candidates", List.of());
            return "vote";
        }

        String type = election.getType();
        String userState = user.getState();

        if (userState == null || userState.isBlank()) {
            model.addAttribute("error", "Your state is not set");
            model.addAttribute("user", user);
            model.addAttribute("candidates", List.of());
            return "vote";
        }

        List<Candidate> filteredCandidates = candidateRepo.findAll()
                .stream()
                .filter(c -> c.getElectionType() != null &&
                        c.getElectionType().trim().equalsIgnoreCase(type.trim()))
                .filter(c -> {

                    if (c.getState() == null) return false;

                    if (type.equalsIgnoreCase("Assembly")) {
                        return c.getState().trim().equalsIgnoreCase(userState.trim());
                    }

                    return c.getState().trim().equalsIgnoreCase("India")
                            || c.getState().trim().equalsIgnoreCase(userState.trim());
                })
                .toList();

        model.addAttribute("candidates", filteredCandidates);
        model.addAttribute("user", user);
        model.addAttribute("electionType", type);

        if (election.getEndTime() != null) {
            model.addAttribute("endTime", election.getEndTime().toString());
        }

        model.addAttribute("electionEnded",
                election.getEndTime() != null &&
                        election.getEndTime().isBefore(LocalDateTime.now()));

        return "vote";
    }

    // ================= VOTE AJAX =================
    @PostMapping("/vote/{id}")
    @ResponseBody
    public String voteAjax(@PathVariable Long id, HttpSession session) {

        String username = (String) session.getAttribute("user");
        if (username == null) return "error";

        User user = userRepo.findByUsername(username).orElse(null);
        if (user == null) return "error";

        Candidate candidate = candidateRepo.findById(id).orElse(null);

        // ✅ FIXED ACTIVE ELECTION HANDLING
        List<Election> activeElections = electionRepo.findByActiveTrue();
        Election election = activeElections.isEmpty() ? null : activeElections.get(0);

        if (candidate == null || election == null) return "error";

        if (election.getEndTime() != null &&
                election.getEndTime().isBefore(LocalDateTime.now())) {
            return "ended";
        }

        String type = election.getType();

        if (user.getVotedTypes() == null) {
            user.setVotedTypes(new HashSet<>());
        }

        if (!candidate.getElectionType().equalsIgnoreCase(type)) {
            return "invalid";
        }

        if (user.getVotedTypes().contains(type)) {
            return "alreadyVoted";
        }

        if (type.equalsIgnoreCase("State")) {
            if (user.getState() == null ||
                    !candidate.getState().equalsIgnoreCase(user.getState())) {
                return "invalidState";
            }
        }

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setCandidateId(candidate.getId());
        vote.setElectionType(type);
        vote.setState(user.getState());

        voteRepo.save(vote);

        user.getVotedTypes().add(type);
        userRepo.save(user);

        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction("VOTE");
        log.setDetails("User cast vote in " + type + " election");
        log.setTimestamp(LocalDateTime.now());

        auditRepo.save(log);

        return "success";
    }
}