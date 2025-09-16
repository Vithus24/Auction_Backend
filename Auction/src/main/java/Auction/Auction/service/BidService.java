package Auction.Auction.service;

import Auction.Auction.entity.Bid;
import Auction.Auction.entity.Player;
import Auction.Auction.entity.Team;
import Auction.Auction.repository.BidRepository;
import Auction.Auction.repository.PlayerRepository;
import Auction.Auction.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
}