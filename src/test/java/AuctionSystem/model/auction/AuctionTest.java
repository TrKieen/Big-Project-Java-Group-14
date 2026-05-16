package AuctionSystem.model.auction;

import AuctionSystem.exception.AuctionClosedException;
import AuctionSystem.exception.InvalidBidException;
import AuctionSystem.model.Electronics;
import AuctionSystem.model.Item;
import AuctionSystem.model.user.Bidder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class AuctionTest {

    private Auction auction;
    private Item testItem;
    private Bidder bidder1;
    private Bidder bidder2;

    @BeforeEach
    public void setUp() {
        testItem = new Electronics("ID_TEST", "Laptop Asus", "Core i7, 16GB RAM", 1000.0,
                LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1), 12);
        auction = new Auction(testItem);
        bidder1 = new Bidder("user_A", "pass123");
        bidder2 = new Bidder("user_B", "pass123");
    }

    @Test
    public void testPlaceValidBid_ShouldUpdateHighestPrice() {
        // Mở phiên đấu giá
        auction.startAuction();

        // Đặt giá hợp lệ (1500 > 1000)
        auction.placeBid(bidder1, 1500.0);

        // Kiểm tra xem giá và người dẫn đầu đã được cập nhật chưa
        assertEquals(1500.0, auction.getItem().getCurrentHighestPrice());
        assertEquals("user_A", auction.getLeadingBidder().getUsername());
        assertEquals(1, auction.getBidHistory().size());
    }

    @Test
    public void testPlaceInvalidBid_TooLow_ShouldThrowException() {
        auction.startAuction();
        auction.placeBid(bidder1, 1500.0);

        // Bidder 2 cố tình đặt giá thấp hơn giá hiện tại (1200 < 1500)
        assertThrows(InvalidBidException.class, () -> {
            auction.placeBid(bidder2, 1200.0);
        });

        // Giá cao nhất vẫn phải là 1500 của Bidder 1
        assertEquals(1500.0, auction.getItem().getCurrentHighestPrice());
    }

    @Test
    public void testPlaceBidOnClosedAuction_ShouldThrowException() {
        // Không gọi startAuction() hoặc gọi finishAuction()
        auction.finishAuction();

        // Cố tình đặt giá khi phiên đã đóng
        assertThrows(AuctionClosedException.class, () -> {
            auction.placeBid(bidder1, 2000.0);
        });
    }
}
