package com.example.estheteadminservice.controller;

import com.example.estheteadminservice.dto.UserDto;
import com.example.estheteadminservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sign-in")
    public ResponseEntity<UserDto.SignInResponse> signIn(@RequestBody UserDto.SignInRequest signInRequest) {

        UserDto.SignInResponse signInResponse = userService.signIn(signInRequest);

        return ResponseEntity.status(HttpStatus.OK).body(signInResponse);
    }
}
