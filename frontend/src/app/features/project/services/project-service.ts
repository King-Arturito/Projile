import { Injectable, inject } from '@angular/core';
import { ProjectView } from '../models/project-view';
import { HttpClient } from '@angular/common/http';
@Injectable({
  providedIn: 'root',
})
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/projects';
}
