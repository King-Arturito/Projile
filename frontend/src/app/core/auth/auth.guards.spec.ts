import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRouteSnapshot, provideRouter, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { accountStateGuard } from './auth.guards';
import { AuthService, AuthSession } from './auth.service';

const complete: AuthSession = { userId: '1', email: 'alex@example.com', username: 'alex_rivera', displayName: 'Alex Rivera', emailVerified: true, profileComplete: true };
const username = { ...complete, username: null, displayName: null, profileComplete: false };
const displayName = { ...complete, displayName: null, profileComplete: false };

describe('accountStateGuard', () => {
  beforeEach(() => TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()] }));
  it.each([
    [null, 'login', 'login', true], [null, 'register', 'register', true],
    [null, 'verify', 'register/verify', true], [null, 'welcome', 'welcome', '/login'],
    [null, 'verified', 'register/verified', '/login'], [null, 'username', 'onboarding/username', '/login'],
    [username, 'welcome', 'welcome', '/onboarding/username'],
    [username, 'display-name', 'onboarding/display-name', '/onboarding/username'],
    [username, 'username', 'onboarding/username', true],
    [username, 'verified', 'register/verified', true],
    [displayName, 'username', 'onboarding/username', '/onboarding/display-name'],
    [displayName, 'display-name', 'onboarding/display-name', true],
    [complete, 'register', 'register', '/welcome'], [complete, 'login', 'login', '/welcome'],
    [complete, 'welcome', 'welcome', true], [complete, undefined, 'users', true],
    [{ ...username, emailVerified: false }, 'welcome', 'welcome', '/register/verify'],
  ] as const)('routes state %j at %s using server state', async (user, step, path, expected) => {
    vi.spyOn(TestBed.inject(AuthService), 'refreshSession').mockResolvedValue(user);
    const route = { data: { step }, routeConfig: { path } } as unknown as ActivatedRouteSnapshot;
    const result = await TestBed.runInInjectionContext(() => accountStateGuard(route, {} as RouterStateSnapshot));
    expect(result instanceof UrlTree ? TestBed.inject(Router).serializeUrl(result) : result).toBe(expected);
  });
  it.each([['login', true], ['register', true], ['verify', true], ['welcome', '/login'], ['display-name', '/login']])(
    'allows only public views when session bootstrap fails (%s)', async (step, expected) => {
      vi.spyOn(TestBed.inject(AuthService), 'refreshSession').mockRejectedValue(new Error('offline'));
      const route = { data: { step }, routeConfig: { path: step } } as unknown as ActivatedRouteSnapshot;
      const result = await TestBed.runInInjectionContext(() => accountStateGuard(route, {} as RouterStateSnapshot));
      expect(result instanceof UrlTree ? TestBed.inject(Router).serializeUrl(result) : result).toBe(expected);
    });
});
