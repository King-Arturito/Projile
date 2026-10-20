import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Client routing is a convenience; the server enforces the same account state. */
export const accountStateGuard: CanActivateFn = async route => {
  const auth = inject(AuthService);
  const router = inject(Router);
  try {
    const user = await auth.refreshSession();
    const step = route.data['step'] as string | undefined;
    if (!user) {
      return ['login', 'register', 'verify'].includes(step ?? '') || router.parseUrl('/login');
    }
    const destination = auth.nextRoute(user);
    if (step === 'verified' && user.emailVerified && !user.profileComplete) return true;
    if (route.routeConfig?.path === destination.substring(1)) return true;
    if (!step && user.profileComplete) return true;
    return router.parseUrl(destination);
  } catch {
    return ['login', 'register', 'verify'].includes(route.data['step'] ?? '') || router.parseUrl('/login');
  }
};
