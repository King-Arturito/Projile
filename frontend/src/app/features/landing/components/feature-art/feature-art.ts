import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-feature-art',
  templateUrl: './feature-art.html',
  styleUrl: './feature-art.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class FeatureArt {
  readonly kind = input.required<string>();
}
