import { By } from '@angular/platform-browser';
import { NgControl } from '@angular/forms';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { AuthService, AuthSession } from '../../../../core/auth/auth.service';
import { AuthPage } from './auth-page';
import { LandingPage } from '../../../landing/pages/landing-page/landing-page';

const USER: AuthSession = { userId: '1', email: 'alex@example.com', username: 'alex_rivera', displayName: 'Alex Rivera', emailVerified: true, profileComplete: true };
const steps = [ ['login','login'], ['register','register'], ['register/verify','verify'], ['register/verified','verified'], ['onboarding/username','username'], ['onboarding/display-name','display-name'], ['welcome','welcome'] ];

describe('Authentication pages', () => {
  let harness: RouterTestingHarness;
  let auth: {
    session: ReturnType<typeof signal<AuthSession | null>>; pendingEmail: ReturnType<typeof signal<string | null>>;
    googleEnabled: ReturnType<typeof signal<boolean>>; login: ReturnType<typeof vi.fn>; register: ReturnType<typeof vi.fn>;
    verifyEmail: ReturnType<typeof vi.fn>; resendVerification: ReturnType<typeof vi.fn>;
    chooseUsername: ReturnType<typeof vi.fn>; chooseDisplayName: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>; startGoogleLogin: ReturnType<typeof vi.fn>; nextRoute: ReturnType<typeof vi.fn>;
  };
  const element = () => harness.routeNativeElement!;
  const input = (id: string, value: string) => {
    const field = element().querySelector<HTMLInputElement>(`#${id}`)!;
    field.value = value; field.dispatchEvent(new Event('input')); field.dispatchEvent(new Event('blur'));
    harness.detectChanges(); return field;
  };
  const submit = async () => {
    element().querySelector<HTMLFormElement>('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    await harness.fixture.whenStable(); harness.detectChanges();
  };
  beforeEach(async () => {
    auth = {
      session: signal<AuthSession | null>(null), pendingEmail: signal<string | null>('alex@example.com'), googleEnabled: signal(false),
      login: vi.fn().mockResolvedValue(undefined), register: vi.fn().mockResolvedValue(undefined),
      verifyEmail: vi.fn().mockResolvedValue(undefined), resendVerification: vi.fn().mockResolvedValue(undefined),
      chooseUsername: vi.fn().mockResolvedValue(undefined), chooseDisplayName: vi.fn().mockResolvedValue(undefined),
      logout: vi.fn().mockResolvedValue(undefined), startGoogleLogin: vi.fn(), nextRoute: vi.fn().mockReturnValue('/welcome'),
    };
    TestBed.configureTestingModule({ providers: [
      { provide: AuthService, useValue: auth },
      provideRouter([{ path: '', component: LandingPage }, ...steps.map(([path, step]) => ({ path, component: AuthPage, data: { step } }))]),
    ] });
    harness = await RouterTestingHarness.create();
  });
  afterEach(() => { vi.restoreAllMocks(); localStorage.clear(); sessionStorage.clear(); });

  it('connects the landing-page get-started and login actions', async () => {
    await harness.navigateByUrl('/');
    element().querySelector<HTMLButtonElement>('app-site-header .header-cta')!.click();
    await harness.fixture.whenStable(); harness.detectChanges();
    expect(TestBed.inject(Router).url).toBe('/register');
    await harness.navigateByUrl('/');
    element().querySelector<HTMLAnchorElement>('app-site-header a[href="/login"]')!.click();
    await harness.fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/login');
  });
  it('validates email and submits no password during registration', async () => {
    await harness.navigateByUrl('/register');
    input('register-email', 'not-email'); await submit();
    expect(auth.register).not.toHaveBeenCalled();
    expect(element().textContent).toContain('email');
    input('register-email', 'alex@example.com'); await submit();
    expect(auth.register).toHaveBeenCalledExactlyOnceWith('alex@example.com');
    expect(TestBed.inject(Router).url).toBe('/register/verify');
    expect(element().textContent).toContain('Check your inbox');
  });
  it('never consumes a verification link merely by rendering it', async () => {
    await harness.navigateByUrl('/register/verify?token=emailed-token');
    expect(auth.verifyEmail).not.toHaveBeenCalled();
    expect(element().querySelector<HTMLInputElement>('#verify-password')?.autocomplete).toBe('new-password');
    input('verify-password', 'too short'); await submit();
    expect(auth.verifyEmail).not.toHaveBeenCalled();
    input('verify-password', 'a private new passphrase'); await submit();
    expect(auth.verifyEmail).toHaveBeenCalledExactlyOnceWith('emailed-token', 'a private new passphrase');
    expect(TestBed.inject(Router).url).toBe('/register/verified');
    expect(element().querySelector('input[type=password]')).toBeNull();
  });
  it.each([
    ['choose-username', '/onboarding/username', 'abcde', false],
    ['choose-username', '/onboarding/username', 'a12345', true],
    ['choose-username', '/onboarding/username', 'a'.repeat(32), true],
    ['choose-username', '/onboarding/username', 'a'.repeat(33), false],
    ['choose-username', '/onboarding/username', '1alex_name', false],
    ['choose-username', '/onboarding/username', 'Alex Rivera', false],
    ['choose-display-name', '/onboarding/display-name', 'Alex Rivera', true],
    ['choose-display-name', '/onboarding/display-name', ' 名字 ', true],
    ['choose-display-name', '/onboarding/display-name', '🌱'.repeat(80), true],
    ['choose-display-name', '/onboarding/display-name', '🌱'.repeat(81), false],
    ['choose-display-name', '/onboarding/display-name', '   ', false],
    ['choose-display-name', '/onboarding/display-name', 'Alex\u200B', false],
    ['choose-display-name', '/onboarding/display-name', 'Alex\nName', false],
    ['choose-display-name', '/onboarding/display-name', '\nAlex', false],
    ['choose-display-name', '/onboarding/display-name', 'Alex\t', false],
    ['verify-password', '/register/verify?token=token', 'x'.repeat(14), false],
    ['verify-password', '/register/verify?token=token', '🌱'.repeat(15), true],
    ['verify-password', '/register/verify?token=token', 'x'.repeat(128), true],
    ['verify-password', '/register/verify?token=token', 'x'.repeat(129), false],
    ['verify-password', '/register/verify?token=token', 'a long\ninvalid password', false],
  ])('validates %s boundary %j', async (id, route, value, valid) => {
    await harness.navigateByUrl(route);
    // Single-line browser inputs strip line breaks. Set the model directly to also
    // exercise validation of programmatically supplied, unsupported characters.
    const control = harness.routeDebugElement!.query(By.css(`#${id}`)).injector.get(NgControl).control!;
    control.setValue(value); control.markAsTouched(); harness.detectChanges();
    expect(element().querySelector(`#${id}`)?.getAttribute('aria-invalid')).toBe(String(!valid));
  });
  it('shows a username conflict without advancing and permits correction', async () => {
    await harness.navigateByUrl('/onboarding/username');
    auth.chooseUsername.mockRejectedValueOnce(new Error('That username is unavailable.'));
    input('choose-username', 'alex_rivera'); await submit();
    expect(TestBed.inject(Router).url).toBe('/onboarding/username');
    expect(element().querySelector('[role=alert]')?.textContent).toContain('unavailable');
    input('choose-username', 'alex_other'); await submit();
    expect(TestBed.inject(Router).url).toBe('/onboarding/display-name');
  });
  it('trims only the display name and renders user-controlled markup as text', async () => {
    await harness.navigateByUrl('/onboarding/display-name');
    input('choose-display-name', '  Alex Rivera  '); await submit();
    expect(auth.chooseDisplayName).toHaveBeenCalledExactlyOnceWith('Alex Rivera');
    auth.session.set({ ...USER, displayName: '<img src=x onerror=alert(1)>' }); harness.detectChanges();
    expect(element().textContent).toContain('<img src=x onerror=alert(1)>');
    expect(element().querySelector('img[onerror]')).toBeNull();
  });
  it('blocks duplicate submission while a request is pending', async () => {
    await harness.navigateByUrl('/register');
    let release!: () => void;
    auth.register.mockReturnValue(new Promise<void>(resolve => { release = resolve; }));
    input('register-email', 'alex@example.com');
    const form = element().querySelector('form')!;
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true })); harness.detectChanges();
    form.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    expect(auth.register).toHaveBeenCalledTimes(1);
    expect(element().querySelector<HTMLButtonElement>('button[type=submit]')!.disabled).toBe(true);
    expect(element().querySelector('main')?.getAttribute('aria-busy')).toBe('true');
    release(); await harness.fixture.whenStable();
  });
  it('announces resend feedback and retryable offline failures', async () => {
    await harness.navigateByUrl('/register/verify');
    const resend = () => [...element().querySelectorAll<HTMLButtonElement>('button')].find(button => button.textContent?.includes('Send a new link'))!;
    auth.resendVerification.mockRejectedValueOnce(new Error('We could not reach Projile.'));
    resend().click(); await harness.fixture.whenStable(); harness.detectChanges();
    expect(element().querySelector('[role=alert]')?.textContent).toContain('could not reach');
    resend().click(); await harness.fixture.whenStable(); harness.detectChanges();
    expect(element().querySelector('[role=status]')?.textContent).toContain('eligible');
  });
  it('shows Google only when configured and uses its authorization action', async () => {
    await harness.navigateByUrl('/login');
    expect(element().querySelector('.google-button')).toBeNull();
    auth.googleEnabled.set(true); harness.detectChanges();
    element().querySelector<HTMLButtonElement>('.google-button')!.click();
    expect(auth.startGoogleLogin).toHaveBeenCalledOnce();
  });
  it('displays a generic OAuth error and supports password managers, paste, and visibility', async () => {
    await harness.navigateByUrl('/login?error=oauth');
    expect(element().querySelector('[role=alert]')?.textContent).toContain('couldn’t complete sign-in');
    const field = input('login-password', '  a pasted private passphrase  ');
    expect(field.autocomplete).toBe('current-password');
    const toggle = element().querySelector<HTMLButtonElement>('[aria-label="Show password"]')!;
    toggle.click(); harness.detectChanges();
    expect(field.type).toBe('text');
    expect(toggle.getAttribute('aria-pressed')).toBe('true');
    input('login-username', 'alex_rivera'); await submit();
    expect(auth.login).toHaveBeenCalledExactlyOnceWith('alex_rivera', '  a pasted private passphrase  ');
  });
  it.each(steps)('labels every input on /%s', async (path) => {
    await harness.navigateByUrl('/' + path + (path === 'register/verify' ? '?token=token' : ''));
    for (const field of element().querySelectorAll<HTMLInputElement>('input')) {
      expect(field.id).toBeTruthy();
      expect(element().querySelector(`label[for="${field.id}"]`)).not.toBeNull();
    }
  });
});
