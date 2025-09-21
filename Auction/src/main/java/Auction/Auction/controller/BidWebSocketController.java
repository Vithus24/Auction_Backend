package Auction.Auction.controller;

import Auction.Auction.dto.BidRequest;
import Auction.Auction.dto.BidResponse;
import Auction.Auction.service.BidService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BidWebSocketController {
    private final BidService bidService;

    public BidWebSocketController(BidService bidService) {
        this.bidService = bidService;
    }

    @MessageMapping("/bid/{auctionId}/{playerId}")  // Client sends to /app/bid/{auctionId}/{playerId}
    @SendTo("/topic/auction/{auctionId}/player/{playerId}")  // Broadcasts updates
    public BidResponse handleBid(@DestinationVariable Long auctionId,
                                 @DestinationVariable Long playerId,
                                 BidRequest bidRequest) {
        // Broadcast response
        return bidService.saveBid(auctionId, playerId, bidRequest);  // Assume getTeamName from user or lookup
    }
}