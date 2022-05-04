package com.api.frontendmeet.serviceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.api.frontendmeet.Entity.UserDto;
import com.api.frontendmeet.Entity.UserEntity;
import com.api.frontendmeet.constant.ApplicationConstant;
import com.api.frontendmeet.repository.UserRepository;
import com.api.frontendmeet.service.EmailService;
import com.api.frontendmeet.service.RegistrationService;

@Service
public class RegistrationServiceImpl implements RegistrationService {

	@Autowired
	EmailService emailService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	public Map<String, Object> saveUser(final UserDto userData) {
		Map<String, Object> map = new HashMap<String, Object>();

		Optional<UserEntity> userEntity = userRepository.findOneByEmailIgnoreCase(userData.getEmail());
		// for new user
		if (!userEntity.isPresent()) {
			UserEntity entity = userRepository.save(populateUserData(userData));

			if (entity != null) {
				userData.setPassword(entity.getPassword());
				emailService.sendWelcomeMailToUser(userData);
			}
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.REGISTRATION_SUCCESS);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());

			// for guest user
		} else if (userEntity.isPresent() && userEntity.get().getIsGuest().equals(true)) {

				UserEntity user = userEntity.get();
				user.setName(userData.getName());
				user.setPassword(passwordEncoder.encode(userData.getPassword()));
				user.setIsVerified(false);
				user.setIsGuest(false);
				UserEntity entity = userRepository.save(user);

				if (entity != null) {
					userData.setPassword(entity.getPassword());
					emailService.sendWelcomeMailToUser(userData);
				}
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.GUEST_USER_CONVERTING_INTO_REGISTRATION_FLOW);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		} else {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.REGISTRATION_EMAIL_EXISTS);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	public UserEntity populateUserData(final UserDto userData) {
		UserEntity user = new UserEntity();
		user.setName(userData.getName());
		user.setEmail(userData.getEmail());
		user.setPassword(passwordEncoder.encode(userData.getPassword()));
		user.setIsVerified(false);
		user.setIsGuest(false);
		return user;
	}

	@Override
	public Map<String, Object> verifyAccount(String email, String password) {

		Map<String, Object> map = new HashMap<String, Object>();
		Optional<UserEntity> userEntity = userRepository.findOneByEmailIgnoreCase(email);
		if (userEntity.isPresent()) {
			UserEntity userData = userEntity.get();
			if (userData.getPassword().equalsIgnoreCase(password)) {
				userData.setIsVerified(true);
				userRepository.save(userData);
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.USER_VERIFICATION_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}
		} else {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.USER_VERIFICATION_FAILURE);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> forgetPassword(String email) {
		Map<String, Object> map = new HashMap<String, Object>();
		Optional<UserEntity> userEntity = userRepository.findOneByEmailIgnoreCase(email);
		if (userEntity.isPresent()) {
			UserEntity userData = userEntity.get();
			if (userData.getIsVerified().equals(Boolean.TRUE)) {
				emailService.sendPasswordResetMailToUser(userData);
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.FORGOT_PASSWORD_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			} else {
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.LOGIN_NOT_VERIFIED);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}
		} else {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.FORGOT_PASSWORD_EMAIL_NOT_EXISTS);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> resetPassword(String email, String password, String oldPassword) {
		Map<String, Object> map = new HashMap<String, Object>();
		Optional<UserEntity> userEntity = userRepository.findOneByEmailIgnoreCase(email);
		if (userEntity.isPresent()) {
			UserEntity userModel = userEntity.get();
			if (userModel.getPassword().equalsIgnoreCase(oldPassword)) {
				userModel.setPassword(passwordEncoder.encode(password));
				userRepository.save(userModel);
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.RESET_PASSWORD_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			} else {
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.OLD_PASSWORD_NOT_MATCHED);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}
		} else {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.USER_NOT_FOUND);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

}