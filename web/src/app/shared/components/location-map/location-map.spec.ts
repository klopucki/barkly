import { vi } from 'vitest';
import { locationMapFixture, waitForMapReady } from './testing/location-map-fixtures';
import { markerWasPlacedInOrder } from './testing/location-map-assertions';

const mapState = vi.hoisted(() => ({
  handlers: {} as Record<string, (event: any) => void>,
  calls: [] as string[],
  coordinates: null as unknown,
}));
vi.mock('maplibre-gl', () => ({
  setWorkerUrl: vi.fn(),
  NavigationControl: class {},
  Map: class {
    addControl() {}
    resize() {}
    remove() {}
    on(name: string, callback: (event: any) => void) {
      mapState.handlers[name] = callback;
    }
  },
  Marker: class {
    setLngLat(coords: unknown) {
      mapState.calls.push('setLngLat');
      mapState.coordinates = coords;
      return this;
    }
    addTo() {
      mapState.calls.push('addTo');
      return this;
    }
  },
}));

describe('training location map', () => {
  beforeEach(() => {
    mapState.calls = [];
    mapState.handlers = {};
    vi.stubGlobal(
      'ResizeObserver',
      class {
        observe() {}
        disconnect() {}
      },
    );
  });
  afterEach(() => vi.unstubAllGlobals());

  it('sets coordinates before adding the first marker to the map', async () => {
    const fixture = await locationMapFixture();
    await waitForMapReady(() => !!mapState.handlers['click']);
    mapState.handlers['click']({ lngLat: { wrap: () => ({ lat: 52.23, lng: 21.01 }) } });
    markerWasPlacedInOrder(mapState.calls, mapState.coordinates, fixture.selected);
  });
});
