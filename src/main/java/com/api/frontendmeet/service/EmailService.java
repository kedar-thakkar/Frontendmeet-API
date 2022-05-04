package com.api.frontendmeet.service;

import com.api.frontendmeet.Entity.MeetingDto;
import com.api.frontendmeet.Entity.MeetingEntity;
import com.api.frontendmeet.Entity.UserDto;
import com.api.frontendmeet.Entity.UserEntity;

public interface EmailService {

	public void sendWelcomeMailToUser(UserDto userDto);

	public void sendInvitationMailToUser(String inviteeemail , MeetingDto meetingDto,
			String email1,String requestType,String action);

	public void sendPasswordResetMailToUser(UserEntity userDto);

	void removeInvitteFromMeeting(String invitee,String action, MeetingEntity meetingEntity);

}