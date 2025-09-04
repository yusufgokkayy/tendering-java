package com.tendering.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:8080}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom(fromEmail, "Tendering Platform");
            helper.setSubject("Şifre Sıfırlama Talebi - Tendering Platform");

            String resetUrl = frontendUrl + "/reset-password.html?token=" + token;
            String htmlContent = createPasswordResetEmailTemplate(toEmail, resetUrl, token);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Professional password reset email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to: {}", toEmail, e);
            throw new RuntimeException("Email gönderilemedi: " + e.getMessage());
        }
    }

    private String createPasswordResetEmailTemplate(String email, String resetUrl, String token) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, HH:mm");

        // String.format yerine + concatenation kullan (daha güvenli)
        return "<!DOCTYPE html>" +
                "<html lang=\"tr\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Şifre Sıfırlama</title>" +
                "<style>" +
                "* { margin: 0; padding: 0; box-sizing: border-box; }" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f8f9fa; line-height: 1.6; }" +
                ".container { max-width: 600px; margin: 40px auto; background: white; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.1); overflow: hidden; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 40px 30px; text-align: center; }" +
                ".header h1 { font-size: 28px; margin-bottom: 10px; font-weight: 700; }" +
                ".header p { font-size: 16px; opacity: 0.9; }" +
                ".content { padding: 40px 30px; }" +
                ".greeting { font-size: 18px; color: #333; margin-bottom: 20px; }" +
                ".message { color: #666; font-size: 16px; margin-bottom: 30px; line-height: 1.7; }" +
                ".reset-button { text-align: center; margin: 40px 0; }" +
                ".reset-button a { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; display: inline-block; }" +
                ".info-box { background: #f8f9fa; border-left: 4px solid #667eea; padding: 20px; margin: 30px 0; border-radius: 0 8px 8px 0; }" +
                ".info-box h3 { color: #333; margin-bottom: 10px; font-size: 16px; }" +
                ".info-box p { color: #666; font-size: 14px; margin-bottom: 8px; }" +
                ".warning { background: #fff3cd; border-left-color: #ffc107; color: #856404; }" +
                ".footer { background: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e9ecef; }" +
                ".footer p { color: #6c757d; font-size: 14px; margin-bottom: 8px; }" +
                ".token-info { font-family: 'Courier New', monospace; background: #f1f3f4; padding: 8px; border-radius: 4px; font-size: 12px; word-break: break-all; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<h1>🔐 Tendering Platform</h1>" +
                "<p>Şifre Sıfırlama Talebi</p>" +
                "</div>" +
                "<div class=\"content\">" +
                "<div class=\"greeting\">Merhaba,</div>" +
                "<div class=\"message\">" +
                "<strong>" + email + "</strong> email adresi için şifre sıfırlama talebiniz alınmıştır. " +
                "Yeni şifrenizi belirlemek için aşağıdaki butona tıklayın." +
                "</div>" +
                "<div class=\"reset-button\">" +
                "<a href=\"" + resetUrl + "\" target=\"_blank\">Şifremi Sıfırla</a>" +
                "</div>" +
                "<div class=\"info-box\">" +
                "<h3>📋 Önemli Bilgiler:</h3>" +
                "<p><strong>⏰ Geçerlilik Süresi:</strong> " + expiryTime.format(formatter) + "'e kadar</p>" +
                "<p><strong>🔒 Güvenlik:</strong> Bu link sadece bir kez kullanılabilir</p>" +
                "<p><strong>📧 Email:</strong> " + email + "</p>" +
                "</div>" +
                "<div class=\"info-box warning\">" +
                "<h3>⚠️ Güvenlik Uyarısı</h3>" +
                "<p>Eğer bu talebi siz yapmadıysanız, bu emaili görmezden gelebilirsiniz. Hesabınız güvendedir.</p>" +
                "</div>" +
                "<div class=\"message\">" +
                "<strong>Buton çalışmıyor mu?</strong><br>" +
                "Aşağıdaki linki kopyalayıp tarayıcınıza yapıştırabilirsiniz:<br>" +
                "<div class=\"token-info\">" + resetUrl + "</div>" +
                "</div>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p><strong>Tendering Platform</strong></p>" +
                "<p>Bu otomatik bir emaildir, lütfen yanıtlamayın.</p>" +
                "<p style=\"font-size: 12px; color: #adb5bd; margin-top: 20px;\">" +
                "Email ID: " + token.substring(0, 8) + "... | Gönderim: " + now.format(formatter) +
                "</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}