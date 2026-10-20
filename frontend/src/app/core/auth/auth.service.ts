import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

export interface AuthSession {
  userId: string;
  email: string;
  username: string | null;
  displayName: string | null;
  emailVerified: boolean;
  profileComplete: boolean;
}

interface SessionResponse {
  authenticated: boolean;
  user: AuthSession | null;
  googleEnabled: boolean;
}

/** Authentication lives in an HttpOnly server session, never browser storage. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  readonly session = signal<AuthSession | null>(null);
  readonly pendingEmail = signal<string | null>(null);
  readonly googleEnabled = signal(false);

  async refreshSession(): Promise<AuthSession | null> {
    try {
      const response = await firstValueFrom(this.http.get<SessionResponse>('/api/auth/session'));
      this.session.set(response.authenticated ? response.user : null);
      this.googleEnabled.set(response.googleEnabled);
      return this.session();
    } catch (error) {
      this.session.set(null);
      throw this.failure(error);
    }
  }

  async login(username: string, password: string): Promise<void> {
    await this.refreshSession();
    await this.perform(() => firstValueFrom(this.http.post('/api/auth/login',
      new HttpParams().set('username', username.trim()).set('password', password),
      { responseType: 'text' },
    )));
    await this.refreshSession();
  }

  async register(email: string): Promise<void> {
    await this.refreshSession();
    const normalizedEmail = email.trim().toLowerCase();
    await this.perform(() => firstValueFrom(this.http.post('/api/auth/register', {
      email: normalizedEmail,
    })));
    this.pendingEmail.set(normalizedEmail);
  }

  async verifyEmail(token: string, password: string): Promise<void> {
    await this.refreshSession();
    await this.perform(() => firstValueFrom(this.http.post('/api/auth/verify', { token, password })));
    this.pendingEmail.set(null);
    await this.refreshSession();
  }

  async resendVerification(): Promise<void> {
    const email = this.pendingEmail();
    if (!email) throw new Error('Enter your email on the registration page to request a new link.');
    await this.refreshSession();
    await this.perform(() => firstValueFrom(this.http.post('/api/auth/verification', { email })));
  }

  async chooseUsername(username: string): Promise<void> {
    await this.perform(() => firstValueFrom(this.http.patch('/api/auth/profile', { username })));
    await this.refreshSession();
  }

  async chooseDisplayName(displayName: string): Promise<void> {
    await this.perform(() => firstValueFrom(this.http.patch('/api/auth/profile', { displayName })));
    await this.refreshSession();
  }

  async logout(): Promise<void> {
    await this.refreshSession();
    await this.perform(() => firstValueFrom(this.http.post('/api/auth/logout', {}, { responseType: 'text' })));
    this.session.set(null);
    this.pendingEmail.set(null);
    // The login route guard bootstraps a fresh CSRF cookie. A failed bootstrap must
    // not leave the user on a protected screen after the server confirmed logout.
  }

  startGoogleLogin(): void {
    window.location.assign('/oauth2/authorization/google');
  }

  nextRoute(user = this.session()): string {
    if (!user) return '/login';
    if (!user.emailVerified) return '/register/verify';
    if (!user.username) return '/onboarding/username';
    if (!user.displayName || !user.profileComplete) return '/onboarding/display-name';
    return '/welcome';
  }

  private async perform(operation: () => Promise<unknown>): Promise<void> {
    try {
      await operation();
    } catch (error) {
      if (error instanceof HttpErrorResponse && error.status === 401) this.session.set(null);
      throw this.failure(error);
    }
  }

  private failure(error: unknown): Error {
    if (!(error instanceof HttpErrorResponse)) return new Error('Something went wrong. Please try again.');
    switch (error.status) {
      case 0: return new Error('We could not reach Projile. Check your connection and try again.');
      case 401: return new Error('Sign-in failed. Check your credentials and verify your email, then try again.');
      case 403: return new Error('Your session needs refreshing. Reload this page and try again.');
      case 409: return new Error('That username is unavailable. Please choose another.');
      case 429: return new Error('Too many attempts. Wait a few minutes before trying again.');
      case 503: return new Error('Email delivery is temporarily unavailable. Please try again later.');
      case 400: return new Error('Check your details. Verification links may have expired; request a new one if needed.');
      default: return new Error('Something went wrong. Please try again.');
    }
  }
}
