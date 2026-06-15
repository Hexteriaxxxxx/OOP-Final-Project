package main.utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Sends emails via Gmail SMTP.
 *
 * SETUP REQUIRED:
 *   1. Use a Gmail account dedicated to this app.
 *   2. Enable 2-Step Verification on the Gmail account.
 *   3. Go to: Google Account → Security → App Passwords
 *   4. Create an App Password (type: Mail, device: Windows Computer)
 *   5. Paste the 16-character App Password in SENDER_APP_PASSWORD below.
 *
 *   NOTE: Regular Gmail passwords will NOT work here. Must use App Password.
 */
public class EmailService {

    // ─── CONFIGURE THESE ──────────────────────────────────────────────────────
    private static final String SENDER_EMAIL        = "passslippupsantarosa@gmail.com";
    private static final String SENDER_APP_PASSWORD = "ndwu zaff zbiv zrah"; // ← paste your new one here
    private static final String SENDER_NAME         = "PUPSRC Pass Slip System";
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Sends an OTP email to the given address.
     * @return true if sent successfully, false otherwise.
     */
    public static boolean sendOTPEmail(String toEmail, String otp) {
        System.out.println("[EMAIL] Starting sendOTPEmail...");
        System.out.println("[EMAIL] To: " + toEmail);
        System.out.println("[EMAIL] From: " + SENDER_EMAIL);

        Properties props = new Properties();
        props.put("mail.smtp.host",               "smtp.gmail.com");
        props.put("mail.smtp.port",               "465");
        props.put("mail.smtp.auth",               "true");
        props.put("mail.smtp.starttls.enable",    "false");
        props.put("mail.smtp.ssl.enable",      "true");
        props.put("mail.smtp.starttls.required",  "true");
        props.put("mail.smtp.ssl.protocols",      "TLSv1.2");
        props.put("mail.smtp.connectiontimeout",  "10000");
        props.put("mail.smtp.timeout",            "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });

        session.setDebug(true); // prints full SMTP conversation to console

        try {
            System.out.println("[EMAIL] Building message...");
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, SENDER_NAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("PUP Pass Slip System - Password Reset Code");
            message.setContent(buildEmailHTML(otp), "text/html; charset=utf-8");

            System.out.println("[EMAIL] Sending...");
            Transport.send(message);
            System.out.println("[EMAIL] OTP sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.out.println("[EMAIL ERROR] Exception type: " + e.getClass().getName());
            System.out.println("[EMAIL ERROR] Message: " + e.getMessage());
            Throwable cause = e.getCause();
            while (cause != null) {
                System.out.println("[CAUSED BY] " + cause.getClass().getName() + ": " + cause.getMessage());
                cause = cause.getCause();
            }
            e.printStackTrace();
            return false;
        }
    }

    private static String buildEmailHTML(String otp) {
        return "<!DOCTYPE html>" +
                "<html><body style='margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;'>" +
                "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f4f4;padding:30px 0;'>" +
                "<tr><td align='center'>" +
                "<table width='500' cellpadding='0' cellspacing='0' " +
                "style='background:#ffffff;border-radius:12px;overflow:hidden;" +
                "box-shadow:0 2px 12px rgba(0,0,0,0.1);'>" +

                // Header
                "<tr><td style='background:#8B0000;padding:24px 30px;text-align:center;'>" +
                "<h2 style='color:#ffffff;margin:0;font-size:18px;'>🏫 PUP Pass Slip System</h2>" +
                "<p style='color:#ffcccc;margin:4px 0 0;font-size:12px;'>Polytechnic University of the Philippines</p>" +
                "</td></tr>" +

                // Body
                "<tr><td style='padding:30px;'>" +
                "<p style='color:#333333;font-size:14px;margin:0 0 16px;'>" +
                "We received a request to reset the password for your account. " +
                "Use the verification code below to proceed:" +
                "</p>" +

                // OTP Box
                "<div style='text-align:center;margin:24px 0;'>" +
                "<div style='display:inline-block;background:#fdf0f0;border:2px dashed #8B0000;" +
                "border-radius:12px;padding:20px 48px;'>" +
                "<p style='margin:0 0 4px;color:#888888;font-size:11px;letter-spacing:1px;'>VERIFICATION CODE</p>" +
                "<span style='font-size:38px;font-weight:bold;color:#8B0000;letter-spacing:10px;'>" +
                otp + "</span>" +
                "</div>" +
                "</div>" +

                "<p style='color:#666666;font-size:12px;text-align:center;margin:0 0 20px;'>" +
                "⏱ This code expires in <strong>5 minutes</strong>." +
                "</p>" +

                "<hr style='border:none;border-top:1px solid #eeeeee;margin:20px 0;'/>" +
                "<p style='color:#999999;font-size:11px;text-align:center;margin:0;'>" +
                "If you did not request a password reset, you can safely ignore this email. " +
                "Your password will not be changed." +
                "</p>" +
                "</td></tr>" +

                // Footer
                "<tr><td style='background:#f9f9f9;padding:14px 30px;text-align:center;" +
                "border-top:1px solid #eeeeee;'>" +
                "<p style='color:#aaaaaa;font-size:10px;margin:0;'>" +
                "PUP Santa Rosa Campus &bull; Pass Slip Issuance &amp; Monitoring System" +
                "</p>" +
                "</td></tr>" +

                "</table>" +
                "</td></tr></table>" +
                "</body></html>";
    }
}