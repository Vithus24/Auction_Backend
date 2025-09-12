package Auction.Auction.service;

import Auction.Auction.entity.Team;
import Auction.Auction.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    @Autowired
    private TeamRepository teamRepository;

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    public Team save(Team team) {
        if (team.getAuction() != null && team.getAuction().getId() != null && team.getName() != null) {
            Optional<Team> existingTeam = teamRepository.findByAuctionIdAndName(team.getAuction().getId(), team.getName());
            if (existingTeam.isPresent()) {
                throw new RuntimeException("Team with name " + team.getName() + " already exists in auction " + team.getAuction().getId());
            }
        }
        return teamRepository.save(team);
    }

    public Team update(Long id, Team updatedTeam) {
        Optional<Team> existingTeam = teamRepository.findById(id);
        if (existingTeam.isPresent()) {
            Team team = existingTeam.get();
            team.setName(updatedTeam.getName());
            team.setBudget(updatedTeam.getBudget());
            team.setOwner(updatedTeam.getOwner());
            team.setOwnerMail(updatedTeam.getOwnerMail());
            team.setAuction(updatedTeam.getAuction());
            return teamRepository.save(team);
        } else {
            throw new RuntimeException("Team not found with id: " + id);
        }
    }

    public void delete(Long id) {
        teamRepository.deleteById(id);
    }
}