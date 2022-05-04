package com.api.frontendmeet.controller;

import java.util.Map;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.api.frontendmeet.service.UserService;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = { "${origins_url}" })
public class UserController {

	public static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	UserService userService;

	@Autowired
	ServletContext servletContext;

	@PostMapping("/editUserName")
	public ResponseEntity<Map<String, Object>> editUserName(@RequestParam("userId") Long userId,
			@RequestParam("name") String name) {
		try {
			logger.info("editUser : " + userId);
			return new ResponseEntity<>(userService.editUserName(userId, name), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error occured while editUserAPI {} :Reason :{}", name, e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/resetPasswordfromOldPassword")
	public ResponseEntity<Map<String, Object>> resetPasswordfromOldPassword(@RequestParam("userId") Long userId,
			@RequestParam("oldpassword") String oldpassword, @RequestParam("newpassword") String newpassword) {
		try {
			logger.info("resetUser : ");
			return new ResponseEntity<>(userService.resetPasswordfromOldPassword(userId, oldpassword, newpassword),
					HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error occured while resetPasswordfromOldPassword {} :Reason :{}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@PostMapping("/uploadProfilePicture")
	public ResponseEntity<Map<String, Object>> uploadProfile(@RequestParam("userId") Long userId,
			@RequestParam("file") MultipartFile multipartFile) {
		try {
			logger.info("uploadProfile() : ");
			return new ResponseEntity<>(userService.uploadProfile(userId, multipartFile), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error occured while uploadProfile {} :Reason :{}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}

	@GetMapping("/getProfilePicture")
	public ResponseEntity<Map<String, Object>> getProfilePicture(@RequestParam("userId") Long userId) {
		try {
			logger.info("getProfilePicture : ");
			return new ResponseEntity<>(userService.getProfilePicture(userId), HttpStatus.OK);
		} catch (Exception e) {
			logger.error("Error occured while getProfile {} :Reason :{}", e.getMessage());
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}