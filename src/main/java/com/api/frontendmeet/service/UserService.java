package com.api.frontendmeet.service;

import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {

	public Map<String, Object> editUserName(Long userId, String name);

	public Map<String, Object> resetPasswordfromOldPassword(Long userId, String oldpassword, String newpassword);

	public Map<String, Object> uploadProfile(Long userId, MultipartFile multipartFile);

	public Map<String, Object> getProfilePicture(Long userId);

}
