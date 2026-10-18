import { TestBed } from '@angular/core/testing';
import { BOARD_STORAGE_KEY, BoardStore } from './board.store';

describe('Demo board', () => {
  beforeEach(() => localStorage.removeItem(BOARD_STORAGE_KEY));
  afterEach(() => localStorage.removeItem(BOARD_STORAGE_KEY));

  it('moves a task between columns without losing or duplicating tasks', () => {
    const store = TestBed.inject(BoardStore);
    const count = store.taskCount();
    store.moveTask('research', 'done', 0);
    expect(store.columns()[0].tasks.some((task) => task.id === 'research')).toBe(false);
    expect(store.columns()[3].tasks[0].id).toBe('research');
    expect(store.completedCount()).toBe(3);
    expect(store.taskCount()).toBe(count);
  });

  it('keeps task order when editing details without changing the status', () => {
    const store = TestBed.inject(BoardStore);
    const task = store.columns()[0].tasks[0];
    store.updateTask({ ...task, title: 'A clearer next step' }, 'todo');
    expect(store.columns()[0].tasks[0].title).toBe('A clearer next step');
    expect(store.columns()[0].tasks[1].id).toBe('copy');
  });

  it('restores a newly created board and its tasks after a new session', () => {
    const store = TestBed.inject(BoardStore);
    store.createBoard(' Our next project ');
    store.addTask('progress', '  Share the first draft  ');
    const restored = new BoardStore();
    expect(restored.title()).toBe('Our next project');
    expect(restored.taskCount()).toBe(1);
    expect(restored.columns()[1].tasks[0].title).toBe('Share the first draft');
  });

  it('ignores blank tasks and safely falls back from corrupt saved data', () => {
    localStorage.setItem(BOARD_STORAGE_KEY, '{broken');
    const store = TestBed.inject(BoardStore);
    expect(store.title()).toBe('Website launch');
    store.addTask('todo', '   ');
    expect(store.taskCount()).toBe(7);
  });

  it('keeps the board usable when browser storage cannot be written', () => {
    const store = TestBed.inject(BoardStore);
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('Storage unavailable');
    });
    store.addTask('todo', 'Still make progress');
    expect(store.taskCount()).toBe(8);
    expect(store.storageAvailable()).toBe(false);
    vi.restoreAllMocks();
  });
});
