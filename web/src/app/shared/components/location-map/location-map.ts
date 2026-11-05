import {
  AfterViewInit,
  Component,
  ElementRef,
  input,
  OnDestroy,
  output,
  signal,
  ViewChild,
} from '@angular/core';
import type { Map as MapLibreMap, Marker } from 'maplibre-gl';

@Component({
  selector: 'app-location-map',
  template: `
    @if (editable()) {
      <p class="mb-2 text-sm text-stone-600">Kliknij na mapie, aby zaznaczyć miejsce treningu.</p>
    }
    <div #map class="location-map" aria-label="Mapa miejsca treningu"></div>
    @if (error()) {
      <p role="alert" class="mt-2 text-sm text-red-700">
        Nie udało się załadować mapy. Sprawdź połączenie z internetem lub ustawienia przeglądarki.
        Możesz podać adres bez wybierania punktu.
        <button type="button" class="ml-1 font-medium underline" (click)="retry()">
          Spróbuj ponownie
        </button>
      </p>
    }
  `,
  styles: [
    '.location-map { height: 300px; width: 100%; border-radius: 12px; overflow: hidden; background: #e7e5e4; }',
  ],
})
export class LocationMap implements AfterViewInit, OnDestroy {
  latitude = input<number | null>(null);
  longitude = input<number | null>(null);
  editable = input(false);
  selected = output<{ latitude: number; longitude: number }>();
  error = signal(false);
  @ViewChild('map') container!: ElementRef<HTMLDivElement>;
  private map?: MapLibreMap;
  private marker?: Marker;
  private destroyed = false;
  private loaded = false;
  private resizeObserver?: ResizeObserver;

  async ngAfterViewInit(): Promise<void> {
    await this.loadMap();
  }

  async retry(): Promise<void> {
    this.resizeObserver?.disconnect();
    this.map?.remove();
    this.map = undefined;
    this.marker = undefined;
    this.loaded = false;
    this.error.set(false);
    await this.loadMap();
  }

  private async loadMap(): Promise<void> {
    try {
      const maplibre = await import('maplibre-gl');
      if (this.destroyed) return;
      maplibre.setWorkerUrl(
        new URL('assets/maplibre/maplibre-gl-worker.mjs', document.baseURI).href,
      );
      const lat = this.latitude();
      const lng = this.longitude();
      const hasPoint = lat !== null && lng !== null;
      this.map = new maplibre.Map({
        container: this.container.nativeElement,
        style: 'https://tiles.openfreemap.org/styles/liberty',
        center: hasPoint ? [lng, lat] : [19.1451, 51.9194],
        zoom: hasPoint ? 13 : 5,
      });
      this.map.addControl(new maplibre.NavigationControl(), 'top-right');
      this.resizeObserver = new ResizeObserver(() => this.map?.resize());
      this.resizeObserver.observe(this.container.nativeElement);
      this.map.on('load', () => {
        this.loaded = true;
        this.error.set(false);
      });
      this.map.on('error', (event) => {
        console.warn('Map loading error:', event.error);
        if (!this.loaded) this.error.set(true);
      });
      if (hasPoint) this.marker = new maplibre.Marker().setLngLat([lng, lat]).addTo(this.map);
      if (this.editable())
        this.map.on('click', (event) => {
          const point = event.lngLat.wrap();
          const latitude = Math.round(point.lat * 1e6) / 1e6;
          const longitude = Math.round(point.lng * 1e6) / 1e6;
          if (this.marker) {
            this.marker.setLngLat([longitude, latitude]);
          } else {
            this.marker = new maplibre.Marker().setLngLat([longitude, latitude]).addTo(this.map!);
          }
          this.selected.emit({ latitude, longitude });
        });
    } catch (error) {
      console.error('Map initialization failed:', error);
      if (!this.destroyed) this.error.set(true);
    }
  }
  ngOnDestroy(): void {
    this.destroyed = true;
    this.resizeObserver?.disconnect();
    this.map?.remove();
  }
}
