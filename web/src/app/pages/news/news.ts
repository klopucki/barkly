import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { SchoolService } from '../../features/schools/school.service';
import { SchoolNews } from '../../features/schools/school.model';

@Component({
  selector: 'app-news',
  imports: [DatePipe],
  templateUrl: './news.html',
  styleUrl: './news.css',
})
export class News implements OnInit { private readonly schools=inject(SchoolService); readonly items=signal<SchoolNews[]>([]); ngOnInit(){this.schools.allNews$().subscribe({next:n=>this.items.set(n)});} }
