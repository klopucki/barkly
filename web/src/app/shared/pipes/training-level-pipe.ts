import { Pipe, PipeTransform } from '@angular/core';
import { TrainingLevel } from '../../features/trainings/training.model';

@Pipe({
  name: 'trainingLevel',
})
export class TrainingLevelPipe implements PipeTransform {
  transform(value: TrainingLevel): string {
    switch (value) {
      case 'PUPPY':
        return 'Puppy Training';
      case 'BASIC':
        return 'Basic Obedience';
      case 'ADVANCED':
        return 'Advanced Training';
      case 'SPORT':
        return 'Sport Training';
      default:
        return value;
    }
  }
}
