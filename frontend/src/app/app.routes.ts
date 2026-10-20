import { Routes } from '@angular/router';
import { accountStateGuard } from './core/auth/auth.guards';

const authPage = () => import('./features/auth/pages/auth-page/auth-page').then(module => module.AuthPage);

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    title: 'Projile — A little more together',
    loadComponent: () => import('./features/landing/pages/landing-page/landing-page').then(module => module.LandingPage),
  },
  ...[
    { path: 'login', step: 'login', title: 'Welcome back' },
    { path: 'register', step: 'register', title: 'Create your account' },
    { path: 'register/verify', step: 'verify', title: 'Verify your email' },
    { path: 'register/verified', step: 'verified', title: 'Email verified' },
    { path: 'onboarding/username', step: 'username', title: 'Choose your username' },
    { path: 'onboarding/display-name', step: 'display-name', title: 'Make yourself at home' },
    { path: 'welcome', step: 'welcome', title: 'You’re all set' },
  ].map(page => ({
    path: page.path,
    title: `Projile — ${page.title}`,
    data: { step: page.step },
    canActivate: [accountStateGuard],
    loadComponent: authPage,
  })),
  {
    path: 'users',
    title: 'Projile — user lookup',
    canActivate: [accountStateGuard],
    loadComponent: () => import('./features/user/component/user-component/user-component').then(module => module.UserComponent),
  },
  { path: '**', redirectTo: '' },
];
