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

    @Test
    public void testMultipleBidders_HighestPriceShouldWin() {
        auction.startAuction();
        auction.placeBid(bidder1, 1200.0);
        auction.placeBid(bidder2, 1800.0); // bidder2 đặt cao hơn
        auction.placeBid(bidder1, 2000.0); // bidder1 đặt cao nhất

        assertEquals(2000.0, auction.getItem().getCurrentHighestPrice());
        assertEquals("user_A", auction.getLeadingBidder().getUsername());
        assertEquals(3, auction.getBidHistory().size());
    }

    @Test
    public void testUpdateStatus_WhenBeforeStart_ShouldBeOpen() {
        // Item có startTime trong tương lai
        Item futureItem = new Electronics("FUTURE_001", "Future Phone", "mô tả", 500.0,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(5), 6);
        Auction futureAuction = new Auction(futureItem);

        assertEquals(AuctionStatus.OPEN, futureAuction.getStatus());
    }

    @Test
    public void testUpdateStatus_WhenAfterEnd_ShouldBeFinished() {
        // Item có endTime trong quá khứ
        Item pastItem = new Electronics("PAST_001", "Old Laptop", "cũ", 300.0,
                LocalDateTime.now().minusHours(3),
                LocalDateTime.now().minusHours(1), 0);
        Auction pastAuction = new Auction(pastItem);

        assertEquals(AuctionStatus.FINISHED, pastAuction.getStatus());
        assertTrue(pastAuction.isClosed());
    }
}
