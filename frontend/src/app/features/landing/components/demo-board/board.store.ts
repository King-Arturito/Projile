import { Injectable, computed, signal } from '@angular/core';

export type ColumnId = 'todo' | 'progress' | 'review' | 'done';
export type Label = 'Planning' | 'Design' | 'Development' | 'Content';
export interface DemoTask {
  id: string;
  title: string;
  description: string;
  label: Label;
  assignee: string;
  due: string;
  comments: number;
  cover?: boolean;
}
export interface DemoColumn {
  id: ColumnId;
  title: string;
  tasks: DemoTask[];
}
interface BoardSnapshot {
  version: 1;
  title: string;
  columns: DemoColumn[];
}

export const BOARD_STORAGE_KEY = 'projile.demo-board.v1';
export const LABELS: Label[] = ['Planning', 'Design', 'Development', 'Content'];
export const MEMBERS = [
  { initials: 'AM', name: 'Alex Morgan', color: 'peach' },
  { initials: 'JL', name: 'Jamie Lee', color: 'lilac' },
  { initials: 'SK', name: 'Sam Kim', color: 'sage' },
];

function initialColumns(): DemoColumn[] {
  return [
    {
      id: 'todo',
      title: 'To do',
      tasks: [
        {
          id: 'research',
          title: 'A little research goes a long way',
          description:
            'Explore our audience, collect inspiration, and share the three things we should keep in mind for the new website.',
          label: 'Planning',
          assignee: 'AM',
          due: '2026-10-20',
          comments: 2,
        },
        {
          id: 'copy',
          title: 'Find the words that feel like us',
          description:
            'Draft the homepage headline and a short introduction. Keep it simple, warm, and unmistakably us.',
          label: 'Content',
          assignee: 'JL',
          due: '2026-10-22',
          comments: 3,
        },
      ],
    },
    {
      id: 'progress',
      title: 'In progress',
      tasks: [
        {
          id: 'homepage',
          title: 'Give our homepage a fresh start',
          description:
            'Turn the approved direction into a homepage design. Start with the hero, then work through the rest of the page.',
          label: 'Design',
          assignee: 'JL',
          due: '2026-10-21',
          comments: 4,
          cover: true,
        },
        {
          id: 'components',
          title: 'Build the little building blocks',
          description:
            'Create our reusable buttons, inputs, and navigation components, including their keyboard and focus states.',
          label: 'Development',
          assignee: 'SK',
          due: '',
          comments: 1,
        },
      ],
    },
    {
      id: 'review',
      title: 'In review',
      tasks: [
        {
          id: 'brand',
          title: 'A new look, the same us',
          description:
            'Review the updated colors and typography together. Leave feedback before we share the brand kit with the team.',
          label: 'Design',
          assignee: 'AM',
          due: '2026-10-19',
          comments: 6,
        },
      ],
    },
    {
      id: 'done',
      title: 'Done',
      tasks: [
        {
          id: 'kickoff',
          title: 'Get the whole team on the same page',
          description:
            'Kickoff complete! We agreed on our goals, responsibilities, and the first milestone.',
          label: 'Planning',
          assignee: 'SK',
          due: '',
          comments: 3,
        },
        {
          id: 'moodboard',
          title: 'Collect a little inspiration',
          description:
            'Our moodboard is ready, with references for layout, color, and photography.',
          label: 'Design',
          assignee: 'JL',
          due: '',
          comments: 2,
        },
      ],
    },
  ];
}

function isSnapshot(value: unknown): value is BoardSnapshot {
  if (!value || typeof value !== 'object') return false;
  const board = value as Partial<BoardSnapshot>;
  if (
    board.version !== 1 ||
    typeof board.title !== 'string' ||
    !board.title.trim() ||
    !Array.isArray(board.columns) ||
    board.columns.length !== 4
  )
    return false;
  const ids = new Set<string>();
  return board.columns.every((column, index) => {
    if (
      column?.id !== ['todo', 'progress', 'review', 'done'][index] ||
      typeof column.title !== 'string' ||
      !Array.isArray(column.tasks)
    )
      return false;
    return column.tasks.every((task: DemoTask) => {
      if (
        !task ||
        typeof task.id !== 'string' ||
        ids.has(task.id) ||
        typeof task.title !== 'string' ||
        !task.title.trim() ||
        typeof task.description !== 'string' ||
        !LABELS.includes(task.label) ||
        !MEMBERS.some((member) => member.initials === task.assignee) ||
        typeof task.due !== 'string' ||
        typeof task.comments !== 'number' ||
        !Number.isFinite(task.comments) ||
        (task.cover !== undefined && typeof task.cover !== 'boolean')
      )
        return false;
      ids.add(task.id);
      return true;
    });
  });
}

@Injectable({ providedIn: 'root' })
export class BoardStore {
  private readonly snapshot = this.restore();
  readonly title = signal(this.snapshot?.title ?? 'Website launch');
  readonly columns = signal<DemoColumn[]>(this.snapshot?.columns ?? initialColumns());
  readonly storageAvailable = signal(true);
  readonly taskCount = computed(() =>
    this.columns().reduce((sum, column) => sum + column.tasks.length, 0),
  );
  readonly completedCount = computed(
    () => this.columns().find((column) => column.id === 'done')?.tasks.length ?? 0,
  );

  createBoard(title: string): void {
    if (!title.trim()) return;
    this.title.set(title.trim().slice(0, 80));
    this.columns.set(initialColumns().map((column) => ({ ...column, tasks: [] })));
    this.save();
  }

  addTask(columnId: ColumnId, title: string): void {
    if (!title.trim()) return;
    const task: DemoTask = {
      id: crypto.randomUUID(),
      title: title.trim().slice(0, 160),
      description: '',
      label: 'Planning',
      assignee: 'AM',
      due: '',
      comments: 0,
    };
    this.columns.update((columns) =>
      columns.map((column) =>
        column.id === columnId ? { ...column, tasks: [...column.tasks, task] } : column,
      ),
    );
    this.save();
  }

  moveTask(taskId: string, targetId: ColumnId, targetIndex?: number): void {
    const task = this.columns()
      .flatMap((column) => column.tasks)
      .find((item) => item.id === taskId);
    if (!task || !this.columns().some((column) => column.id === targetId)) return;
    this.columns.update((columns) =>
      columns.map((column) => {
        const tasks = column.tasks.filter((item) => item.id !== taskId);
        if (column.id === targetId) tasks.splice(targetIndex ?? tasks.length, 0, task);
        return { ...column, tasks };
      }),
    );
    this.save();
  }

  updateTask(task: DemoTask, columnId: ColumnId): void {
    if (!task.title.trim()) return;
    const previousColumn = this.columns().find((column) =>
      column.tasks.some((item) => item.id === task.id),
    );
    if (!previousColumn) return;
    this.columns.update((columns) =>
      columns.map((column) => ({
        ...column,
        tasks: column.tasks.map((item) =>
          item.id === task.id ? { ...task, title: task.title.trim().slice(0, 160) } : item,
        ),
      })),
    );
    if (previousColumn.id !== columnId) this.moveTask(task.id, columnId);
    else this.save();
  }

  resetDemo(): void {
    this.title.set('Website launch');
    this.columns.set(initialColumns());
    this.save();
  }

  private restore(): BoardSnapshot | null {
    try {
      const value: unknown = JSON.parse(localStorage.getItem(BOARD_STORAGE_KEY) ?? 'null');
      return isSnapshot(value) ? value : null;
    } catch {
      return null;
    }
  }

  private save(): void {
    try {
      localStorage.setItem(
        BOARD_STORAGE_KEY,
        JSON.stringify({ version: 1, title: this.title(), columns: this.columns() }),
      );
    } catch {
      this.storageAvailable.set(false);
    }
  }
}
