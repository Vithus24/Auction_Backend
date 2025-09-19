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
}
