import { Injectable } from '@angular/core';
import { userInfo } from 'os';
import User from '../../classes/User';
import { RestService } from '../REST/rest.service';

@Injectable({
  providedIn: 'root'
})
export class AreaService {

  private usersindrawings: { [key: number]: User[] } = [];

  constructor(private restService: RestService) { }

  haveUserData(userdata: User, multibledrawingdata: any) {
    for (const singledrawingdata of multibledrawingdata) {
      if (singledrawingdata.type === "Polygon") {
        const index = multibledrawingdata.indexOf(singledrawingdata);
        if (this.calculateInPolygon([userdata.location.latitude, userdata.location.longitude], singledrawingdata.coordinates)) {
          if (!this.usersindrawings[index].some(user => user.userEmail === userdata.userEmail)) {
            this.usersindrawings[index].push(userdata);
            this.userInDrawing(userdata.userEmail, index);
          }
          //check, if user alredy in polygon
          //if not add and userInDrawing()
        } else {
          if (this.usersindrawings[index].some(user => user.userEmail === userdata.userEmail)) {
            this.usersindrawings[index] = this.usersindrawings[index].filter(user => user.userEmail !== userdata.userEmail);
            this.userOutDrawing(userdata.userEmail, index);
          }
          //check, if user was in polygon
          //if remove and userOutDrawing()
        }
      } else if (singledrawingdata.type === "Circle") {
        for (const index in singledrawingdata) {
          const drawing = singledrawingdata[index];
          console.log(drawing)
          //this.calculateInCircle(userdata.position, drawing.coordinates);
        }
      }
    }
  }

  haveDrawingData(users: User[], drawingdata: any) {
    if (drawingdata.type === "Polygon") {
      let allUsersInDrawing: User[] = [];
      for (const index in users) {
        console.log(users[index])
        if (this.calculateInPolygon([users[index].location.latitude, users[index].location.longitude], drawingdata.coordinates)) {
          allUsersInDrawing.push(users[index]);
        }
      }
      const uuid = this.createSquad(allUsersInDrawing);
      this.usersindrawings[Object.keys(this.usersindrawings).length] = allUsersInDrawing;
      console.log(this.usersindrawings)
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

  calculateInPolygon(point: [number, number], polygon: [number, number][][]): boolean {
    const [lat, lng] = point;
    let inside = false;

    for (let i = 0, j = polygon[0].length - 1; i < polygon[0].length; j = i++) {
      const [xi, yi] = polygon[0][i];
      const [xj, yj] = polygon[0][j];

      const intersect = yi > lat !== yj > lat && lng < ((xj - xi) * (lat - yi)) / (yj - yi) + xi;
      if (intersect) inside = !inside;
    }
    return inside;
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

  createSquad(users: User[]): string {

    return "11111111-1111-1111-1111-111111111111"
  }

  userInDrawing(userEmail: string, drawing: number) {
    console.log(userEmail + " went into " + drawing)
    this.restService.POST("squadmaster/11111111-1111-1111-1111-111111111111/users", {
      userEmail
    }).then(observable => {
      observable.subscribe({
        next: () => console.log("Successfully added User to Squad! ;)"),
        error: (err) => console.error("Error:", err)
      });
    });
    //not my business
  }
  userOutDrawing(userEmail: string, drawing: any) {
    console.log(userEmail + " went out of " + drawing)
    this.restService.DELETE(`squadmaster/11111111-1111-1111-1111-111111111111/users/${userEmail}`).then(observable => {
      observable.subscribe({
        next: () => console.log("Successfully removed User from Squad! ;)"),
        error: (err) => console.error("Error:", err)
      });
    });
    //not my business
  }
}
