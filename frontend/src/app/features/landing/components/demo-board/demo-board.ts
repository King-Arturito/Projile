import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  output,
  signal,
  viewChild,
  viewChildren,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CdkDragDrop, DragDropModule } from '@angular/cdk/drag-drop';
import { Icon } from '../../../../shared/icons/icon';
import { BoardStore, ColumnId, DemoTask, LABELS, MEMBERS } from './board.store';
import { BoardSidebar } from '../board-sidebar/board-sidebar';
import { BoardColumn } from '../board-column/board-column';
import { BoardToolbar } from '../board-toolbar/board-toolbar';

@Component({
  selector: 'app-demo-board',
  imports: [FormsModule, DragDropModule, Icon, BoardColumn, BoardToolbar, BoardSidebar],
  templateUrl: './demo-board.html',
  styleUrl: './demo-board.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class DemoBoard {
  readonly newBoard = output<void>();
  readonly store = inject(BoardStore);
  readonly view = signal<'board' | 'list'>('board');
  readonly filter = signal('All tasks');
  readonly query = signal('');
  readonly message = signal('');
  readonly labels = LABELS;
  readonly members = MEMBERS;
  readonly columnIds: ColumnId[] = ['todo', 'progress', 'review', 'done'];
  readonly visibleColumns = computed(() =>
    this.store.columns().map((column) => ({
      ...column,
      tasks: column.tasks.filter(
        (task) =>
          (this.filter() === 'All tasks' || task.label === this.filter()) &&
          task.title.toLowerCase().includes(this.query().toLowerCase()),
      ),
    })),
  );
  readonly visibleCount = computed(() =>
    this.visibleColumns().reduce((sum, column) => sum + column.tasks.length, 0),
  );
  readonly taskDialog = viewChild.required<ElementRef<HTMLDialogElement>>('taskDialog');
  private readonly taskTitleInput = viewChild<ElementRef<HTMLInputElement>>('taskTitleInput');
  private readonly columnComponents = viewChildren(BoardColumn);
  editingTask: DemoTask | null = null;
  editingColumn: ColumnId = 'todo';

  startAdding(columnId: ColumnId): void {
    this.columnComponents()
      .find((component) => component.column().id === columnId)
      ?.startAdding();
  }

  drop(event: CdkDragDrop<DemoTask[]>, columnId: ColumnId): void {
    const task = event.item.data as DemoTask;
    const visibleTarget =
      this.visibleColumns()
        .find((column) => column.id === columnId)
        ?.tasks.filter((item) => item.id !== task.id) ?? [];
    const nextTask = visibleTarget[event.currentIndex];
    const fullTarget =
      this.store
        .columns()
        .find((column) => column.id === columnId)
        ?.tasks.filter((item) => item.id !== task.id) ?? [];
    const index = nextTask
      ? fullTarget.findIndex((item) => item.id === nextTask.id)
      : fullTarget.length;
    this.store.moveTask(task.id, columnId, index);
    this.message.set(
      `Moved “${task.title}” to ${this.store.columns().find((column) => column.id === columnId)?.title}.`,
    );
  }

  editTask(task: DemoTask, columnId: ColumnId): void {
    this.editingTask = { ...task };
    this.editingColumn = columnId;
    this.taskDialog().nativeElement.showModal();
    setTimeout(() => this.taskTitleInput()?.nativeElement.focus());
  }

  saveTask(): void {
    if (!this.editingTask?.title.trim()) return;
    this.store.updateTask(this.editingTask, this.editingColumn);
    this.taskDialog().nativeElement.close();
    this.message.set('Changes saved. Keep the good work going.');
  }

  closeOnBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.taskDialog().nativeElement.close();
  }

  resetFilters(): void {
    this.query.set('');
    this.filter.set('All tasks');
  }
}
