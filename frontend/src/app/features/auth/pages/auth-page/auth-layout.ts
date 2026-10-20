import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../../../shared/icons/icon';
import { AuthStory } from './auth-story';

@Component({
  selector: 'app-auth-layout',
  standalone: true,
  imports: [RouterLink, Icon, AuthStory],
  templateUrl: './auth-layout.html',
  styleUrl: './auth-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthLayout {
  readonly step = input.required<string>();
}
