package tool;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import debug.Log;

public class SendEmail implements Runnable {
	String text;
	String filename;
	String title;

	public SendEmail(String _title, String _text, String _filename) {
		super();
		title = _title;
		text = _text;
		filename = _filename;
	}

	public void run() {
		File attactment = null;
		Properties props = new Properties();
		// 开启debug调试
		props.setProperty("mail.debug", "false");
		// 发送服务器需要身份验证
		props.setProperty("mail.smtp.auth", "true");
		// 设置邮件服务器主机名
		props.setProperty("mail.host", "smtp.163.com");
		// 发送邮件协议名称
		props.setProperty("mail.transport.protocol", "smtp");

		// 设置环境信息
		Session session = Session.getInstance(props);
		session.setDebug(false);

		// 创建邮件对象
		Message msg = new MimeMessage(session);
		try {
			msg.setFrom(new InternetAddress("simulator_test@163.com"));

			msg.setSubject(title);

			// create and fill the first message part
			MimeBodyPart mbp1 = new MimeBodyPart();
			mbp1.setText(text);

			MimeBodyPart mbp2 = null;
			if (filename != null) {
				attactment = new File(filename);
				if(attactment.exists() == true){
					// create the second message part
					mbp2 = new MimeBodyPart();

					// attach the file to the message
					try {
						mbp2.attachFile(attactment);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Log.e("File attatch excepiton is happened");
						filename = null;
						e.printStackTrace();
					}
				}
			}

			// create the multi part and add its parts to it
			Multipart mp = new MimeMultipart();
			mp.addBodyPart(mbp1);
			if (mbp2 != null) {
				mp.addBodyPart(mbp2);
			}

			// add the multi part to the message
			msg.setContent(mp);

			// set the Date: header
			msg.setSentDate(new Date());

			Transport transport = session.getTransport();
			// 连接邮件服务器
			transport.connect("simulator_test", "simulator123");
			// 发送邮件
			transport.sendMessage(msg, new Address[] {
					new InternetAddress("suxiaocheng2010@hotmail.com"),
					new InternetAddress("simulator_test@163.com") });
			// 关闭连接
			transport.close();

			Log.d("SendMail " + text + " successfully");

		} catch (MessagingException e1) {
			// TODO Auto-generated catch block
			Log.e("Exception is happened: " + e1.getMessage());
			e1.printStackTrace();
		}
		Log.d("SendMail thread is quit");
	}
}
