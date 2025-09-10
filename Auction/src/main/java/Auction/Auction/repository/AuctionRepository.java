package Auction.Auction.repository;
import Auction.Auction.entity.Auction;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
}