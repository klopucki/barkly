import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Dog, dogImageUrl } from '../../features/dogs/dog.model';
import { DogService } from '../../features/dogs/dog.service';
import { AuthService } from '../../features/auth/auth.service';

@Component({
  selector: 'app-dogs',
  imports: [RouterLink],
  templateUrl: './dogs.html',
})
export class Dogs implements OnInit {
  private readonly api = inject(DogService);
  protected readonly auth = inject(AuthService);
  readonly dogs = signal<Dog[]>([]);
  readonly imageUrl = dogImageUrl;
  ngOnInit(): void {
    this.api.all$().subscribe((dogs) => this.dogs.set(dogs));
  }
}
