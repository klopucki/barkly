import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home').then((m) => m.Home),
  },

  {
    path: 'schools',
    loadComponent: () => import('./pages/schools/schools').then((m) => m.Schools),
  },

  {
    path: 'trainings',
    loadComponent: () => import('./pages/trainings/trainings').then((m) => m.Trainings),
  },
  {
    path: 'trainings/:id',
    loadComponent: () =>
      import('./pages/training-details/training-details').then((m) => m.TrainingDetails),
  },

  {
    path: 'news',
    loadComponent: () => import('./pages/news/news').then((m) => m.News),
  },

  { path: '**', redirectTo: '' },
];
