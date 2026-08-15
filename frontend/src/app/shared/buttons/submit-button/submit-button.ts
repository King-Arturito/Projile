import { Component, ChangeDetectionStrategy } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
@Component({
  selector: 'app-submit-button',
  imports: [MatButtonModule],
  templateUrl: './submit-button.html',
  styleUrl: './submit-button.scss',
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
})
export class SubmitButton {}
