import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { SchoolNews } from '../../features/schools/school.model';
import { SchoolService } from '../../features/schools/school.service';
import { ImageLightbox } from '../../shared/components/image-lightbox/image-lightbox';

@Component({
  selector: 'app-article-details',
  imports: [RouterLink, DatePipe, ImageLightbox],
  templateUrl: './article-details.html',
  styleUrl: './article-details.css',
})
export class ArticleDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly schools = inject(SchoolService);
  readonly article = signal<SchoolNews | null>(null);
  readonly notFound = signal(false);
  readonly enlargedImage = signal<string | null>(null);

  openImage(event: MouseEvent): void {
    const target = event.target;
    if (target instanceof HTMLImageElement) this.enlargedImage.set(target.src);
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.schools.newsById$(id).subscribe({
      next: (article) => this.article.set(article),
      error: () => this.notFound.set(true),
    });
  }
}
