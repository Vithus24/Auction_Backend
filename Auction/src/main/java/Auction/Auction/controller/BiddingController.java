package Auction.Auction.controller;

import Auction.Auction.entity.Bid;
import Auction.Auction.service.BidService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BiddingController {
    @Autowired
    private BidService bidService;

    @MessageMapping("/bid")
    @SendTo("/topic/bids")
    public Bid broadcastBid(Bid bid) {
        return bidService.save(bid);
    }

    @MessageMapping("/current-player")
    @SendTo("/topic/current-player")
    public Bid handleCurrentPlayer(Bid message) {
        return message; // Placeholder; adjust if needed
    }
}