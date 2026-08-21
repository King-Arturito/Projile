import {inject, Service} from '@angular/core';
import {environment} from '../../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {UserDto} from '../models/user.dto';
@Service()
export class UserService {
  private readonly apiUrl = `${environment.apiUrl}/users`;
  private readonly http = inject(HttpClient);

  findByUsername(username: string): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}`, {
      params: {username}
    });
  }


}
