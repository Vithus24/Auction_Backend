package Auction.Auction.dto;
import java.util.Base64;

public class PlayerResponse {
    private Long id;
    private String firstname;
    private String lastname;
    private String mobileno;
    private String email;
    private String dob;
    private String tshirtSize;
    private String bottomSize;
    private String typeOfSportCategory;
    private boolean sold;
    private Long auctionId;
    private String imageBase64; // Base64 encoded image

    // Constructors
    public PlayerResponse() {}

    public PlayerResponse(Auction.Auction.entity.Player player) {
        this.id = player.getId();
        this.firstname = player.getFirstname();
        this.lastname = player.getLastname();
        this.mobileno = player.getMobileno();
        this.email = player.getEmail();
        this.dob = player.getDob() != null ? player.getDob().toString() : null;
        this.tshirtSize = player.getTshirtSize();
        this.bottomSize = player.getBottomSize();
        this.typeOfSportCategory = player.getTypeOfSportCategory();
        this.sold = player.isSold();
        this.auctionId = player.getAuction() != null ? player.getAuction().getId() : null;
        this.imageBase64 = player.getImage() != null ? Base64.getEncoder().encodeToString(player.getImage()) : null;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getMobileno() { return mobileno; }
    public void setMobileno(String mobileno) { this.mobileno = mobileno; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDob() { return dob; }
    public void setDob(String dob) { this.dob = dob; }
    public String getTshirtSize() { return tshirtSize; }
    public void setTshirtSize(String tshirtSize) { this.tshirtSize = tshirtSize; }
    public String getBottomSize() { return bottomSize; }
    public void setBottomSize(String bottomSize) { this.bottomSize = bottomSize; }
    public String getTypeOfSportCategory() { return typeOfSportCategory; }
    public void setTypeOfSportCategory(String typeOfSportCategory) { this.typeOfSportCategory = typeOfSportCategory; }
    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }
    public Long getAuctionId() { return auctionId; }
    public void setAuctionId(Long auctionId) { this.auctionId = auctionId; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
