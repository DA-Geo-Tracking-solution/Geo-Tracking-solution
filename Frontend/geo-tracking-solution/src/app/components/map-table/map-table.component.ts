import { Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import User from '../../classes/User';
import { ThemeService } from '../../services/Theme/theme.service';
import { TranslateService } from '@ngx-translate/core';
import { CookieSettingsService } from '../../services/Cookies/cookie-settings.service';
import { MapType } from '../../models/interfaces';
import { ServerDataService } from '../../services/server-data/server-data.service';
import { MatSort } from '@angular/material/sort';
import { RestService } from '../../services/REST/rest.service';

@Component({
  selector: 'app-map-table',
  templateUrl: './map-table.component.html',
  styleUrl: './map-table.component.css'
})
export class MapTableComponent implements OnInit {
  users: User[] = [];

  displayedColumns: string[] = ['username', 'longitude', 'latitude'];

  tableSource!: MatTableDataSource<User>;
  @ViewChild(MatSort) sort!: MatSort;

  currentTheme: string = '';

  //parameter to declare maptype ('vector', 'raster', 'satellite') default should be vector because best performance
  public selectedMap: string = 'vector';

  //different MapTypes for html Dropdown Selection
  public mapTypes: MapType[] = [
    { value: 'vector', viewValue: 'Vektor' },
    { value: 'raster', viewValue: 'Raster' },
    { value: 'satellite', viewValue: 'Satelit' },
  ];

  constructor(private themeService: ThemeService, private cookieService: CookieSettingsService, private translateService: TranslateService, private serverDataService: ServerDataService, private restService: RestService) {
    this.translateService.use(this.cookieService.getLanguage());
  }

  ngOnInit(): void {
    this.themeService.currentTheme.subscribe((theme) => {
      this.currentTheme = theme;
    });

    this.tableSource = new MatTableDataSource(this.users);
    this.tableSource.sort = this.sort;

    const date = new Date(Date.now());
    date.setFullYear(date.getFullYear()-1);


    const earliestDate = date.toISOString().split('.')[0] + date.toISOString().split('.')[1].substring(3);


    this.restService.GET("member/squads").then(observable => {
      observable.subscribe({
        next: (squads: any[]) => {
          for (const squad of squads) {
            console.log(squad["key"]["squadId"]);
            this.serverDataService.getGeoLocationData(squad["key"]["squadId"], earliestDate, (data: any) => {
              if (data.key?.timestamp && data.key?.userEmail && data.longitude && data.latitude) {
                const userEmail = data.key.userEmail;
                const newLocation = { longitude: data.longitude, latitude: data.latitude };
        
                const existingUserIndex = this.users.findIndex(user => user.userEmail === userEmail);
                if (existingUserIndex !== -1) {
                  this.tableSource.data[existingUserIndex].location = newLocation;
                } else {
                  this.tableSource.data.push({ userEmail, location: newLocation });
                }
                console.log(userEmail)
                this.users.push({ userEmail, location: newLocation });
              }
            });
            this.updateTableSource(); 
          }
        },
        error: () => console.log("squaderror")
      });
    })
    .catch(() => console.log("squaderror"));

    
  }

  updateTableSource(): void {
    const userMap = new Map<string, User>();
    this.users.forEach(user => {
      userMap.set(user.userEmail, user);
    });
    this.tableSource.data = Array.from(userMap.values());
  }


  //delete current Map and make init of new chosen Map
  changeMapType(selectedMap: string) {
    this.selectedMap = selectedMap;

    //insert some testdata
    /*const newUsers = [
      { userEmail: "currentUser0", location: { longitude: 16.0, latitude: 48.627 } },
      { userEmail: "currentUser1", location: { longitude: 14.5, latitude: 52.520 } },
      { userEmail: "currentUser1", location: { longitude: -74.006, latitude: 40.7128 } },
      { userEmail: "currentUser2", location: { longitude: 2.3522, latitude: 48.8566 } },
      { userEmail: "currentUser2", location: { longitude: 151.2093, latitude: -33.8688 } },
      { userEmail: "currentUser2", location: { longitude: -0.1276, latitude: 51.5074 } },
      { userEmail: "currentUser3", location: { longitude: 139.6917, latitude: 35.6895 } },
      { userEmail: "currentUser3", location: { longitude: -118.2437, latitude: 34.0522 } },
      { userEmail: "currentUser3", location: { longitude: 10.4515, latitude: 51.1657 } },
      { userEmail: "currentUser3", location: { longitude: -3.7038, latitude: 40.4168 } }
    ];

    newUsers.forEach(newUser => {
      this.users.push(newUser);
    });*/

    this.updateTableSource();
  }
}
