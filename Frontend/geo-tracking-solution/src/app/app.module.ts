import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule, provideClientHydration } from '@angular/platform-browser';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { ScrollerModule } from 'primeng/scroller';
import { NgScrollbarModule } from 'ngx-scrollbar';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { NavigationBarComponent } from './components/navigation-bar/navigation-bar.component';
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { FooterComponent } from './components/footer/footer.component';
import { KeycloakService } from './services/keycloak/keycloak.service';

import { CreateUserComponent } from './components/user-group/create-user/create-user.component';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { ReactiveFormsModule } from '@angular/forms';
import { UserGroupComponent } from './components/user-group/user-group.component';
import { CreateGroupComponent } from './components/user-group/create-group/create-group.component';

import { ContentChatComponent } from './components/chat/content-chat/content-chat.component';
import { ChatListComponent } from './components/chat/chat-list/chat-list.component';
import { ChatComponent } from './components/chat/chat.component';
import { LowerBarComponent } from './components/chat/chat-list/lower-bar/lower-bar.component';
import { AddChatAlertComponent } from './components/chat/chat-list/lower-bar/add-chat-alert/add-chat-alert.component';
import { MatDialogModule } from '@angular/material/dialog';
import { TeamComponent } from './components/home/team/team.component';
import { AboutTheProjectComponent } from './components/home/about-the-project/about-the-project.component';
import { CardsComponent } from './components/home/cards/cards.component';

import { MapComponent } from './components/map-table/map/map.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { MapTableComponent } from './components/map-table/map-table.component';
import { SplitterModule } from 'primeng/splitter';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';

import { SettingsComponent } from './components/settings/settings.component';
import { UserComponent } from './components/settings/user/user.component';
import { ChangeLanguageComponent } from './components/settings/change-language/change-language.component';
import { SuccessAlertComponent } from './components/settings/success-alert/success-alert.component';

// ngx-translate Imports
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { ManageMembersComponent } from './components/user-group/manage-members/manage-members.component';
import { CostumErrorMessageComponent } from './components/costum-error-message/costum-error-message.component';

// Funktion zur Initialisierung des KeycloakService
export function kcFactory(keycloakService: KeycloakService) {
  return () => keycloakService.init();
}

// HttpLoaderFactory für ngx-translate
export function HttpLoaderFactory(http: HttpClient) {
  return new TranslateHttpLoader(http, './assets/i18n/', '.json');
}

@NgModule({
  declarations: [
    AppComponent,
    NavigationBarComponent,
    MapComponent,
    MapTableComponent,
    LoginComponent,
    HomeComponent,
    FooterComponent,
    CreateUserComponent,
    UserGroupComponent,
    CreateGroupComponent,
    ContentChatComponent,
    ChatListComponent,
    ChatComponent,
    LowerBarComponent,
    AddChatAlertComponent,
    SettingsComponent,
    UserComponent,
    ChangeLanguageComponent,
    TeamComponent,
    AboutTheProjectComponent,
    CardsComponent,
    SuccessAlertComponent,
    ManageMembersComponent,
    CostumErrorMessageComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    AppRoutingModule,
    ScrollerModule,
    NgScrollbarModule,
    FormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatSelectModule,
    MatOptionModule,
    SplitterModule,
    MatSortModule,
    MatTableModule,

    // ngx-translate Module hinzufügen
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [HttpClient],
      },
    })
  ],
  providers: [
    provideClientHydration(),
    provideAnimationsAsync(),
    {
      provide: APP_INITIALIZER,
      deps: [KeycloakService],
      useFactory: kcFactory,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
