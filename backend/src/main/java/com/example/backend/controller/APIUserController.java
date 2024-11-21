package com.example.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.backend.dto.SignupRequestDTO;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.service.MailService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/api/v1")
public class APIUserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    interface UserBasicView extends User.BasicInfo {
    }

    interface UserUsername extends User.UsernameInfo {
    }

    interface UserFollowersView extends User.FollowersInfo {
    }

    interface UserCommunitiesView extends User.CommunitiesInfo {
    }

    interface CommunitiesBasicView extends Community.BasicInfo, UserUsername {
    }

    public APIUserController(UserService userService, PasswordEncoder passwordEncoder, MailService mailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    // Get current user
    @JsonView(UserBasicView.class)
    @Operation(summary = "Get current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
    })
    @GetMapping("/users/me")
    public ResponseEntity<User> getCurrentUser(HttpServletRequest request) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // Not logged in
        }
        User user = userService.getUserByUsername(principal.getName());
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    // Get User Info by username
    @JsonView(UserBasicView.class)
    @Operation(summary = "Get user by username or email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Users not found"),
    })
    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            user = userService.getUserByEmail(username);
        }
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(user, HttpStatus.OK);
        }
    }

    // Is username taken
    @Operation(summary = "Check if username is taken")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Username is available", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = boolean.class)),
            }),
            @ApiResponse(responseCode = "200", description = "Username is not available", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = boolean.class)),
            }),
    })
    @GetMapping("/users/{username}/taken")
    public ResponseEntity<Boolean> isUsernameTaken(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(false, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
    }

    // Get User Followers list
    @JsonView(UserBasicView.class)
    @Operation(summary = "Get user's followers list (pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Followers found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/users/{username}/followers")
    public ResponseEntity<List<User>> getUserFollowers(@PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userService.getUserByUsername(username) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<User> followers = userService.getFollowers(username, PageRequest.of(page, size)).getContent();
            return new ResponseEntity<>(followers, HttpStatus.OK);
        }
    }

    // Is user following another user
    @Operation(summary = "Check if user is following another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User is following", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = boolean.class)),
            }),
            @ApiResponse(responseCode = "200", description = "User is not following", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = boolean.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/users/{follower}/following/{following}")
    public ResponseEntity<Boolean> isUserFollowing(@PathVariable String follower, @PathVariable String following) {
        if (userService.getUserByUsername(follower) == null || userService.getUserByUsername(following) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        if (userService.isUserFollowing(follower, following)){
            return new ResponseEntity<>(true, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(false, HttpStatus.OK);
        }
    }

    // Get User Following list
    @JsonView(UserBasicView.class)
    @Operation(summary = "Get user's following list (pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Following found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/users/{username}/following")
    public ResponseEntity<List<User>> getUserFollowing(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userService.getUserByUsername(username) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<User> following = userService.getFollowing(username, PageRequest.of(page, size)).getContent();
            return new ResponseEntity<>(following, HttpStatus.OK);
        }
    }

    // SEARCH ENGINE: USERS
    @JsonView(UserBasicView.class)
    @Operation(summary = "Search users by username, email, alias or description. Search Engine's default behaviour")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
    })
    @GetMapping("/users")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam String query,
            @RequestParam boolean orderByCreationDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (query == null || query.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else {
            List<User> users = orderByCreationDate
                    ? userService.searchUsersOrderByCreationDate(query, PageRequest.of(page, size)).getContent()
                    : userService.searchUsers(query, PageRequest.of(page, size)).getContent();
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
    }

    // Get User's Communities
    @JsonView(CommunitiesBasicView.class)
    @Operation(summary = "Get user's communities list (pageable). If admin=true, returns the communities the user is an admin of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Communities found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunitiesBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
    })
    @GetMapping("/users/{username}/communities")
    public ResponseEntity<List<Community>> getUserCommunities(
            @PathVariable String username,
            @RequestParam(defaultValue = "false") boolean admin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (userService.getUserByUsername(username) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            List<Community> communities = admin
                    ? userService.findAdminCommunities(username, PageRequest.of(page, size)).getContent()
                    : userService.findCommunities(username, PageRequest.of(page, size)).getContent();
            return new ResponseEntity<>(communities, HttpStatus.OK);
        }
    }

    // Get User's Communities Count
    @Operation(summary = "Get the number of communities a user is a member of. If admin=true, returns the number of communities the user is an admin of")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Communities found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
    })
    @GetMapping("/users/{username}/communities/count")
    public ResponseEntity<Integer> getUserCommunitiesCount(
            @PathVariable String username,
            @RequestParam(required = false) Boolean admin,
            HttpServletRequest request) {

        if (request.getParameter("admin") == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (userService.getUserByUsername(username) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            int count = Boolean.TRUE.equals(admin)
                    ? userService.getNumberOfAdminCommunities(username)
                    : userService.getNumberOfCommunities(username);

            if (count == -1) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(count, HttpStatus.OK);
        }
    }

    // Get User's Posts Count
    @Operation(summary = "Get the number of posts a user has made")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class)),
            }),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @GetMapping("/users/{username}/posts/count")
    public ResponseEntity<Integer> getUserPostsCount(@PathVariable String username) {
        if (userService.getUserByUsername(username) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            int count = userService.getNumberOfPosts(username);
            return new ResponseEntity<>(count, HttpStatus.OK);
        }
    }

    // Register User
    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
    })
    @JsonView(UserBasicView.class)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/users")
    public ResponseEntity<User> registerUser(@ModelAttribute SignupRequestDTO userInfo)
            throws IOException, SQLException {
        if (userInfo == null || userInfo.getUsername() == null || userInfo.getEmail() == null
                || userInfo.getAlias() == null
                || userInfo.getPassword() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String username = userInfo.getUsername();
        String email = userInfo.getEmail();
        String alias = userInfo.getAlias();
        String password = userInfo.getPassword();

        if (username == null || username.isBlank()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!userService.isUsernameAvailable(username)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!userService.isEmailAvailable(email)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!mailService.isCorrectEmail(email)) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (alias == null || alias.isBlank()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (password == null || password.isBlank()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Create user
        List<String> roles = List.of("USER"); // Default role
        User user = new User(username, alias, "Hey there! I am new, what's up?", "", email,
                passwordEncoder.encode(password), roles);
        userService.saveUser(user);

        // Resource URL (location header)
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        // Return response
        return ResponseEntity.created(location).body(user);
    }

    // Update User (modify data)
    @Operation(summary = "Update user data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content),
    })
    @JsonView(UserBasicView.class)
    @PutMapping("/users/{username}")
    public ResponseEntity<User> updateUser(HttpServletRequest request, @PathVariable String username,
            @RequestParam String action,
            @RequestParam(required = false) String otherUsername,
            @RequestBody(required = false) Map<String, String> userInfo) throws IOException {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is authorized (if user is admin or the user itself)
        if (!principal.getName().equals(username)
                && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (action.equals("edit")) {
            // Old username and email
            String oldUsername = user.getUsername();
            String oldEmail = user.getEmail();

            if (userInfo.containsKey("alias")) {
                user.setAlias(userInfo.get("alias"));
            }
            if (userInfo.containsKey("description")) {
                user.setDescription(userInfo.get("description"));
            }
            if (userInfo.containsKey("email")) {
                String email = userInfo.get("email");
                if (!mailService.isCorrectEmail(email)) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                user.setEmail(email);
                try {
                    mailService.sendEmail(oldEmail, oldUsername, "Email changed",
                            "Your email has been changed successfully to " + email);
                } catch (MessagingException e) {
                    return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
            if (userInfo.containsKey("password")) {
                // Check that the password complies with the requirements
                String newPassword = userInfo.get("password");
                if (newPassword != null && !isValidPassword(newPassword)) {
                    throw new InvalidPasswordException("Password does not meet the requirements");
                } else {
                    try {
                        user.setPassword(passwordEncoder.encode(newPassword));
                        mailService.sendEmail(oldEmail, oldUsername, "Password changed",
                                "Your password has been changed successfully");
                    } catch (MessagingException e) {
                        return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
                    }
                }
            }
        } else if (action.equals("follow")) {
            User following = userService.getUserByUsername(otherUsername);
            if (following == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            userService.followUser(user, following);
        } else if (action.equals("unfollow")) {
            User following = userService.getUserByUsername(otherUsername);
            if (following == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            userService.unfollowUser(user, following);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Save changes
        userService.saveUser(user);

        // Return updated user
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    private boolean isValidPassword(String password) {
        // Password requirements
        // At least 8 characters
        // At least 1 digit
        // At least 1 lowercase letter
        // At least 1 uppercase letter
        // At least 1 special character
        return password.matches("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");
    }

    // Delete User
    @Operation(summary = "Delete user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
    })
    @DeleteMapping("/users/{username}")
    public ResponseEntity<String> deleteUser(HttpServletRequest request, @PathVariable String username) {
        // is the user logged in?
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Check if user is authorized
        List<String> roles = userService.getUserByUsername(request.getUserPrincipal().getName()).getRoles();
        boolean isAdmin = roles.contains("ADMIN");

        // the admin of the site can't delete their account
        if (isAdmin && request.getUserPrincipal().getName().equals(username)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Only admins can delete users or the user itself
        if (!request.getUserPrincipal().getName().equals(username) && !isAdmin) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Remove references to the user in other entities
        userService.removeUserReferences(user);

        // Delete user
        userService.deleteUser(user);

        // if the user is the same as the one logged in, logout
        if (request.getUserPrincipal().getName().equals(username)) {
            // Invalidate the session
            request.getSession().invalidate();

            // Return a response indicating the user should logout
            return ResponseEntity.status(HttpStatus.OK)
                    .header(HttpHeaders.SET_COOKIE, "token=; HttpOnly; Path=/; Max-Age=0")
                    .body("User deleted. Logout");
        }

        return new ResponseEntity<>(username + " has been deleted", HttpStatus.OK);
    }

    // Change Profile Picture
    @Operation(summary = "Change user's profile picture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture changed", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    @JsonView(UserBasicView.class)
    @PutMapping("/users/{username}/pictures")
    public ResponseEntity<Object> changeProfilePicture(HttpServletRequest request, @PathVariable String username,
            @RequestParam MultipartFile file) throws IOException {

        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Logged user = user or is admin?
        if (!principal.getName().equals(username)
                && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (file == null || file.isEmpty()) {
            return new ResponseEntity<>("File couldn't be uploaded, please try again.", HttpStatus.BAD_REQUEST);
        }

        // Check file
        try (InputStream is = file.getInputStream()) {
            try {
                ImageIO.read(is).toString();
                long size = file.getSize() / 1024 / 1024; // MB
                if (size >= 5) {
                    return new ResponseEntity<>("File is too large. Max size is 5MB", HttpStatus.BAD_REQUEST);
                }
                user.setPfp(BlobProxy.generateProxy(file.getInputStream(), file.getSize()));
                userService.saveUser(user);

                // Resource URL for the image (location header)
                URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{username}/pictures")
                        .buildAndExpand(user.getUsername()).toUri();

                // Return response
                return ResponseEntity.created(location).body(user);
            } catch (Exception e) {
                return new ResponseEntity<>("File is not an image", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get Profile Picture
    @Operation(summary = "Get user's profile picture")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile picture found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Profile picture not found"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error"),
    })
    @GetMapping("/users/{username}/pictures")
    public ResponseEntity<Object> getProfilePicture(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Blob image = user.getPfp();

        if (image == null) {
            return new ResponseEntity<>("Profile picture not found", HttpStatus.NOT_FOUND);
        }

        try {
            int blobLength = (int) image.length();
            byte[] blobAsBytes = image.getBytes(1, blobLength);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(blobAsBytes);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Exception handler for missing parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String name = ex.getParameterName();
        return new ResponseEntity<>(name + " parameter is missing", HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) {
            super(message);
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public class InvalidUsernameChangeDateException extends RuntimeException {
        public InvalidUsernameChangeDateException(String message) {
            super(message);
        }
    }

    // Get users with the most liked posts
    @JsonView(UserBasicView.class)
    @Operation(summary = "Get users with the most liked posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = UserBasicView.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Users not found"),
    })
    @GetMapping("/users/most-popular")
    public ResponseEntity<List<Object[]>> getMostPopularUsers(@RequestParam(defaultValue = "10") int size) {
            return new ResponseEntity<>(userService.getMostPopularUsersCount(size), HttpStatus.OK);

    }
}
