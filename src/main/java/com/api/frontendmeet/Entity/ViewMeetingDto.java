package com.api.frontendmeet.Entity;

import java.time.LocalDateTime;
import java.util.List;

public class ViewMeetingDto {

	private Long meetingId;

	private String meetingTitle;

	private String meetingDesc;

	private String startMeetingTime;

	private String endMeetingTime;

	private String totalMeetingTime;

	private LocalDateTime startDate;

	private LocalDateTime endDate;

	private List<ProfileData> profiledata;

	public Long getMeetingId() {
		return meetingId;
	}

	public void setMeetingId(Long meetingId) {
		this.meetingId = meetingId;
	}

	public String getMeetingTitle() {
		return meetingTitle;
	}

	public void setMeetingTitle(String meetingTitle) {
		this.meetingTitle = meetingTitle;
	}

	public String getMeetingDesc() {
		return meetingDesc;
	}

	public void setMeetingDesc(String meetingDesc) {
		this.meetingDesc = meetingDesc;
	}

	public String getStartMeetingTime() {
		return startMeetingTime;
	}

	public void setStartMeetingTime(String startMeetingTime) {
		this.startMeetingTime = startMeetingTime;
	}

	public String getEndMeetingTime() {
		return endMeetingTime;
	}

	public void setEndMeetingTime(String endMeetingTime) {
		this.endMeetingTime = endMeetingTime;
	}

	public String getTotalMeetingTime() {
		return totalMeetingTime;
	}

	public void setTotalMeetingTime(String totalMeetingTime) {
		this.totalMeetingTime = totalMeetingTime;
	}

	public List<ProfileData> getProfiledata() {
		return profiledata;
	}

	public void setProfiledata(List<ProfileData> profiledata) {
		this.profiledata = profiledata;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}
}
