package Auction.Auction.dto;

public record BidRequest(
        double amount,
        Long userId
) {
}
