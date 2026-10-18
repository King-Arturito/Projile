import { ChangeDetectionStrategy, Component, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Icon } from '../../../../shared/icons/icon';
import { LABELS } from '../demo-board/board.store';

@Component({
  selector: 'app-board-toolbar',
  imports: [FormsModule, Icon],
  templateUrl: './board-toolbar.html',
  styleUrl: './board-toolbar.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoardToolbar {
  readonly view = model<'board' | 'list'>('board');
  readonly query = model('');
  readonly filter = model('All tasks');
  readonly addRequested = output<void>();
  readonly labels = LABELS;
}
