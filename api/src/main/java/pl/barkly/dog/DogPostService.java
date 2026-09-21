package pl.barkly.dog;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.barkly.dog.api.DogPostResponse;
import pl.barkly.dog.api.DogPostCommentRequest;
import pl.barkly.dog.api.DogPostCommentResponse;
import pl.barkly.dog.api.PostReactionRequest;
import pl.barkly.dog.api.PostReactionSummary;
import pl.barkly.training.image.TrainingImageStorage;
import pl.barkly.user.UserEntity;
import pl.barkly.user.UserService;

import java.util.List;
import java.util.Arrays;

@Service
class DogPostService {
    private final DogRepository dogs;
    private final DogPostRepository posts;
    private final UserService users;
    private final TrainingImageStorage storage;
    private final DogPostReactionRepository reactions;
    private final DogPostCommentRepository comments;

    DogPostService(DogRepository dogs, DogPostRepository posts, UserService users, TrainingImageStorage storage,
                   DogPostReactionRepository reactions, DogPostCommentRepository comments) {
        this.dogs = dogs; this.posts = posts; this.users = users; this.storage = storage; this.reactions = reactions; this.comments = comments;
    }

    @Transactional(readOnly = true)
    List<DogPostResponse> feed() {
        UserEntity user = users.optionalCurrentUser().orElse(null);
        return posts.findFeed().stream().filter(post -> canView(post.getDog(), user)).map(this::response).toList();
    }

    @Transactional
    DogPostResponse create(Long dogId, String content, MultipartFile image) {
        DogEntity dog = dogs.findById(dogId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dog not found"));
        UserEntity user = users.currentUser();
        if (!dog.getOwner().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot post for this dog");
        String text = content == null ? "" : content.trim();
        if (text.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post content is required");
        String imageKey = image == null || image.isEmpty() ? null : storage.store(image);
        try {
            return response(posts.save(new DogPostEntity(dog, text, imageKey)));
        } catch (RuntimeException exception) {
            if (imageKey != null) storage.delete(imageKey);
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    Resource loadImage(Long id) { return storage.load(imagePost(id).getImageKey()); }

    @Transactional(readOnly = true)
    MediaType imageContentType(Long id) {
        String key = imagePost(id).getImageKey();
        if (key.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (key.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    @Transactional
    DogPostResponse react(Long id, PostReactionRequest request) {
        DogPostEntity post = visiblePost(id);
        UserEntity user = users.currentUser();
        reactions.findByPost_IdAndUser_Id(id, user.getId()).ifPresentOrElse(existing -> {
            if (existing.getReactionType() == request.reactionType()) reactions.delete(existing);
            else existing.setReactionType(request.reactionType());
        }, () -> reactions.save(new DogPostReactionEntity(post, user, request.reactionType())));
        return response(post);
    }

    @Transactional(readOnly = true)
    List<DogPostCommentResponse> comments(Long id) {
        visiblePost(id);
        UserEntity user = users.optionalCurrentUser().orElse(null);
        return comments.findAllByPost_IdOrderByPublishedAtAsc(id).stream().map(comment -> commentResponse(comment, user)).toList();
    }

    @Transactional
    DogPostCommentResponse addComment(Long id, DogPostCommentRequest request) {
        DogPostEntity post = visiblePost(id);
        String content = request.content().trim();
        DogPostCommentEntity comment = comments.save(new DogPostCommentEntity(post, users.currentUser(), content));
        return commentResponse(comment, users.currentUser());
    }

    @Transactional
    void deleteComment(Long id) {
        DogPostCommentEntity comment = comments.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
        if (!comment.getUser().getId().equals(users.currentUser().getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this comment");
        comments.delete(comment);
    }

    private DogPostEntity visiblePost(Long id) {
        DogPostEntity post = posts.findWithDogById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!canView(post.getDog(), users.optionalCurrentUser().orElse(null))) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        return post;
    }

    private DogPostEntity imagePost(Long id) {
        DogPostEntity post = visiblePost(id);
        if (post.getImageKey() == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post image not found");
        return post;
    }

    private boolean canView(DogEntity dog, UserEntity user) {
        return (user != null && dog.getOwner().getId().equals(user.getId())) || dog.getVisibility() == DogVisibility.PUBLIC || (dog.getVisibility() == DogVisibility.MEMBERS && user != null);
    }

    private DogPostResponse response(DogPostEntity post) {
        UserEntity user = users.optionalCurrentUser().orElse(null);
        List<DogPostReactionEntity> allReactions = reactions.findAllByPost_Id(post.getId());
        List<PostReactionSummary> summaries = Arrays.stream(PostReactionType.values())
                .map(type -> new PostReactionSummary(type, allReactions.stream().filter(reaction -> reaction.getReactionType() == type).count()))
                .filter(summary -> summary.count() > 0).toList();
        PostReactionType myReaction = user == null ? null : allReactions.stream()
                .filter(reaction -> reaction.getUser().getId().equals(user.getId())).map(DogPostReactionEntity::getReactionType).findFirst().orElse(null);
        return new DogPostResponse(post.getId(), post.getDog().getId(), post.getDog().getName(), post.getDog().getOwner().getDisplayName(), post.getContent(), post.getImageKey() != null, post.getPublishedAt(), summaries, myReaction, comments.countByPost_Id(post.getId()));
    }

    private DogPostCommentResponse commentResponse(DogPostCommentEntity comment, UserEntity user) {
        return new DogPostCommentResponse(comment.getId(), comment.getUser().getDisplayName(), comment.getContent(), comment.getPublishedAt(), user != null && comment.getUser().getId().equals(user.getId()));
    }
}
