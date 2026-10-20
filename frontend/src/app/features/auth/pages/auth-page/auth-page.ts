import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { NgTemplateOutlet } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/auth/auth.service';
import { Icon } from '../../../../shared/icons/icon';
import { AuthLayout } from './auth-layout';

type AuthStep =
  'login' | 'register' | 'verify' | 'verified' | 'username' | 'display-name' | 'welcome';

const codePointLength =
  (minimum: number, maximum: number, trim = false): ValidatorFn =>
  (control: AbstractControl): ValidationErrors | null => {
    const value = String(control.value ?? '');
    const length = Array.from(trim ? value.trim() : value).length;
    return length >= minimum && length <= maximum ? null : { length: true };
  };

const displayNameValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  /[\p{Cc}\p{Cf}\p{Cs}\p{Zl}\p{Zp}]/u.test(String(control.value ?? ''))
    ? { characters: true }
    : null;

const passwordValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null =>
  /[\p{Cc}\p{Cs}\p{Zl}\p{Zp}]/u.test(String(control.value ?? '')) ? { characters: true } : null;

@Component({
  selector: 'app-auth-page',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, NgTemplateOutlet, Icon, AuthLayout],
  templateUrl: './auth-page.html',
  styleUrl: './auth-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthPage {
  protected readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly step = signal<AuthStep>('login');
  protected readonly busy = signal(false);
  protected readonly error = signal('');
  protected readonly notice = signal('');
  protected readonly passwordVisible = signal(false);
  protected readonly hasVerificationToken = signal(false);
  private verificationToken = '';

  protected readonly loginForm = new FormGroup({
    username: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
    password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  });
  protected readonly registerForm = new FormGroup({
    email: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.email, Validators.maxLength(254)],
    }),
  });
  protected readonly verificationForm = new FormGroup({
    password: new FormControl('', {
      nonNullable: true,
      validators: [codePointLength(15, 128), passwordValidator],
    }),
  });
  protected readonly usernameForm = new FormGroup({
    username: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/^[A-Za-z][A-Za-z0-9_]{5,31}$/)],
    }),
  });
  protected readonly displayNameForm = new FormGroup({
    displayName: new FormControl('', {
      nonNullable: true,
      validators: [codePointLength(1, 80, true), displayNameValidator],
    }),
  });

  protected readonly profileStep = computed(() => {
    const steps: Partial<Record<AuthStep, number>> = {
      register: 1,
      verify: 2,
      verified: 2,
      username: 3,
      'display-name': 4,
    };
    return steps[this.step()] ?? 0;
  });

  constructor() {
    this.route.data.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((data) => {
      this.step.set((data['step'] as AuthStep | undefined) ?? 'login');
      this.error.set('');
      this.notice.set('');
      this.passwordVisible.set(false);
      this.loginForm.controls.password.reset();
      this.verificationForm.reset();
    });
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      this.verificationToken = params.get('token') ?? '';
      this.hasVerificationToken.set(Boolean(this.verificationToken));
      if (params.get('error')) {
        this.error.set('We couldn’t complete sign-in. Please try again.');
      }
    });
  }

  protected invalid(control: AbstractControl): boolean {
    return control.invalid && (control.touched || control.dirty);
  }

  protected async login(): Promise<void> {
    this.loginForm.markAllAsTouched();
    if (this.loginForm.invalid || this.busy()) return;
    const { username, password } = this.loginForm.getRawValue();
    await this.run(async () => {
      await this.auth.login(username.trim(), password);
      this.loginForm.controls.password.reset();
      await this.navigateAfterSignIn();
    });
  }

  protected async register(): Promise<void> {
    this.registerForm.markAllAsTouched();
    if (this.registerForm.invalid || this.busy()) return;
    const { email } = this.registerForm.getRawValue();
    await this.run(async () => {
      await this.auth.register(email.trim());
      await this.router.navigateByUrl('/register/verify');
    });
  }

  protected async verify(): Promise<void> {
    this.verificationForm.markAllAsTouched();
    if (!this.verificationToken || this.verificationForm.invalid || this.busy()) return;
    await this.run(async () => {
      await this.auth.verifyEmail(
        this.verificationToken,
        this.verificationForm.controls.password.value,
      );
      this.verificationToken = '';
      this.verificationForm.reset();
      await this.router.navigateByUrl('/register/verified', { replaceUrl: true });
    });
  }

  protected async resend(): Promise<void> {
    await this.run(async () => {
      await this.auth.resendVerification();
      this.notice.set(
        'If your email is eligible, a new verification link is on its way. Please check your inbox and spam folder.',
      );
    });
  }

  protected async saveUsername(): Promise<void> {
    this.usernameForm.markAllAsTouched();
    if (this.usernameForm.invalid || this.busy()) return;
    await this.run(async () => {
      await this.auth.chooseUsername(this.usernameForm.controls.username.value);
      await this.router.navigateByUrl('/onboarding/display-name');
    });
  }

  protected async saveDisplayName(): Promise<void> {
    this.displayNameForm.markAllAsTouched();
    if (this.displayNameForm.invalid || this.busy()) return;
    await this.run(async () => {
      await this.auth.chooseDisplayName(this.displayNameForm.controls.displayName.value.trim());
      await this.router.navigateByUrl('/welcome');
    });
  }

  protected googleLogin(): void {
    if (this.busy()) return;
    this.busy.set(true);
    this.auth.startGoogleLogin();
  }

  protected async signOut(): Promise<void> {
    await this.run(async () => {
      await this.auth.logout();
      await this.router.navigateByUrl('/login');
    });
  }

  private async navigateAfterSignIn(): Promise<void> {
    await this.router.navigateByUrl(this.auth.nextRoute());
  }

  private async run(action: () => Promise<void>): Promise<void> {
    if (this.busy()) return;
    this.busy.set(true);
    this.error.set('');
    this.notice.set('');
    try {
      await action();
    } catch (error: unknown) {
      this.error.set(
        error instanceof Error ? error.message : 'Something went wrong. Please try again.',
      );
    } finally {
      this.busy.set(false);
    }
  }
}
