import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    title: 'Projile — A little more together',
    // Keep the public landing page separate from the application features.
    loadComponent: () =>
      import('./features/landing/pages/landing-page/landing-page').then(
        (module) => module.LandingPage,
      ),
  },
  {
    path: 'users',
    title: 'Projile — user lookup',
    loadComponent: () =>
      import('./features/user/component/user-component/user-component').then(
        (module) => module.UserComponent,
      ),
  },

  {
    path: '**',
    redirectTo: '',
  },
];
