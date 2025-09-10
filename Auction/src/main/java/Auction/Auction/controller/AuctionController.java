package Auction.Auction.controller;

import Auction.Auction.entity.Auction;
import Auction.Auction.entity.Bid;
import Auction.Auction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class AuctionController {
    @Autowired
    private AuctionService auctionService;

    @GetMapping
    public List<Auction> getAllAuctions() {
        return auctionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Long id) {
        return auctionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction) {
        return ResponseEntity.ok(auctionService.createAuction(auction));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Auction> updateAuction(@PathVariable Long id, @RequestBody Auction auction) {
        try {
            return ResponseEntity.ok(auctionService.update(id, auction));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long id) {
        auctionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{playerId}/bid")
    @PreAuthorize("hasRole('TEAM_OWNER')")
    public ResponseEntity<Bid> placeBid(@PathVariable Long playerId, @RequestParam Long teamId, @RequestParam double amount) {
        return ResponseEntity.ok(auctionService.placeBid(playerId, teamId, amount));
    }

    @PostMapping("/{playerId}/allocate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> allocatePlayer(@PathVariable Long playerId) {
        auctionService.allocatePlayer(playerId);
        return ResponseEntity.ok().build();
    }
}