import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { Icon } from '../../../../shared/icons/icon';
import { MEMBERS } from '../demo-board/board.store';
@Component({
  selector: 'app-board-sidebar',
  imports: [Icon],
  templateUrl: './board-sidebar.html',
  styleUrl: './board-sidebar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoardSidebar {
  readonly title = input.required<string>();
  readonly members = MEMBERS;
  readonly newBoard = output<void>();
}
