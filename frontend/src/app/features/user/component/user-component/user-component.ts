import { Component, inject, signal } from '@angular/core';
import { UserService } from '../../services/user.service';
import { UserDto } from '../../models/user.dto';

@Component({
  selector: 'app-user-component',
  imports: [],
  templateUrl: './user-component.html',
  styleUrl: './user-component.scss',
  standalone: true
})
export class UserComponent {
  private readonly userService = inject(UserService);

  user = signal<UserDto | null>(null);

  searchUser(username: string): void {
    this.userService.findByUsername(username).subscribe({
      next: user => {
        console.log("found");
        this.user.set(user);
      },
      error: error => {
        console.error(error);
        this.user.set(null);
      }
    });
  }

}
