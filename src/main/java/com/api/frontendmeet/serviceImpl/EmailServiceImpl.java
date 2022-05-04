package com.api.frontendmeet.serviceImpl;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import javax.activation.DataHandler;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.api.frontendmeet.Entity.MeetingDto;
import com.api.frontendmeet.Entity.MeetingEntity;
import com.api.frontendmeet.Entity.UserDto;
import com.api.frontendmeet.Entity.UserEntity;
import com.api.frontendmeet.constant.ApplicationConstant;
import com.api.frontendmeet.repository.MeetingRepository;
import com.api.frontendmeet.repository.UserRepository;
import com.api.frontendmeet.service.EmailService;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Version;

@Service("emailService")
@Transactional
public class EmailServiceImpl implements EmailService {

	public static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

	@Autowired
	Session session;

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private MeetingRepository meetingRepository;

	private final TemplateEngine templateEngine;

	public EmailServiceImpl(TemplateEngine templateEngine, JavaMailSender javaMailSender) {
		this.templateEngine = templateEngine;
	}

	@Override
	@Async
	public void sendWelcomeMailToUser(UserDto userDto) {
		try {
			Context context = new Context();
			context.setVariable("user", userDto);
			context.setVariable("hostname", ApplicationConstant.URL);
			MimeMessage message = new MimeMessage(session);

			message.addRecipient(Message.RecipientType.TO, new InternetAddress(userDto.getEmail()));
			String process = templateEngine.process("Registration/sendwelcomeemail.html", context);

			Multipart multipart = new MimeMultipart();
			BodyPart msg = new MimeBodyPart();
			msg.setDataHandler(new DataHandler(new ByteArrayDataSource(process, "text/html; charset=\"utf-8\"")));
			multipart.addBodyPart(msg);
			message.setSubject("Welcome to Frontendmeet", userDto.getName());
			message.setContent(multipart);

			Transport.send(message);

			logger.info("Mail sent successfully to newly created user {} - verify ", userDto.getEmail());
		} catch (Exception e) {

			logger.error("Problem occured while sending mail , Please check logs : " + e.getMessage());
		}
	}

	@Override
	@Async
	public void sendInvitationMailToUser(String inviteeemail, MeetingDto meetingDto, String organizeremail,
			String requestType, String action) {
		try {
			String roomname;
			Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(inviteeemail);
			if (action == "update" || action == "editsave") {
				roomname = meetingRepository.meetingdata(meetingDto.getMeetingEntity().getMeetingId()).getRoomName();
			} else {
				roomname = meetingDto.getMeetingEntity().getRoomName();
			}
			String username = user.get().getName();
			String meetingtitle = meetingDto.getMeetingEntity().getMeetingTitle();
			String meetingdesc = meetingDto.getMeetingEntity().getMeetingDesc();
			Context context = new Context();
			context.setVariable("roomname", roomname);
			context.setVariable("username", username);
			context.setVariable("email", inviteeemail);
			context.setVariable("hostname", ApplicationConstant.URL);
			context.setVariable("meetingtitle", meetingtitle);
			context.setVariable("meetingdesc", meetingdesc);
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(inviteeemail));

			// Set Subject: header field

			message.setSubject(getSubjectLineFromAction(action , meetingDto.getMeetingEntity().getMeetingTitle()));

			String process = templateEngine.process("Invitation/meetinginvitation.html", context);
			TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
			TimeZone timezone = registry.getTimeZone("UTC");
			VTimeZone tz = timezone.getVTimeZone();

			// Start Date and time
			LocalDateTime startTime = meetingDto.getMeetingEntity().getStartDate();
			Calendar startDate = Calendar.getInstance();
			startDate.clear();
			startDate.set(Calendar.YEAR, startTime.getYear());
			startDate.set(Calendar.MONTH, startTime.getMonthValue() - 1);
			startDate.set(Calendar.DAY_OF_MONTH, startTime.getDayOfMonth());
			startDate.set(Calendar.HOUR_OF_DAY, startTime.getHour());
			startDate.set(Calendar.MINUTE, startTime.getMinute());
			startDate.set(Calendar.SECOND, startTime.getSecond());

			// End Date and time
			LocalDateTime endTime = meetingDto.getMeetingEntity().getEndDate();
			Calendar endDate = Calendar.getInstance();
			endDate.clear();
			endDate.set(Calendar.YEAR, endTime.getYear());
			endDate.set(Calendar.MONTH, endTime.getMonthValue() - 1);
			endDate.set(Calendar.DAY_OF_MONTH, endTime.getDayOfMonth());
			endDate.set(Calendar.HOUR_OF_DAY, endTime.getHour());
			endDate.set(Calendar.MINUTE, endTime.getMinute());
			endDate.set(Calendar.SECOND, endTime.getSecond());

			// Create the event
			String eventName = meetingDto.getMeetingEntity().getMeetingTitle();
			List<String> invitee = meetingDto.getMeetingEntity().getInvites();
			DateTime start = new DateTime(startDate.getTime());
			start.setTimeZone(timezone);
			DateTime end = new DateTime(endDate.getTime());
			end.setTimeZone(timezone);
			VEvent vEvent = new VEvent(start, end, eventName);
			vEvent.getProperties().add(new Sequence(0));
			vEvent.getProperties().add(Status.VEVENT_CONFIRMED);
			vEvent.getProperties().add(Transp.TRANSPARENT);

			for (String participant : invitee) {
				Attendee attendee = new Attendee(URI.create("mailto:" + participant));
				vEvent.getProperties().add(attendee);
			}

			Attendee organizer = new Attendee(URI.create("mailto:" + organizeremail));
			organizer.getParameters().add(Role.REQ_PARTICIPANT);
			organizer.getParameters().add(Rsvp.TRUE);
			organizer.getParameters().add(PartStat.ACCEPTED);
			organizer.getParameters().add(CuType.INDIVIDUAL);
			organizer.getParameters().add(new Cn(organizeremail));
			vEvent.getProperties().add(organizer);

			Organizer orger = new Organizer(URI.create("mailto:" + organizeremail));
			orger.getParameters().add(new Cn(organizeremail));
			vEvent.getProperties().add(orger);

			// add discription
			vEvent.getProperties().add(new Description(meetingDto.getMeetingEntity().getMeetingDesc()));

			// add timezone info..
			vEvent.getProperties().add(tz.getTimeZoneId());

			// Create the message part
			BodyPart messageBodyPart = new MimeBodyPart();

			// Create a Multipart
			Multipart multipart = new MimeMultipart();

			// Add part one
			multipart.addBodyPart(messageBodyPart);

			// Now set the actual message
			net.fortuna.ical4j.model.Calendar icsCalendar = new net.fortuna.ical4j.model.Calendar();
			icsCalendar.getProperties().add(new ProdId(ApplicationConstant.EMAILPRODUCTID));
			icsCalendar.getProperties().add(CalScale.GREGORIAN);
			icsCalendar.getProperties().add(Version.VERSION_2_0);

			if (requestType.equalsIgnoreCase("request"))
				icsCalendar.getProperties().add(Method.REQUEST);

			if (requestType.equalsIgnoreCase("cancel"))
				icsCalendar.getProperties().add(Method.CANCEL);

			// Add the event and print
			icsCalendar.getComponents().add(vEvent);

			messageBodyPart
					.setDataHandler(new DataHandler(new ByteArrayDataSource(icsCalendar.toString(), "text/calendar")));
			BodyPart msg = new MimeBodyPart();
			if (requestType.equals("request")) {
				msg.setDataHandler(new DataHandler(new ByteArrayDataSource(process, "text/html; charset=\"utf-8\"")));
			} else
				msg.setDataHandler(new DataHandler(new ByteArrayDataSource("", "text/html; charset=\"utf-8\"")));
			multipart.addBodyPart(msg);
			message.setContent(multipart);

			// Send message
			Transport.send(message);
			logger.info("Mail sent successfully to invited user {} - "+action, inviteeemail);
		} catch (Exception e) {
			logger.error("Error occured whilr sending mail to invitees , Please check logs : " + e.getStackTrace());
		}
	}

	@Override
	@Async
	public void sendPasswordResetMailToUser(UserEntity userEntity) {

		try {
			Context context = new Context();
			context.setVariable("user", userEntity);
			context.setVariable("hostname", ApplicationConstant.URL);
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(userEntity.getEmail()));

			// Set Subject: header field
			message.setSubject("Reset Password -  Frontendmeet");
			String process = templateEngine.process("ResetPassword/resetpassword.html", context);

			// Now set the actual message
			Multipart multipart = new MimeMultipart();
			BodyPart msg = new MimeBodyPart();
			msg.setDataHandler(new DataHandler(new ByteArrayDataSource(process, "text/html; charset=\"utf-8\"")));
			multipart.addBodyPart(msg);
			message.setContent(multipart);

			// Send message
			Transport.send(message);
			logger.info("Mail sent successfully to forgetPassword{} - Reset", userEntity.getEmail());
		} catch (Exception e) {
			logger.error("Problem occured while sending mail , Please check logs : " + e.getMessage());
		}

	}

	@Override
	@Async
	public void removeInvitteFromMeeting(String invitee,String action,MeetingEntity meetingEntity) {

		try {

			Context context = new Context();
			context.setVariable("hostname", ApplicationConstant.URL);
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(invitee));

			// Set Subject: header field
			message.setSubject(getSubjectLineFromAction(action,meetingEntity.getMeetingTitle()));
			String process = templateEngine.process("RemoveFromMeeting/removeFromMeeting.html", context);
			String replaceString=process.replace("$M2",meetingEntity.getMeetingTitle()).replace("$ST",meetingEntity.getStartMeetingTime()).replace("$ET", meetingEntity.getEndMeetingTime());
			Multipart multipart = new MimeMultipart();
			BodyPart msg = new MimeBodyPart();
			msg.setDataHandler(new DataHandler(new ByteArrayDataSource(replaceString, "text/html; charset=\"utf-8\"")));
			multipart.addBodyPart(msg);
			message.setContent(multipart);

			// Send message
			Transport.send(message);
			logger.info("Mail sent successfully to newly created user {} - remove" , invitee);
		} catch (Exception e) {
			logger.error("Problem occured while sending mail , Please check logs : " + e.getMessage());
		}
	}

	public String getSubjectLineFromAction(String action,String title) {

		switch (action) {
		case "save":
			return title +" - Meeting Invitation";
		case "delete":
			return title +" - Cancelled Meeting";
		case "update":
			return title +" - Updated Meeting Invitation";
		case "remove":
			return title +" - Removed From Meeting";
	    case "editsave":
			return title +" - Meeting Invitation";
		default:
			return title +" - Meeting Invitation";
		}
	}

}
