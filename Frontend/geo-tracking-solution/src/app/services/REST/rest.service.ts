import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { KeycloakService } from '../keycloak/keycloak.service';
import { ErrorDialogService } from '../error-dialog/error-dialog.service';

@Injectable({
  providedIn: 'root'
})
export class RestService {

  private url: string = 'http://localhost:8080';

  constructor(
    private http: HttpClient,
    private keycloakService: KeycloakService,
    private errorDialogService: ErrorDialogService
  ) {}

  async GET(path: string): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    
    return this.http.get(`${this.url}/${path}`, { headers }).pipe(
      catchError((error: HttpErrorResponse) => {
        // Ensure that error.error contains the JSON response
        const errorResponse = error.error;
        const errorMessage = errorResponse?.message || 'Unknown error';
        const errorTitle = errorResponse?.error || 'Error';
    
        // Now open the error dialog with the extracted values
        console.log(`this.errorDialogService.openErrorDialog(${errorTitle}, ${errorMessage});`)
        this.errorDialogService.openErrorDialog(errorTitle, errorMessage);
    
        // Re-throw the error so subscribers can handle it too
        return throwError(() => error);
      })
    );
  }

  async POST(path: string, body: any): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    console.log('Making POST request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
    console.log('Body:', body);

    return this.http.post(`${this.url}/${path}`, body, { headers }).pipe(
      catchError((error: HttpErrorResponse) => {
        // Ensure that error.error contains the JSON response
        const errorResponse = error.error;
        const errorMessage = errorResponse?.message || 'Unknown error';
        const errorTitle = errorResponse?.error || 'Error';
    
        // Now open the error dialog with the extracted values
        console.log(`this.errorDialogService.openErrorDialog(${errorTitle}, ${errorMessage});`)
        this.errorDialogService.openErrorDialog(errorTitle, errorMessage);
    
        // Re-throw the error so subscribers can handle it too
        return throwError(() => error);
      })
    );
  }

  async PATCH(path: string, body: any): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
    console.log('Making PATCH request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
    console.log('Body:', body);
  
    return this.http.patch(`${this.url}/${path}`, body, { headers }).pipe(
      catchError((error: HttpErrorResponse) => {
        // Ensure that error.error contains the JSON response
        const errorResponse = error.error;
        const errorMessage = errorResponse?.message || 'Unknown error';
        const errorTitle = errorResponse?.error || 'Error';
    
        // Now open the error dialog with the extracted values
        console.log(`this.errorDialogService.openErrorDialog(${errorTitle}, ${errorMessage});`)
        this.errorDialogService.openErrorDialog(errorTitle, errorMessage);
    
        // Re-throw the error so subscribers can handle it too
        return throwError(() => error);
      })
    );
  }

  async DELETE(path: string): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
    console.log('Making DELETE request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
  
    return this.http.delete(`${this.url}/${path}`, { headers }).pipe(
      catchError((error: HttpErrorResponse) => {
        // Ensure that error.error contains the JSON response
        const errorResponse = error.error;
        const errorMessage = errorResponse?.message || 'Unknown error';
        const errorTitle = errorResponse?.error || 'Error';
    
        // Now open the error dialog with the extracted values
        console.log(`this.errorDialogService.openErrorDialog(${errorTitle}, ${errorMessage});`)
        this.errorDialogService.openErrorDialog(errorTitle, errorMessage);
    
        // Re-throw the error so subscribers can handle it too
        return throwError(() => error);
      })
    );
  }


  // Fehlerbehandlung
  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // Client-seitiger Fehler
      console.error('Ein Fehler ist aufgetreten:', error.error.message);
    } else {
      // Server-seitiger Fehler
      console.error(`Backend antwortete mit Status ${error.status}, ` + `Fehler-Body war: ${error.error}`);
    }
    // Einen benutzerfreundlichen Fehler zurückgeben
    return throwError('Etwas ist schiefgelaufen; bitte versuchen Sie es später erneut.');
  }
}


