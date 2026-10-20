import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDto} from '../models/user.dto';
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly apiUrl = '/api/users';
  private readonly http = inject(HttpClient);

  findByUsername(username: string): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}`, {
      params: {username}
    });
  }


}
