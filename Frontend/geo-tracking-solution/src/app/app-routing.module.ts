import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// Components
import { LoginComponent } from './components/login/login.component';
import { HomeComponent } from './components/home/home.component';
import { authGuard, groupmasterGuard } from './services/guard/auth.guard';
import { CreateUserComponent } from './components/user-group/create-user/create-user.component';
import { UserGroupComponent } from './components/user-group/user-group.component';
import { CreateGroupComponent } from './components/user-group/create-group/create-group.component';
import { ChatComponent } from './components/chat/chat.component';
import { MapTableComponent } from './components/map-table/map-table.component';

import { SettingsComponent } from './components/settings/settings.component';
import { UserComponent } from './components/settings/user/user.component';
import { ChangeLanguageComponent } from './components/settings/change-language/change-language.component';
import { ManageMembersComponent } from './components/user-group/manage-members/manage-members.component';

const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [authGuard] },
  {
    path: 'create', component: UserGroupComponent, canActivate: [authGuard], children: [
      { path: 'user', component: CreateUserComponent, data: { width: '100%', maxWidth: '500px' }, canActivate: [authGuard, groupmasterGuard] },
      { path: 'group', component: CreateGroupComponent, data: { width: '100%', maxWidth: '500px' }, canActivate: [authGuard, groupmasterGuard] },
      { path: 'member', component: ManageMembersComponent, data: { width: '100%', maxWidth: '500px' }, canActivate: [authGuard, groupmasterGuard] }
    ]
  },
  { path: 'chat', component: ChatComponent, canActivate: [authGuard] },
  { path: 'map', component: MapTableComponent, canActivate: [authGuard] },

  { path: 'settings', component: SettingsComponent, children: [
    {path: 'user', component: UserComponent},
    {path: 'language', component: ChangeLanguageComponent}
  ] }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
