package com.api.frontendmeet.serviceImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.api.frontendmeet.Entity.MeetingDto;
import com.api.frontendmeet.Entity.MeetingEntity;
import com.api.frontendmeet.Entity.MeetingInviteeEntity;
import com.api.frontendmeet.Entity.ProfileData;
import com.api.frontendmeet.Entity.UserEntity;
import com.api.frontendmeet.Entity.ViewMeetingDto;
import com.api.frontendmeet.constant.ApplicationConstant;
import com.api.frontendmeet.repository.MeetingInviteeRepository;
import com.api.frontendmeet.repository.MeetingRepository;
import com.api.frontendmeet.repository.UserRepository;
import com.api.frontendmeet.service.EmailService;
import com.api.frontendmeet.service.MeetingService;

@Service
public class MeetingSeviceImpl implements MeetingService {

	public static final Logger logger = LoggerFactory.getLogger(MeetingSeviceImpl.class);

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private MeetingInviteeRepository meetingInviteeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EmailService emailService;

	@Override
	public Map<String, Object> saveMeeting(MeetingDto meetingDto) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<String> emails = new ArrayList<String>();

		if (meetingDto != null) {
			MeetingEntity meetingEntity = populateMeetingData(meetingDto);
			MeetingEntity DbMeetingEntity = meetingRepository.save(meetingEntity);
			if (DbMeetingEntity != null) {
				List<String> inviteeList = meetingDto.getMeetingEntity().getInvites().stream().distinct()
						.collect(Collectors.toList());
				String organizeremail = userRepository.getemailid(meetingDto.getMeetingEntity().getUser().getId());
				for (String invitee : inviteeList) {
					Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(invitee);

					if (user.isPresent()) {
						UserEntity userEntity = user.get();
						emails.add(userEntity.getEmail());
						emailService.sendInvitationMailToUser(userEntity.getEmail(), meetingDto, organizeremail,
								"request", "save");
					}
				}
			}
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_SAVE_SUCCESSFULLY);
			map.put(ApplicationConstant.RESPONSE_DATA, DbMeetingEntity);

		} else {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_NOT_SAVED);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());

		}
		return map;
	}

	@Override
	public UserEntity populateGuestUserData(final String invitee) {
		UserEntity user = new UserEntity();
		String name = StringUtils.substringBefore(invitee, "@");
		user.setName(name);
		user.setEmail(invitee);
		user.setIsGuest(true);
		return user;
	}

	private MeetingEntity populateMeetingData(MeetingDto meetingDto) {
		MeetingEntity meetingEntity = new MeetingEntity();
		String startMeetingTime = getTimeFromDate(meetingDto.getMeetingEntity().getStartDate());
		String endMeetingTime = getTimeFromDate(meetingDto.getMeetingEntity().getEndDate());
		String totalMeetingTime = getDifferenceBetweenDate(meetingDto.getMeetingEntity().getStartDate(),
				meetingDto.getMeetingEntity().getEndDate());
		meetingEntity.setRoomName(meetingDto.getMeetingEntity().getRoomName());
		meetingEntity.setMeetingTitle(meetingDto.getMeetingEntity().getMeetingTitle());
		meetingEntity.setUser(meetingDto.getMeetingEntity().getUser());
		meetingEntity.setMeetingDesc(meetingDto.getMeetingEntity().getMeetingDesc());
		meetingEntity.setStartTime(meetingDto.getMeetingEntity().getStartDate());
		meetingEntity.setEndTime(meetingDto.getMeetingEntity().getEndDate());
		meetingEntity.setCreatedOn(LocalDateTime.now());
		meetingEntity.setModifiedOn(LocalDateTime.now());
		meetingEntity.setStartMeetingTime(startMeetingTime);
		meetingEntity.setEndMeetingTime(endMeetingTime);
		meetingEntity.setTotalMeetingTime(totalMeetingTime);

		List<String> inviteeList = meetingDto.getMeetingEntity().getInvites().stream().distinct()
				.collect(Collectors.toList());
		for (String inv : inviteeList) {
			Optional<UserEntity> dbUser = userRepository.findByEmailIgnoreCase(inv);

			// adding guest user in table with email id
			if (!dbUser.isPresent()) {
				UserEntity userModel = populateGuestUserData(inv);
				userRepository.save(userModel);
			}
			Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(inv);
			if (user.isPresent()) {
				UserEntity userEntity = user.get();
				MeetingInviteeEntity meetingInviteeEntity = new MeetingInviteeEntity();
				meetingInviteeEntity.setMeetingEntity(meetingEntity);
				meetingInviteeEntity.setInviteeId(userEntity.getId());
				meetingInviteeRepository.save(meetingInviteeEntity);
			}
		}
		return meetingEntity;
	}

	@Override
	public Map<String, Object> deleteMeeting(Long meetingId) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			MeetingDto meetingDtoObj = new MeetingDto();
			MeetingEntity meeitngObj = getByMeetingId(meetingId);
			meetingDtoObj.setMeetingEntity(meeitngObj);
			List<String> invitees = meeitngObj.getInvites();
			String organizeremail = userRepository.getemailid(meeitngObj.getUser().getId());
			for (String inv : invitees) {
				emailService.sendInvitationMailToUser(inv, meetingDtoObj, organizeremail, "cancel", "delete");
			}
			meetingInviteeRepository.deleteFromInvitee(meetingId);
			meetingRepository.deleteFromMeeting(meetingId);
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_DELETED_SUCCESSFULLY);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		} catch (Exception e) {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> editMeeting(MeetingDto meetingDto) {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (meetingDto != null && meetingDto.getMeetingEntity() != null
					&& meetingDto.getMeetingEntity().getMeetingId() != null) {
				MeetingEntity meetingEntity = populateMeetingDataForEdit(meetingDto);
				meetingEntity.setMeetingId(meetingDto.getMeetingEntity().getMeetingId());
				meetingRepository.save(meetingEntity);
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_EDIT_SUCCESSFULLY);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			} else {
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_EDIT_FAILURE);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}
		} catch (Exception e) {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	private MeetingEntity populateMeetingDataForEdit(MeetingDto meetingDto) {
		MeetingEntity meetingEntity = new MeetingEntity();
		MeetingEntity meetingdata = meetingRepository.meetingdata(meetingDto.getMeetingEntity().getMeetingId());
		String startMeetingTime = getTimeFromDate(meetingDto.getMeetingEntity().getStartDate());
		String endMeetingTime = getTimeFromDate(meetingDto.getMeetingEntity().getEndDate());
		String totalMeetingTime = getDifferenceBetweenDate(meetingDto.getMeetingEntity().getStartDate(),
				meetingDto.getMeetingEntity().getEndDate());
		meetingEntity.setCreatedOn(meetingdata.getCreatedOn());
		meetingEntity.setRoomName(meetingdata.getRoomName());
		meetingEntity.setMeetingTitle(meetingDto.getMeetingEntity().getMeetingTitle());
		meetingEntity.setUser(meetingDto.getMeetingEntity().getUser());
		meetingEntity.setMeetingDesc(meetingDto.getMeetingEntity().getMeetingDesc());
		meetingEntity.setStartTime(meetingDto.getMeetingEntity().getStartDate());
		meetingEntity.setEndTime(meetingDto.getMeetingEntity().getEndDate());
		meetingEntity.setModifiedOn(LocalDateTime.now());
		meetingEntity.setStartMeetingTime(startMeetingTime);
		meetingEntity.setEndMeetingTime(endMeetingTime);
		meetingEntity.setTotalMeetingTime(totalMeetingTime);

		if (meetingDto.getMeetingEntity().getMeetingId() != null) {
			meetingEntity.setMeetingId(meetingDto.getMeetingEntity().getMeetingId());
		}

		List<MeetingInviteeEntity> existingInviteeEntity = meetingInviteeRepository
				.findbyMeetingId(meetingDto.getMeetingEntity().getMeetingId());

		List<Long> existingInviteeId = meetingInviteeRepository
				.findinviteeid(meetingDto.getMeetingEntity().getMeetingId());
		List<String> existingInviteeEmail = new ArrayList<>();

		// retrive existing invitee emailID
		for (Long old : existingInviteeId) {
			existingInviteeEmail.add(userRepository.getemailid(old));
		}

		// deleted exisitng invitee
		meetingInviteeRepository.deleteAll(existingInviteeEntity);

		String organizeremail = userRepository.getemailid(meetingDto.getMeetingEntity().getUser().getId());

		List<String> updatedInviteeList = meetingDto.getMeetingEntity().getInvites().stream().distinct()
				.collect(Collectors.toList());

		// find newly added invitee list & sent mail
		List<String> newluAddedInviteeList = new ArrayList<>(updatedInviteeList);
		newluAddedInviteeList.removeAll(existingInviteeEmail);
		if (!newluAddedInviteeList.isEmpty()) {
			for (String invitee : newluAddedInviteeList) {
				Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(invitee);
				// adding guest user in table with email id
				if (!user.isPresent()) {
					UserEntity userModel = populateGuestUserData(invitee);
					userRepository.save(userModel);
				}
				emailService.sendInvitationMailToUser(invitee, meetingDto, organizeremail, "request", "editsave");
			}
		}
		// find already prsent user in meeitng
		if (!meetingdata.getStartDate().equals(meetingDto.getMeetingEntity().getStartDate())
				|| !meetingdata.getEndDate().equals(meetingDto.getMeetingEntity().getEndDate())
				|| !meetingdata.getMeetingDesc().equals(meetingDto.getMeetingEntity().getMeetingDesc())
				|| !meetingdata.getMeetingTitle().equals(meetingDto.getMeetingEntity().getMeetingTitle())
				|| !updatedInviteeList.equals(existingInviteeEmail)) {
			List<String> existingemail = new ArrayList<>(updatedInviteeList);
			existingemail.removeAll(newluAddedInviteeList);
			if (!existingemail.isEmpty()) {
				for (String exist : existingemail) {
					emailService.sendInvitationMailToUser(exist, meetingDto, organizeremail, "request", "update");
				}
			}
		}

		// find removed invitee from edited meeting & sent mail
		List<String> removedinvitee = new ArrayList<>(existingInviteeEmail);
		removedinvitee.removeAll(updatedInviteeList);
		if (!removedinvitee.isEmpty()) {
			for (String remove : removedinvitee) {
				emailService.removeInvitteFromMeeting(remove, "remove", meetingEntity);
			}
		}
		// save updated data into table
		for (String inv : updatedInviteeList) {
			Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(inv);
			MeetingInviteeEntity meetingInviteeEntity = new MeetingInviteeEntity();
			meetingInviteeEntity.setMeetingEntity(meetingEntity);
			meetingInviteeEntity.setInviteeId(user.get().getId());
			meetingInviteeRepository.addMeetingInvitee(meetingEntity.getMeetingId(), user.get().getId());
		}
		return meetingEntity;
	}

	@Override
	public MeetingEntity getByMeetingId(Long meetingId) {
		MeetingEntity meetingEntity = meetingRepository.getOne(meetingId);
		List<MeetingInviteeEntity> meetingInviteeEntities = meetingInviteeRepository
				.findbyMeetingId(meetingEntity.getMeetingId());
		List<String> inviteeEmail = new ArrayList<>();

		for (MeetingInviteeEntity meetingInviteeEntity : meetingInviteeEntities) {
			UserEntity userData = userRepository.getOne(meetingInviteeEntity.getInviteeId());
			inviteeEmail.add(userData.getEmail());
		}
		meetingEntity.setInvites(inviteeEmail);
		return populateMeetingEntityData(meetingEntity);
	}

	private MeetingEntity populateMeetingEntityData(MeetingEntity meetingEntityData) {
		MeetingEntity meetingEntity = new MeetingEntity();
		meetingEntity.setMeetingId(meetingEntityData.getMeetingId());
		meetingEntity.setRoomName(meetingEntityData.getRoomName());
		meetingEntity.setMeetingTitle(meetingEntityData.getMeetingTitle());
		meetingEntity.setUser(meetingEntityData.getUser());
		meetingEntity.setMeetingDesc(meetingEntityData.getMeetingDesc());
		meetingEntity.setCreatedOn(meetingEntityData.getCreatedOn());
		meetingEntity.setModifiedOn(meetingEntityData.getModifiedOn());
		meetingEntity.setInvites(meetingEntityData.getInvites());
		meetingEntity.setStartTime(meetingEntityData.getStartDate());
		meetingEntity.setEndTime(meetingEntityData.getEndDate());
		meetingEntity.setEndMeetingTime(meetingEntityData.getEndMeetingTime());
		meetingEntity.setStartMeetingTime(meetingEntityData.getStartMeetingTime());
		meetingEntity.setTotalMeetingTime(meetingEntityData.getTotalMeetingTime());
		return meetingEntity;
	}

	@Override
	public Map<String, Object> getMeetingByDateAPI(Date meetingDate, Integer userId, String searchText) {
		Map<String, Object> map = new HashMap<String, Object>();

		List<MeetingEntity> meetingEntity = meetingRepository.findByStartTimeBetweenIgnorecaEntities(meetingDate,
				userId.longValue());
		for (MeetingEntity meetingEntityobj : meetingEntity) {
			List<MeetingInviteeEntity> meetingInviteeEntities = meetingInviteeRepository
					.findbyMeetingId(meetingEntityobj.getMeetingId());
			List<String> inviteeEmail = new ArrayList<>();

			for (MeetingInviteeEntity meetingInviteeEntity : meetingInviteeEntities) {
				UserEntity userData = userRepository.getOne(meetingInviteeEntity.getInviteeId());
				inviteeEmail.add(userData.getEmail());
			}
			meetingEntityobj.setInvites(inviteeEmail);
		}

		map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
		if (searchText != null) {
			List<MeetingEntity> entity = meetingEntity.stream()
					.filter(contact -> contact.getMeetingTitle().toLowerCase().contains(searchText.toLowerCase()))
					.collect(Collectors.toList());
			if (entity.size() > 0) {
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_LIST_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, entity);
			} else {
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_LIST_NOT_FOUND);
				map.put(ApplicationConstant.RESPONSE_DATA, entity);
			}
		} else {
			if (meetingEntity.size() > 0) {
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_LIST_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, meetingEntity);
			} else {
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_LIST_NOT_FOUND);
				map.put(ApplicationConstant.RESPONSE_DATA, meetingEntity);
			}
		}
		return map;
	}

	@Override
	public Map<String, Object> validateRoom(String roomName,String email,String username) {
		Map<String, Object> map = new HashMap<String, Object>();
		MeetingEntity meetingEntity = new MeetingEntity();
		String prelaunchURL=ApplicationConstant.URL + "/meeting?username=" + username + "&email="+email+"&roomname="+roomName;
		LocalDateTime now = LocalDateTime.now();
		meetingEntity = meetingRepository.roomExistsOrNot(now, roomName);
		if (meetingEntity != null) {
			map.put(ApplicationConstant.RESPONSE_DATA, prelaunchURL);
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_ROOM_EXISTS);
		} else {
			map.put("meetingName",meetingRepository.roomname(roomName).getMeetingTitle());
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_ROOM_NOT_EXISTS);
		}
		return map;
	}

	@Override
	public Map<String, Object> viewMeetingDetail(Long meetingId) {
		Map<String, Object> map = new HashMap<String, Object>();
		ViewMeetingDto viewmeetingdto = new ViewMeetingDto();

		MeetingEntity meetingEntity = meetingRepository.meetingdata(meetingId);
		List<MeetingInviteeEntity> meetingInviteeEntities = meetingInviteeRepository
				.findbyMeetingId(meetingEntity.getMeetingId());

		List<ProfileData> profileDataObj = new ArrayList<ProfileData>();
		for (MeetingInviteeEntity meetingInviteeEntity : meetingInviteeEntities) {
			UserEntity userData = userRepository.getOne(meetingInviteeEntity.getInviteeId());
			ProfileData profiledata = new ProfileData();
			profiledata.setName(userData.getName());
			profiledata.setProfilePic(userData.getProfilePic());
			if (meetingEntity.getUser().getId().equals(userData.getId())) {
				profiledata.setIshost(true);
			} else {
				profiledata.setIshost(false);
			}
			profileDataObj.add(profiledata);
		}
		viewmeetingdto.setProfiledata(profileDataObj);
		viewmeetingdto.setStartMeetingTime(meetingEntity.getStartMeetingTime());
		viewmeetingdto.setEndMeetingTime(meetingEntity.getEndMeetingTime());
		viewmeetingdto.setMeetingDesc(meetingEntity.getMeetingDesc());
		viewmeetingdto.setMeetingId(meetingEntity.getMeetingId());
		viewmeetingdto.setMeetingTitle(meetingEntity.getMeetingTitle());
		viewmeetingdto.setTotalMeetingTime(meetingEntity.getTotalMeetingTime());
		viewmeetingdto.setStartDate(meetingEntity.getStartDate());
		viewmeetingdto.setEndDate(meetingEntity.getEndDate());
		map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
		map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_LIST_SUCCESS);
		map.put(ApplicationConstant.RESPONSE_DATA, viewmeetingdto);

		return map;
	}

	static Specification<MeetingEntity> hasRoomName(String roomName) {
		return (users, cq, cb) -> cb.or(cb.like(cb.lower(users.get("roomName")), "%" + roomName.toLowerCase() + "%"));
	}

	public static String getTimeFromDate(LocalDateTime d1) {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		String Time = null;
		Time = d1.format(dateTimeFormatter);
		return Time;
	}

	public static String getDifferenceBetweenDate(LocalDateTime start, LocalDateTime end) {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		Date startTime = null;
		Date endTime = null;

		try {
			startTime = simpleDateFormat.parse(start.toString());
			endTime = simpleDateFormat.parse(end.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long diff = endTime.getTime() - startTime.getTime();
		String diffHours = (String.format("%d hr %d min", diff / (1000 * 60 * 60),
				(diff % (1000 * 60 * 60)) / (1000 * 60)));
		return diffHours;
	}

	@Override
	public Map<String, Object> addParticipant(MeetingDto meetingDto) {
		Map<String, Object> map = new HashMap<String, Object>();
		MeetingEntity meetingEntity = meetingRepository.getById(meetingDto.getMeetingEntity().getMeetingId());
		MeetingDto existmeetingDto = new MeetingDto();
		existmeetingDto.setMeetingEntity(meetingEntity);

		try {
			if (meetingDto != null && meetingDto.getMeetingEntity() != null
					&& meetingDto.getMeetingEntity().getMeetingId() != null) {
				List<String> invitee = meetingDto.getMeetingEntity().getInvites();
				String newParticipant = "";
				for (String inv : invitee) {
					newParticipant = inv;
				}

				// adding guest user in table with email id
				Optional<UserEntity> user = userRepository.findByEmailIgnoreCase(newParticipant);
				// adding guest user in table with email id
				if (!user.isPresent()) {
					UserEntity userModel = populateGuestUserData(newParticipant);
					userRepository.save(userModel);
				}
				Optional<UserEntity> userEntity = userRepository.findByEmailIgnoreCase(newParticipant);
				MeetingInviteeEntity meetingInviteeEntity = new MeetingInviteeEntity();

				meetingInviteeEntity.setInviteeId(userEntity.get().getId());
				meetingInviteeRepository.addMeetingInvitee(meetingDto.getMeetingEntity().getMeetingId(),
						userEntity.get().getId());
				String organizeremail = userRepository.getemailid(meetingDto.getMeetingEntity().getUser().getId());
				if (!invitee.isEmpty()) {
					List<Long> existingInviteeId = meetingInviteeRepository
							.findinviteeid(meetingDto.getMeetingEntity().getMeetingId());
					List<String> existingInviteeEmail = new ArrayList<>();
					meetingEntity.setInvites(existingInviteeEmail);
					// retrive existing invitee emailID
					for (Long old : existingInviteeId) {
						existingInviteeEmail.add(userRepository.getemailid(old));
					}
					existmeetingDto.setMeetingEntity(meetingEntity);
					emailService.sendInvitationMailToUser(newParticipant, existmeetingDto, organizeremail, "request",
							"save");

					List<String> existingemail = new ArrayList<>(existingInviteeEmail);
					existingemail.removeAll(invitee);
					if (!existingemail.isEmpty()) {
						for (String exist : existingemail) {
							emailService.sendInvitationMailToUser(exist, existmeetingDto, organizeremail, "request",
									"update");
						}
					}
				}
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_EDIT_SUCCESSFULLY);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			} else {
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.MEETING_EDIT_FAILURE);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}

		} catch (Exception e) {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> getEmailList(String searchText) {
		List<String> email = userRepository.findByEmail(searchText);
		Map<String, Object> map = new HashMap<String, Object>();

		map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
		map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.EMAIL_LIST_SUCCESS);
		map.put(ApplicationConstant.RESPONSE_DATA, email);
		return map;
	}

}
