package com.example.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.backend.service.CommunityService;
import com.example.backend.service.PostService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.backend.entity.Ban;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.entity.Post;

@RestController
@RequestMapping("/api/v1")
public class APICommunityController {
    private final CommunityService communityService;
    private final PostService postService;
    private final UserService userService;

    interface CommunityBasicInfo extends Community.BasicInfo, User.UsernameInfo {
    }

    interface CommunityUsersInfo extends Community.UsersInfo, User.BasicInfo {
    }

    interface CommunityPostsBasicInfo extends Post.CommunityInfo, User.UsernameInfo, Community.NameInfo {
    }

    interface CommunityBanInfo extends Ban.BasicInfo {
    }

    public APICommunityController(CommunityService communityService, PostService postService, UserService userService) {
        this.communityService = communityService;
        this.userService = userService;
        this.postService = postService;
    }

    // Get communities by ID
    @JsonView(CommunityBasicInfo.class)
    @Operation(summary = "Get a community by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityBasicInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content)
    })
    @GetMapping("/communities/{id}")
    public ResponseEntity<Community> getCommunityById(@PathVariable Long id) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(community, HttpStatus.OK);
        }
    }

    // Get communities (order: creationDate, members, lastPostDate) (by: name,
    // description, admin, default, general) DEFAULT = by name and description
    @JsonView(CommunityBasicInfo.class)
    @Operation(summary = "Get communities by specified criteria and sort:\n"
            + "Default: search by name and description. Sortings: creationDate, members, lastPostDate, alphabetical (default)\n"
            + "General: get all communities (no query). Sortings: creationDate, lastPostDate, members, alphabetical (default)\n"
            + "Admin: get communities by admin username. Sortings: NONE\n"
            + "Name: search by name. Sortings: creationDate, lastPostDate, members, alphabetical (default)\n"
            + "Description: search by description. Sortings: creationDate, lastPostDate, members, alphabetical (default)\n")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found communities", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityBasicInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Communities not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Missing parameter", content = @Content)
    })
    @GetMapping("/communities")
    public ResponseEntity<List<Community>> getCommunitiesByName(@RequestParam(required = false) String query,
            @RequestParam int page,
            @RequestParam int size, @RequestParam(required = false) String sort,
            @RequestParam(required = true) String by) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Community> communities = null;
        switch (by) {
            case "name":
                communities = communityService.getCommunitiesByName(query, sort, pageable);
                break;
            case "description":
                communities = communityService.getCommunitiesByDescription(query, sort, pageable);
                break;
            case "admin":
                communities = communityService.getCommunitiesByAdmin(query, pageable);
                break;
            case "general":
                communities = communityService.getCommunities(sort, pageable);
                break;
            default:
                communities = communityService.engineSearchCommunities(query, sort, pageable);
                break;
        }
        if (communities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(communities.getContent(), HttpStatus.OK);
        }
    }

    // Get members of a community
    @Operation(summary = "Get members of a community (pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found members", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityUsersInfo.class))
            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Missing parameter", content = @Content)
    })
    @JsonView(CommunityUsersInfo.class)
    @GetMapping("/communities/{id}/users")
    public ResponseEntity<Object> getMembers(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean count) {

        Pageable pageable = PageRequest.of(page, size);
        if (count) {
            int numberOfUsers = communityService.getNumberOfUsers(id);
            return new ResponseEntity<>(numberOfUsers, HttpStatus.OK);
        } else {
            Page<User> members = communityService.getMembers(id, pageable);
            if (members.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(members.getContent(), HttpStatus.OK);
            }
        }
    }

    // Get pageable of moderators of a community
    @Operation(summary = "Get moderators of a community (pageable)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found moderators", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityUsersInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Missing parameter", content = @Content)
    })
    @JsonView(CommunityUsersInfo.class)
    @GetMapping("/communities/{id}/moderators")
    public ResponseEntity<List<User>> getModerators(@PathVariable Long id, @RequestParam int page,
            @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> moderators = communityService.getModerators(id, pageable);
        if (communityService.getCommunityById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(moderators.getContent(), HttpStatus.OK);
        }
    }

    // Get admin of a community
    @Operation(summary = "Get admin of a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the admin", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityUsersInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content)
    })
    @JsonView(CommunityUsersInfo.class)
    @GetMapping("/communities/{id}/admins")
    public ResponseEntity<User> getAdmin(@PathVariable Long id) {
        User admin = communityService.getAdmin(id);
        if (communityService.getCommunityById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(admin, HttpStatus.OK);
        }
    }

    // Update community (format this as the UserController update method)
    @Operation(summary = "Update a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @JsonView(CommunityBasicInfo.class)
    @PutMapping("/communities/{id}")
    public ResponseEntity<Community> updateCommunity(HttpServletRequest request, @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is user the admin of the community? or a site admin?
        String username = request.getUserPrincipal().getName();
        if (!community.getAdmin().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (body.containsKey("name")) {
            community.setName(body.get("name"));
        }
        if (body.containsKey("description")) {
            community.setDescription(body.get("description"));
        }
        if (body.containsKey("admin")) {
            // check that the user performing the request is the admin of the community
            if (!community.getAdmin().getUsername().equals(username)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            User admin = userService.getUserByUsername(body.get("admin"));
            if (admin == null) {
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
            }
            communityService.promoteUserToAdmin(admin.getUsername(), community.getIdentifier());
        }

        communityService.saveCommunity(community);
        return new ResponseEntity<>(community, HttpStatus.OK);
    }

    // Delete community
    @Operation(summary = "Delete a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deleted the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping("/communities/{id}")
    public ResponseEntity<String> deleteCommunity(HttpServletRequest request, @PathVariable Long id) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // check that the user performing the request is the admin of the community or a
        // site admin
        String username = request.getUserPrincipal().getName();
        if (!community.getAdmin().getUsername().equals(username)
                && !userService.getUserByUsername(username).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        communityService.deleteCommunity(community);
        return new ResponseEntity<>("Community " + id + " deleted!", HttpStatus.OK);
    }

    // Create community
    @Operation(summary = "Create a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflict", content = @Content)
    })
    @JsonView(CommunityBasicInfo.class)
    @PostMapping("/communities")
    public ResponseEntity<Community> createCommunity(HttpServletRequest request,
            @RequestBody Map<String, String> body) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String username = request.getUserPrincipal().getName();
        User admin = userService.getUserByUsername(username);
        if (admin == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        if (!body.containsKey("name") || !body.containsKey("description")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // Does a community with the same name already exist?
        if (communityService.doesCommunityExist(body.get("name"))) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        // Create community
        Community community = new Community(body.get("name"), body.get("description"), null, admin);
        communityService.saveCommunity(community);

        // Resource URL (location header)
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(community.getIdentifier()).toUri();

        // Return response
        return ResponseEntity.created(location).body(community);
    }

    // Manage users in community (add, remove, ban, unban)
    // ban durations: day, week, 2weeks, month, 6months, year, forever
    @Operation(summary = "Add, remove or ban a user from a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action succesfully performed", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityUsersInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @PutMapping("/communities/{id}/users/{username}")
    @JsonView(CommunityUsersInfo.class)
    public ResponseEntity<Community> manageCommunityUsers(HttpServletRequest request, @PathVariable Long id,
            @PathVariable String username, @RequestParam(required = true) String action,
            @RequestParam(required = false) String duration, @RequestParam(required = false) String reason) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is action valid?
        if (action == null || (!action.equals("join") && !action.equals("leave"))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        switch (action) {
            case "join":
                return addUserToCommunity(request, community, username);
            case "leave":
                return removeUserFromCommunity(request, community, username);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<Community> addUserToCommunity(HttpServletRequest request, Community community,
            String username) {

        // check that the user performing the request is the user being added or a site
        // admin
        String requesterUsername = request.getUserPrincipal().getName();

        if (!requesterUsername.equals(username)
                && !userService.getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // check ban
        if (communityService.isUserBannedFromCommunity(user.getUsername(), community.getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        communityService.joinCommunity(user.getUsername(), community.getIdentifier());
        return new ResponseEntity<>(community, HttpStatus.OK);
    }

    private ResponseEntity<Community> removeUserFromCommunity(HttpServletRequest request, Community community,
            String username) {
        // check that the user performing the request is the user being removed or a
        // site admin
        String requesterUsername = request.getUserPrincipal().getName();
        if (!requesterUsername.equals(username)
                && !userService.getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        communityService.leaveCommunity(user.getUsername(), community.getIdentifier());
        return new ResponseEntity<>(community, HttpStatus.OK);
    }

    // Ban user from community (can be banned for a day, 1 week, 2 weeks, 1 month, 6
    // months, a year or forever)
    @Operation(summary = "Ban a user from a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Banned the user from the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Ban.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @PostMapping("/bans")
    @JsonView(Ban.BasicInfo.class)
    public ResponseEntity<Ban> banUser(HttpServletRequest request, @RequestParam Long communityID,
            @RequestParam String username, @RequestParam String duration, @RequestParam String reason) {
        Community community = communityService.getCommunityById(communityID);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // check that the user performing the request is the admin or a moderator of the
        // community
        String requesterUsername = request.getUserPrincipal().getName();
        if (!community.getAdmin().getUsername().equals(requesterUsername)
                && !communityService.isUserModeratorOfCommunity(requesterUsername, community.getIdentifier())
                && !userService.getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        // check if user is already banned
        if (communityService.isUserBannedFromCommunity(user.getUsername(), community.getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        switch (duration) {
            case "day":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 1, reason);
                break;
            case "week":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 7, reason);
                break;
            case "2weeks":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 14, reason);
                break;
            case "month":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 30, reason);
                break;
            case "6months":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 180, reason);
                break;
            case "year":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), 365, reason);
                break;
            case "forever":
                communityService.banUserFromCommunity(user.getUsername(), community.getIdentifier(), -1, reason);
                break;
            default:
                return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Ban ban = communityService.getBan(user.getUsername(), community.getIdentifier());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(ban.getId()).toUri();
        return ResponseEntity.created(location).body(ban);
    }

    // Unban user from community
    @Operation(summary = "Unban a user from a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unbanned the user from the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @DeleteMapping("/bans/{id}")
    public ResponseEntity<String> unbanUser(HttpServletRequest request, @PathVariable Long id) {
        Ban ban = communityService.getBanById(id);
        if (ban == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is the admin or a moderator of the
        // community
        String requesterUsername = request.getUserPrincipal().getName();
        if (!communityService.isUserModeratorOfCommunity(requesterUsername, ban.getCommunity().getIdentifier())
                && !ban.getCommunity().getAdmin().getUsername().equals(requesterUsername)
                && !userService.getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        communityService.unbanUserFromCommunityById(id);
        return new ResponseEntity<>("Ban removed", HttpStatus.OK);
    }

    // Modify moderators
    @Operation(summary = "Add or remove a moderator from a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Action succesfully performed", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityUsersInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @PutMapping("/communities/{id}/moderators/{username}")
    @JsonView(CommunityUsersInfo.class)
    public ResponseEntity<Community> manageModerators(HttpServletRequest request, @PathVariable Long id,
            @PathVariable String username, @RequestParam(required = true) String action) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is the admin of the community or a
        // site admin
        String requesterUsername = request.getUserPrincipal().getName();
        if (!community.getAdmin().getUsername().equals(requesterUsername) && !userService
                .getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        switch (action) {
            case "add":
                communityService.promoteUserToModerator(user.getUsername(), community.getIdentifier());
                break;
            case "remove":
                communityService.demoteUserFromModerator(user.getUsername(), community.getIdentifier());
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(community, HttpStatus.OK);
    }

    // Upload banner
    @Operation(summary = "Upload a banner to a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Uploaded the banner to the community", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @JsonView(CommunityBasicInfo.class)
    @PutMapping("/communities/{id}/pictures")
    public ResponseEntity<Object> uploadCommunityBanner(HttpServletRequest request, @PathVariable Long id,
            @RequestParam MultipartFile file) throws IOException {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // does community exist?
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is user logged in
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is user the admin of the community? or a site admin?
        if (!community.getAdmin().getUsername().equals(principal.getName()) && !userService
                .getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
                community.setBanner(BlobProxy.generateProxy(file.getInputStream(), file.getSize()));
                communityService.saveCommunity(community);

                // Resource URL for the image (location header)
                URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                        .buildAndExpand(community.getIdentifier()).toUri();

                // Return response
                return ResponseEntity.created(location).body(community);
            } catch (Exception e) {
                return new ResponseEntity<>("File is not an image", HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get banner
    @Operation(summary = "Get the banner of a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the banner", content = {
                    @Content(mediaType = "image/jpeg", schema = @Schema(implementation = Community.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping("/communities/{id}/pictures")
    public ResponseEntity<Object> getCommunityBanner(@PathVariable Long id) {
        Community community = communityService.getCommunityById(id);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Blob banner = community.getBanner();
        if (banner == null) {
            return new ResponseEntity<>("Community banner not found", HttpStatus.NOT_FOUND);
        }

        try {
            int blobLength = (int) banner.length();
            byte[] blobAsBytes = banner.getBytes(1, blobLength);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(blobAsBytes);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get ban info of a user in a community
    @Operation(summary = "Get ban info of a user in a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the ban info", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityBanInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @JsonView(CommunityBanInfo.class)
    @GetMapping("/bans/{id}")
    public ResponseEntity<Object> getBanInfo(HttpServletRequest request, @PathVariable Long id,
            @RequestParam(required = false) String banInfo) {

        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // does ban exist?
        if (communityService.getBanById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Ban ban = communityService.getBanById(id);

        Community community = communityService.getCommunityById(ban.getCommunity().getIdentifier());
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is the admin of the community, a
        // mod or the user being banned
        String requesterUsername = request.getUserPrincipal().getName();
        if (!requesterUsername.equals(ban.getUser().getUsername())
                && !userService.getUserByUsername(requesterUsername).getRoles().contains("ADMIN") && !communityService
                        .isUserModeratorOfCommunity(requesterUsername, community.getIdentifier())
                && !community.getAdmin().getUsername()
                        .equals(requesterUsername)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String banReason = ban.getBanReason();
        LocalDateTime banUntil = ban.getBanUntil();

        // is ban over?
        if (banUntil != null && banUntil.isBefore(LocalDateTime.now())) {
            communityService.unbanUserFromCommunityById(id);
            banReason = null;
            banUntil = null;
        }

        if (banReason == null || banUntil == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String banUntilFormatted = banUntil.toString();

        switch (banInfo) {
            case "reason":
                return new ResponseEntity<>(banReason, HttpStatus.OK);
            case "duration":
                return new ResponseEntity<>(banUntilFormatted, HttpStatus.OK);
            case "status": // true = banned, false = not banned
                return new ResponseEntity<>(true, HttpStatus.OK);
            default:
                // return a Ban
                return new ResponseEntity<>(communityService.getBanById(id), HttpStatus.OK);

        }

    }

    // Get all banned users in a community (pageable)
    @Operation(summary = "Get all banned users in a community")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the banned users", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = CommunityBanInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity", content = @Content)
    })
    @JsonView(CommunityBanInfo.class)
    @GetMapping("/bans/communities/{id}")
    public ResponseEntity<List<Ban>> getBannedUsers(HttpServletRequest request, @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        // is user logged in
        if (request.getUserPrincipal() == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // does community exist?
        if (communityService.getCommunityById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // check that the user performing the request is the admin or a moderator of the
        // community
        String requesterUsername = request.getUserPrincipal().getName();
        if (!communityService.getCommunityById(id).getAdmin().getUsername().equals(requesterUsername)
                && !communityService.isUserModeratorOfCommunity(requesterUsername, id) && !userService
                        .getUserByUsername(requesterUsername).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Ban> bans = communityService.getBannedUsers(id, pageable);
        if (bans.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(bans.getContent(), HttpStatus.OK);
        }
    }
}