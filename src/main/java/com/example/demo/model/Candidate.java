//package com.example.demo.model;
//
//import jakarta.persistence.*;
//
//@Entity
//public class Candidate {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//    private int votes;
//    private String party;
//
//    private String electionType;
//
//    // getters & setters
//    public Long getId() { return id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public int getVotes() { return votes; }
//    public void setVotes(int votes) { this.votes = votes; }
//
//    // ✅ ADD THIS (VERY IMPORTANT)
//    public String getParty() { return party; }
//    public void setParty(String party) { this.party = party; }
//
//    public String getElectionType() { return electionType; }
//    public void setElectionType(String electionType) { this.electionType = electionType; }
//}



//package com.example.demo.model;
//
//import jakarta.persistence.*;
//
//@Entity
//public class Candidate {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String name;
//    private int votes;
//    private String party;
//    private String electionType;
//
//    private String partyLogo; // ✅ NEW FIELD
//
//    // getters & setters
//    public Long getId() { return id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public int getVotes() { return votes; }
//    public void setVotes(int votes) { this.votes = votes; }
//
//    public String getParty() { return party; }
//    public void setParty(String party) { this.party = party; }
//
//    public String getElectionType() { return electionType; }
//    public void setElectionType(String electionType) { this.electionType = electionType; }
//
//    // ✅ NEW GETTER & SETTER
//    public String getPartyLogo() { return partyLogo; }
//    public void setPartyLogo(String partyLogo) { this.partyLogo = partyLogo; }
//}

package com.example.demo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "candidate") // ✅ good practice
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "votes")
    private int votes = 0;   // ✅ default value

    private String party;

    private String electionType;

    @Column(name = "party_logo")
    private String partyLogo;
    private String state;

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getElectionType() {
        return electionType;
    }

    public void setElectionType(String electionType) {
        this.electionType = electionType;
    }

    public String getPartyLogo() {
        return partyLogo;
    }

    public void setPartyLogo(String partyLogo) {
        this.partyLogo = partyLogo;
    }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}