package com.api.frontendmeet.repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import com.api.frontendmeet.Entity.MeetingEntity;

public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {

	@Transactional
	@Modifying
	@Query("DELETE FROM MeetingEntity m WHERE m.meetingId = :id")
	void deleteFromMeeting(Long id);

	@Query("SELECT m FROM MeetingEntity m WHERE m.meetingId=:meetingId")
	List<MeetingEntity> findBymeetingId(@RequestParam("meetingId") Long meetingId);

	@Query("SELECT DISTINCT m FROM MeetingEntity m inner join MeetingInviteeEntity e "
			+ "on m.meetingId=e.meetingEntity "
			+ "WHERE :meetingdate BETWEEN DATE(m.startDate) AND DATE(m.endDate) AND e.inviteeId = :userId ")
	List<MeetingEntity> findByStartTimeBetweenIgnorecaEntities(@Param("meetingdate") Date meetingdate,
			@Param("userId") Long userId);

	List<MeetingEntity> findAll(Specification<MeetingEntity> hasRoomName);

	@Query("SELECT m FROM MeetingEntity m WHERE m.meetingId=:meetingId")
	MeetingEntity meetingdata(Long meetingId);

	@Query("SELECT m FROM MeetingEntity m WHERE :now <= m.endDate AND m.roomName = :roomName")
	MeetingEntity roomExistsOrNot(LocalDateTime now, String roomName);
	
	@Query("SELECT m FROM MeetingEntity m WHERE m.roomName=:roomName")
	MeetingEntity roomname(String roomName);
}
