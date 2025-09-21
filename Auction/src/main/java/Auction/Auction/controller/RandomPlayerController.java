//package Auction.Auction.controller;
//
//import Auction.Auction.service.RandomPlayerService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/random-player")
//public class RandomPlayerController {
//
//    private final RandomPlayerService randomPlayerService;
//
//    public RandomPlayerController(RandomPlayerService randomPlayerService) {
//        this.randomPlayerService = randomPlayerService;
//    }
//
//    /**
//     * Get one random AVAILABLE player ID from a specific auction
//     * GET /random-player/{auctionId}/available
//     */
//    @GetMapping(value = "/{auctionId}/available", produces = "application/json")
//    public ResponseEntity<Long> getRandomAvailablePlayerId(@PathVariable Long auctionId) {
//        Long randomId = randomPlayerService.findRandomAvailableIdByAuctionId(auctionId);
//        return ResponseEntity.ok(randomId);
//    }
//}
