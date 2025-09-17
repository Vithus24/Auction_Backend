package Auction.Auction.controller;

import Auction.Auction.dto.PlayerResponse;
import Auction.Auction.entity.Player;
import Auction.Auction.service.PlayerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/players")
public class PlayerController {
    @Autowired
    private PlayerService playerService;

    @GetMapping
    public List<PlayerResponse> getAllPlayers() {
        return playerService.findAll().stream()
                .map(PlayerResponse::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        return playerService.findById(id)
                .map(player -> ResponseEntity.ok(new PlayerResponse(player)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<PlayerResponse>> getPlayersByAuctionId(@PathVariable Long auctionId) {
        List<Player> players = playerService.findByAuctionId(auctionId);
        List<PlayerResponse> response = players.stream()
                .map(PlayerResponse::new)
                .collect(Collectors.toList());
        return response.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Player> createPlayer(
            @RequestPart("player") String playerJson,
            @RequestPart("image") MultipartFile image) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            Player player = mapper.readValue(playerJson, Player.class);

            if (!image.isEmpty()) {
                player.setImage(image.getBytes());
            }
            return ResponseEntity.ok(playerService.save(player));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Player> updatePlayer(@PathVariable Long id, @RequestPart("player") Player player, @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                player.setImage(image.getBytes());
            }
            return ResponseEntity.ok(playerService.update(id, player));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}