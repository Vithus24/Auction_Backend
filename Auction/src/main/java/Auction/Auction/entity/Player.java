package Auction.Auction.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import Auction.Auction.entity.PlayerStatus;

@Entity
@Table(name = "players", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"auction_id", "email"})
})
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@ToString(exclude = {"image", "auction", "bidTeam"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    private String firstname;
    private String lastname;
    private String mobileno;
    private String email;
    private LocalDate dob;
    private String tshirtSize;
    private String bottomSize;
    private String typeOfSportCategory;
    private boolean sold;

    @ManyToOne
    @JoinColumn(name = "auction_id")
    private Auction auction;

    @Lob
    @Column(name = "image", columnDefinition = "LONGBLOB")
    private byte[] image;
    @Enumerated(EnumType.STRING)
    @Column(name = "player_status", nullable = false)
    private PlayerStatus playerStatus;


    @ManyToOne
    @JoinColumn(name = "bid_team_id")
    private Team bidTeam;

    // NEW
    @Column(name = "bid_amount", precision = 19, scale = 2)
    private BigDecimal bidAmount;

    @PrePersist
    public void prePersist() {
        if (playerStatus == null) {
            playerStatus = PlayerStatus.AVAILABLE;
        }
        // keep "sold" consistent
        this.sold = (playerStatus == PlayerStatus.SOLD);
    }

    @PreUpdate
    public void preUpdate() {
        this.sold = (playerStatus == PlayerStatus.SOLD);
    }
}
