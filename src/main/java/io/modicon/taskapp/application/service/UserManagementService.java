package io.modicon.taskapp.application.service;

import io.modicon.taskapp.web.dto.UserLoginRequest;
import io.modicon.taskapp.web.dto.UserLoginResponse;
import io.modicon.taskapp.web.dto.UserRegisterRequest;
import io.modicon.taskapp.web.dto.UserRegisterResponse;

public interface UserManagementService {

    UserRegisterResponse register(UserRegisterRequest request);

    UserLoginResponse login(UserLoginRequest request);

}
