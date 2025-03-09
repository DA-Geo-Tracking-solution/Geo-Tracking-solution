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
import { KeycloakService } from '../../services/keycloak/keycloak.service';

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

  constructor(private themeService: ThemeService, private cookieService: CookieSettingsService, private translateService: TranslateService, 
    private serverDataService: ServerDataService, private restService: RestService, private keycloakService: KeycloakService) {
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

    const handleGeoData = (data: any) => {
      if (data.key?.timestamp && data.key?.userEmail && data.longitude && data.latitude) {
        const userEmail = data.key.userEmail;
        const newLocation = { longitude: data.longitude, latitude: data.latitude };

        const existingUserIndex = this.users.findIndex(user => user.userEmail === userEmail);
        if (existingUserIndex !== -1) {
          this.tableSource.data[existingUserIndex].location = newLocation;
        } else {
          this.tableSource.data.push({ userEmail, location: newLocation });
        }
        this.users = [...this.users, { userEmail, location: newLocation }];
        this.updateTableSource(); 
      }
    }

    if (this.keycloakService.isSquadmaster()) {
      this.restService.GET("member/group").then(observable => {
        observable.subscribe({
          next: (group: string) => {
            console.log(group);
            this.serverDataService.getGroupMemberGeoLocationData(group, earliestDate, handleGeoData);
          },
          error: () => console.log("squaderror")
        });
      }).catch(() => console.log("squaderror"));
    } else {
      this.restService.GET("member/squads").then(observable => {
        observable.subscribe({
          next: (squads: any[]) => {
            for (const squad of squads) {
              console.log(squad["key"]["squadId"]);
              this.serverDataService.getSquadMemberGeoLocationData(squad["key"]["squadId"], earliestDate, handleGeoData);
            }
          },
          error: () => console.log("squaderror")
        });
      }).catch(() => console.log("squaderror"));
    }
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
    this.updateTableSource();
  }
}
