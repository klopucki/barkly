import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { SchoolService } from '../../features/schools/school.service';
import { School } from '../../features/schools/school.model';
import { AuthService } from '../../features/auth/auth.service';

@Component({
  selector: 'app-schools',
  imports: [RouterLink],
  templateUrl: './schools.html',
  styleUrl: './schools.css',
})
export class Schools implements OnInit {
  private readonly schools = inject(SchoolService);
  protected readonly auth = inject(AuthService);
  readonly items = signal<School[]>([]);
  ngOnInit(): void { this.schools.all$().subscribe({ next: value => this.items.set(value) }); }
}
