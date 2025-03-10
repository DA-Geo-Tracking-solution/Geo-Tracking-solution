import { Injectable } from '@angular/core';
import * as L from 'leaflet';
import User from '../../classes/User';
import 'leaflet-draw';
import { AreaService } from '../area/area.service';

@Injectable({
  providedIn: 'root'
})
export class RasterMapService {

  private users: User[] = [];
  private map!: L.Map;
  private rastermarkers: { [key: string]: L.Marker } = {};
  private userLocations: { [key: string]: [number, number][] } = {};

  private allLocations: any;
  private drawnItems = new L.FeatureGroup();
  //temporary to store coordinates from map should be pushed to backend
  private drawingCoordinates: any = [];

  constructor(private areaService: AreaService) { }

  setMap(map: L.Map): void {
    this.map = map;
  }

  drawUserMarkers(users: User[]) {
    this.users = users;
    if (this.map) {
      for (const user of users) {
        this.areaService.haveUserData(user, this.drawingCoordinates);
        if (this.rastermarkers[user.userEmail]) {
          this.rastermarkers[user.userEmail].setLatLng([user.location.latitude, user.location.longitude])
        } else {
          const marker = L.marker([user.location.latitude, user.location.longitude]);
          marker.addTo(this.map);
          //marker.bindPopup(`<b>I am ${user.userEmail}</b>`).openPopup();
          this.rastermarkers[user.userEmail] = marker;
        }
      }
      if (Object.keys(this.rastermarkers).length > users.length) {
        for (const key of Object.keys(this.rastermarkers)) {
          users.findIndex((user) => user.userEmail !== key);
        }
      }
    }
    return this.allLocations;
  }
  //Draw a line for each User
  drawUserLines(users: User[]) {
    for (const user of users) {
      const lat = user.location.latitude;
      const lng = user.location.longitude;
      if (!this.userLocations[user.userEmail]) {
        this.userLocations[user.userEmail] = [];
      }
      //add current location to Array and get rid of the oldest when more than 10 locations
      this.userLocations[user.userEmail].push([lat, lng]);
      if (this.userLocations[user.userEmail].length > 10) {
        this.userLocations[user.userEmail].shift();
      }
    }
    // Check if the map is defined
    if (!this.map) {
      return;
    }

    // Remove all lines from the map
    this.map.eachLayer((layer) => {
      if (layer instanceof L.Polyline) {
        this.map?.removeLayer(layer);
      }
    });

    // Draw a line for each user
    for (const userId in this.userLocations) {
      const latlngs = this.userLocations[userId];
      if (latlngs.length > 1) {
        L.polyline(latlngs, { color: 'red' }).addTo(this.map);
      }
    }
  }

  drawDrawings(drawingData: any) {
    this.drawingCoordinates = drawingData;
    if (drawingData) {
      for (const figure of drawingData) {
        if (figure.type === 'Polygon') {
          const polygonCoordinates = [];
          for (const coordinates of figure.coordinates[0]) {
            polygonCoordinates.push([coordinates[1], coordinates[0]]);
          }
          L.polygon(polygonCoordinates, {
            color: 'blue',
            fillColor: '#3388ff',
            fillOpacity: 0.5
          }).addTo(this.drawnItems);
        } else if (figure.type === 'Circle') {
          const center: L.LatLngExpression = [figure.coordinates[0][1], figure.coordinates[0][0]];
          const radius = figure.coordinates[1];
          //factor for latitude specific factor from DegreeLongitude to meters
          const metersPerDegreeLongitude = 111320 * Math.cos(figure.coordinates[0][1] * (Math.PI / 180));

          L.circle(center, {
            radius: radius * metersPerDegreeLongitude,
            color: 'blue',
            fillColor: '#3388ff',
            fillOpacity: 0.5
          }).addTo(this.drawnItems);
        } else if (figure.type === 'Linestring') {
          const linestringCoordinates = [];
          for (const coordinates of figure.coordinates) {
            linestringCoordinates.push([coordinates[1], coordinates[0]]);
          }
          L.polyline(linestringCoordinates, {
            color: 'blue',
            fillColor: '#3388ff',
            fillOpacity: 0.5
          }).addTo(this.drawnItems);
        } else if (figure.type === 'Point') {
          const pointCoordinates: L.LatLngExpression = [figure.coordinates[1], figure.coordinates[0]];
          L.circleMarker(pointCoordinates, { radius: 5 }).addTo(this.drawnItems);
        }
      }
    }
  }

  //clear the MarkersArray
  clearMarkers() {
    this.rastermarkers = {};
  }

  initializeDrawing(): void {
    // Zeichenebene
    this.map.addLayer(this.drawnItems);

    // Zeichnungsoptionen
    const drawControl = new L.Control.Draw({
      draw: {
        marker: false,
        polyline: {
          shapeOptions: {
            color: '#0000ff',   // Blue stroke color
            weight: 4,          // Stroke width of 4 pixels
            opacity: 0.7,       // 70% opacity for the line
            dashArray: '5, 5',  // Dashed line (5px dash, 5px gap)
            lineCap: 'round',   // Rounded line ends
            lineJoin: 'round'   // Rounded line joins
          }
        },
        polygon: {
          shapeOptions: {
            color: '#00ff00'
          }
        },
        rectangle: {
          shapeOptions: {
            color: '#000000'
          }
        },
        circle: {
          shapeOptions: {
            color: '#ff8800'
          }
        }
      },
      edit: {
        featureGroup: this.drawnItems, // Set feature group for editing
        remove: true, // Enable delete button at the bottom
      }
    });

    this.map.addControl(drawControl);

    // Ereignisse, um auf die gezeichneten Formen zu reagieren
    this.map.on(L.Draw.Event.CREATED, (event) => {
      let data: any;
      const layer = event.layer;
      this.drawnItems.addLayer(layer);

      if (layer instanceof L.CircleMarker) {
        //needs to be like that, because the circle drawing function in leaflet is just a marker, but the real marker always has the radius 10
        const singlePointCoordinates = layer.getLatLng();
        const singlePointSwappedCoordinates = this.swapLatLngs(singlePointCoordinates);
        if (layer.getRadius() == 10) {
          data = {
            type: "Point",
            coordinates: singlePointSwappedCoordinates
          };
        } else {
          const radiusInDegrees = layer.getRadius() / (111320 * Math.cos(singlePointSwappedCoordinates[0] * Math.PI / 180));
          data = {
            type: "Circle",
            coordinates: [singlePointSwappedCoordinates, radiusInDegrees]
          };
        }
      } else {
        const multiPointCoordinates = layer.getLatLngs();
        const multiPointSwappedCoordinates = this.swapLatLngs(multiPointCoordinates);
        if (layer instanceof L.Polygon) {

          multiPointSwappedCoordinates[0].push(multiPointSwappedCoordinates[0][0]);
          data = {
            type: "Polygon",
            coordinates: multiPointSwappedCoordinates
          };
        } else if (layer instanceof L.Polyline) {
          data = {
            type: "Linestring",
            coordinates: multiPointSwappedCoordinates
          };
        }
      }

      if (data) {
        this.areaService.haveDrawingData(this.users, data);
        this.drawingCoordinates.push(data);
      }
    });
  }

  swapLatLngs(coords: L.LatLng | L.LatLng[] | L.LatLng[][] | L.LatLng[][][]): any {
    if (Array.isArray(coords)) {
      return coords.map(coord => this.swapLatLngs(coord));
    } else {
      return [parseFloat(coords.lng.toFixed(9)), parseFloat(coords.lat.toFixed(9))];
    }
  }

  getDrawingData(): void {
    return this.drawingCoordinates;
  }
}
