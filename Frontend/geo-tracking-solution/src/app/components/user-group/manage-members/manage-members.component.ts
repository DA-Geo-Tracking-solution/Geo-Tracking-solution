import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { RestService } from '../../../services/REST/rest.service';
import { TranslateService } from '@ngx-translate/core';
import { CookieSettingsService } from '../../../services/Cookies/cookie-settings.service';
import { MatDialog } from '@angular/material/dialog';
import { SuccessAlertComponent } from '../../settings/success-alert/success-alert.component';
import { Contact } from '../../../models/interfaces';
import { faTrash } from '@fortawesome/free-solid-svg-icons';

@Component({
  selector: 'app-manage-members',
  templateUrl: './manage-members.component.html',
  styleUrl: './manage-members.component.css'
})
export class ManageMembersComponent implements OnInit {

  contacts: Contact[] = [];
  selectedContacts: Contact[] = [];
  isAlertVisible: boolean = false;

  // Icons
  faTrash = faTrash;

  constructor(private restService: RestService, private translateService: TranslateService, private cookieService: CookieSettingsService, private matDialog: MatDialog) {
    this.translateService.use(this.cookieService.getLanguage());
  }

  openDialog() {
    const dialogRef = this.matDialog.open(SuccessAlertComponent, {
      width: '50%',
      data: { message: 'Member deleted successfully!' }
    });
  }

ngOnInit(): void {
  this.restService.GET("member/group-members").then(observable => {
    observable.subscribe({
      next: (users) => {
        this.contacts = users.map((user: any) => ({
          name: `${user.firstname} ${user.lastname}`,
          email: user.email,
          // username: user.username
        }));
      },
      error: (err) => console.error("Error fetching members:", err),
    });
  });
}

  showAlert() {
    this.isAlertVisible = true;
  }

  removeMember(userEmail: string) {
    this.restService.DELETE(`groupmaster/user/${userEmail}`).then(observable => {
      observable.subscribe({
        next: (message) => {
          console.log(message)
        },
        error: (err) => console.error("Error fetching members:", err),
      });
    });
  }

}
