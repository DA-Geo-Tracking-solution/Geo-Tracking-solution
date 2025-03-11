import { Component, OnDestroy, OnInit } from '@angular/core';
import { ThemeService } from '../../services/Theme/theme.service';
import { from, Subscription } from 'rxjs';
import { FormBuilder, FormGroup, Validators } from '@angular/forms'
import { Router, ActivatedRoute } from '@angular/router';
import { faUser, faUserGroup } from '@fortawesome/free-solid-svg-icons';
import { KeycloakService } from '../../services/keycloak/keycloak.service';
import { CookieSettingsService } from '../../services/Cookies/cookie-settings.service';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-user-group',
  templateUrl: './user-group.component.html',
  styleUrl: './user-group.component.css'
})
export class UserGroupComponent {
  backgroundImage: string = '';
  private themeSubscription: Subscription | undefined;
  activeTab: string = 'user'

  faUser = faUser;
  faUserGroup = faUserGroup;
  isAlertVisible = false;

  // Variablen für dynamisches Styling
  width: string = '100%';
  maxWidth: string = 'none';
  containerClass: string = 'container box';


  constructor(private themeService: ThemeService, private route: Router, private activatedRoute: ActivatedRoute, private cookieService: CookieSettingsService, private translateService: TranslateService, private keycloakService: KeycloakService) {
    this.translateService.use(this.cookieService.getLanguage());
  }


  ngOnInit(): void {
    this.themeSubscription = this.themeService.currentTheme.subscribe((theme: string) => {
      this.updateBackgroundImage(theme);
    });


    const routeData = this.activatedRoute.snapshot.firstChild?.data;
    if (routeData) {
      this.width = routeData['width'] || '100%';
      this.maxWidth = routeData['maxWidth'] || 'none';
    }

  }

  updateBackgroundImage(theme: string) {
    if (theme === 'dark') {
      this.backgroundImage = '../../../assets/background/BI-1-DEEP_BLUE_CLOSE_UP_2.jpg';
    } else if (theme === 'light') {
      this.backgroundImage = '../../../assets/background/BI-1-ORANGE_CLOSE_UP_2.jpg';
    } else {
      // Optional: Default oder systemabhängige Logik
      this.backgroundImage = window.matchMedia('(prefers-color-scheme: dark)').matches
        ? '../../../assets/background/BI-1-DEEP_BLUE_CLOSE_UP_2.jpg'
        : '../../../assets/background/BI-1-ORANGE_CLOSE_UP_2.jpg';
    }
  }

  ngOnDestroy(): void {
    if (this.themeSubscription) {
      this.themeSubscription.unsubscribe();
    }
  }

  isGroupmaster(): boolean {
    return this.keycloakService.isGroupMaster();
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
  }

  isActiveTab(tab: string): boolean {
    return this.activeTab == tab;
  }

}
