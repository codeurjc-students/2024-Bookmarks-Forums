package com.example.backend.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.Principal;
import java.sql.Blob;
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

import com.example.backend.dto.PostDTO;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.entity.Post;

@RestController
@RequestMapping("/api/v1")
public class APIPostController {

    private final CommunityService communityService;
    private final PostService postService;
    private final UserService userService;

    interface PostInfo extends Post.BasicInfo, User.UsernameInfo, Community.NameInfo {
    }

    public APIPostController(CommunityService communityService, PostService postService, UserService userService) {
        this.communityService = communityService;
        this.postService = postService;
        this.userService = userService;
    }

    // Get post by ID
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
     */
    @Operation(summary = "Search posts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts found", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)),
            }),
            @ApiResponse(responseCode = "404", description = "Posts not found"),
    })
    @JsonView(PostInfo.class)
    @GetMapping("/posts")
    public ResponseEntity<Page<Post>> searchPosts(@RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "creationDate") String order) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Post> posts = postService.searchPosts(query, pageable, order);
        if (posts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(posts);
    }

    // Search posts by community identifier
    /*
     * Possible orders:
     * - creationDate
     * - lastModifiedDate
     * - replies
     * - likes
     */
    // Get posts of a community
    @Operation(summary = "Get posts of a community using its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found the posts", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = PostInfo.class))

            }),
            @ApiResponse(responseCode = "404", description = "Community not found", content = @Content)
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
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(posts.getContent(), HttpStatus.OK);
            }
        }
    }

    // Create a post
    @Operation(summary = "Create a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
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

    // Edit a post (or upvote/downvote)
    @Operation(summary = "Edit a post (or upvote/downvote)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post edited", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
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

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // upvote or downvote
        switch (action) {
            case "upvote":
            // TODO: check if the user has already upvoted the post
                postService.upvotePost(post);
                return new ResponseEntity<>(post, HttpStatus.OK);
            case "downvote":
            // TODO: check if the user has already downvoted the post
                postService.downvotePost(post);
                return new ResponseEntity<>(post, HttpStatus.OK);
            case "edit":
                // is the user the author of the post?
                User author = userService.getUserByUsername(principal.getName());
                if (!post.getAuthor().equals(author)) {
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
                        } catch (IOException e) {
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                    } catch (IOException e) {
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                } else {
                    post.setImage(null);
                }

                postService.updatePost(post);
                return new ResponseEntity<>(post, HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    // Update post image (if action = "delete", the image will be deleted)
    @Operation(summary = "Update post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post image updated", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = Post.class)),
            }),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
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

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the post?
        User author = userService.getUserByUsername(principal.getName());
        if (!post.getAuthor().equals(author)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        // delete image
        if (action != null && action.equals("delete")) {
            post.setImage(null);
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

    // Delete a post
    @Operation(summary = "Delete a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
    })
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(HttpServletRequest request, @PathVariable Long postId) {

        // is user logged in?
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // is the post found?
        Post post = postService.getPostById(postId);
        if (post == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // is the user the author of the post?
        User author = userService.getUserByUsername(principal.getName());
        if (!post.getAuthor().equals(author)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        postService.deletePost(post);

        return new ResponseEntity<>("Post deleted", HttpStatus.OK);
    }

    // Get post image
    @Operation(summary = "Get post image")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post image found", content = {
                    @Content(mediaType = "image/jpeg"),
                    @Content(mediaType = "image/png"),
            }),
            @ApiResponse(responseCode = "404", description = "Post image not found"),
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
}
