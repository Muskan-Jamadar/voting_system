package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.EmailService;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class AuthController {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserRepository userRepository;

    // ================= OTP STORE =================
    class OtpData {
        String otp;
        long expiryTime;
        int attempts;

        public OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
            this.attempts = 0;
        }
    }

    private Map<String, OtpData> otpStore = new ConcurrentHashMap<>();

    // ================= REGISTER PAGE =================
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    // ================= SEND OTP =================
    @GetMapping("/send-otp")
    @ResponseBody
    public String sendOtp(@RequestParam String email) {

        SecureRandom random = new SecureRandom();
        String otp = String.valueOf(100000 + random.nextInt(900000));

        long expiry = System.currentTimeMillis() + (2 * 60 * 1000);

        otpStore.put(email, new OtpData(otp, expiry));

        emailService.sendOtp(email, otp);

        System.out.println("OTP sent to: " + email + " OTP: " + otp);

        return "OTP_SENT";
    }

    // ================= REGISTER USER =================
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
                               BindingResult result,
                               @RequestParam String otp,
                               Model model) {

        if (result.hasErrors()) {
            model.addAttribute("error",
                    result.getAllErrors().get(0).getDefaultMessage());
            return "register";
        }

        user.setEmail(user.getEmail().toLowerCase().trim());

        if (repo.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username already exists!");
            return "register";
        }

        OtpData data = otpStore.get(user.getEmail());

        if (data == null) {
            model.addAttribute("error", "Please request OTP first!");
            return "register";
        }

        if (System.currentTimeMillis() > data.expiryTime) {
            otpStore.remove(user.getEmail());
            model.addAttribute("error", "OTP expired!");
            return "register";
        }

        data.attempts++;

        if (data.attempts > 3) {
            otpStore.remove(user.getEmail());
            model.addAttribute("error", "Too many attempts!");
            return "register";
        }

        if (!data.otp.equals(otp)) {
            model.addAttribute("error", "Invalid OTP!");
            return "register";
        }

        otpStore.remove(user.getEmail());

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("VOTER");
        user.setVerified(true);

        repo.save(user);

        return "redirect:/login";
    }
    // ================= LOGIN PAGE =================
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login-otp")
    @ResponseBody
    public String loginOtp(@RequestBody Map<String, String> data,
                           HttpSession session) {

        String username = data.get("username");
        String password = data.get("password");

        Optional<User> userOpt = repo.findByUsername(username);

        if (userOpt.isEmpty()) {
            return "ERROR:Invalid credentials";
        }

        User user = userOpt.get();

        // check password
        if (!encoder.matches(password, user.getPassword())) {
            return "ERROR:Invalid credentials";
        }

        // check verification
        if (!user.isVerified()) {
            return "ERROR:Account not verified";
        }

        // generate OTP
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        long expiry = System.currentTimeMillis() + (2 * 60 * 1000);

        otpStore.put(username, new OtpData(otp, expiry));

        emailService.sendOtp(user.getEmail(), otp);

        // optional debug
        System.out.println("OTP sent for user: " + username);

        return "OTP_SENT:" + expiry;
    }
    @PostMapping("/verify-login-otp")
    @ResponseBody
    public String verifyLoginOtp(@RequestBody Map<String, String> data,
                                 HttpSession session) {

        String username = data.get("username");
        String otp = data.get("otp");

        OtpData stored = otpStore.get(username);

        if (stored == null) return "ERROR:Request OTP first";

        if (System.currentTimeMillis() > stored.expiryTime) {
            otpStore.remove(username);
            return "ERROR:OTP expired";
        }

        if (!stored.otp.equals(otp)) return "ERROR:Invalid OTP";

        otpStore.remove(username);

        Optional<User> userOpt = repo.findByUsername(username);

        if (userOpt.isEmpty()) return "ERROR:User not found";

        User user = userOpt.get();

        List<SimpleGrantedAuthority> auth =
                List.of(new SimpleGrantedAuthority(user.getRole()));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        null,
                        auth
                );

        // 🔥 SET SECURITY CONTEXT
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 🔥 IMPORTANT: STORE IN SESSION (THIS FIXES 403)
        session.setAttribute("SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.getContext());

        // SESSION DATA
        session.setAttribute("user", username);
        session.setAttribute("role", user.getRole());

        return "SUCCESS:" + user.getRole();
    }
    
    // ================= HOME =================

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {

        String username = (String) session.getAttribute("user");

        if (username == null) {
            return "redirect:/login";
        }

        // FIXED OPTIONAL ERROR
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        // example election type
        model.addAttribute("electionType", "Assembly");

        return "home";
    }
    @GetMapping({"/", "/landing"})
    public String landingPage() {
        return "landing"; // because your file is index.html
    }

    // ================= CLEAN OTP =================
    @Scheduled(fixedRate = 60000)
    public void cleanOtp() {
        long now = System.currentTimeMillis();

        otpStore.entrySet().removeIf(e -> e.getValue().expiryTime < now);
    }
}