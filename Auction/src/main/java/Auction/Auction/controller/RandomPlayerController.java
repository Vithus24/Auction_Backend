//package Auction.Auction.controller;
//
//import Auction.Auction.entity.Player;
//import Auction.Auction.service.RandomPlayerService;
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
//     * Get one random AVAILABLE player
//     * GET /api/random-player/available
//     */
//    @GetMapping("/available")
//    public Player getRandomAvailablePlayer() {
//        return randomPlayerService.getRandomAvailablePlayer();
//    }
//}


package Auction.Auction.controller;

import Auction.Auction.dto.PlayerResponse;
import Auction.Auction.entity.Player;
import Auction.Auction.service.RandomPlayerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/random-player")
public class RandomPlayerController {

    private final RandomPlayerService randomPlayerService;

    public RandomPlayerController(RandomPlayerService randomPlayerService) {
        this.randomPlayerService = randomPlayerService;
    }

    /**
     * Get one random AVAILABLE player
     * GET /random-player/available
     */
    @GetMapping(value = "/available", produces = "application/json")
    public ResponseEntity<PlayerResponse> getRandomAvailablePlayer() {
        Player picked = randomPlayerService.getRandomAvailablePlayer(); // fetch entity
        return ResponseEntity.ok(new PlayerResponse(picked));           // map -> DTO
    }

    // Optional: return 404 with message if service throws "no available players" error
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
