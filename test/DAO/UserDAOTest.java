package DAO;

import dao.UserDAO;
import model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit 4 Test for UserDAO with robust cleanup and unique data.
 */
public class UserDAOTest {

    private UserDAO userDAO;
    private List<Integer> usersToDelete;
    private String uniqueSuffix;

    @Before
    public void setUp() {
        userDAO = new UserDAO();
        usersToDelete = new ArrayList<>();
        uniqueSuffix = String.valueOf(System.currentTimeMillis());
    }

    @After
    public void tearDown() {
        for (int userId : usersToDelete) {
            userDAO.deleteUser(userId);
        }
    }

    @Test
    public void testAddUser() {
        User user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test_" + uniqueSuffix + "@example.com");
        user.setPassword("password123");
        user.setPhone("1234567890");
        user.setAddress("123 Test St");
        user.setNicPassport("NIC_" + uniqueSuffix);

        boolean result = userDAO.addUser(user);

        if (result && user.getUserId() > 0) {
            usersToDelete.add(user.getUserId());
        }

        assertTrue("User should be added successfully", result);
        assertTrue("Generated user ID should be greater than 0", user.getUserId() > 0);
    }

    @Test
    public void testGetAllUsers() {
        User user = new User();
        user.setFirstName("List");
        user.setLastName("Test");
        user.setEmail("list_" + uniqueSuffix + "@example.com");
        user.setPassword("password123");
        user.setNicPassport("NIC_L_" + uniqueSuffix);
        userDAO.addUser(user);

        if (user.getUserId() > 0) {
            usersToDelete.add(user.getUserId());
        }

        List<User> users = userDAO.getAllUsers();
        assertNotNull("The user list should not be null", users);
        assertFalse("The user list should not be empty", users.isEmpty());
    }

    @Test
    public void testGetUserById() {
        User user = new User();
        user.setFirstName("Fetch");
        user.setLastName("Test");
        user.setEmail("fetch_" + uniqueSuffix + "@example.com");
        user.setPassword("password123");
        user.setNicPassport("NIC_F_" + uniqueSuffix);
        userDAO.addUser(user);

        if (user.getUserId() > 0) {
            usersToDelete.add(user.getUserId());
        }

        User fetchedUser = userDAO.getUserById(user.getUserId());
        assertNotNull("Fetched user should not be null", fetchedUser);
        assertEquals("Name should match", "Fetch", fetchedUser.getFirstName());
        assertEquals("ID should match", user.getUserId(), fetchedUser.getUserId());
    }
}
