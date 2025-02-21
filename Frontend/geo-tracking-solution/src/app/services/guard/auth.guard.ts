
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from '../keycloak/keycloak.service';

export const authGuard: CanActivateFn = (route, state) => {
  const keycloakService = inject(KeycloakService); 
  const router = inject(Router);
  console.log(keycloakService.roles)

  if (keycloakService.keycloak?.isTokenExpired()) {
    //router.navigate(['']);
    return false;
  }
  return true;
};

export const groupmasterGuard: CanActivateFn = (route, state) => {
  const keycloakService = inject(KeycloakService); 
  const router = inject(Router);

  if (!keycloakService.isGroupMaster()) {
    //router.navigate(['']);
    return false;
  }
  return true;
};
