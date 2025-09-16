package Auction.Auction.repository;

import Auction.Auction.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Query("SELECT p FROM Player p WHERE p.auction.id = :auctionId")
    List<Player> findByAuctionId(@Param("auctionId") Long auctionId);

    @Query("SELECT p FROM Player p WHERE p.auction.id = :auctionId AND p.email = :email")
    Optional<Player> findByAuctionIdAndEmail(@Param("auctionId") Long auctionId, @Param("email") String email);
}