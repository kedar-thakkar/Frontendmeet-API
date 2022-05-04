package com.api.frontendmeet.service;

import java.util.Date;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.api.frontendmeet.Entity.MeetingDto;
import com.api.frontendmeet.Entity.MeetingEntity;
import com.api.frontendmeet.Entity.UserEntity;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface MeetingService {

	public Map<String, Object> saveMeeting(MeetingDto meetingDto);

	public Map<String, Object> deleteMeeting(Long meetingId);

	public Map<String, Object> editMeeting(MeetingDto meetingDto);

	public UserEntity populateGuestUserData(String invitee);

	public MeetingEntity getByMeetingId(Long meetingId) throws JsonProcessingException;

	public Map<String, Object> getMeetingByDateAPI(Date meetingDate, Integer userId, String searchText);

	public Map<String, Object> validateRoom(String roomName,String email,String username);

	public Map<String, Object> viewMeetingDetail(Long meetingId);

	public Map<String, Object> addParticipant(MeetingDto meetingDto);
	
	public Map<String, Object> getEmailList(String searchText);
}
