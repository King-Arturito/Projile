import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
@Component({
  selector: 'app-submit-button',
  imports: [MatButtonModule],
  templateUrl: './submit-button.html',
  styleUrl: './submit-button.scss',
  standalone: true,
})
export class SubmitButton {
}
