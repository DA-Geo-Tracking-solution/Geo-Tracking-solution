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

    console.log('Making GET request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
    
    return this.http.get(`${this.url}/${path}`, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  async POST(path: string, body: any): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    console.log('Making POST request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
    console.log('Body:', body);

    return this.http.post(`${this.url}/${path}`, body, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  async PATCH(path: string, body: any): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
    console.log('Making PATCH request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
    console.log('Body:', body);
  
    return this.http.patch(`${this.url}/${path}`, body, { headers }).pipe(
      catchError(this.handleError)
    );
  }

  async DELETE(path: string): Promise<Observable<any>> {
    const token = this.keycloakService.profile?.token;
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
  
    console.log('Making DELETE request to:', `${this.url}/${path}`);
    console.log('Headers:', headers);
  
    return this.http.delete(`${this.url}/${path}`, { headers }).pipe(
      catchError(this.handleError)
    );
  }


  // Fehlerbehandlung
  private handleError(error: HttpErrorResponse) {
    // Ensure that error.error contains the JSON response
    const errorResponse = error.error;
    const errorMessage = errorResponse?.message || 'Unknown error';
    const errorTitle = errorResponse?.error || 'Error';

    // Now open the error dialog with the extracted values
    console.log(`this.errorDialogService.openErrorDialog(${errorTitle}, ${errorMessage});`)
    this.errorDialogService.openErrorDialog(errorTitle, errorMessage);

    // Re-throw the error so subscribers can handle it too
    return throwError(() => error);
  }
}


