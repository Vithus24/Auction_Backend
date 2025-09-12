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

    public Player save(Player player) {
        if (player.getAuction() != null && player.getAuction().getId() != null && player.getEmail() != null) {
            Optional<Player> existingPlayer = playerRepository.findByAuctionIdAndEmail(player.getAuction().getId(), player.getEmail());
            if (existingPlayer.isPresent()) {
                throw new RuntimeException("Player with email " + player.getEmail() + " already exists in auction " + player.getAuction().getId());
            }
        }
        return playerRepository.save(player);
    }

    public Player update(Long id, Player updatedPlayer) {
        Optional<Player> existingPlayer = playerRepository.findById(id);
        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setFirstname(updatedPlayer.getFirstname());
            player.setLastname(updatedPlayer.getLastname());
            player.setMobileno(updatedPlayer.getMobileno());
            player.setEmail(updatedPlayer.getEmail());
            player.setDob(updatedPlayer.getDob());
            player.setTshirtSize(updatedPlayer.getTshirtSize());
            player.setBottomSize(updatedPlayer.getBottomSize());
            player.setTypeOfSportCategory(updatedPlayer.getTypeOfSportCategory());
            player.setSold(updatedPlayer.isSold());
            player.setAuction(updatedPlayer.getAuction());
            return playerRepository.save(player);
        } else {
            throw new RuntimeException("Player not found with id: " + id);
        }
    }

    public void delete(Long id) {
        playerRepository.deleteById(id);
    }
}