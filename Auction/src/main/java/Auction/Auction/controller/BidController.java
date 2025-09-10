package Auction.Auction.controller;

import Auction.Auction.entity.Bid;
import Auction.Auction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bids")
public class BidController {
    @Autowired
    private BidService bidService;

    @GetMapping
    public List<Bid> getAllBids() {
        return bidService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bid> getBidById(@PathVariable Long id) {
        return bidService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('TEAM_OWNER')")
    public Bid createBid(@RequestBody Bid bid) {
        return bidService.save(bid);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEAM_OWNER')")
    public ResponseEntity<Bid> updateBid(@PathVariable Long id, @RequestBody Bid bid) {
        try {
            return ResponseEntity.ok(bidService.update(id, bid));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBid(@PathVariable Long id) {
        bidService.delete(id);
        return ResponseEntity.noContent().build();
    }
}