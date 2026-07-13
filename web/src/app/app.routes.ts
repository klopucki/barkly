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
  { path: 'schools/:slug', loadComponent: () => import('./pages/school-details/school-details').then((m) => m.SchoolDetails) },
  { path: 'account', loadComponent: () => import('./pages/account/account').then((m) => m.Account) },
  { path: 'my-school', loadComponent: () => import('./pages/my-school/my-school').then((m) => m.MySchool) },

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
