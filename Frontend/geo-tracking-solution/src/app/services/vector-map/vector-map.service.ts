import { Injectable } from '@angular/core';
import maplibregl, { Color, Feature } from 'maplibre-gl';
import User from '../../classes/User';
import MaplibreTerradrawControl from '@watergis/maplibre-gl-terradraw';
import { GeoJSONStoreFeatures, TerraDraw } from 'terra-draw';
import { AreaService } from '../area/area.service';


@Injectable({
  providedIn: 'root'
})

export class VectorMapService {
  private users: User[] = [];
  private map!: maplibregl.Map;
  private drawControl!: MaplibreTerradrawControl;
  private vectormarkers: { [key: string]: maplibregl.Marker } = {};
  private userLocations: { [key: string]: [number, number][] } = {};
  private mapHasLoaded: boolean = false;

  private allLocations: any;
  private terraDrawInstance!: TerraDraw;
  private geojsonData: GeoJSONStoreFeatures[] = [];
  //temporary to store coordinates from map should be pushed to backend
  private drawingCoordinates: any;


  constructor(private areaService: AreaService) { }

  setMap(map: maplibregl.Map): void {
    this.map = map;
  }

  //a method to draw the Markers of Users, which are received from the backend and draws them depending on where they are in real live.
  //Also updates the position whenever changes from the Backend come in
  drawUserMarkers(users: User[]): any {
    if (this.map) {
      for (const user of users) {
        this.areaService.haveUserData(user, this.drawingCoordinates);
        // when there already is a marker for a User change its position
        if (this.vectormarkers[user.userEmail]) {
          this.vectormarkers[user.userEmail].setLngLat([user.location.longitude, user.location.latitude]);
        }
        // when there currently is no marker for a User add a new one to the map
        else {
          const marker = new maplibregl.Marker().setLngLat([user.location.longitude, user.location.latitude]).addTo(this.map);
          this.vectormarkers[user.userEmail] = marker;
        }
      }
      if (Object.keys(this.vectormarkers).length > users.length) {
        for (const key of Object.keys(this.vectormarkers)) {
          users.findIndex((user) => user.userEmail !== key);
        }
      }
    }
    return this.allLocations;
  }

  //a method to draw trails behind the markers to show the previous path of the user
  drawUserLines(users: User[]): void {
    console.log("hi-1")
    const features: any = [];
    for (const user of users) {
      const lat = user.location.latitude;
      const lng = user.location.longitude;
      // when a user does not have previous locations create a new array for them
      if (!this.userLocations[user.userEmail]) {
        this.userLocations[user.userEmail] = [];
      }
      this.userLocations[user.userEmail].push([lng, lat]);
      // if the user already has the maximum number of stored locations (at the moment 10) remove the oldest
      if (this.userLocations[user.userEmail].length > 10) {
        this.userLocations[user.userEmail].shift();
      }
      //finally draw the line onto the map
      features.push({
        'type': 'Feature',
        'properties': {
          'color': this.generateColorFromEmail(user.userEmail)
        },
        'geometry': {
          'type': 'LineString',
          'coordinates': this.userLocations[user.userEmail]
        }
      });
    }
    console.log("hi0")
    if (this.map) {
      console.log("hi1")
      if (this.map.loaded()) {
        if (this.map.getSource('lines')) {
          this.map.removeLayer('lines');
          this.map.removeSource('lines');
        }
        this.map.addSource('lines', {
          'type': 'geojson',
          'data': {
            'type': 'FeatureCollection',
            'features': features
          }
        });
        console.log("hi2")
        this.map.addLayer({
          'id': 'lines',
          'type': 'line',
          'source': 'lines',
          'paint': {
            'line-width': 3,
            'line-color': ['get', 'color']
          }
        });
      }
    }
  }

  //draw the figures already drawn on the other maptypes
  //gets all previous figures in a json format
  drawPreviousFigures(drawingData: any) {
    this.drawingCoordinates = drawingData;
    for (const figure of drawingData) {
      const id: number = drawingData.findIndex((drawing: any) => drawing === figure);
      // when the type is 'Polygon' reformat it and call addPolygon method
      if (figure.type === 'Polygon') {
        const polygonCoordinates: [number, number][] = [];
        for (const coordinates of figure.coordinates[0]) {
          polygonCoordinates.push([coordinates[0], coordinates[1]]);
        }
        this.addPolygon(polygonCoordinates, id);
      }
      // when the type is 'Circle' reformat it and call addCircle method
      else if (figure.type === 'Circle') {
        const center: [number, number] = [figure.coordinates[0][0], figure.coordinates[0][1]];
        const radius: number = figure.coordinates[1];
        this.addCircle(center, radius, id);

      }
      // when the type is 'Linestring' reformat it and call addLineString method
      else if (figure.type === 'Linestring') {
        const linestringCoordinates: [number, number][] = [];
        for (const coordinates of figure.coordinates) {
          linestringCoordinates.push([coordinates[0], coordinates[1]]);
        }
        this.addLineString(linestringCoordinates, id);

      }
      // when the type is 'Point' reformat it and call addPoint method
      else if (figure.type === 'Point') {
        const pointCoordinates: [number, number] = [figure.coordinates[0], figure.coordinates[1]];
        this.addPoint(pointCoordinates, id);
      }
    }
  }

  //a method to create a new polygon feature and add it to geojsonData array
  addPolygon(coordinates: [number, number][], id: number) {
    const newPolygon: GeoJSONStoreFeatures = {
      "type": "Feature",
      "geometry": {
        "type": "Polygon",
        "coordinates": [coordinates]
      },
      "properties": {
        mode: "polygon",
        id: id
      }
    };
    this.geojsonData.push(newPolygon);
  }

  //a method to calculate the corners of the polygon, create a new polygon feature and add it to geojsonData array
  addCircle(center: [number, number], radius: number, id: number) {
    const vertices: [number, number][] = [];
    for (let i = 0; i < 64; i++) {
      const angle = (2 * Math.PI * i) / 64;
      // Calculate the coordinates for each vertex
      const latitude = center[1] + (radius * Math.cos(angle));
      const longitude = center[0] + (radius * Math.sin(angle)) / Math.cos(center[1] * (Math.PI / 180));
      vertices.push([parseFloat(longitude.toFixed(9)), parseFloat(latitude.toFixed(9))]);
    }
    // Close the polygon by repeating the first point at the end
    vertices.push(vertices[0]);

    const newCircle: GeoJSONStoreFeatures = {
      "type": "Feature",
      "geometry": {
        "type": "Polygon",
        "coordinates": [vertices]
      },
      "properties": {
        mode: "polygon",
        id: id
      }
    };
    this.geojsonData.push(newCircle);
  }

  //a method to create a new linestring feature and add it to geojsonData array
  addLineString(coordinates: [number, number][], id: number) {
    const newPolyLine: GeoJSONStoreFeatures = {
      "type": "Feature",
      "geometry": {
        "type": "LineString",
        "coordinates": coordinates
      },
      "properties": {
        mode: "linestring",
        id: id
      }
    };
    this.geojsonData.push(newPolyLine);
  }

  //a method to create a new point feature and add it to geojsonData array
  addPoint(coordinates: [number, number], id: number) {
    const newPoint: GeoJSONStoreFeatures = {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": coordinates
      },
      "properties": {
        mode: "point",
        id: id
      }
    };
    this.geojsonData.push(newPoint);
  }


  //clear the MarkersArray
  clearMarkers() {
    this.vectormarkers = {};
  }

  //initialize the drawControl and configure it to fit our needs
  initializeDrawControl(): void {
    this.drawControl = new MaplibreTerradrawControl();
    this.map.addControl(this.drawControl, 'bottom-left');
    //when the map is loaded draw all the previous figures
    this.map.on('load', (event) => {
      let data = [];
      this.terraDrawInstance = this.drawControl.getTerraDrawInstance();
      this.terraDrawInstance.addFeatures(this.geojsonData);

      //when a figure is finished drawing extract the important data and add it to the data array
      this.terraDrawInstance.on('finish', (event) => {
        let data: any;
        const storedFeatures = this.terraDrawInstance["_store"]["store"]; // Access the stored features from the Map

        for (const key in storedFeatures) {
          if (storedFeatures.hasOwnProperty(key)) {
            const feature = storedFeatures[key];
            //categorize all sorts of polygons into polygons and add them to the data array
            if (["polygon", "rectangle", "angled-rectangle", "freehand"].includes(feature.properties.mode)) {
              data = {
                type: "Polygon",
                coordinates: feature.geometry.coordinates
              };
            }
            //calculate the center and the radius in meters of the circle and add them to the data array
            else if (["circle"].includes(feature.properties.mode)) {
              let lngcoordinates = 0;
              let latcoordinates = 0;

              for (let i = 0; i < feature.geometry.coordinates[0].length - 1; i++) {
                lngcoordinates += Number(feature.geometry.coordinates[0][i][0]);
                latcoordinates += Number(feature.geometry.coordinates[0][i][1]);
              }

              const avglng = lngcoordinates / (feature.geometry.coordinates[0].length - 1);
              const avglat = latcoordinates / (feature.geometry.coordinates[0].length - 1);
              //caculate radius (first Longitude - the Longitude on the opposit side of the circle)
              const radius = (Number(feature.geometry.coordinates[0][0][0]) - Number(feature.geometry.coordinates[0][(feature.geometry.coordinates[0].length - 1) / 2][0])) / 2;
              data = {
                type: "Circle",
                coordinates: [[avglng, avglat], radius]
              };
            }
            //get the coordinates of linestrings and add them to the data array
            else if (["linestring"].includes(feature.properties.mode)) {
              data = {
                type: "Linestring",
                coordinates: feature.geometry.coordinates
              };
            }
            //get the coordinates of points and add them to the data array
            else if (["point"].includes(feature.properties.mode)) {
              data = {
                type: "Point",
                coordinates: feature.geometry.coordinates
              };
            }
          }
        }
        if (data) {
          this.areaService.haveDrawingData(this.users, data);
          this.drawingCoordinates.push(data);
        }
      });
    });
  }

  //returns the coordinates of all drawn figures
  getDrawingData(): void {
    return this.drawingCoordinates;
  }

  generateColorFromEmail(email: string): string {
    const hash = this.hashEmail(email);
    const color = this.hashToColor(hash);

    return color;
  }

  private hashEmail(email: string): number {
    let hash = 0;
    for (let i = 0; i < email.length; i++) {
      const char = email.charCodeAt(i);
      hash = (hash << 5) - hash + char;
      hash = hash & hash;
    }
    return hash;
  }

  private hashToColor(hash: number): string {
    const r = (hash >> 16) & 0xff;
    const g = (hash >> 8) & 0xff;
    const b = hash & 0xff;

    return `#${(1 << 24 | r << 16 | g << 8 | b).toString(16).slice(1)}`;
  }
}
