import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { Icon } from '../../../../shared/icons/icon';
import { BoardStore, DemoColumn, DemoTask } from '../demo-board/board.store';
import { TaskCard } from '../demo-board/task-card';

@Component({
  selector: 'app-board-column',
  imports: [FormsModule, DragDropModule, Icon, TaskCard],
  templateUrl: './board-column.html',
  styleUrl: './board-column.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BoardColumn {
  readonly column = input.required<DemoColumn>();
  readonly listView = input(false);
  readonly taskSelected = output<DemoTask>();
  readonly taskDropped = output<CdkDragDrop<DemoTask[]>>();
  readonly taskAdded = output<void>();
  readonly adding = signal(false);
  readonly columnIds = ['todo', 'progress', 'review', 'done'];
  private readonly store = inject(BoardStore);
  private readonly draftInput = viewChild<ElementRef<HTMLTextAreaElement>>('draftInput');
  draft = '';

  startAdding(): void {
    this.draft = '';
    this.adding.set(true);
    setTimeout(() => this.draftInput()?.nativeElement.focus());
  }

  addTask(): void {
    if (!this.draft.trim()) return;
    this.store.addTask(this.column().id, this.draft);
    this.adding.set(false);
    this.taskAdded.emit();
  }
}
