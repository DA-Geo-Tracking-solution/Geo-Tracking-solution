import { Inject, Injectable, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import Keycloak from 'keycloak-js';
import { UserProfile } from './user-profile';
import User from '../../classes/User';
import { UserInformation } from '../../models/interfaces';

@Injectable({
  providedIn: 'root'
})
export class KeycloakService {

  private _keycloak: Keycloak | undefined;
  private _profile : UserProfile | undefined;

  get keycloak(): Keycloak | undefined{
    if (!this._keycloak) {
      this._keycloak = new Keycloak({
        url: 'http://localhost:8081',
        realm: 'geo-tracking-solution',
        clientId: 'angular-client'
      });   
    }
    return this._keycloak
  }


  get profile(): UserProfile | undefined {
    return this._profile;
  }

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      setInterval(() => {
        this.refreshToken();
      }, 60000);
    }
  }
  

  async init(): Promise<boolean> {
    if (isPlatformBrowser(this.platformId)) {
      console.log("init keycloak");
      const authenticated = await this.keycloak?.init({
        onLoad: 'login-required',
        pkceMethod: 'S256',
        flow: 'standard',
      })
      if (authenticated) {
        this._profile = (await this.keycloak?.loadUserProfile()) as UserProfile;
        this._profile.token = this._keycloak?.token;
        this.refreshToken()
        return true;
      }
      this.refreshToken();
    }
    return false;
  }

  async refreshToken(): Promise<void> {
    if (this.keycloak && this._profile) {
      try {
        const refreshed = await this.keycloak.updateToken(60); // Refresh if token expires in 60 seconds
        if (refreshed) {
          console.log("Token successfully refreshed");
          this._profile.token = this.keycloak.token; // Update the token in your profile
        } else {
          console.log("Token is still valid, no need to refresh");
        }
      } catch (error) {
        console.error("Failed to refresh token", error);
        this.keycloak.logout(); // Log out user if refresh fails
      }
    }
  }

  async updateUserProfile(): Promise<void> {
    if (this.keycloak) {
      this._profile = await this.keycloak.loadUserProfile() as UserProfile;
      // Optionally force a token refresh after reloading the user profile:
      await this.refreshToken();
    }
  }
  
  
  get roles(): string[] {
    if (this.keycloak?.tokenParsed) {
      const tokenParsed = this.keycloak.tokenParsed as any;
      const realmRoles = tokenParsed.realm_access?.roles || [];
      const resourceRoles = tokenParsed.resource_access?.['angular-client']?.roles || [];
      return [...realmRoles, ...resourceRoles];
    }
    return [];
  }

  get user(): UserInformation | undefined {
    if (this.keycloak?.tokenParsed) {
      const tokenParsed = this.keycloak.tokenParsed as any;
      return (tokenParsed as UserInformation);
    } 
    return undefined;
  }

  hasRole(role: string): boolean {
    return this.roles.includes(role);
  }
  isGroupMaster(): boolean {return this.hasRole('groupmaster');}
  isSquadmaster(): boolean {return this.hasRole('squadmaster');}
  isMember(): boolean {return (this.hasRole('member') || this.isSquadmaster() || this.isGroupMaster())}
  
  
  login() {
    return this.keycloak?.login();
  }

  logout() {
    return this.keycloak?.logout({
      redirectUri:'http://localhost:4200'
    });
  }
}
