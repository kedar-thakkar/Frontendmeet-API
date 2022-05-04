package com.api.frontendmeet.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.api.frontendmeet.Entity.MeetingDto;
import com.api.frontendmeet.Entity.MeetingEntity;
import com.api.frontendmeet.repository.MeetingRepository;
import com.api.frontendmeet.service.MeetingService;

@RestController
@RequestMapping("/meeting")
@CrossOrigin(origins = { "${origins_url}" })
public class MeetingController {

	public static final Logger logger = LoggerFactory.getLogger(MeetingController.class);

	@Autowired
	MeetingService meetingService;

	@Autowired
	MeetingRepository meetingRepository;

	@PostMapping("/saveMeeting")
	public ResponseEntity<Map<String, Object>> saveMeeting(@RequestBody MeetingDto meetingDto) {
		try {
			logger.info("Inside saveMeeting : " + meetingDto.getMeetingEntity().getUser());
			return new ResponseEntity<>(meetingService.saveMeeting(meetingDto), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while save meeting {} :Reason :{}",
					// meetingDto.getMeetingTitle(),
					e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/deleteMeeting")
	public ResponseEntity<Map<String, Object>> deleteMeeting(@RequestParam(value = "meetingId") Integer meetingId) {
		try {
			logger.info("Inside deleteMeeting userId : " + meetingId);
			return new ResponseEntity<>(meetingService.deleteMeeting(meetingId.longValue()), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while deleteMeetingAPI userId {} :Reason :{}", meetingId, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/editMeeting")
	public ResponseEntity<Map<String, Object>> editMeeting(@RequestBody MeetingDto meetingDto) {
		try {
			logger.info("editMeeting : " + meetingDto.getMeetingEntity().getUser());
			return new ResponseEntity<>(meetingService.editMeeting(meetingDto), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while editMeetingAPI meeting {} :Reason :{}",
					meetingDto.getMeetingEntity().getMeetingTitle(), e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/addParticipant")
	public ResponseEntity<Map<String, Object>> addParticipant(@RequestBody MeetingDto meetingDto) {
		try {
			logger.info("addParticipant : " + meetingDto.getMeetingEntity().getUser());
			return new ResponseEntity<>(meetingService.addParticipant(meetingDto), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while editMeetingAPI meeting {} :Reason :{}",
					meetingDto.getMeetingEntity().getMeetingTitle(), e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/getMeetingById")
	public ResponseEntity<MeetingEntity> getMeetingById(@RequestParam(value = "meetingId") Long meetingId) {
		try {
			logger.info("Inside getMeetingById : " + meetingId);
			return new ResponseEntity<>(meetingService.getByMeetingId(meetingId), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while getMeetingById {} :Reason :{}", meetingId, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/getMeetingListByDateAPI")
	public ResponseEntity<Map<String, Object>> getMeetingListByDateAPI(@RequestBody String userData) {
		Date meetingDate = null;
		try {
			JSONObject jsonObject = new JSONObject(userData);
			String search = null;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String meetingDateData = jsonObject.getString("meetingDate");
			meetingDate = dateFormat.parse(meetingDateData);
			Integer userId = jsonObject.getInt("userId");

			if (!jsonObject.isNull("search")) {
				search = jsonObject.getString("search");
			}
			logger.info("Inside getMeetingListByDateAPI : " + userData);
			return new ResponseEntity<>(meetingService.getMeetingByDateAPI(meetingDate, userId, search), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error occured while getMeetingListByDateAPI {} :Reason :{}", meetingDate, e.getMessage());
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/validateRoom")
	public ResponseEntity<Map<String, Object>> validateRoom(@RequestParam(value = "roomName") String roomName,
			@RequestParam(value = "email") String email, @RequestParam(value = "username") String username) {
		try {
			logger.info("Inside validateRoom  : " + roomName);
			return new ResponseEntity<>(meetingService.validateRoom(roomName,email,username), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while validateRoom roomaName {} :Reason :{}", roomName, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/viewMeetingDetail")
	public ResponseEntity<Map<String, Object>> viewMeetingDetail(@RequestParam(value = "meetingId") Long meetingId) {
		try {
			logger.info("Inside viewMeetingDetail  : " + meetingId);
			return new ResponseEntity<>(meetingService.viewMeetingDetail(meetingId), HttpStatus.OK);

		} catch (Exception e) {
			logger.error("Error occured while viewMeetingDetail meetingId {} :Reason :{}", meetingId, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/getEmailList")
	public ResponseEntity<Map<String, Object>> getEmailList(@RequestParam String searchText) {
		try {
			logger.info("Inside getEmailListBysearchtext : " + searchText);
			return new ResponseEntity<>(meetingService.getEmailList(searchText), HttpStatus.OK);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

}
