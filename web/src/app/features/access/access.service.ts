import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

export interface AccessInvitation {
  id: number;
  type: 'SCHOOL' | 'DOG';
  resourceId: number;
  resourceName: string;
  inviteeId: number;
  inviteeName: string;
  status: 'PENDING' | 'ACCEPTED';
}

@Injectable({ providedIn: 'root' })
export class AccessService {
  private readonly http = inject(HttpClient);
  pending() {
    return this.http.get<AccessInvitation[]>('/api/access/invitations');
  }
  members(type: 'DOG' | 'SCHOOL', id: number) {
    return this.http.get<AccessInvitation[]>(
      `/api/access/${type === 'DOG' ? 'dogs' : 'schools'}/${id}`,
    );
  }
  invite(type: 'DOG' | 'SCHOOL', id: number, email: string) {
    return this.http.post<void>(`/api/access/${type === 'DOG' ? 'dogs' : 'schools'}/${id}`, {
      email,
    });
  }
  accept(invitation: AccessInvitation) {
    return this.http.post<void>(
      `/api/access/${invitation.type === 'DOG' ? 'dog' : 'school'}-invitations/${invitation.id}/accept`,
      {},
    );
  }
  revoke(invitation: AccessInvitation) {
    return this.http.delete<void>(
      `/api/access/${invitation.type === 'DOG' ? 'dog' : 'school'}-invitations/${invitation.id}`,
    );
  }
}
