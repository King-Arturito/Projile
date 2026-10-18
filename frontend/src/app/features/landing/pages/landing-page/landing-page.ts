import { ChangeDetectionStrategy, Component, ElementRef, inject, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Icon } from '../../../../shared/icons/icon';
import { SiteHeader } from '../../components/site-header/site-header';
import { FeatureSection } from '../../components/feature-section/feature-section';
import { PageClosing } from '../../components/page-closing/page-closing';
import { DemoBoard } from '../../components/demo-board/demo-board';
import { BoardStore } from '../../components/demo-board/board.store';

@Component({
  selector: 'app-landing-page',
  imports: [FormsModule, Icon, DemoBoard, SiteHeader, FeatureSection, PageClosing],
  templateUrl: './landing-page.html',
  styleUrl: './landing-page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class LandingPage {
  protected readonly createDialog =
    viewChild.required<ElementRef<HTMLDialogElement>>('createDialog');
  protected readonly boardNameInput =
    viewChild.required<ElementRef<HTMLInputElement>>('boardNameInput');
  protected readonly store = inject(BoardStore);
  protected boardName = '';
  protected created = false;

  protected startBoard(): void {
    this.boardName = '';
    this.created = false;
    this.createDialog().nativeElement.showModal();
    this.boardNameInput().nativeElement.focus();
  }

  protected createBoard(): void {
    if (!this.boardName.trim()) return;
    this.store.createBoard(this.boardName);
    this.created = true;
    this.createDialog().nativeElement.close();
    document.getElementById('demo')?.scrollIntoView({
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches
        ? 'instant'
        : 'smooth',
      block: 'start',
    });
  }

  protected closeOnBackdrop(event: MouseEvent): void {
    if (event.target === event.currentTarget) this.createDialog().nativeElement.close();
  }
}
