package Auction.Auction.repository;


import Auction.Auction.entity.Bid;
import Auction.Auction.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByPlayer(Player player);
}