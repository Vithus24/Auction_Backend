//package Auction.Auction.service;
//
//import Auction.Auction.entity.Player;
//import Auction.Auction.entity.PlayerStatus;
//import Auction.Auction.repository.PlayerRepository;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.concurrent.ThreadLocalRandom;
//
//@Service
//public class RandomPlayerService {
//
//    private final PlayerRepository playerRepository;
//
//    public RandomPlayerService(PlayerRepository playerRepository) {
//        this.playerRepository = playerRepository;
//    }
//
//    /**
//     * Selects a random player from all AVAILABLE players.
//     * Throws RuntimeException if none are available.
//     */
////    public Player getRandomAvailablePlayer() {
////        List<Player> available = playerRepository.findIdsByAuctionIdAndPlayerStatus(PlayerStatus.AVAILABLE);
////        if (available.isEmpty()) {
////            throw new RuntimeException("No AVAILABLE players found");
////        }
////        int idx = ThreadLocalRandom.current().nextInt(available.size());
////        return available.get(idx);
////    }
//
//    public Long findRandomAvailableIdByAuctionId(Long auctionId) {
//        List<Long> ids = playerRepository.findIdsByAuctionIdAndPlayerStatus(
//                auctionId, PlayerStatus.AVAILABLE);
//
//        if (ids.isEmpty()) {
//            throw new IllegalStateException("No AVAILABLE players for auction " + auctionId);
//        }
//
//        Random random = new Random();
//        return ids.get(random.nextInt(ids.size()));
//    }
//
//}
