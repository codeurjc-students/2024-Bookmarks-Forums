package com.example.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
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
import com.example.backend.service.ReplyService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.example.backend.dto.PostDTO;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.entity.Post;
import com.example.backend.entity.Reply;

@RestController
@RequestMapping("/api/v1")
public class APIPostController {

    private final CommunityService communityService;
    private final PostService postService;
    private final UserService userService;
    private final ReplyService replyService;

    interface PostInfo extends Post.BasicInfo {
    }

    interface ReplyInfo extends User.UsernameInfo, Reply.BasicInfo {
    }

    public APIPostController(CommunityService communityService, PostService postService, UserService userService,
            ReplyService replyService) {
        this.communityService = communityService;
        this.postService = postService;
        this.userService = userService;
        this.replyService = replyService;
    }

    // Get post by ID | SECURITY: CHECKED
    @Operation(summary = "Get post by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Post not found"),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/posts/{postId}")
    public ResponseEntity<Post> getPostById(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    // Search posts
    /*
     * Possible orders:
     * - creationDate
     * - lastModifiedDate
     * - replies
     * - likes
     */ // SECURITY: CHECKED
    @Operation(summary = "Search posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/posts")
    public ResponseEntity<List<Post>> searchPosts(@RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "creationDate") String order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchPosts(query, pageable, order);
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(posts.getContent());
    }

    // Search posts by community identifier
    /*
     * Possible orders:
     * - creationDate
     * - lastModifiedDate
     * - replies
     * - likes
     */ // SECURITY: CHECKED
    // Get posts of a community
    @Operation(summary = "Get posts of a community using its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the posts", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class))

            }),
            @ApiResponse(responseCode = "204", description = "No posts found", content = @Content),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/communities/{communityID}/posts")
    public ResponseEntity<Object> getCommunityPosts( // Search for posts inside a community (all variants of searching
                                                     // posts by community ID)
            @PathVariable Long communityID,
            @RequestParam(value = "count", required = false, defaultValue = "false") boolean count,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sort", required = false, defaultValue = "lastModifiedDate") String sort,
            @RequestParam(value = "query", required = false) String query) {

        Community community = communityService.getCommunityById(communityID);
        if (community == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (count) {
            return new ResponseEntity<>(communityService.getNumberOfPosts(communityID), HttpStatus.OK);
        } else {
            Pageable pageable = PageRequest.of(page, size);
            Page<Post> posts = null;
            if (query == null || query.isEmpty()) {
                posts = postService.searchPostsByCommunityIdentifier(communityID, "", pageable, sort, false);
            } else {
                posts = postService.searchPostsByCommunityIdentifier(communityID, query, pageable, sort, true);
            }

            if (posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
            }
        }
    }

    // Get posts by username | SECURITY: CHECKED
    @Operation(summary = "Get posts by username")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/users/{username}/posts")
    public ResponseEntity<List<Post>> getPostsByUsername(@PathVariable String username,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "creationDate") String order) {
        Pageable pageable = PageRequest.of(page, size);
        if (query == null || query.isEmpty()) {
            Page<Post> posts = postService.getPostsOfUser(username, pageable, order);
            if (posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
        } else {
            Page<Post> posts = postService.searchPostsOfUser(username, query, pageable, order);
            if (posts.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
        }
    }

    // Create a post | SECURITY: CHECKED
    @Operation(summary = "Create a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @JsonView(PostInfo.class)
    @PostMapping("/communities/{communityID}/posts")
    public ResponseEntity<Post> createPost(HttpServletRequest request, @ModelAttribute PostDTO postDTO,
            @PathVariable Long communityID) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // does the community exist?
        if (communityService.getCommunityById(communityID) == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // is the user a member of the community?
        User author = userService.getUserByUsername(principal.getName());
        Community community = communityService.getCommunityById(communityID);
        if (!community.getMembers().contains(author)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), community.getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // does the post have a title and content?
        if (postDTO.getTitle() == null || postDTO.getTitle().isEmpty() || postDTO.getContent() == null
                || postDTO.getContent().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Post post = new Post(postDTO.getTitle(), postDTO.getContent(), author, community);

        // image (optional)
        MultipartFile image = postDTO.getImage();
        if (image != null) {
            try (InputStream is = image.getInputStream()) {
                try {
                    ImageIO.read(is).toString();
                    long size = image.getSize() / 1024 / 1024; // MB
                    if (size >= 5) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    post.setImage(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
                    post.setHasImage(true);
                } catch (IOException e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        postService.savePost(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(post.getIdentifier()).toUri();

        return ResponseEntity.created(location).body(post);
    }

    // Delete post image | SECURITY: CHECKED
    @Operation(summary = "Delete post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post image deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
    })
    @DeleteMapping("/posts/{postId}/pictures")
    public ResponseEntity<String> deletePostImage(HttpServletRequest request, @PathVariable Long postId) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the post, a community admin, moderator or a site
        // admin?
        User author = userService.getUserByUsername(principal.getName());
        if (!post.getAuthor().equals(author) && !communityService.isUserAdminOfCommunity(author.getUsername(),
                post.getCommunity().getIdentifier())
                && !communityService.isUserModeratorOfCommunity(author.getUsername(),
                        post.getCommunity().getIdentifier())
                && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), post.getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        post.setImage(null);
        post.setHasImage(false);
        postService.updatePost(post);

        return new ResponseEntity<>("Post image deleted", HttpStatus.OK);
    }

    // Has user upvoted or downvoted a post? | SECURITY: CHECKED
    @Operation(summary = "Returns whether the user has upvoted or downvoted a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User has upvoted or downvoted the post", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
    })
    @GetMapping("/posts/{postId}/votes")
    public ResponseEntity<Boolean> hasUserVotedPost(@PathVariable Long postId, @RequestParam String username,
            @RequestParam String type) {
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (username == null || username.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        switch (type) {
            case "upvote":
                return new ResponseEntity<>(postService.hasUserUpvotedPost(username, postId), HttpStatus.OK);
            case "downvote":
                return new ResponseEntity<>(postService.hasUserDownvotedPost(username, postId), HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Edit a post (or upvote/downvote) | SECURITY: CHECKED
    @Operation(summary = "Edit a post (or upvote/downvote)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post edited", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @JsonView(PostInfo.class)
    @PutMapping("/posts/{postId}")
    public ResponseEntity<Post> editPost(HttpServletRequest request, @PathVariable Long postId,
            @ModelAttribute PostDTO postDTO, @RequestParam(required = true) String action) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        User author = userService.getUserByUsername(principal.getName());

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), post.getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // upvote or downvote
        switch (action) {
            case "upvote":
                // has the user already upvoted the post?
                if (postService.hasUserUpvotedPost(author.getUsername(), postId)) {
                    postService.removeUpvote(post, author.getUsername());
                } else {
                    postService.upvotePost(post, author.getUsername());
                }
                return new ResponseEntity<>(post, HttpStatus.OK);
            case "downvote":
                // has the user already downvoted the post?
                if (postService.hasUserDownvotedPost(author.getUsername(), postId)) {
                    postService.removeDownvote(post, author.getUsername());
                } else {
                    postService.downvotePost(post, author.getUsername());
                }
                return new ResponseEntity<>(post, HttpStatus.OK);
            case "edit":
                // is the user the author of the post a community admin, moderator or a site
                // admin?
                if (!post.getAuthor().equals(author) && !communityService.isUserAdminOfCommunity(author.getUsername(),
                        post.getCommunity().getIdentifier())
                        && !communityService.isUserModeratorOfCommunity(author.getUsername(),
                                post.getCommunity().getIdentifier())
                        && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                }

                // does the post have a title and content?
                if (postDTO.getTitle() == null || postDTO.getTitle().isEmpty() || postDTO.getContent() == null
                        || postDTO.getContent().isEmpty()) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }

                post.setTitle(postDTO.getTitle());
                post.setContent(postDTO.getContent());

                // image (optional)
                MultipartFile image = postDTO.getImage();
                if (image != null) {
                    try (InputStream is = image.getInputStream()) {
                        try {
                            ImageIO.read(is).toString();
                            long size = image.getSize() / 1024 / 1024; // MB
                            if (size >= 5) {
                                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                            }
                            post.setImage(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
                            post.setHasImage(true);
                        } catch (IOException e) {
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                    } catch (IOException e) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                }

                postService.updatePost(post);
                return new ResponseEntity<>(post, HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // Update post image (if action = "delete", the image will be deleted) | SECURITY: CHECKED
    @Operation(summary = "Update post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post image updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @PutMapping("/posts/{postId}/pictures")
    public ResponseEntity<Post> updatePostImage(HttpServletRequest request, @PathVariable Long postId,
            @RequestParam(required = false) String action, @RequestParam(required = false) MultipartFile image) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the post, a community admin, moderator or a site
        // admin?
        User author = userService.getUserByUsername(principal.getName());
        if (!post.getAuthor().equals(author) && !communityService.isUserAdminOfCommunity(author.getUsername(),
                post.getCommunity().getIdentifier())
                && !communityService.isUserModeratorOfCommunity(author.getUsername(),
                        post.getCommunity().getIdentifier())
                && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), post.getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // delete image
        if (action != null && action.equals("delete")) {
            post.setImage(null);
            post.setHasImage(false);
            postService.updatePost(post);
            return new ResponseEntity<>(post, HttpStatus.OK);
        }

        // update image
        if (image != null) {
            try (InputStream is = image.getInputStream()) {
                try {
                    ImageIO.read(is).toString();
                    long size = image.getSize() / 1024 / 1024; // MB
                    if (size >= 5) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                    post.setImage(BlobProxy.generateProxy(image.getInputStream(), image.getSize()));
                    post.setHasImage(true);
                } catch (IOException e) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (IOException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            postService.updatePost(post);
            return new ResponseEntity<>(post, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    // Delete a post | SECURITY: CHECKED
    @Operation(summary = "Delete a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
    })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(HttpServletRequest request, @PathVariable Long postId) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the post, a community admin, moderator or a site
        // admin?
        User author = userService.getUserByUsername(principal.getName());
        if (!post.getAuthor().equals(author) && !communityService.isUserAdminOfCommunity(author.getUsername(),
                post.getCommunity().getIdentifier())
                && !communityService.isUserModeratorOfCommunity(author.getUsername(),
                        post.getCommunity().getIdentifier())
                && !userService.getUserByUsername(principal.getName()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), post.getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        postService.deletePost(post);

        return new ResponseEntity<>("Post deleted", HttpStatus.OK);
    }

    // Get post image | SECURITY: CHECKED
    @Operation(summary = "Get post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post image found", content = {
                    @Content(mediaType = "image/jpeg"),
                    @Content(mediaType = "image/png"),
            }),
            @ApiResponse(responseCode = "404", description = "Post image not found", content = @Content),
    })
    @GetMapping("/posts/{postId}/pictures")
    public ResponseEntity<Object> getPostImage(@PathVariable Long postId) {
        Post post = postService.getPostById(postId);
        if (post == null || post.getImage() == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Blob image = post.getImage();
        if (image == null) {
            return new ResponseEntity<>("Post image not found", HttpStatus.NOT_FOUND);
        }

        try {
            int blobLength = (int) image.length();
            byte[] blobAsBytes = image.getBytes(1, blobLength);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(blobAsBytes);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // REPLIES ----------------------------------------------------------------

    // Get replies of a post | SECURITY: CHECKED
    @Operation(summary = "Get replies of a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Replies not found", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @GetMapping("/posts/{postId}/replies/all")
    public ResponseEntity<List<Reply>> getRepliesOfPost(@PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "creationDate") String order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replies = replyService.getRepliesByPost(postId, pageable, order);
        if (replies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(replies.getContent(), HttpStatus.OK);
    }

    // Get reply by ID | SECURITY: CHECKED
    @Operation(summary = "Get reply by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reply found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Reply not found", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @GetMapping("/replies/{replyId}")
    public ResponseEntity<Reply> getReplyById(@PathVariable Long replyId) {
        Reply reply = replyService.getReplyById(replyId);
        if (reply == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(reply, HttpStatus.OK);
    }

    // Search replies
    /*
     * Possible search criteria:
     * - title
     * - content
     * - author
     */ // SECURITY: CHECKED
    @Operation(summary = "Search replies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Replies not found", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @GetMapping("/replies")
    public ResponseEntity<List<Reply>> searchReplies(@RequestParam String criteria, @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replies = null;
        switch (criteria) {
            case "title":
                replies = replyService.getRepliesByTitle(query, pageable);
                break;
            case "content":
                replies = replyService.getReplyByContent(query, pageable);
                break;
            case "author":
                replies = replyService.getRepliesByAuthor(query, pageable);
                break;
            default:
                replies = replyService.searchReplies(query, pageable);
                break;
        }
        if (replies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(replies.getContent(), HttpStatus.OK);
    }

    // Search replies by post
    /*
     * Possible search criteria:
     * - title
     * - content
     * - author
     * - query (searches by title and content) (default)
     */ // SECURITY: CHECKED
    @Operation(summary = "Search replies by post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Replies found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Replies not found", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @GetMapping("/posts/{postId}/replies")
    public ResponseEntity<List<Reply>> searchRepliesByPost(@PathVariable Long postId, @RequestParam String criteria,
            @RequestParam String query, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Reply> replies = null;
        switch (criteria) {
            case "title":
                replies = replyService.searchRepliesByPost(postId, query, "title", pageable);
                break;
            case "content":
                replies = replyService.searchRepliesByPost(postId, query, "content", pageable);
                break;
            case "author":
                replies = replyService.searchRepliesByPost(postId, query, "author", pageable);
                break;
            default:
                replies = replyService.searchRepliesByPost(postId, query, "query", pageable);
                break;
        }
        if (replies.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(replies.getContent(), HttpStatus.OK);
    }

    // Create a reply | SECURITY: CHECKED
    @Operation(summary = "Create a reply")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reply created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "413", description = "Payload too large", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @PostMapping("/posts/{postId}/replies")
    public ResponseEntity<Reply> createReply(HttpServletRequest request, @PathVariable Long postId,
            @RequestBody Map<String, String> replyData) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // does the reply have a title and content?
        if (!replyData.containsKey("title") || !replyData.containsKey("content")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        // is the user a member of the community?
        User author = userService.getUserByUsername(principal.getName());
        
        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(), post.getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Max title length: 150 characters
        if (replyData.get("title").length() > 150) {
            return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);
        }

        // Max content length: 500 characters
        if (replyData.get("content").length() > 500) {
            return new ResponseEntity<>(HttpStatus.PAYLOAD_TOO_LARGE);
        }

        Reply reply = new Reply(replyData.get("title"), replyData.get("content"), author, post);
        replyService.saveReply(reply);
        post.setComments(post.getComments() + 1);
        postService.updatePost(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(reply.getIdentifier()).toUri();

        return ResponseEntity.created(location).body(reply);
    }

    // Like a reply | SECURITY: CHECKED
    @Operation(summary = "Modify a reply (like)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reply liked", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Reply.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Reply not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
    })
    @JsonView(ReplyInfo.class)
    @PutMapping("/replies/{replyId}")
    public ResponseEntity<Reply> likeReply(HttpServletRequest request, @PathVariable Long replyId,
            @RequestParam String action) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the reply found?
        Reply reply = replyService.getReplyById(replyId);
        if (reply == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(principal.getName(),
                reply.getPost().getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        User user = userService.getUserByUsername(principal.getName());

        // like or unlike
        if ("like".equals(action)) {
            // if user has not liked the reply yet
            if (!replyService.hasUserLikedReply(user.getUsername(), replyId)) {
                replyService.likeReply(reply, user.getUsername());
                return new ResponseEntity<>(reply, HttpStatus.OK);
            } else {
                replyService.unlikeReply(reply, user.getUsername());
                return new ResponseEntity<>(reply, HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Has user liked a reply? | SECURITY: CHECKED
    @Operation(summary = "Returns whether the user has liked a reply")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User has liked the reply", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Boolean.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Reply not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
    })
    @GetMapping("/replies/{replyId}/votes")
    public ResponseEntity<Boolean> hasUserLikedReply(@PathVariable Long replyId, @RequestParam String username) {
        Reply reply = replyService.getReplyById(replyId);
        if (reply == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (username == null || username.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(replyService.hasUserLikedReply(username, replyId), HttpStatus.OK);
    }

    // Delete a reply | SECURITY: CHECKED
    @Operation(summary = "Delete a reply")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reply deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reply not found", content = @Content),
    })
    @DeleteMapping("/replies/{replyId}")
    public ResponseEntity<String> deleteReply(HttpServletRequest request, @PathVariable Long replyId) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the user's account disabled?
        if (userService.isAccountDisabled(request.getUserPrincipal().getName())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the reply found?
        Reply reply = replyService.getReplyById(replyId);
        if (reply == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the reply, the post author, an admin community,
        // moderator or a site admin?
        User author = userService.getUserByUsername(principal.getName());
        if (!reply.getAuthor().equals(author) && !reply.getPost().getAuthor().equals(author)
                && !communityService.isUserAdminOfCommunity(author.getUsername(),
                        reply.getPost().getCommunity().getIdentifier())
                && !communityService.isUserModeratorOfCommunity(author.getUsername(),
                        reply.getPost().getCommunity().getIdentifier())
                && !userService.getUserByUsername(author.getUsername()).getRoles().contains("ADMIN")) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // is the user banned from the community?
        if (communityService.isUserBannedFromCommunity(author.getUsername(),
                reply.getPost().getCommunity().getIdentifier())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // get the post
        Post post = reply.getPost();
        post.setComments(post.getComments() - 1);
        replyService.deleteReply(reply);
        postService.updatePost(post);

        return new ResponseEntity<>("Reply deleted", HttpStatus.OK);
    }

    // Get the most liked posts of the most followed users the user follows (sorting
    // the posts by upvotes) | SECURITY: CHECKED
    @Operation(summary = "Get the most liked posts of the most followed users the user follows")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/users/me/following/posts/most-liked")
    public ResponseEntity<List<Post>> getMostLikedPostsOfMostFollowedUsers(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostLikedPostsOfMostFollowedUsers(principal.getName(), pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }

    // Get the most liked posts of the user's communities (sorting the posts by
    // upvotes) | SECURITY: CHECKED
    @Operation(summary = "Get the most liked posts of the user's communities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/users/me/communities/posts/most-liked")
    public ResponseEntity<List<Post>> getMostLikedPostsOfUserCommunities(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostLikedPostsOfUserCommunities(principal.getName(), pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }

    // Get the most recent posts of the user's communities (sorting the posts by
    // creation date) | SECURITY: CHECKED
    @Operation(summary = "Get the most recent posts of the user's communities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/users/me/communities/posts/most-recent")
    public ResponseEntity<List<Post>> getMostRecentPostsOfUserCommunities(HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostRecentPostsOfFollowedCommunities(principal.getName(), pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }

    // Get the most liked posts of all communities (sorting the posts by upvotes) | SECURITY: CHECKED
    @Operation(summary = "Get the most liked posts of the most followed (popular) communities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/communities/most-popular/posts/most-liked")
    public ResponseEntity<List<Post>> getMostLikedPostsOfAllCommunities(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostLikedPostsOfMostFollowedCommunities(pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }

    // Get the most liked posts of the most followed users (sorting the posts by upvotes) | SECURITY: CHECKED
    @Operation(summary = "Get the most liked posts of the most followed users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/users/posts/most-liked")
    public ResponseEntity<List<Post>> getMostLikedPostsOfMostFollowedUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostLikedPostsOfMostFollowedUsersGeneral(pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }

    // Get the most recent posts of the most followed communities (sorting the posts by creation date) | SECURITY: CHECKED
    @Operation(summary = "Get the most recent posts of the most followed communities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "204", description = "Posts not found", content = @Content),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/communities/most-popular/posts/most-recent")
    public ResponseEntity<List<Post>> getMostRecentPostsOfMostFollowedCommunities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.getMostRecentPostsOfMostFollowedCommunities(pageable);
        if (posts.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
    }
}
