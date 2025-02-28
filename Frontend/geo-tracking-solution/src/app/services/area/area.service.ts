import { Injectable } from '@angular/core';
import { userInfo } from 'os';
import User from '../../classes/User';

@Injectable({
  providedIn: 'root'
})
export class AreaService {

  constructor() { }
  haveUserData(userdata: any, drawingdata: any) {
    if (drawingdata.type === "Polygon") {

      for (const drawing in drawingdata) {
        console.log(drawing)
        //this.calculateInPolygon(userdata.position, drawing.coordinates);
      }
    } else {
      for (const drawing in drawingdata) {
        console.log(drawing)
        //this.calculateInCircle(userdata.position, drawing.coordinates);
      }
    }
  }

  haveDrawingData(users: User[], drawingdata: any) {
    if (drawingdata.type === "Polygon") {

      for (const user in users) {
        console.log(user)
        //this.calculateInPolygon(user.location, drawingdata.coordinates);
      }
    } else {
      for (const user in users) {
        console.log(user);
        //this.calculateInCircle(user.location, drawingdata.coordinates);
      }
    }
  }

  calculateInCircle(userposition: any, circledata: any) {
    const point = userposition;
    const center = circledata[0];
    const distance = this.angularDistance(point, center);

    if (distance <= circledata[1]) {
      console.log("Point is inside the circle");
    } else {
      console.log("Point is outside the circle");
    }
  }

  calculateInPolygon(point: [number, number], polygon: [number, number][][]) {
    const [lng, lat] = point;
    let inside = false;

    for (let i = 0, j = polygon[0].length - 1; i < polygon[0].length; j = i++) {
      const [xi, yi] = polygon[0][i];
      const [xj, yj] = polygon[0][j];

      const intersect = yi > lat !== yj > lat && lng < ((xj - xi) * (lat - yi)) / (yj - yi) + xi;
      if (intersect) inside = !inside;
    }

    if (inside) {
      console.log("Point is inside the Polygon");
    } else {
      console.log("Point is outside the Polygon");
    }
  }

  angularDistance(coord1: [number, number], coord2: [number, number]): number {
    const toRad = (angle: number) => (angle * Math.PI) / 180;
    const toDeg = (angle: number) => (angle * 180) / Math.PI;

    const [lng1, lat1] = coord1;
    const [lng2, lat2] = coord2;

    const φ1 = toRad(lat1);
    const φ2 = toRad(lat2);
    const Δφ = toRad(lat2 - lat1);
    const Δλ = toRad(lng2 - lng1);

    const a = Math.sin(Δφ / 2) ** 2 +
      Math.cos(φ1) * Math.cos(φ2) *
      Math.sin(Δλ / 2) ** 2;

    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return toDeg(c);
  }
}
