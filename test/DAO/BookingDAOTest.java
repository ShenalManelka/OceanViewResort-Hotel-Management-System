package DAO;

import dao.BookingDAO;
import dao.RoomDAO;
import dao.UserDAO;
import model.Booking;
import model.Room;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BookingDAOTest {

    private BookingDAO bookingDAO;
    private UserDAO userDAO;
    private RoomDAO roomDAO;

    private List<Integer> bookingsToDelete;
    private List<Integer> usersToDelete;
    private List<Integer> roomsToDelete;

    private int testUserId;
    private int testRoomId;
    private String uniqueSuffix;

    @Before
    public void setUp() {
        bookingDAO = new BookingDAO();
        userDAO = new UserDAO();
        roomDAO = new RoomDAO();

        bookingsToDelete = new ArrayList<>();
        usersToDelete = new ArrayList<>();
        roomsToDelete = new ArrayList<>();
        uniqueSuffix = String.valueOf(System.currentTimeMillis());

        // Create a User for testing
        User user = new User();
        user.setFirstName("B-Guest");
        user.setLastName("Test");
        user.setEmail("b_guest_" + uniqueSuffix + "@example.com");
        user.setPassword("pass");
        user.setNicPassport("NIC_B_" + uniqueSuffix);
        userDAO.addUser(user);
        testUserId = user.getUserId();
        usersToDelete.add(testUserId);

        // Create a Room for testing
        Room room = new Room();
        room.setRoomNumber("BR-" + uniqueSuffix.substring(10));
        room.setType("Double");
        room.setPrice(200.0);
        room.setStatus("Available");
        roomDAO.addRoom(room);
        testRoomId = roomDAO.searchRooms(room.getRoomNumber(), null, null).get(0).getRoomId();
        roomsToDelete.add(testRoomId);
    }

    @After
    public void tearDown() {
        for (int id : bookingsToDelete)
            bookingDAO.deleteBooking(id);
        for (int id : roomsToDelete)
            roomDAO.deleteRoom(id);
        for (int id : usersToDelete)
            userDAO.deleteUser(id);
    }

    @Test
    public void testAddAndGetBooking() {
        Booking b = new Booking();
        b.setGuestId(testUserId);
        b.setRoomId(testRoomId);
        b.setCheckIn(new Date(System.currentTimeMillis()));
        b.setCheckOut(new Date(System.currentTimeMillis() + 86400000)); // +1 day
        b.setTotalPrice(200.0);
        b.setStatus("Confirmed");

        boolean result = bookingDAO.addBooking(b);
        assertTrue("Booking should be added", result);

        // Find the booking ID (it doesn't auto-populate in the DAO addBooking method
        // shown earlier)
        List<Booking> all = bookingDAO.getAllBookings();
        Booking saved = null;
        for (Booking item : all) {
            if (item.getGuestId() == testUserId && item.getRoomId() == testRoomId) {
                saved = item;
                break;
            }
        }
        assertNotNull("Saved booking should be found", saved);
        bookingsToDelete.add(saved.getBookingId());

        Booking fetched = bookingDAO.getBookingById(saved.getBookingId());
        assertNotNull("Fetched booking should not be null", fetched);
        assertEquals("Guest ID should match", testUserId, fetched.getGuestId());
    }
}
