import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withXsrfConfiguration } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService, AuthSession } from './auth.service';

const USER: AuthSession = {
  userId: '00000000-0000-4000-8000-000000000001', email: 'alex@example.com',
  username: 'alex_rivera', displayName: 'Alex Rivera', emailVerified: true, profileComplete: true,
};
const settle = async () => { for (let i = 0; i < 8; i++) await Promise.resolve(); };

describe('AuthService HTTP contract', () => {
  let auth: AuthService;
  let http: HttpTestingController;
  const session = (user: AuthSession | null = null, googleEnabled = false) => {
    http.expectOne('/api/auth/session').flush({ authenticated: !!user, user, googleEnabled });
  };
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' })),
      provideHttpClientTesting(),
    ] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    document.cookie = 'XSRF-TOKEN=test-csrf; Path=/';
  });
  afterEach(() => {
    http.verify();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; Path=/';
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('uses server identity and provider availability, never browser storage', async () => {
    const local = vi.spyOn(Storage.prototype, 'setItem');
    const request = auth.refreshSession();
    session(USER, true);
    expect(await request).toEqual(USER);
    expect(auth.googleEnabled()).toBe(true);
    expect(local).not.toHaveBeenCalled();
  });
  it('does not trust a user DTO when authenticated is false', async () => {
    const request = auth.refreshSession();
    http.expectOne('/api/auth/session').flush({ authenticated: false, user: USER, googleEnabled: false });
    expect(await request).toBeNull();
  });
  it('clears stale identity on a failed session fetch', async () => {
    auth.session.set(USER);
    const request = auth.refreshSession();
    const rejected = expect(request).rejects.toThrow('could not reach');
    http.expectOne('/api/auth/session').error(new ProgressEvent('error'));
    await rejected;
    expect(auth.session()).toBeNull();
  });
  it('bootstraps CSRF then sends form-encoded login and refreshes the identity', async () => {
    const pending = auth.login('  alex_rivera  ', '  spaces are part of my password  ');
    session(); await settle();
    const request = http.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.get('username')).toBe('alex_rivera');
    expect(request.request.body.get('password')).toBe('  spaces are part of my password  ');
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('test-csrf');
    request.flush(''); await settle(); session(USER); await pending;
    expect(auth.session()).toEqual(USER);
  });
  it('registers only a normalized email and remembers it only in memory', async () => {
    const storage = vi.spyOn(Storage.prototype, 'setItem');
    const pending = auth.register(' Alex@EXAMPLE.com ');
    session(); await settle();
    const request = http.expectOne('/api/auth/register');
    expect(request.request.body).toEqual({ email: 'alex@example.com' });
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('test-csrf');
    request.flush(null, { status: 202, statusText: 'Accepted' }); await pending;
    expect(auth.pendingEmail()).toBe('alex@example.com');
    expect(storage).not.toHaveBeenCalled();
  });
  it('verifies with token plus new password, then replaces pending state with the server response', async () => {
    auth.pendingEmail.set(USER.email);
    const pending = auth.verifyEmail('emailed-token', 'a private new passphrase');
    session(); await settle();
    const request = http.expectOne('/api/auth/verify');
    expect(request.request.body).toEqual({ token: 'emailed-token', password: 'a private new passphrase' });
    request.flush(null); await settle(); session({ ...USER, username: null, displayName: null, profileComplete: false });
    await pending;
    expect(auth.pendingEmail()).toBeNull();
    expect(auth.session()?.profileComplete).toBe(false);
  });
  it('resends only after an email has been submitted', async () => {
    await expect(auth.resendVerification()).rejects.toThrow('Enter your email');
    http.expectNone('/api/auth/verification');
    auth.pendingEmail.set(USER.email);
    const pending = auth.resendVerification(); session(); await settle();
    const request = http.expectOne('/api/auth/verification');
    expect(request.request.body).toEqual({ email: USER.email });
    request.flush(null); await pending;
  });
  it.each([
    ['chooseUsername', 'alex_rivera', { username: 'alex_rivera' }],
    ['chooseDisplayName', 'Alex Rivera', { displayName: 'Alex Rivera' }],
  ] as const)('saves %s as a single profile field and refreshes authorities from the server', async (method, value, body) => {
    const pending = auth[method](value);
    const request = http.expectOne('/api/auth/profile');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual(body);
    request.flush(USER); await settle(); session(USER); await pending;
  });
  it('clears local state after confirmed logout without depending on another network request', async () => {
    auth.session.set(USER); auth.pendingEmail.set(USER.email);
    const pending = auth.logout(); session(USER); await settle();
    const request = http.expectOne('/api/auth/logout');
    expect(request.request.headers.get('X-XSRF-TOKEN')).toBe('test-csrf');
    request.flush(''); await pending;
    expect(auth.session()).toBeNull(); expect(auth.pendingEmail()).toBeNull();
    http.expectNone('/api/auth/session'); // The login route guard owns the next anonymous bootstrap.
  });
  it.each([
    [400, 'Check your details'], [401, 'Sign-in failed'], [403, 'refreshing'],
    [409, 'username is unavailable'], [429, 'Too many attempts'],
    [503, 'Email delivery'], [500, 'Something went wrong'],
  ])('maps HTTP %i without exposing backend diagnostics', async (status, message) => {
    const pending = auth.chooseUsername('alex_rivera');
    const rejected = expect(pending).rejects.toThrow(message);
    http.expectOne('/api/auth/profile').flush({ message: 'secret diagnostic or account existence' }, { status, statusText: 'Error' });
    await rejected;
  });
  it('starts Google login using the backend authorization route', () => {
    const assign = vi.fn();
    vi.stubGlobal('window', { location: { assign } });
    auth.startGoogleLogin();
    expect(assign).toHaveBeenCalledExactlyOnceWith('/oauth2/authorization/google');
  });
});
