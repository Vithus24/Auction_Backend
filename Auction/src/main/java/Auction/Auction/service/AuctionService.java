package Auction.Auction.service;

import Auction.Auction.entity.Auction;
import Auction.Auction.entity.Bid;
import Auction.Auction.entity.Player;
import Auction.Auction.entity.PlayerAllocation;
import Auction.Auction.entity.Team;
import Auction.Auction.repository.AuctionRepository;
import Auction.Auction.repository.BidRepository;
import Auction.Auction.repository.PlayerAllocationRepository;
import Auction.Auction.repository.PlayerRepository;
import Auction.Auction.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {

    @Autowired private AuctionRepository auctionRepository;
    @Autowired private PlayerRepository playerRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private BidRepository bidRepository;
    @Autowired private PlayerAllocationRepository allocationRepository;
    @Autowired private AuctionWheelService wheelService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    private int currentRound = 0; // Track the current bidding round

    // ---------------- Auction CRUD ----------------

    public List<Auction> findAll() {
        return auctionRepository.findAll();
    }

    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id);
    }

    public Auction createAuction(Auction auction) {
        Auction savedAuction = auctionRepository.save(auction);

        // Reset wheel + rounds for a new auction
        currentRound = 0;
        wheelService.resetRounds();

        return savedAuction;
    }

    public Auction update(Long id, Auction updatedAuction) {
        return auctionRepository.findById(id)
                .map(existing -> {
                    existing.setAuctionDate(updatedAuction.getAuctionDate());
                    existing.setStatus(updatedAuction.getStatus());
                    return auctionRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Auction not found with id: " + id));
    }

    public void delete(Long id) {
        auctionRepository.deleteById(id);
    }
    public List<Auction> findByAdminId(Long adminId) {
        return auctionRepository.findByAdminId(adminId);
    }
    // ---------------- Bidding ----------------

    public Bid placeBid(Long playerId, Long teamId, double amount) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        if (team.getBudget() < amount) {
            throw new RuntimeException("Invalid bid: Insufficient budget");
        }
        if (player.isSold()) {
            throw new RuntimeException("Invalid bid: Player already sold");
        }

        Bid bid = new Bid();
        bid.setPlayer(player);
        bid.setTeam(team);
        bid.setBidAmount(amount);
        bid.setTimestamp(LocalDateTime.now());

        Bid savedBid = bidRepository.save(bid);

        // ✅ Broadcast bid to all connected clients
        messagingTemplate.convertAndSend("/topic/bids", savedBid);

        return savedBid;
    }

    // ---------------- Allocation ----------------

    public void allocatePlayer(Long playerId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));

        if (player.isSold()) {
            return; // Already sold
        }

        List<Bid> bids = bidRepository.findByPlayer(player);
        if (bids.isEmpty()) {
            return; // No bids placed
        }

        // Find the highest bid
        Bid highestBid = bids.stream()
                .max((b1, b2) -> Double.compare(b1.getBidAmount(), b2.getBidAmount()))
                .get();

        Team winningTeam = highestBid.getTeam();
        double finalPrice = highestBid.getBidAmount();

        // Deduct budget
        winningTeam.setBudget(winningTeam.getBudget() - finalPrice);
        teamRepository.save(winningTeam);

        // Save allocation
        PlayerAllocation allocation = new PlayerAllocation();
        allocation.setPlayer(player);
        allocation.setTeam(winningTeam);
        allocation.setFinalPrice(finalPrice);
        allocationRepository.save(allocation);

        // Mark player as sold
        player.setSold(true);
        playerRepository.save(player);

        // ✅ Broadcast allocation
        messagingTemplate.convertAndSend("/topic/allocations", allocation);

        // Move to next round
        currentRound++;
        wheelService.startNewRound();
    }

    // ---------------- Wheel Selection ----------------

    public Player startWheelSelection() {
        Player selectedPlayer = wheelService.selectNextPlayer(currentRound + 1);

        if (selectedPlayer != null) {
            // ✅ Broadcast new selected player
            messagingTemplate.convertAndSend("/topic/current-player", selectedPlayer);
        }

        return selectedPlayer;
    }

    public List<Player> getAvailablePlayersForWheel() {
        return wheelService.getAvailablePlayers(currentRound + 1);
    }
}
