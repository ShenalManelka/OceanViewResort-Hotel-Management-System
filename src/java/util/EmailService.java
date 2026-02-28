package util;

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class EmailService {

    // ⚠ UPDATE THESE CREDENTIALS
    // Use an App Password if using Gmail, NOT your account password.
    private static final String SMTP_USER = "your-email@gmail.com";
    private static final String SMTP_PASS = "your-app-password";

    public static boolean sendCheckInEmail(String recipientEmail, String guestName, String roomNumber,
            String checkOutDate) {

        // Skip actual sending if placeholder is still present
        if ("your-email@gmail.com".equals(SMTP_USER)) {
            System.out.println("⚠ [EmailService] SMTP credentials not configured. Skipping email to " + recipientEmail);
            System.out.println("Email Content: Welcome " + guestName + ", you are checked in to Room " + roomNumber);
            return false;
        }

        // Setup properties for Gmail SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTP_USER, "Ocean View Resort"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Welcome to Ocean View Resort! Your Check-In is Confirmed");

            String emailContent = "<h3>Welcome to Ocean View Resort, " + guestName + "!</h3>"
                    + "<p>We are delighted to confirm your check-in.</p>"
                    + "<p><b>Room Number:</b> " + roomNumber + "<br>"
                    + "<b>Check-out Date:</b> " + checkOutDate + "</p>"
                    + "<p>If you need any assistance during your stay, please contact the front desk.</p>"
                    + "<p>Enjoy your stay!</p>"
                    + "<br><p>Best regards,<br>The Ocean View Resort Team</p>";

            message.setContent(emailContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Check-in email successfully sent to " + recipientEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
