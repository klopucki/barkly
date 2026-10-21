import { Component, effect, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { School, SchoolPayload, schoolImageUrl } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { AuthService } from '../../features/auth/auth.service';
@Component({
  selector: 'app-my-school',
  imports: [FormsModule, RouterLink],
  templateUrl: './my-school.html',
  styleUrls: ['../schools/schools.css', './my-school.css'],
})
export class MySchool {
  private readonly schools = inject(SchoolService);
  protected readonly auth = inject(AuthService);
  protected readonly schoolImageUrl = schoolImageUrl;
  readonly mine = signal<School[]>([]);
  readonly loading = signal(false);
  editing: School | null = null;
  error = '';
  showFormDialog = false;
  form: SchoolPayload = {
    name: '',
    address: '',
    krs: null,
    description: '',
    activities: '',
    pricing: '',
  };
  constructor() {
    effect(() => {
      if (this.canManageSchools()) this.load();
      else this.mine.set([]);
    });
  }
  canManageSchools() {
    const role = this.auth.currentUser()?.role;
    return role === 'SCHOOL_ADMIN' || role === 'SUPER_ADMIN';
  }
  load() {
    this.loading.set(true);
    this.error = '';
    this.schools.mine$().subscribe({
      next: (s) => {
        this.mine.set(s);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.error = 'Nie udało się pobrać szkół. Odśwież stronę i spróbuj ponownie.';
      },
    });
  }
  openCreateDialog() {
    if (!this.canManageSchools()) return;
    this.editing = null;
    this.form = this.emptyForm();
    this.error = '';
    this.showFormDialog = true;
  }
  openEditDialog(school: School) {
    this.editing = school;
    this.form = {
      name: school.name,
      address: school.address,
      krs: school.krs,
      description: school.description,
      activities: school.activities,
      pricing: school.pricing,
    };
    this.error = '';
    this.showFormDialog = true;
  }
  closeFormDialog() {
    this.editing = null;
    this.showFormDialog = false;
  }
  save() {
    if (!this.canManageSchools()) {
      this.error = 'Tylko administrator szkoły może zarządzać szkołą.';
      return;
    }
    const body = {
      ...this.form,
      name: this.form.name.trim(),
      address: this.form.address.trim(),
      krs: this.form.krs?.trim() || null,
      description: this.form.description.trim(),
      activities: this.form.activities.trim(),
      pricing: this.form.pricing.trim(),
    };
    const editingId = this.editing?.id;
    const request = editingId ? this.schools.update$(editingId, body) : this.schools.create$(body);
    request.subscribe({
      next: (s) => {
        this.mine.update((items) =>
          editingId ? items.map((item) => (item.id === s.id ? s : item)) : [...items, s],
        );
        this.closeFormDialog();
      },
      error: (e) => (this.error = e.error?.message ?? 'Nie udało się zapisać szkoły.'),
    });
  }
  private emptyForm(): SchoolPayload {
    return { name: '', address: '', krs: null, description: '', activities: '', pricing: '' };
  }
}
