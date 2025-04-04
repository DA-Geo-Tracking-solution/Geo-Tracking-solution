import { Component, Renderer2, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { ThemeService } from '../../services/Theme/theme.service';
import { CookieService } from 'ngx-cookie-service';
import { TranslateService } from '@ngx-translate/core';
import { CookieSettingsService } from '../../services/Cookies/cookie-settings.service';
import { KeycloakService } from '../../services/keycloak/keycloak.service';

@Component({
  selector: 'app-navigation-bar',
  templateUrl: './navigation-bar.component.html',
  styleUrls: ['./navigation-bar.component.css']
})
export class NavigationBarComponent implements AfterViewInit {
  @ViewChild('navbar') navbar!: ElementRef;

  dashboardIcon: string = '../../../assets/icons/navigation-bar/black/dashboard_black.png';
  createUserIcon: string = '../../../assets/icons/navigation-bar/black/user_black.png';
  chatIcon: string = '../../../assets/icons/navigation-bar/black/chat_black.png';
  mapIcon: string = '../../../assets/icons/navigation-bar/black/map_black.png';
  moreIcon: string = '../../../assets/icons/menue_black.png';
  evidenLogo: string = '../../../assets/Logo/Eviden_Black.png';

  themeIcon: string = '../../../assets/icons/theme/monitor.png';
  currentTheme: string = 'system';
  navbarOpen: boolean = false;

  constructor(private renderer: Renderer2, private themeService: ThemeService, private cookieService: CookieService, private translateService: TranslateService, private cookieSettingsService: CookieSettingsService, private keycloakService: KeycloakService) { 
    this.translateService.use(this.cookieSettingsService.getLanguage());
  }

  ngAfterViewInit() {
    // Hole das Theme direkt aus den Cookies
    const savedTheme = this.cookieService.get('theme') || 'system';
    this.themeService.setTheme(savedTheme);

    // Setze das initiale Theme
    this.themeService.currentTheme.subscribe(theme => {
      this.currentTheme = theme;
      this.updateThemeIcons(theme);
      this.applyTheme(theme); // Wende das Theme an
    });
  }

  isGroupmaster(): boolean {
    return this.keycloakService.isGroupMaster();
  }

  changeTheme(theme: string) {
    this.themeService.setTheme(theme);
  }

  updateThemeIcons(theme: string) {
    if (theme === 'dark') {
      this.themeIcon = '../../../assets/icons/theme/dark.png';
      this.updateIconsForDarkTheme();
    } else if (theme === 'light') {
      this.themeIcon = '../../../assets/icons/theme/sonne.png';
      this.updateIconsForLightTheme();
    } else {
      this.themeIcon = '../../../assets/icons/theme/monitor.png';
      this.applySystemTheme();
    }
  }

  applyTheme(theme: string) {
    if (this.navbar) {
      this.renderer.setAttribute(this.navbar.nativeElement, 'data-theme', theme);
      theme === 'dark' ? this.applyDarkTheme() : this.applyLightTheme();
    }
  }

  applyDarkTheme() {
    this.renderer.setAttribute(this.navbar.nativeElement, 'data-theme', 'dark');
    this.updateIconsForDarkTheme();
  }

  applyLightTheme() {
    this.renderer.setAttribute(this.navbar.nativeElement, 'data-theme', 'light');
    this.updateIconsForLightTheme();
  }

  applySystemTheme() {
    const isDarkMode = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.applyTheme(isDarkMode ? 'dark' : 'light');
  }

  updateIconsForDarkTheme() {
    this.dashboardIcon = '../../../assets/icons/navigation-bar/white/dashboard_white.png';
    this.createUserIcon = '../../../assets/icons/navigation-bar/white/user_white.png';
    this.chatIcon = '../../../assets/icons/navigation-bar/white/chat_white.png';
    this.mapIcon = '../../../assets/icons/navigation-bar/white/map_white.png';
    this.moreIcon = '../../../assets/icons/menue_white.png';
    this.evidenLogo = '../../../assets/Logo/Eviden_White.png';
  }

  updateIconsForLightTheme() {
    this.dashboardIcon = '../../../assets/icons/navigation-bar/black/dashboard_black.png';
    this.createUserIcon = '../../../assets/icons/navigation-bar/black/user_black.png';
    this.chatIcon = '../../../assets/icons/navigation-bar/black/chat_black.png';
    this.mapIcon = '../../../assets/icons/navigation-bar/black/map_black.png';
    this.moreIcon = '../../../assets/icons/menue_black.png';
    this.evidenLogo = '../../../assets/Logo/Eviden_Black.png';
  }

  toggleNavbar() {
    this.navbarOpen = !this.navbarOpen;
  }

  logOut() {
    this.keycloakService.logout();
  }

}
