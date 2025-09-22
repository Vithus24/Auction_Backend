package Auction.Auction.service;

import Auction.Auction.dto.BidResponse;
import Auction.Auction.entity.*;
import Auction.Auction.exception.*;
import Auction.Auction.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BidService {
    private final BidRepository bidRepository;

    private final PlayerRepository playerRepository;

    private final TeamRepository teamRepository;

    private final SimpMessagingTemplate messagingTemplate;

    private final AuctionWheelService wheelService;

    private final PlayerAllocationRepository allocationRepository;

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    private int currentRound = 0;

    public BidService(BidRepository bidRepository, PlayerRepository playerRepository, TeamRepository teamRepository, SimpMessagingTemplate messagingTemplate, AuctionWheelService wheelService, PlayerAllocationRepository allocationRepository, AuctionRepository auctionRepository, UserRepository userRepository) {
        this.bidRepository = bidRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.messagingTemplate = messagingTemplate;
        this.wheelService = wheelService;
        this.allocationRepository = allocationRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
    }


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

    public BidResponse saveBid(Long auctionId, Long playerId, Long userId) {

        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        Optional<Player> optionalPlayer = playerRepository.findById(playerId);
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }

        if (optionalUser.get().getRole() != Role.TEAM_OWNER) {
            throw new UserNotTeamOwnerException("Only TEAM_OWNER can place bids");
        }

        User teamOwner = optionalUser.get();

        if (optionalAuction.isEmpty()) {
            throw new AuctionNotFoundException("Auction not found.");
        }

        if (!optionalAuction.get().getStatus().toString().equals("LIVE")) {
            throw new AuctionIsNotLiveException("This auction not in live.");
        }

        Auction auction = optionalAuction.get();

        if (optionalPlayer.isEmpty()) {
            throw new PlayerNotFoundException("Player not found.");
        }

        if (!optionalPlayer.get().getPlayerStatus().toString().equals("AVAILABLE")) {
            throw new PlayerNotAvailableException("This player not available.");
        }

        Player player = optionalPlayer.get();

        Team team = teamRepository.findByOwner(teamOwner).orElseThrow(() -> new UserNotFoundException("User not found!"));
        player.setBidTeam(team);
        String biddingTeamName = team.getName();

        Double bidAmount;

        if (player.getBidAmount() == null || player.getBidAmount().equals(0.0)) {
            bidAmount = auction.getMinimumBid();
        } else {
            bidAmount = player.getBidAmount();
        }

        Double bidIncreaseBy = auction.getBidIncreaseBy();

        double currentBidAmount = bidAmount + bidIncreaseBy;
        player.setBidAmount(currentBidAmount);
        playerRepository.save(player);
        return new BidResponse(currentBidAmount, biddingTeamName);
    }
}