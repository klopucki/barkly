package pl.barkly.training.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barkly.training.api.TrainingResponse;
import pl.barkly.training.persistence.TrainingPawReactionEntity;
import pl.barkly.training.persistence.TrainingPawReactionRepository;
import pl.barkly.training.persistence.TrainingRepository;
import pl.barkly.user.UserService;

@Service
class TrainingPawReactionService {
    private final TrainingRepository trainings;
    private final TrainingPawReactionRepository reactions;
    private final UserService users;
    TrainingPawReactionService(TrainingRepository trainings, TrainingPawReactionRepository reactions, UserService users) { this.trainings = trainings; this.reactions = reactions; this.users = users; }

    @Transactional
    TrainingResponse toggle(Long trainingId) {
        var training = trainings.findByIdAndDeletedAtIsNull(trainingId).orElseThrow();
        var user = users.currentUser();
        reactions.findByTrainingIdAndUser_Id(trainingId, user.getId()).ifPresentOrElse(reactions::delete,
                () -> reactions.save(new TrainingPawReactionEntity(trainingId, user)));
        long count = reactions.countByTrainingId(trainingId);
        boolean mine = reactions.findByTrainingIdAndUser_Id(trainingId, user.getId()).isPresent();
        return training.toResponse(0, count, mine);
    }
}
