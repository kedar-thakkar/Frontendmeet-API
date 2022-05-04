package com.api.frontendmeet.exception;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.api.frontendmeet.constant.ApplicationConstant;

@RestControllerAdvice
public class TokenControllerAdvice {

	@ExceptionHandler(value = TokenRefreshException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public void handleTokenRefreshException(TokenRefreshException ex, HttpServletRequest request,
			HttpServletResponse response) throws JSONException, IOException {
		response.setContentType("application/json;charset=UTF-8");
		response.getWriter()
				.write(new JSONObject().put("Status", ApplicationConstant.STATUS_400)
						.putOnce("data", new ArrayList<>())
						.put("message", "Refresh token was expired. Please make a new sign in request").toString());
//		Map<String, Object> map = new HashMap<String, Object>();
//		map.put(ApplicationConstant.RESPONSE_STATUS, ApplicationConstant.STATUS_400);
//		map.put(ApplicationConstant.RESPONSE_MESSAGE, "Refresh token was expired!.Please make a new sign in request");
//		map.put(ApplicationConstant.RESPONSE_DATA, new ArrayList<>());
//		return ResponseEntity.ok(map);
	}
}