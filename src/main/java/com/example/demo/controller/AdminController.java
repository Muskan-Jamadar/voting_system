package com.example.demo.controller;

import com.example.demo.model.AuditLog;
import com.example.demo.model.Candidate;
import com.example.demo.model.Election;
import com.example.demo.model.User;
import com.example.demo.model.Vote;
import com.example.demo.repository.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class AdminController {

    @Autowired
    private CandidateRepository candidateRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ElectionRepository electionRepo;

    @Autowired
    private VoteRepository voteRepo;

    @Autowired
    private AuditLogRepository auditLogRepo;

    // ================= ADMIN CHECK =================
    private boolean isAdmin(HttpSession session) {

        String username = (String) session.getAttribute("user");
        if (username == null) return false;

        Optional<User> user = userRepo.findByUsername(username);

        return user.isPresent() &&
                "ADMIN".equalsIgnoreCase(user.get().getRole());
    }

    private boolean isElectionActive() {

    	List<Election> active = electionRepo.findByActiveTrue();
    	Election e = active.isEmpty() ? null : active.get(0);

        if (e == null) return false;

        // ✅ check if ended
        if (e.getEndTime() != null &&
            e.getEndTime().isBefore(LocalDateTime.now())) {

            // 🔥 auto deactivate (optional but best)
            e.setActive(false);
            electionRepo.save(e);

            return false;
        }

        return true;
    }

    // ================= DASHBOARD =================
    @GetMapping("/admin")
    public String adminDashboard(HttpSession session, Model model) {

        if (!isAdmin(session)) return "redirect:/home";

        model.addAttribute("totalCandidates", candidateRepo.count());
        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalVotes", voteRepo.count());
        model.addAttribute("logs", auditLogRepo.findAll());

        return "admin/dashboard";
    }

    // ================= AUDIT =================
    @GetMapping("/admin/audit")
    public String auditPage(Model model, HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        model.addAttribute("logs", auditLogRepo.findAll());
        return "admin/audit";
    }

    // ================= GENERATE OLD LOGS =================
    @GetMapping("/admin/generate-old-logs")
    public String generateOldLogs(HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        List<Vote> votes = voteRepo.findAll();

        for (Vote v : votes) {

            if (v.getUser() == null) continue;

            String username = v.getUser().getUsername();
            String details = "Previous vote in " + v.getElectionType();

            boolean exists = auditLogRepo
                    .existsByUsernameAndActionAndDetails(username, "VOTE", details);

            if (exists) continue;

            AuditLog log = new AuditLog();
            log.setUsername(username);
            log.setAction("VOTE");
            log.setDetails(details);
            log.setTimestamp(LocalDateTime.now());

            auditLogRepo.save(log);
        }

        return "redirect:/admin?logsGenerated=true";
    }

    // ================= MANAGE CANDIDATES =================
    @GetMapping("/admin/candidates")
    public String manageCandidates(HttpSession session, Model model) {

        if (!isAdmin(session)) return "redirect:/home";

        List<Candidate> candidates = candidateRepo.findAll();

        Map<Long, Long> voteCountMap = new HashMap<>();

        for (Candidate c : candidates) {
            voteCountMap.put(c.getId(),
                    voteRepo.countByCandidateId(c.getId()));
        }

        model.addAttribute("candidates", candidates);
        model.addAttribute("voteCountMap", voteCountMap);

        return "admin/manage-candidates";
    }

    // ================= ADD PAGE =================
    @GetMapping("/admin/add-candidate")
    public String addCandidatePage(HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        return "admin/addCandidate";
    }

    // ================= SAVE CANDIDATE =================
    @PostMapping("/admin/add-candidate")
    public String saveCandidate(@RequestParam String name,
                                @RequestParam String party,
                                @RequestParam String electionType,
                                @RequestParam String state,
                                @RequestParam String logoFileName,
                                HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        if (isElectionActive()) {
            return "redirect:/admin/candidates?error=add";
        }

        Candidate c = new Candidate();
        c.setName(name);
        c.setParty(party);
        c.setVotes(0);
        c.setElectionType(electionType);
        c.setState(state);

        // ✅ ONLY TEXT (NO FILE UPLOAD)
        c.setPartyLogo(logoFileName);  // e.g. bjp.png

        candidateRepo.save(c);

        return "redirect:/admin/candidates?added=true";
    }
    // ================= DELETE =================
    @GetMapping("/admin/delete-candidate/{id}")
    public String deleteCandidate(@PathVariable Long id,
                                  HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        if (isElectionActive()) {
            return "redirect:/admin/candidates?error=delete";
        }

        candidateRepo.deleteById(id);

        return "redirect:/admin/candidates?deleted=true";
    }

    // ================= EDIT PAGE =================
    @GetMapping("/admin/edit-candidate/{id}")
    public String editCandidatePage(@PathVariable Long id,
                                    HttpSession session,
                                    Model model) {

        if (!isAdmin(session)) return "redirect:/home";

        Candidate candidate = candidateRepo.findById(id).orElse(null);
        model.addAttribute("candidate", candidate);

        return "admin/edit-candidate";
    }

    // ================= UPDATE =================
    @PostMapping("/admin/update-candidate")
    public String updateCandidate(@ModelAttribute Candidate candidate,
                                  HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        if (isElectionActive()) {
            return "redirect:/admin/candidates?error=update";
        }

        candidateRepo.save(candidate);

        return "redirect:/admin/candidates?updated=true";
    }

    // ================= ELECTION SETTINGS =================
    @GetMapping("/admin/election-settings")
    public String electionSettings(HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        return "admin/election-settings";
    }

    @PostMapping("/admin/set-active-election")
    public String setActiveElection(@RequestParam String type,
                                    HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        List<Election> all = electionRepo.findAll();

        for (Election e : all) {
            e.setActive(false);
        }

        electionRepo.saveAll(all);

        Election election = electionRepo.findByTypeIgnoreCase(type);

        if (election != null) {
            election.setActive(true);
            election.setStartTime(LocalDateTime.now());
            electionRepo.save(election);
        }

        return "redirect:/admin/election-settings?success=active";
    }

    @PostMapping("/admin/set-election-time")
    public String setElectionTime(@RequestParam String type,
                                  @RequestParam(required = false) Integer hours,
                                  @RequestParam(required = false) Integer minutes,
                                  @RequestParam(required = false) String endTime,
                                  HttpSession session) {

        if (!isAdmin(session)) return "redirect:/home";

        Election election = electionRepo.findByTypeIgnoreCase(type);

        if (election != null) {

            election.setStartTime(LocalDateTime.now());

            if (hours != null || minutes != null) {

                election.setEndTime(
                        LocalDateTime.now()
                                .plusHours(hours != null ? hours : 0)
                                .plusMinutes(minutes != null ? minutes : 0)
                );

            } else if (endTime != null && !endTime.isEmpty()) {

                election.setEndTime(LocalDateTime.parse(endTime));
            }

            election.setActive(true);
            electionRepo.save(election);
        }

        return "redirect:/admin/election-settings?success=time";
    }

    // ================= USERS =================
    @GetMapping("/admin/users")
    public String usersPage(HttpSession session, Model model) {

        if (!isAdmin(session)) return "redirect:/home";

        model.addAttribute("users", userRepo.findAll());

        return "admin/users";
    }

    // ================= ANALYTICS =================
    @GetMapping("/admin/analytics")
    public String analyticsPage(HttpSession session, Model model) {

        if (!isAdmin(session)) return "redirect:/home";

        List<Candidate> candidates = candidateRepo.findAll();

        Map<String, Long> partyVotes = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<Long> votes = new ArrayList<>();

        for (Candidate c : candidates) {

            long count = voteRepo.countByCandidateId(c.getId());

            names.add(c.getName());
            votes.add(count);

            partyVotes.put(
                    c.getParty(),
                    partyVotes.getOrDefault(c.getParty(), 0L) + count
            );
        }

        int totalVotes = votes.stream().mapToInt(Long::intValue).sum();

        model.addAttribute("partyVotes", partyVotes);
        model.addAttribute("names", names);
        model.addAttribute("votes", votes);
        model.addAttribute("totalVotes", totalVotes);

        return "admin/analytics";
    }
    @PostMapping("/admin/end-election")
    public String endElection(RedirectAttributes redirectAttributes, HttpSession session) {

        String username = (String) session.getAttribute("user");

        if (username == null) return "redirect:/login";

        Optional<User> user = userRepo.findByUsername(username);

        if (user.isEmpty() || !"ADMIN".equalsIgnoreCase(user.get().getRole())) {
            return "redirect:/home";
        }

        List<Election> activeList = electionRepo.findByActiveTrue();
        Election election = activeList.isEmpty() ? null : activeList.get(0);

        if (election != null) {
            election.setActive(false);
            election.setEndTime(LocalDateTime.now());
            electionRepo.save(election);
        }

        redirectAttributes.addFlashAttribute("electionEnded", true);

        return "redirect:/admin/election-settings";
    }
}