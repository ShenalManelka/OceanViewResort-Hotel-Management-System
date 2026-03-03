package DAO;

import dao.RoomDAO;
import model.Room;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAOTest {

    private RoomDAO roomDAO;
    private List<Integer> roomsToDelete;
    private String uniqueSuffix;

    @Before
    public void setUp() {
        roomDAO = new RoomDAO();
        roomsToDelete = new ArrayList<>();
        uniqueSuffix = String.valueOf(System.currentTimeMillis()).substring(7); // Shorter for room number
    }

    @After
    public void tearDown() {
        for (int roomId : roomsToDelete) {
            roomDAO.deleteRoom(roomId);
        }
    }

    @Test
    public void testAddAndGetRoom() {
        Room room = new Room();
        room.setRoomNumber("T-" + uniqueSuffix);
        room.setType("Suite");
        room.setPrice(500.00);
        room.setStatus("Available");
        room.setDescription("Test Room");

        boolean added = roomDAO.addRoom(room);
        assertTrue("Room should be added", added);

        // Find the room to get its ID
        List<Room> searchResults = roomDAO.searchRooms(room.getRoomNumber(), null, null);
        assertFalse("Should find the room", searchResults.isEmpty());
        Room savedRoom = searchResults.get(0);
        roomsToDelete.add(savedRoom.getRoomId());

        // Test getById
        Room fetched = roomDAO.getRoomById(savedRoom.getRoomId());
        assertNotNull("Fetched room should not be null", fetched);
        assertEquals("Room number should match", room.getRoomNumber(), fetched.getRoomNumber());
    }

    @Test
    public void testUpdateRoom() {
        Room room = new Room();
        room.setRoomNumber("U-" + uniqueSuffix);
        room.setType("Single");
        room.setPrice(100.00);
        room.setStatus("Available");
        roomDAO.addRoom(room);

        Room saved = roomDAO.searchRooms(room.getRoomNumber(), null, null).get(0);
        roomsToDelete.add(saved.getRoomId());

        saved.setPrice(150.00);
        saved.setStatus("Maintenance");
        boolean updated = roomDAO.updateRoom(saved);
        assertTrue("Room should be updated", updated);

        Room fetched = roomDAO.getRoomById(saved.getRoomId());
        assertEquals("Price should be updated", 150.00, fetched.getPrice(), 0.001);
        assertEquals("Status should be updated", "Maintenance", fetched.getStatus());
    }
}
