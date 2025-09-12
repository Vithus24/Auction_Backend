package Auction.Auction.service;

import Auction.Auction.entity.Player;
import Auction.Auction.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerService {
    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Optional<Player> findById(Long id) {
        return playerRepository.findById(id);
    }

    public Optional<Player> findByName(String name) {
        return playerRepository.findByName(name);
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public Player update(Long id, Player updatedPlayer) {
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setName(updatedPlayer.getName());
            player.setRole(updatedPlayer.getRole());
            player.setBasePrice(updatedPlayer.getBasePrice());
            player.setStats(updatedPlayer.getStats());
            player.setSold(updatedPlayer.isSold());
            return playerRepository.save(player);
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }

    public void delete(Long id) {
        playerRepository.deleteById(id);
    }
}