package Auction.Auction.repository;

import Auction.Auction.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @Query("SELECT a FROM Auction a WHERE a.admin.id = :adminId")
    List<Auction> findByAdminId(@Param("adminId") Long adminId);
}