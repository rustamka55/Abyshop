package com.web.jwtauth.controllers;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import com.web.jwtauth.exception.TokenRefreshException;
import com.web.jwtauth.models.*;
import com.web.jwtauth.payload.request.*;
import com.web.jwtauth.payload.response.StatusReponse;
import com.web.jwtauth.payload.response.TokenRefreshResponse;
import com.web.jwtauth.repository.SessionRepository;
import com.web.jwtauth.security.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.web.jwtauth.payload.response.JwtResponse;
import com.web.jwtauth.payload.response.MessageResponse;
import com.web.jwtauth.repository.RoleRepository;
import com.web.jwtauth.repository.UserRepository;
import com.web.jwtauth.security.jwt.JwtUtils;
import com.web.jwtauth.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    RefreshTokenService refreshTokenService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(userDetails);


        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());
        return ResponseEntity.ok(new JwtResponse(jwt,
                refreshToken.getToken(),
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok().body(new TokenRefreshResponse(token, requestRefreshToken)) ;
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PutMapping("/changePassword")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest httpServletRequest){
        String newPassword = changePasswordRequest.getPassword();
        String oldPassword = changePasswordRequest.getOldPassword();

        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            if(user.get().getPassword().equals(encoder.encode(oldPassword))) {
                user.get().setPassword(encoder.encode(changePasswordRequest.getPassword()));
                userRepository.save(user.get());
                return ResponseEntity.ok().body(new StatusReponse(true));
            }
            else {
                return ResponseEntity.badRequest().body(new StatusReponse(false));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }

    @PostMapping("/addSession")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addSession(@RequestBody AddSessionRequest addSessionRequest, HttpServletRequest httpServletRequest){
        String time = addSessionRequest.getTime();
        String duration = addSessionRequest.getDuration();
        String date = addSessionRequest.getDate();
        String room = addSessionRequest.getRoom();

        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            Session session = new Session(time,duration,date,room,user.get());
            sessionRepository.save(session);
            return ResponseEntity.ok().body(new StatusReponse(true));
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }

    @GetMapping("/getSessions")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getSessions(HttpServletRequest httpServletRequest){
        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            List<Session> sessions = sessionRepository.findSessionsByUser(user.get());
            return ResponseEntity.ok().body(sessions);
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }

    @PutMapping("/changeEmail")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> changePassword(@RequestBody ChangeEmailRequest changeEmailRequest, HttpServletRequest httpServletRequest){
        String password = changeEmailRequest.getPassword();
        String newEmail = changeEmailRequest.getEmail();

        String headerAuth = httpServletRequest.getHeader("Authorization");
        String jwt = null;
        String username = null;
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            jwt =  headerAuth.substring(7, headerAuth.length());
        }
        if (jwt != null && jwtUtils.validateJwtToken(jwt)) {
            username = jwtUtils.getUserNameFromJwtToken(jwt);
        }
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isPresent()) {
            if(user.get().getPassword().equals(encoder.encode(password))) {
                user.get().setEmail(newEmail);
                userRepository.save(user.get());
                return ResponseEntity.ok().body(new StatusReponse(true));
            }
            else {
                return ResponseEntity.badRequest().body(new StatusReponse(false));
            }
        }
        return ResponseEntity.badRequest().body(new MessageResponse("Not authorized"));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        /*if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }*/

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(//signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
