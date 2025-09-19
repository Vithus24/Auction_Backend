package Auction.Auction.service;

import Auction.Auction.entity.Bid;
import Auction.Auction.entity.Player;
import Auction.Auction.entity.PlayerAllocation;
import Auction.Auction.entity.Team;
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
public class BidService {
    @Autowired
    private BidRepository bidRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private AuctionWheelService wheelService;
    @Autowired
    private PlayerAllocationRepository allocationRepository;

    private int currentRound = 0;

    public List<Bid> findAll() {
        return bidRepository.findAll();
    }

    public Optional<Bid> findById(Long id) {
        return bidRepository.findById(id);
    }

    public Bid save(Bid bid) {
        // Validate player and team
        Player player = playerRepository.findById(bid.getPlayer().getId())
                .orElseThrow(() -> new RuntimeException("Player not found"));
        Team team = teamRepository.findById(bid.getTeam().getId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
        if (team.getBudget() < bid.getBidAmount() || player.isSold()) {
            throw new RuntimeException("Invalid bid: Insufficient budget or player already sold");
        }

        Bid savedBid = bidRepository.save(bid);

        // Broadcast the new bid via WebSocket
        messagingTemplate.convertAndSend("/topic/bids", savedBid);

        return savedBid;
    }

    public Bid update(Long id, Bid updatedBid) {
        Optional<Bid> existingBid = bidRepository.findById(id);
        if (existingBid.isPresent()) {
            Bid bid = existingBid.get();
            // Validate updated player and team
            Player player = playerRepository.findById(updatedBid.getPlayer().getId())
                    .orElseThrow(() -> new RuntimeException("Player not found"));
            Team team = teamRepository.findById(updatedBid.getTeam().getId())
                    .orElseThrow(() -> new RuntimeException("Team not found"));
            if (team.getBudget() < updatedBid.getBidAmount() || player.isSold()) {
                throw new RuntimeException("Invalid bid update: Insufficient budget or player already sold");
            }
            bid.setPlayer(updatedBid.getPlayer());
            bid.setTeam(updatedBid.getTeam());
            bid.setBidAmount(updatedBid.getBidAmount());
            bid.setTimestamp(updatedBid.getTimestamp());
            Bid savedBid = bidRepository.save(bid);
            // Broadcast updated bid
            messagingTemplate.convertAndSend("/topic/bids", savedBid);
            return savedBid;
        } else {
            throw new RuntimeException("Bid not found with id: " + id);
        }
    }
    public void delete(Long id) {
        bidRepository.deleteById(id);
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

        //  Broadcast bid to all connected clients
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

        //  Broadcast allocation
        messagingTemplate.convertAndSend("/topic/allocations", allocation);

        // Move to next round
        currentRound++;
        wheelService.startNewRound();
    }

    // ---------------- Wheel Selection ----------------

    public Player startWheelSelection() {
        Player selectedPlayer = wheelService.selectNextPlayer(currentRound + 1);

        if (selectedPlayer != null) {
            //  Broadcast new selected player
            messagingTemplate.convertAndSend("/topic/current-player", selectedPlayer);
        }

        return selectedPlayer;
    }

    public List<Player> getAvailablePlayersForWheel() {
        return wheelService.getAvailablePlayers(currentRound + 1);
    }
}