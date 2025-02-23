import { Component, OnInit } from '@angular/core';
import { CookieSettingsService } from '../../../services/Cookies/cookie-settings.service';
import { TranslateService } from '@ngx-translate/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RestService } from '../../../services/REST/rest.service';
import { faCheck, faUser, faEnvelope, faExclamationTriangle, faKey } from '@fortawesome/free-solid-svg-icons';
import { MatDialog } from '@angular/material/dialog';
import { KeycloakService } from '../../../services/keycloak/keycloak.service';
import { Subscription } from 'rxjs';
import { SuccessAlertComponent } from '../success-alert/success-alert.component';

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})
export class UserComponent {
  backgroundImage: string = '';

  // FontAwesome Icons
  faEnvelope = faEnvelope;
  faUser = faUser;
  faExclamationTriangle = faExclamationTriangle;
  faKey = faKey;
  faCheck = faCheck;

  form: FormGroup;
  isEditable: boolean = false;
  /**
   * Holds the original data for cancel functionality
   */
  originalFormData: any = {};
  showSuccessAlert: boolean = false;


  constructor(
    private keycloakService: KeycloakService, private cookieService: CookieSettingsService, private translateService: TranslateService, private formBuilder: FormBuilder, private restService: RestService, public matDialog: MatDialog) {
    // Set initial language
    this.translateService.use(this.cookieService.getLanguage());

    // Initialize the form
    const user = keycloakService.user;
    this.form = this.formBuilder.group({
      firstname: [user?.given_name || ''],
      lastname: [user?.family_name || ''],
      username: [user?.preferred_username || '', Validators.required],
      email: [user?.email || '', [Validators.required, Validators.email]],
      password: [user?.email || '', [Validators.required, Validators.minLength(8), Validators.pattern(/[A-Z]/)]]
    }, { updateOn: 'change' });
  }


  /**
   * TODO: Load user data from the backend and populate the form.
   */

  /**
   * Save the original form data for restoration on cancel.
   */
  saveOriginalData() {
    this.originalFormData = { ...this.form.value };
  }

  /**
   * Toggle edit mode or save changes.
   */
  toggleEdit() {
    if (this.isEditable) {
      this.updateUser();
    } else {
      this.isEditable = true;
    }
  }

  /**
   * Cancel edit and restore original data.
   */
  cancelEdit() {
    this.form.patchValue(this.originalFormData);
    this.isEditable = false;
  }

  /**
   * Update user details in the backend.
   */
  updateUser() {
    const updatedUser = this.form.value;
    this.restService.PATCH('member/user', {
      user: {
        username: updatedUser.username,
        userEmail: updatedUser.email,
        firstname: updatedUser.firstname,
        lastname: updatedUser.lastname
      }
    }).then(observable => {
      observable.subscribe({
        next: () => {
          console.log('User updated successfully');
          this.saveOriginalData();
          this.isEditable = false;
          this.openDialog(); // Show success dialog
        },
        error: (err) => console.error('Error updating user:', err)
      });
    });
  }

  /**
   * Open a dialog to show success message.
   */
  openDialog() {
    const dialogRef = this.matDialog.open(SuccessAlertComponent, {
      width: '400px',
      data: { message: 'User updated successfully! Now you will be logged out!' }
    });

    dialogRef.afterClosed().subscribe(() => {
      this.keycloakService.updateUserProfile();
      this.keycloakService.logout();
    });
  }
}
