package Auction.Auction.entity;
import jakarta.persistence.*;

@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"auction_id", "name"})
})
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double budget;
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    private String ownerMail;
    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getOwnerMail() {
        return ownerMail;
    }

    public void setOwnerMail(String ownerMail) {
        this.ownerMail = ownerMail;
    }

    public Auction getAuction() {
        return auction;
    }

    public void setAuction(Auction auction) {
        this.auction = auction;
    }
}