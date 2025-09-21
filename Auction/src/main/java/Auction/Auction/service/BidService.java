package Auction.Auction.service;

import Auction.Auction.dto.BidRequest;
import Auction.Auction.dto.BidResponse;
import Auction.Auction.entity.*;
import Auction.Auction.exception.AuctionIsNotLiveException;
import Auction.Auction.exception.AuctionNotFoundException;
import Auction.Auction.exception.UserNotTeamOwnerException;
import Auction.Auction.mapper.BidMapper;
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

    private final BidMapper bidMapper;

    private int currentRound = 0;

    public BidService(BidRepository bidRepository, PlayerRepository playerRepository, TeamRepository teamRepository, SimpMessagingTemplate messagingTemplate, AuctionWheelService wheelService, PlayerAllocationRepository allocationRepository, AuctionRepository auctionRepository, BidMapper bidMapper) {
        this.bidRepository = bidRepository;
        this.playerRepository = playerRepository;
        this.teamRepository = teamRepository;
        this.messagingTemplate = messagingTemplate;
        this.wheelService = wheelService;
        this.allocationRepository = allocationRepository;
        this.auctionRepository = auctionRepository;
        this.bidMapper = bidMapper;
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

    public BidResponse saveBid(Long auctionId, Long playerId, BidRequest bidRequest) {
        Optional<Auction> optionalAuction = auctionRepository.findById(auctionId);
        if(optionalAuction.isEmpty()){
            throw new AuctionNotFoundException("Auction not found.");
        }

        if (!optionalAuction.get().getStatus().toString().equals("LIVE")){
            throw new AuctionIsNotLiveException("This auction not in live.");
        }

//        if (user.getRole() != Role.TEAM_OWNER) {
//            throw new UserNotTeamOwnerException("Only TEAM_OWNER can place bids");
//        }
//
//        Optional<Auction> auction = auctionRepository.findById(auctionId);
//        if (auction.isEmpty()) {
//            throw new AuctionNotFoundException("Auction not found");
//        }
//
//        if (!auction.get().getStatus().toString().equals("LIVE")) {
//            throw new AuctionIsNotLiveException("Auction is not live");
//        }
//
//        // Validate amount, check if user owns a team in auction (custom logic)
//        if (bidRequest.amount() <= auction.getMinimumBid() || bidRequest.amount() % auction.getBidIncreaseBy() != 0) {
//            throw new AppException("Invalid bid amount");
//        }
//
//        // Save bid (MySQL, optional Redis cache)
//        bidService.saveBid(auctionId, playerId, bidRequest.amount(), user);
        return null;
    }
}