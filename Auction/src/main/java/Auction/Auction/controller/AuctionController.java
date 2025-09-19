package Auction.Auction.controller;

import Auction.Auction.entity.Auction;
import Auction.Auction.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auctions")
@CrossOrigin(origins = "http://localhost:3000")
public class AuctionController {
    @Autowired
    private AuctionService auctionService;

    @GetMapping
    public List<Auction> getAllAuctions() {
        return auctionService.findAll();
    }

    @GetMapping("/admin/{adminId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Auction> getAuctionsByAdmin(@PathVariable Long adminId) {
        return auctionService.findByAdminId(adminId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Auction> getAuctionById(@PathVariable Long id) {
        Optional<Auction> auction = auctionService.findById(id);
        return auction.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Auction> createAuction(@RequestBody Auction auction, @RequestParam Long adminId) {
        return ResponseEntity.ok(auctionService.createAuction(auction, adminId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Auction> updateAuction(@PathVariable Long id, @RequestBody Auction auction, @RequestParam(required = false) Long adminId) {
        return ResponseEntity.ok(auctionService.update(id, auction, adminId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuction(@PathVariable Long id) {
        auctionService.delete(id);
        return ResponseEntity.noContent().build();
    }




}