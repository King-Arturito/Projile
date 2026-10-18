import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

const PATHS: Record<string, string[]> = {
  arrow: ['M5 12h14m-6-6 6 6-6 6'],
  diagonal: ['M7 17 17 7M7 7h10v10'],
  chevron: ['m8 10 4 4 4-4'],
  plus: ['M12 5v14M5 12h14'],
  close: ['m6 6 12 12M6 18 18 6'],
  check: ['m5 12 4 4L19 6'],
  board: ['M4 4h6v16H4zM14 4h6v10h-6z'],
  grid: ['M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z'],
  list: ['M9 6h11M9 12h11M9 18h11M4 6h.01M4 12h.01M4 18h.01'],
  search: ['M21 21l-5-5', 'M18 10.5a7.5 7.5 0 1 1-15 0 7.5 7.5 0 0 1 15 0'],
  filter: ['M4 7h16M7 12h10M10 17h4'],
  calendar: [
    'M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1ZM8 3v4m8-4v4M4 10h16',
  ],
  chat: ['M20 11a8 8 0 0 1-8 8H4l1.7-4A8 8 0 1 1 20 11Z'],
  link: [
    'm9 15 6-6M8 16l-1 1a3.5 3.5 0 0 1-5-5l4-4a3.5 3.5 0 0 1 5 0m2 8a3.5 3.5 0 0 0 5 0l4-4a3.5 3.5 0 0 0-5-5l-1 1',
  ],
  clock: ['M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0', 'M12 7v5l3 2'],
  users: [
    'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2m20 0v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
    'M13 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0',
  ],
  spark: ['m12 3 2.5 6.5L21 12l-6.5 2.5L12 21l-2.5-6.5L3 12l6.5-2.5L12 3Z'],
  play: ['m9 5 11 7-11 7V5Z'],
  flag: ['M5 21V4m0 0c5-4 9 4 14 0v10c-5 4-9-4-14 0'],
  help: [
    'M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0',
    'M9.5 9a2.5 2.5 0 1 1 4.2 1.8c-1.2.7-1.7 1.2-1.7 2.7M12 17h.01',
  ],
  menu: ['M4 6h16M4 12h16M4 18h16'],
  leaf: ['M20 4C8 2 2 8 5 15s15 5 15-11ZM4 21 16 9'],
};

@Component({
  selector: 'app-icon',
  template: `
    <svg
      viewBox="0 0 24 24"
      stroke="currentColor"
      stroke-width="1.7"
      stroke-linecap="round"
      stroke-linejoin="round"
      aria-hidden="true"
    >
      @for (path of paths(); track $index) {
        <path [attr.d]="path"/>
      }
    </svg>`,
  styles: [
    ':host{display:inline-flex;width:1.15em;height:1.15em;flex-shrink:0;vertical-align:middle}svg{width:100%;height:100%}',
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class Icon {
  readonly name = input.required<string>();
  protected readonly paths = computed(() => PATHS[this.name()] ?? PATHS['spark']);
}
