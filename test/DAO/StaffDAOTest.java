package DAO;

import dao.StaffDAO;
import model.Staff;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAOTest {

    private StaffDAO staffDAO;
    private List<Integer> staffToDelete;
    private String uniqueSuffix;

    @Before
    public void setUp() {
        staffDAO = new StaffDAO();
        staffToDelete = new ArrayList<>();
        uniqueSuffix = String.valueOf(System.currentTimeMillis());
    }

    @After
    public void tearDown() {
        for (int id : staffToDelete) {
            staffDAO.deleteStaff(id);
        }
    }

    @Test
    public void testAddAndAuthenticateStaff() throws Exception {
        String fullName = "Test Staff " + uniqueSuffix;
        String email = "staff_" + uniqueSuffix + "@oceanview.com";
        String password = "password123";

        // Test Add
        boolean added = staffDAO.addStaff(fullName, email, password);
        assertTrue("Staff should be added successfully", added);

        // Test Authenticate
        Staff authenticated = staffDAO.authenticate(email, password);
        assertNotNull("Staff should be authenticated", authenticated);
        assertEquals("Name should match", fullName, authenticated.getFullName());

        // Track for deletion
        staffToDelete.add(authenticated.getId());
    }

    @Test
    public void testGetAllStaff() {
        String email = "list_staff_" + uniqueSuffix + "@oceanview.com";
        staffDAO.addStaff("List Test", email, "pass");

        List<Staff> staffList = staffDAO.getAllStaff();
        assertNotNull("Staff list should not be null", staffList);

        Staff found = null;
        for (Staff s : staffList) {
            if (email.equals(s.getEmail())) {
                found = s;
                staffToDelete.add(s.getId());
                break;
            }
        }
        assertNotNull("Newly added staff should be in the list", found);
    }
}
