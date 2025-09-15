package Auction.Auction.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "auctions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"admin_id", "auction_name"})
})
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String auctionName;
    private LocalDateTime auctionDate;
    private String typeOfSport;
    private Double bidIncreaseBy;
    private Double minimumBid;
    private Double pointsPerTeam;
    private Integer playerPerTeam;
    private String status;
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private User admin;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuctionName() {
        return auctionName;
    }

    public void setAuctionName(String auctionName) {
        this.auctionName = auctionName;
    }

    public LocalDateTime getAuctionDate() {
        return auctionDate;
    }

    public void setAuctionDate(LocalDateTime auctionDate) {
        this.auctionDate = auctionDate;
    }

    public String getTypeOfSport() {
        return typeOfSport;
    }

    public void setTypeOfSport(String typeOfSport) {
        this.typeOfSport = typeOfSport;
    }

    public Double getBidIncreaseBy() {
        return bidIncreaseBy;
    }

    public void setBidIncreaseBy(Double bidIncreaseBy) {
        this.bidIncreaseBy = bidIncreaseBy;
    }

    public Double getMinimumBid() {
        return minimumBid;
    }

    public void setMinimumBid(Double minimumBid) {
        this.minimumBid = minimumBid;
    }

    public Double getPointsPerTeam() {
        return pointsPerTeam;
    }

    public void setPointsPerTeam(Double pointsPerTeam) {
        this.pointsPerTeam = pointsPerTeam;
    }

    public Integer getPlayerPerTeam() {
        return playerPerTeam;
    }

    public void setPlayerPerTeam(Integer playerPerTeam) {
        this.playerPerTeam = playerPerTeam;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }
}