import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {SubmitButton} from './shared/buttons/submit-button/submit-button';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SubmitButton],
  template: '<app-submit-button></app-submit-button>',
  standalone: true,
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('frontend');
}
