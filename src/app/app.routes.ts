import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Schools } from './pages/schools/schools';
import { Trainings } from './pages/trainings/trainings';
import { News } from './pages/news/news';
import { TrainingDetails } from './pages/training-details/training-details';

export const routes: Routes = [
  { path: '', component: Home },

  { path: 'schools', component: Schools },

  { path: 'trainings', component: Trainings },
  { path: 'trainings/:id', component: TrainingDetails },

  { path: 'news', component: News },

  { path: '**', redirectTo: '' },
];
