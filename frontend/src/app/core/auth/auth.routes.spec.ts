import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../app.routes';
import { AuthService, AuthSession } from './auth.service';

const complete: AuthSession = { userId: '1', email: 'alex@example.com', username: 'alex_rivera', displayName: 'Alex Rivera', emailVerified: true, profileComplete: true };

describe('Application authentication route integration', () => {
  it.each([
    ['/welcome', null, '/login'],
    ['/onboarding/display-name', null, '/login'],
    ['/register/verified', null, '/login'],
    ['/register/verify?token=emailed-token', null, '/register/verify?token=emailed-token'],
    ['/welcome', { ...complete, username: null, displayName: null, profileComplete: false }, '/onboarding/username'],
    ['/onboarding/username', { ...complete, displayName: null, profileComplete: false }, '/onboarding/display-name'],
    ['/onboarding/display-name', { ...complete, displayName: null, profileComplete: false }, '/onboarding/display-name'],
    ['/register', complete, '/welcome'], ['/login', complete, '/welcome'], ['/welcome', complete, '/welcome'],
  ] as const)('direct entry to %s is checked against the server session', async (url, user, destination) => {
    const auth = {
      session: signal<AuthSession | null>(user), pendingEmail: signal(null), googleEnabled: signal(false),
      refreshSession: vi.fn().mockResolvedValue(user),
      nextRoute: (value: AuthSession | null) => AuthService.prototype.nextRoute.call(auth as unknown as AuthService, value),
    };
    TestBed.configureTestingModule({ providers: [provideRouter(routes), provideHttpClient(), { provide: AuthService, useValue: auth }] });
    const harness = await RouterTestingHarness.create(url);
    expect(TestBed.inject(Router).url).toBe(destination);
    expect(auth.refreshSession).toHaveBeenCalled();
    expect(harness.routeNativeElement?.querySelector('h1')).not.toBeNull();
  });
});
