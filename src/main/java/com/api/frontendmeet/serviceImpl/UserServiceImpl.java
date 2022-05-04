package com.api.frontendmeet.serviceImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.api.frontendmeet.Entity.UserEntity;
import com.api.frontendmeet.constant.ApplicationConstant;
import com.api.frontendmeet.repository.UserRepository;
import com.api.frontendmeet.service.UserService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

	@Override
	public Map<String, Object> editUserName(Long userId, String name) {
		Optional<UserEntity> userEntity = userRepository.findById(userId);
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (userEntity != null) {
				UserEntity user = userEntity.get();
				user.setName(name);
				userRepository.save(user);
				map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
				map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.USER_EDIT_SUCCESS);
				map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
			}
		} catch (Exception e) {
			logger.error("Problem occured while editUserName , Please check logs : " + e.getMessage());
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> resetPasswordfromOldPassword(Long userId, String oldpassword, String newpassword) {
		Optional<UserEntity> userEntity = userRepository.findById(userId);
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			if (userEntity != null) {
				UserEntity user = userEntity.get();
				BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
				boolean isPasswordMatches = bcrypt.matches(oldpassword, user.getPassword());
				if (isPasswordMatches) {
					user.setPassword(passwordEncoder.encode(newpassword));
					userRepository.save(user);
					map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
					map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.PASSWORD_EDIT_SUCCESS);
					map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
				} else {
					map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
					map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.OLD_PASSWORD_DOES_NOT_MATCHED);
					map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
				}
			}
		} catch (Exception e) {
			logger.error("Problem occured while resetPasswordfromOldPassword , Please check logs : " + e.getMessage());
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
			map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
		}
		return map;
	}

	@Override
	public Map<String, Object> uploadProfile(Long userId, MultipartFile multipartFile) {
		Map<String, Object> map = new HashMap<String, Object>();
		UserEntity userEntity = userRepository.getById(userId);

		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap("cloud_name", ApplicationConstant.CLOUD_NAME,
				"api_key", ApplicationConstant.API_KEY, "api_secret", ApplicationConstant.API_SECRET));
		try {
			Map uploadResult = cloudinary.uploader().upload(multipartFile.getBytes(),
					ObjectUtils.asMap("public_id", "user_profile/" + userId));

			String url = uploadResult.get("url").toString();
			userEntity.setProfilePic(url);
			userRepository.save(userEntity);
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			map.put(ApplicationConstant.RESPONSE_DATA, url);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.PROFILE_UPLOADED_SUCESSFULLY);
		}

		catch (IOException e) {
			e.printStackTrace();
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
		}

		return map;
	}

	@Override
	public Map<String, Object> getProfilePicture(Long userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		Optional<UserEntity> url = userRepository.findById(userId);
		try {
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_200);
			// map.put(ApplicationConstant.RESPONSE_DATA, url.get().getProfilePic());
			map.put(ApplicationConstant.RESPONSE_DATA, url.get().getProfilePic());
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.PROFILE_IMAGE_SUCESSFULLY);
		} catch (Exception e) {
			logger.error("Problem occured while getProfile , Please check logs : " + e.getMessage());
			map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
			map.put(ApplicationConstant.RESPONSE_MESSAGE, ApplicationConstant.SOMETING_WENT_WRONG);
		}
		return map;
	}
}
