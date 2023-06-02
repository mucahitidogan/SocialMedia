package com.bilgeadam.service;

import com.bilgeadam.dto.request.CreateCommentRequestDto;
import com.bilgeadam.dto.request.CreateNewPostRequestDto;
import com.bilgeadam.dto.request.UpdatePostRequestDto;
import com.bilgeadam.dto.response.UserProfileResponseDto;
import com.bilgeadam.exception.ErrorType;
import com.bilgeadam.exception.PostManagerException;
import com.bilgeadam.manager.IUserProfileManager;
import com.bilgeadam.mapper.IPostMapper;
import com.bilgeadam.rabbitmq.model.CreatePostModel;
import com.bilgeadam.rabbitmq.model.UserProfileResponseModel;
import com.bilgeadam.rabbitmq.producer.CreatePostProducer;
import com.bilgeadam.repository.IPostRepository;
import com.bilgeadam.repository.entity.Comment;
import com.bilgeadam.repository.entity.Like;
import com.bilgeadam.repository.entity.Post;
import com.bilgeadam.utility.JwtTokenProvider;
import com.bilgeadam.utility.ServiceManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService extends ServiceManager<Post, String> {
    private final IPostRepository postRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUserProfileManager userProfileManager;
    private final CreatePostProducer createPostProducer;
    private final LikeService likeService;
    private final DislikeService dislikeService;
    private final CommentService commentService;

    public PostService(IPostRepository postRepository, JwtTokenProvider jwtTokenProvider, IUserProfileManager userProfileManager, CreatePostProducer createPostProducer, LikeService likeService, DislikeService dislikeService, CommentService commentService) {
        super(postRepository);
        this.postRepository = postRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userProfileManager = userProfileManager;
        this.createPostProducer = createPostProducer;
        this.likeService = likeService;
        this.dislikeService = dislikeService;
        this.commentService = commentService;
    }

    public Post createPost(String token, CreateNewPostRequestDto dto){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        if (authid.isEmpty()){
            throw new PostManagerException(ErrorType.INVALID_TOKEN);
        }
        UserProfileResponseDto userProfile = userProfileManager.findByAuthid(authid.get()).getBody();
        Post post = IPostMapper.INSTANCE.toPost(dto);
        post.setUserid(userProfile.getId());
        post.setUsername(userProfile.getUsername());
        post.setAvatar(userProfile.getAvatar());
        return save(post);
    }

    public Post createPostWithRabbitMq(String token, CreateNewPostRequestDto dto){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        if (authid.isEmpty()){
            throw new PostManagerException(ErrorType.INVALID_TOKEN);
        }
        CreatePostModel model = CreatePostModel.builder().authid(authid.get()).build();
        UserProfileResponseModel userProfile = (UserProfileResponseModel) createPostProducer.createPost(model);
        Post post = IPostMapper.INSTANCE.toPost(dto);
        post.setId(userProfile.getUserid());
        post.setUsername(userProfile.getUsername());
        post.setAvatar(userProfile.getAvatar());
        return save(post);
    }

    public Post updatePost(String token, String postid, UpdatePostRequestDto dto){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        authid.orElseThrow(() -> new PostManagerException(ErrorType.INVALID_TOKEN));

        UserProfileResponseDto userProfile = userProfileManager.findByAuthid(authid.get()).getBody();
        Optional<Post> optionalPost = postRepository.findById(postid);
        if(userProfile.getId().equals(optionalPost.get().getUserid())){
            optionalPost.get().getMediaUrls().addAll(dto.getAddMediaUrls());
            optionalPost.get().getMediaUrls().removeAll(dto.getRemoveMediaUrls());
            optionalPost.get().setContent(dto.getContent());
            return update(optionalPost.get());
        }
        throw new PostManagerException(ErrorType.POST_NOT_FOUND);
    }

    public Boolean likePost(String token, String postid){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        if (authid.isEmpty()){
            throw new PostManagerException(ErrorType.INVALID_TOKEN);
        }
        UserProfileResponseDto userProfile = userProfileManager.findByAuthid(authid.get()).getBody();
        Optional<Post> optionalPost = postRepository.findById(postid);
        Optional<Like> optionalLike = likeService.findByUseridAndPostid(userProfile.getId(), postid);
        if(optionalLike.isPresent()){
            optionalPost.get().getLikes().remove(userProfile.getId());
            update(optionalPost.get());
            likeService.delete(optionalLike.get());
            return true;
        }else{
            Like like = Like.builder()
                    .userid(userProfile.getId())
                    .username(userProfile.getUsername())
                    .avatar(userProfile.getAvatar())
                    .postid(postid)
                    .build();
            likeService.save(like);
            if(optionalPost.isEmpty()){
                throw new PostManagerException(ErrorType.POST_NOT_FOUND);
            }
            optionalPost.get().getLikes().add(userProfile.getId());
            update(optionalPost.get());
            return true;
        }
    }

    public Boolean deletePost(String token, String postid){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        if (authid.isEmpty()){
            throw new PostManagerException(ErrorType.INVALID_TOKEN);
        }
        UserProfileResponseDto userProfile = userProfileManager.findByAuthid(authid.get()).getBody();
        Optional<Post> optionalPost = findById(postid);
        if(optionalPost.get().getUserid().equals(userProfile.getId())){
            optionalPost.get().getLikes().forEach(x -> likeService.deleteByUseridAndPostid(x, postid));
            System.out.println(optionalPost.get().getLikes());
            deleteById(postid);
            return true;
        }else{
            throw new PostManagerException(ErrorType.POST_NOT_FOUND);
        }
    }

    public Boolean createComment(String token, CreateCommentRequestDto dto){
        Optional<Long> authid = jwtTokenProvider.getIdFromToken(token);
        if (authid.isEmpty()){
            throw new PostManagerException(ErrorType.INVALID_TOKEN);
        }
        UserProfileResponseDto userProfile = userProfileManager.findByAuthid(authid.get()).getBody();
        Optional<Post> optionalPost = findById(dto.getPostId());
        if(optionalPost.isPresent()){
            Comment comment = Comment.builder()
                    .userid(userProfile.getId())
                    .postid(dto.getPostId())
                    .username(userProfile.getUsername())
                    .comment(dto.getComment())
                    .build();
            commentService.save(comment);
            if(dto.getCommentId() != null){
                Optional<Comment> optionalComment = commentService.findById(dto.getCommentId());
                if(optionalComment.isEmpty())
                    throw new PostManagerException(ErrorType.COMMENT_NOT_FOUND);
                optionalComment.get().getSubCommentid().add(comment.getId());
                commentService.update(optionalComment.get())
;            }
            optionalPost.get().getComments().add(comment.getId());
            save(optionalPost.get());
            return true;
        }
        throw new PostManagerException(ErrorType.POST_NOT_FOUND);
    }
}
