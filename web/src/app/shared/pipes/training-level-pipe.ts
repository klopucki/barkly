import { Pipe, PipeTransform } from '@angular/core';
import { TrainingLevel } from '../../features/trainings/training.model';

@Pipe({
  name: 'trainingLevel',
})
export class TrainingLevelPipe implements PipeTransform {
  transform(value: TrainingLevel): string {
    switch (value) {
      case 'puppy':
        return 'Puppy Training';

      case 'basic':
        return 'Basic Obedience';

      case 'advanced':
        return 'Advanced Training';

      case 'sport':
        return 'Sport Training';

      default:
        return value;
    }
  }
}
