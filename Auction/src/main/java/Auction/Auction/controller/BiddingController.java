package Auction.Auction.controller;
import Auction.Auction.entity.Bid;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BiddingController {
    @MessageMapping("/bid")
    @SendTo("/topic/bids")
    public Bid broadcastBid(Bid bid) {
        return bid;
    }
}