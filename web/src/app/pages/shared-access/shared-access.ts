import { Component, inject, OnInit, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DogService } from '../../features/dogs/dog.service';
import { SchoolService } from '../../features/schools/school.service';
import { Dog } from '../../features/dogs/dog.model';
import { School } from '../../features/schools/school.model';

interface Invitation {
  id: number;
  type: 'SCHOOL' | 'DOG';
  resourceId: number;
  resourceName: string;
  inviteeId: number;
  inviteeName: string;
  status: 'PENDING' | 'ACCEPTED';
}

@Component({
  selector: 'app-shared-access',
  imports: [FormsModule],
  templateUrl: './shared-access.html',
})
export class SharedAccess implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly dogService = inject(DogService);
  private readonly schoolService = inject(SchoolService);
  readonly dogs = signal<Dog[]>([]);
  readonly schools = signal<School[]>([]);
  readonly invitations = signal<Invitation[]>([]);
  readonly members = signal<Invitation[]>([]);
  readonly error = signal('');
  readonly message = signal('');
  type: 'DOG' | 'SCHOOL' = 'DOG';
  resourceId: number | null = null;
  email = '';

  ngOnInit(): void {
    this.refresh();
  }

  refresh(): void {
    forkJoin({
      dogs: this.dogService.mine$(),
      schools: this.schoolService.mine$(),
      invitations: this.http.get<Invitation[]>('/api/access/invitations'),
    }).subscribe({
      next: (result) => {
        this.dogs.set(result.dogs);
        this.schools.set(result.schools);
        this.invitations.set(result.invitations);
      },
      error: () => this.error.set('Nie udało się pobrać danych dostępu.'),
    });
  }

  loadMembers(): void {
    this.members.set([]);
    if (!this.resourceId) return;
    const resource = this.type === 'DOG' ? 'dogs' : 'schools';
    this.http.get<Invitation[]>(`/api/access/${resource}/${this.resourceId}`).subscribe({
      next: (value) => this.members.set(value),
      error: () => this.error.set('Nie możesz zarządzać dostępem do tego profilu.'),
    });
  }

  invite(): void {
    if (!this.resourceId || !this.email.trim()) return;
    const resource = this.type === 'DOG' ? 'dogs' : 'schools';
    this.error.set('');
    this.http
      .post(`/api/access/${resource}/${this.resourceId}`, { email: this.email.trim() })
      .subscribe({
        next: () => {
          this.email = '';
          this.message.set('Zaproszenie zostało wysłane w aplikacji.');
          this.loadMembers();
        },
        error: () =>
          this.error.set('Nie udało się wysłać zaproszenia. Sprawdź adres i uprawnienia.'),
      });
  }

  accept(invitation: Invitation): void {
    const kind = invitation.type === 'DOG' ? 'dog' : 'school';
    this.http.post(`/api/access/${kind}-invitations/${invitation.id}/accept`, {}).subscribe({
      next: () => {
        this.message.set('Zaproszenie zostało przyjęte.');
        this.refresh();
      },
      error: () => this.error.set('Nie udało się przyjąć zaproszenia.'),
    });
  }

  revoke(invitation: Invitation): void {
    const kind = invitation.type === 'DOG' ? 'dog' : 'school';
    this.http.delete(`/api/access/${kind}-invitations/${invitation.id}`).subscribe({
      next: () => {
        this.loadMembers();
        this.refresh();
      },
      error: () => this.error.set('Nie udało się cofnąć dostępu.'),
    });
  }
}
