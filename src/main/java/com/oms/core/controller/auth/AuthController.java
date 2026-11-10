package com.oms.core.controller.auth;

import com.oms.core.constants.AppRole;
import com.oms.core.dto.request.LoginRequest;
import com.oms.core.dto.request.RegisterRequest;
import com.oms.core.dto.response.JwtResponse;
import com.oms.core.dto.response.MessageResponse;
import com.oms.core.entity.Role;
import com.oms.core.entity.User;
import com.oms.core.repository.RoleRepository;
import com.oms.core.repository.UserRepository;
import com.oms.core.security.jwt.JwtUtils;
import com.oms.core.security.service.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder encoder;
	private final JwtUtils jwtUtils;

	public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
			RoleRepository roleRepository, PasswordEncoder encoder, JwtUtils jwtUtils) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.encoder = encoder;
		this.jwtUtils = jwtUtils;
	}

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toList());

		return ResponseEntity.ok(
				new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getEmail(), roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
		if (userRepository.existsByUsername(signUpRequest.username())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.email())) {
			return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
		}

		// This call now matches the newly updated constructor in User.java perfectly!
		User user = new User(signUpRequest.username(), signUpRequest.email(), encoder.encode(signUpRequest.password()));

		Set<String> strRoles = signUpRequest.roles();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null || strRoles.isEmpty()) {
			Role customerRole = roleRepository.findByName(AppRole.ROLE_CUSTOMER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(customerRole);
		} else {
			strRoles.forEach(role -> {
				switch (role.toLowerCase()) {
				case "admin":
					Role adminRole = roleRepository.findByName(AppRole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);
					break;
				case "delivery":
					Role deliveryRole = roleRepository.findByName(AppRole.ROLE_DELIVERY)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(deliveryRole);
					break;
				default:
					Role customerRole = roleRepository.findByName(AppRole.ROLE_CUSTOMER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(customerRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}
}