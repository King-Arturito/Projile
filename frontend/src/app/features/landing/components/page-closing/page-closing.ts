import { ChangeDetectionStrategy, Component, output } from '@angular/core';
import { Icon } from '../../../../shared/icons/icon';

@Component({
  selector: 'app-page-closing',
  imports: [Icon],
  templateUrl: './page-closing.html',
  styleUrl: './page-closing.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class PageClosing {
  readonly startRequested = output<void>();
}
