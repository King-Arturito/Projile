import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Icon } from '../../../../shared/icons/icon';
import { DemoTask } from './board.store';

@Component({
  selector: 'app-demo-task',
  imports: [DatePipe, Icon],
  template: `
    @if (task().cover) {
      <div class="cover" aria-hidden="true">
        <div class="mini-site">
          <div class="mini-nav"><b>hello.</b><span>About &nbsp; Work &nbsp; Contact</span></div>
          <div class="mini-content">
            <span>A fresh perspective.</span><strong>Good things<br/>are growing.</strong
          ><i>Let's make something great ↗</i>
          </div>
          <div class="mini-art">
            <div></div>
            <i></i><b></b>
          </div>
        </div>
        <span class="cover-spark">✳</span>
      </div>
    }
    <span class="label" [class]="'label ' + task().label.toLowerCase()">{{ task().label }}</span>
    <h4 [class.completed]="complete()">{{ task().title }}</h4>
    <div class="card-meta">
      <span class="meta-left">
        @if (task().due) {
          <span class="due"
          ><app-icon name="calendar"/>
            {{
              task().due + 'T12:00:00Z' | date: 'MMM d' : 'UTC'
            }}</span
          >
        }
        <span class="comments"><app-icon name="chat"/>
          {{ task().comments }}</span></span
      >
      <span [class]="'avatar ' + task().assignee.toLowerCase()">{{ task().assignee }}</span>
    </div>
  `,
  styleUrl: './task-card.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class TaskCard {
  readonly task = input.required<DemoTask>();
  readonly complete = input(false);
}
