import { ChangeDetectionStrategy, Component, output, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icon } from '../../../../shared/icons/icon';

@Component({
  selector: 'app-site-header',
  imports: [Icon, RouterLink],
  templateUrl: './site-header.html',
  styleUrl: './site-header.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class SiteHeader {
  protected readonly menuOpen = signal(false);
  readonly startRequested = output<void>();
  protected startBoard(): void {
    this.menuOpen.set(false);
    this.startRequested.emit();
  }
}
