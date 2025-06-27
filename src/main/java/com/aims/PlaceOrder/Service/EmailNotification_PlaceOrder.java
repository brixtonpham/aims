package Project_ITSS.PlaceOrder.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;




@Service
public class EmailNotification_PlaceOrder {
    @Autowired
    private JavaMailSender javaMailSender;
    public void SendSuccessEmail(String toEmail,String subject,String content){
        SimpleMailMessage message = new SimpleMailMessage();
        System.out.println(toEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(content);
        javaMailSender.send(message);
    }
} 