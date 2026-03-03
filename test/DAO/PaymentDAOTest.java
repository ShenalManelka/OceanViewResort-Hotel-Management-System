package DAO;

import dao.BookingDAO;
import dao.PaymentDAO;
import dao.RoomDAO;
import dao.UserDAO;
import model.Booking;
import model.Payment;
import model.Room;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAOTest {

    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;
    private UserDAO userDAO;
    private RoomDAO roomDAO;

    private List<Integer> paymentsToDelete;
    private List<Integer> bookingsToDelete;
    private List<Integer> usersToDelete;
    private List<Integer> roomsToDelete;

    private int testBookingId;
    private String uniqueSuffix;

    @Before
    public void setUp() {
        paymentDAO = new PaymentDAO();
        bookingDAO = new BookingDAO();
        userDAO = new UserDAO();
        roomDAO = new RoomDAO();

        paymentsToDelete = new ArrayList<>();
        bookingsToDelete = new ArrayList<>();
        usersToDelete = new ArrayList<>();
        roomsToDelete = new ArrayList<>();
        uniqueSuffix = String.valueOf(System.currentTimeMillis());

        // Setup chain: User -> Room -> Booking
        User user = new User();
        user.setFirstName("P-Guest");
        user.setEmail("p_guest_" + uniqueSuffix + "@example.com");
        user.setPassword("pass");
        user.setNicPassport("NIC_P_" + uniqueSuffix);
        userDAO.addUser(user);
        usersToDelete.add(user.getUserId());

        Room room = new Room();
        room.setRoomNumber("PR-" + uniqueSuffix.substring(10));
        room.setType("Single");
        room.setPrice(100.0);
        room.setStatus("Available");
        roomDAO.addRoom(room);
        int roomId = roomDAO.searchRooms(room.getRoomNumber(), null, null).get(0).getRoomId();
        roomsToDelete.add(roomId);

        Booking b = new Booking();
        b.setGuestId(user.getUserId());
        b.setRoomId(roomId);
        b.setCheckIn(new Date(System.currentTimeMillis()));
        b.setCheckOut(new Date(System.currentTimeMillis() + 86400000));
        b.setTotalPrice(100.0);
        b.setStatus("Confirmed");
        bookingDAO.addBooking(b);

        // Find booking ID
        for (Booking item : bookingDAO.getAllBookings()) {
            if (item.getGuestId() == user.getUserId()) {
                testBookingId = item.getBookingId();
                break;
            }
        }
        bookingsToDelete.add(testBookingId);
    }

    @After
    public void tearDown() {
        for (int id : paymentsToDelete)
            paymentDAO.deletePayment(id);
        for (int id : bookingsToDelete)
            bookingDAO.deleteBooking(id);
        for (int id : roomsToDelete)
            roomDAO.deleteRoom(id);
        for (int id : usersToDelete)
            userDAO.deleteUser(id);
    }

    @Test
    public void testAddAndGetPayment() {
        Payment p = new Payment();
        p.setBookingId(testBookingId);
        p.setAmount(100.0);
        p.setPaymentMethod("Cash");
        p.setPaymentStatus("Paid");

        boolean result = paymentDAO.addPayment(p);
        assertTrue("Payment should be added", result);

        Payment fetched = paymentDAO.getPaymentByBookingId(testBookingId);
        assertNotNull("Fetched payment should not be null", fetched);
        assertEquals("Amount should match", 100.0, fetched.getAmount(), 0.001);

        paymentsToDelete.add(fetched.getPaymentId());
    }
}
