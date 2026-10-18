import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Icon } from '../../../../shared/icons/icon';
import { FeatureArt } from '../feature-art/feature-art';
@Component({
  selector: 'app-feature-section',
  imports: [Icon, FeatureArt],
  templateUrl: './feature-section.html',
  styleUrl: './feature-section.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class FeatureSection {}
