package Auction.Auction.service;
import Auction.Auction.entity.Auction;
import Auction.Auction.entity.User;
import Auction.Auction.repository.AuctionRepository;
import Auction.Auction.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AuctionService {
    @Autowired private AuctionRepository auctionRepository;


    @Autowired
    private UserRepository userRepository;

    public List<Auction> findAll() {
        return auctionRepository.findAll();
    }

    public Optional<Auction> findById(Long id) {
        return auctionRepository.findById(id);
    }

    public Auction createAuction(Auction auction, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
        auction.setAdmin(admin);
        return auctionRepository.save(auction);
    }

    public Auction update(Long id, Auction updatedAuction, Long adminId) {
        Optional<Auction> existingAuction = auctionRepository.findById(id);
        if (existingAuction.isPresent()) {
            Auction auction = existingAuction.get();
            auction.setAuctionName(updatedAuction.getAuctionName());
            auction.setAuctionDate(updatedAuction.getAuctionDate());
            auction.setTypeOfSport(updatedAuction.getTypeOfSport());
            auction.setBidIncreaseBy(updatedAuction.getBidIncreaseBy());
            auction.setMinimumBid(updatedAuction.getMinimumBid());
            auction.setPointsPerTeam(updatedAuction.getPointsPerTeam());
            auction.setPlayerPerTeam(updatedAuction.getPlayerPerTeam());
            auction.setStatus(updatedAuction.getStatus());
            if (adminId != null) {
                User admin = userRepository.findById(adminId)
                        .orElseThrow(() -> new RuntimeException("Admin not found with id: " + adminId));
                auction.setAdmin(admin);
            }
            return auctionRepository.save(auction);
        } else {
            throw new RuntimeException("Auction not found with id: " + id);
        }
    }

    public void delete(Long id) {
        auctionRepository.deleteById(id);
    }

    public List<Auction> findByAdminId(Long adminId) {
        return auctionRepository.findByAdminId(adminId);
    }
}