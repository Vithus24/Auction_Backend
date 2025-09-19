package Auction.Auction.service;

import Auction.Auction.entity.*;
import Auction.Auction.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private PlayerAllocationRepository allocationRepository;


    @Autowired
    private UserRepository userRepository;

    public List<Auction> findAll() {
        return auctionRepository.findAll();
    }

    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id);
    }

    public Auction createAuction(Auction auction, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
        auction.setAdmin(admin);
        return auctionRepository.save(auction);
    }

    public Auction update(Long id, Auction updatedAuction, Long adminId) {
        Optional<Auction> existingAuction = auctionRepository.findById(id);
        if (existingAuction.isPresent()) {
            Auction auction = existingAuction.get();
            auction.setAuctionName(updatedAuction.getAuctionName());
            auction.setAuctionDate(updatedAuction.getAuctionDate());
            auction.setTypeOfSport(updatedAuction.getTypeOfSport());
            auction.setBidIncreaseBy(updatedAuction.getBidIncreaseBy());
            auction.setMinimumBid(updatedAuction.getMinimumBid());
            auction.setPointsPerTeam(updatedAuction.getPointsPerTeam());
            auction.setPlayerPerTeam(updatedAuction.getPlayerPerTeam());
            auction.setStatus(updatedAuction.getStatus());
            if (adminId != null) {
                User admin = userRepository.findById(adminId)
                        .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
                auction.setAdmin(admin);
            }
            return auctionRepository.save(auction);
        } else {
            throw new RuntimeException("Auction not found with id: " + id);
        }
    }

    public void delete(Long id) {
        auctionRepository.deleteById(id);
    }

    public List<Auction> findByAdminId(Long adminId) {
        return auctionRepository.findByAdminId(adminId);
    }

    public Bid placeBid(Long playerId, Long teamId, double amount) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (team.getBudget() < amount || player.isSold()) {
            throw new RuntimeException("Invalid bid: Insufficient budget or player already sold");
        }
        Bid bid = new Bid();
        bid.setPlayer(player);
        bid.setTeam(team);
        bid.setBidAmount(amount);
        bid.setTimestamp(LocalDateTime.now());
        return bidRepository.save(bid);
    }

    public void allocatePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        if (player.isSold()) {
            return;
        }

        List<Bid> bids = bidRepository.findByPlayer(player);
        if (bids.isEmpty()) {
            return;
        }

        Bid highestBid = bids.stream()
                .max((b1, b2) -> Double.compare(b1.getBidAmount(), b2.getBidAmount()))
                .get();
        Team winningTeam = highestBid.getTeam();
        winningTeam.setBudget(winningTeam.getBudget() - highestBid.getBidAmount());
        teamRepository.save(winningTeam);

        PlayerAllocation allocation = new PlayerAllocation();
        allocation.setPlayer(player);
        allocation.setTeam(winningTeam);
        allocation.setFinalPrice(highestBid.getBidAmount());
        allocationRepository.save(allocation);

        player.setSold(true);
        playerRepository.save(player);
    }
}