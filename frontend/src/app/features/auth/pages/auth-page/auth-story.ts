import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../../../shared/icons/icon';

@Component({
  selector: 'app-auth-story',
  standalone: true,
  imports: [RouterLink, Icon],
  templateUrl: './auth-story.html',
  styleUrl: './auth-story.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthStory {}
